import bluetooth
from enum import Enum

class Status(Enum):
    READY = 1
    UPDATING_MAP = 2


class CommandHandler:
    def __init__(self):
        super()
        self.status =Status.READY
        return self

    def setClientSock(self, socket):
        self.client_socket = socket
        return self
    
    def handleCommand(self, msg):
        '''
        Add how the msg recieved from bluetooth client is handled here
        '''

        print(f"Recieved: {msg}")

        if self.status == Status.READY:
            self.client_socket.send(f"Recieved: {msg}")
            if msg == "RUN_TASK_1":
                pass
            elif msg == "RUN_TASK_2":
                pass
        elif self.status == Status.UPDATING_MAP:
            pass