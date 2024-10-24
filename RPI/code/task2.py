#!/usr/bin/env python3
import json
import queue
import time
from multiprocessing import Process, Manager
from typing import Optional
import os
import requests
from communication.android import AndroidLink, AndroidMessage
from communication.stm32 import STMLink
from consts import SYMBOL_MAP, RESOLUTIONS
from logger import prepare_logger
from settings import API_IP, API_PORT
from cam import PicTaker


class PiAction:
    def __init__(self, cat, value):
        self._cat = cat
        self._value = value

    @property
    def cat(self):
        return self._cat

    @property
    def value(self):
        return self._value


class RaspberryPi:
    def __init__(self):
        # Initialize logger and communication objects with Android and STM
        self.picTaker = PicTaker()

        self.logger = prepare_logger()
        self.android_link = AndroidLink()
        self.stm_link = STMLink()

        # For sharing information between child processes
        self.manager = Manager()

        # Set robot mode to be 1 (Path mode)
        self.robot_mode = self.manager.Value('i', 1)

        # Events
        self.android_dropped = self.manager.Event()  # Set when the android link drops
        self.unpause = self.manager.Event()  # Commands will be retrieved when this event is set

        # Movement Lock
        self.movement_lock = self.manager.Lock()

        # Queues
        self.android_queue = self.manager.Queue()  # Messages to send to Android
        self.rpi_action_queue = self.manager.Queue()  # Messages that need to be processed by RPi
        self.command_queue = self.manager.Queue()  # Messages that need to be processed by STM32

        # Define empty processes
        self.proc_recv_android = None
        self.proc_recv_stm32 = None
        self.proc_android_sender = None
        self.proc_command_follower = None
        self.proc_rpi_action = None

        self.ack_count = 0

    def start(self):
        """Starts the RPi orchestrator"""
        try:
            # Establish Bluetooth connection with Android
            self.android_link.connect()
            self.android_queue.put(AndroidMessage('info', 'You are connected to the RPi!'))

            # Establish connection with STM32
            self.stm_link.connect()

            # Check Image Recognition and Algorithm API status
            self.check_api()

            # Define child processes
            self.proc_recv_android = Process(target=self.recv_android)
            self.proc_recv_stm32 = Process(target=self.recv_stm)
            self.proc_android_sender = Process(target=self.android_sender)
            self.proc_command_follower = Process(target=self.command_follower)
            self.proc_rpi_action = Process(target=self.rpi_action)

            # Start child processes
            self.proc_recv_android.start()
            self.proc_recv_stm32.start()
            self.proc_android_sender.start()
            self.proc_command_follower.start()
            self.proc_rpi_action.start()

            self.logger.info("Child Processes started")

            # Send success message to Android
            self.android_queue.put(AndroidMessage('info', 'Robot is ready!'))
            self.android_queue.put(AndroidMessage('mode', 'path' if self.robot_mode.value == 1 else 'manual'))
            
            # Handover control to the Reconnect Handler to watch over Android connection
            self.reconnect_android()

        except KeyboardInterrupt:
            self.stop()

    def stop(self):
        """Stops all processes on the RPi and disconnects gracefully with Android and STM32"""
        self.android_link.disconnect()
        self.stm_link.disconnect()
        self.logger.info("Program exited!")

    def reconnect_android(self):
        """Handles the reconnection to Android in the event of a lost connection."""
        self.logger.info("Reconnection handler is watching...")

        while True:
            # Wait for android connection to drop
            self.android_dropped.wait()

            self.logger.error("Android link is down!")

            # Kill child processes
            self.proc_android_sender.kill()
            self.proc_recv_android.kill()

            # Recreate Android processes
            self.android_link.disconnect()
            self.android_link.connect()
            self.proc_recv_android = Process(target=self.recv_android)
            self.proc_android_sender = Process(target=self.android_sender)

            # Start previously killed processes
            self.proc_recv_android.start()
            self.proc_android_sender.start()

            self.logger.info("Android child processes restarted")
            self.android_queue.put(AndroidMessage("info", "You are reconnected!"))
            self.android_queue.put(AndroidMessage('mode', 'path' if self.robot_mode.value == 1 else 'manual'))

            self.android_dropped.clear()

        
    def recv_android(self) -> None:
        """[Child Process] Processes the messages received from Android"""
    
        while True:
            msg_str: Optional[str] = None
            try:
                msg_str = self.android_link.recv()
            except OSError:
                self.android_dropped.set()
                self.logger.debug("Event set: Android connection dropped")

            if msg_str is None:
                continue

            message: dict = json.loads(msg_str)

            # Start moving when the start command is received
            if message['cat'] == "control" and message['value'] == "start":
                if not self.check_api():
                    self.logger.error("API is down! Start command aborted.")
                    return

                # Start sequence by sending DT030 to STM32
                self.clear_queues()
                self.command_queue.put("DT020")  # Send DT030 to STM32
                self.logger.info("DT020 command sent to STM32.")
                self.unpause.set()  # Allow command follower to process commands

                # Notify Android of running status
                self.android_queue.put(AndroidMessage('status', 'running'))

    def recv_stm(self) -> None:
        """[Child Process] Receives acknowledgment messages from STM32 and releases the movement lock.Handles sequencing based on ACKs received."""
        while True:
            message: str = self.stm_link.recv()

            if message.startswith("A"):
                self.ack_count += 1
                try:
                    self.movement_lock.release()
                except Exception:
                    self.logger.warning("Attempted to release an already released lock.")

                self.logger.debug(f"ACK from STM32 received, ACK count now: {self.ack_count}")

                # Handle ACK-based sequencing
                if self.ack_count == 1:
                    # After DT030, snap and queue WN030 or WX030
                    # time.sleep(1)
                    self.small_direction = self.snap_and_rec("1")
                    self.command_queue.put("DT030")
                    self.logger.info("DT030 command sent to STM32.")
                    
                    
                elif self.ack_count == 2:
                    # After DT030, snap and queue WN030 or WX030
                    
                    if self.small_direction == "Left Arrow":
                        self.command_queue.put("WN030")  # Left turn
                        self.logger.info("Left turn (WN030) based on first snap result.")
                    elif self.small_direction == "Right Arrow":
                        self.command_queue.put("WX030")  # Right turn
                        self.logger.info("Right turn (WX030) based on first snap result.")

                elif self.ack_count == 3:
                    # After turn, send US030
                    self.command_queue.put("US030")
                    self.logger.info("US030 command sent to STM32.")

                elif self.ack_count == 4:
                    # After US030, snap and queue YL000 or YR000
                    # time.sleep(3)  # Allow time for US030 to complete
                    self.small_direction = self.snap_and_rec("2")
                    if self.small_direction == "Left Arrow":
                        self.command_queue.put("YL030")  # Left turn
                        self.logger.info("Left turn (YL030) based on second snap result.")
                    elif self.small_direction == "Right Arrow":
                        self.command_queue.put("YR030")  # Right turn
                        self.logger.info("Right turn (YR030) based on second snap result.")

                elif self.ack_count == 5:
                    # Final ACK - sequence complete
                    self.android_queue.put(AndroidMessage("status", "finished"))
                    self.command_queue.put("FIN")
                    self.logger.info("Final ACK received: Sequence complete, robot task finished.")
                    self.stop()

            else:
                self.logger.warning(f"Ignored unknown message from STM: {message}")


    def android_sender(self) -> None:
        while True:
            try:
                message: AndroidMessage = self.android_queue.get(timeout=0.5)
            except queue.Empty:
                continue

            try:
                self.android_link.send(message)
            except OSError:
                self.android_dropped.set()
                self.logger.debug("Event set: Android dropped")

    def command_follower(self) -> None:
        while True:
            command: str = self.command_queue.get()
            self.unpause.wait()
            self.movement_lock.acquire()
            stm32_prefixes = ("STOP", "ZZ","DT","WX","WN","YL","YR","US", "UL", "UR", "PL", "PR", "RS", "OB")
            if command.startswith(stm32_prefixes):
                self.stm_link.send(command)
            elif command == "FIN":
                self.unpause.clear()
                self.movement_lock.release()
                self.logger.info("Commands queue finished.")
                self.android_queue.put(AndroidMessage("info", "Commands queue finished."))
                self.android_queue.put(AndroidMessage("status", "finished"))
                self.rpi_action_queue.put(PiAction(cat="stitch", value=""))
            else:
                raise Exception(f"Unknown command: {command}")

    def rpi_action(self):
        while True:
            action: PiAction = self.rpi_action_queue.get()
            self.logger.debug(f"PiAction retrieved from queue: {action.cat} {action.value}")
            if action.cat == "snap": 
                self.snap_and_rec(obstacle_id=action.value)
            elif action.cat == "stitch": 
                self.request_stitch()

    def snap_and_rec(self, obstacle_id: str) -> None:
        self.logger.info(f"Capturing image for obstacle id: {obstacle_id}")
        print("Check")
        url = f"http://{API_IP}:{API_PORT}/notify-upload-arrow/{obstacle_id}"
        print(url)
        filename = f"snap_{obstacle_id}_task2.jpg"
        img_path = os.path.join(os.path.abspath(os.path.dirname(__file__)), filename)

        for res in RESOLUTIONS:
            accept = False
            while(True):
                self.picTaker.take_pic(img_path, res)

                response = requests.post(url, files={"file": (filename, open(filename, 'rb'))})

                # bad connect, retry
                if response.status_code != 200:
                    continue
                    
                results = json.loads(response.content)

                if results['detected'] == 0:
                    break

                else:
                    accept = True
                    break
            
            if accept:
                break

        ans = SYMBOL_MAP.get(str(results['id']))
        self.logger.info(f"Image recognition results: {results} ({ans})")
        return ans

    def request_stitch(self):
        url = f"http://{API_IP}:{API_PORT}/stitch"
        response = requests.get(url)
        if response.status_code != 200:
            self.logger.error("Something went wrong when requesting stitch from the API.")
            return
        self.logger.info("Images stitched!")

    def clear_queues(self):
        while not self.command_queue.empty():
            self.command_queue.get()

    def check_api(self) -> bool:
        url = f"http://{API_IP}:{API_PORT}/status"
        try:
            response = requests.get(url, timeout=1)
            if response.status_code == 200:
                self.logger.debug("API is up!")
                return True
        except requests.RequestException as e:
            self.logger.warning(f"API Exception: {e}")
        return False

    def close(self) -> None:
        '''
        Add cleanup stuff here
        '''
        del self.picTaker


if __name__ == "__main__":
    try:
        rpi = RaspberryPi()
        rpi.start()
    except KeyboardInterrupt:
        rpi.close()
