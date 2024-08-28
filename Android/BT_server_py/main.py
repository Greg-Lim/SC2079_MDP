from bluetoothServer import BluetoothServer

def main():
    bluetoothServer = BluetoothServer()
    bluetoothServer.startService()

# Run the server
if __name__ == "__main__":
    main()