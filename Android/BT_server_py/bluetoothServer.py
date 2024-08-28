import bluetooth
from commandHandler import CommandHandler

class BluetoothServer:
    def __init__(self):
        self.server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.server_socket.bind(("", bluetooth.PORT_ANY)) # people report it needs python 3.11
        self.server_socket.listen(1)

        self.SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB" # don't touch, should match that on Android

        bluetooth.advertise_service(
            self.server_socket,
            "Group27_BT_SERVER",
            service_id=self.SERVICE_UUID,
            service_classes=[self.SERVICE_UUID, bluetooth.SERIAL_PORT_CLASS],
            profiles=[bluetooth.SERIAL_PORT_PROFILE],
        )

    def startService(self):
        while True:
            client_socket, client_address = self.server_socket.accept()
            commandHandler = commandHandler()
            commandHandler.setClientSocket(client_socket)

            while True:
                try:
                    # Receive data from the client
                    data = client_socket.recv(1024)
                    if len(data) == 0:
                        break

                    msg = data.decode('utf-8')

                    if msg == "STOP":
                        client_socket.close()
                        self.server_socket.close()
                        return
                    elif msg == "DISCONNECT":
                        client_socket.close()
                        break

                    commandHandler.handleCommand(msg)

                except:
                    pass
            
