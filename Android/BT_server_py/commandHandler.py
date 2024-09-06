import bluetooth
from enum import Enum

class Status(Enum):
    READY = 1
    UPDATING_MAP = 2
    TASK1 = 3
    TASK2 = 4
    TEST_TASK = 5


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
            if msg == "START TASK_1":
                self.status = Status.TASK1
            elif msg == "START TASK_2":
                self.status = Status.TASK2
            elif msg == "START TEST_TASK":
                self.status = Status.TEST_TASK

        elif self.status == Status.UPDATING_MAP:
            pass

        elif self.status == Status.TASK1:
            self.task1(msg)

        elif self.status == Status.TASK2:
            self.task2(msg)

        elif self.status == Status.TEST_TASK:
            self.task2(msg)

# >>>>>>>>>>>> Handle tasks here <<<<<<<<<<<<<<<<<
    
    def task1(self, msg):
        pass

    def task2(self, msg):
        pass

    def testTask(self, msg):
        # TODO Make robot move forward
        # x, y, dir = move()
        # self.client_socket.send(f"MOVE {x} {y} {dir}")

        self.client_socket.send(f"FINISH")