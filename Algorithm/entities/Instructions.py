#!/usr/bin/env python3

from enum import Enum
import numpy as np

class MovementType(Enum):
    FORWARD= "FORWARD"
    BACKWARD= "BACKWARD"

class MovementDirection(Enum):
    STRAIGHT= "STRAIGHT"
    LEFT= "LEFT"
    RIGHT= "RIGHT"


class CommandType(Enum):
    SNAP= "SNAP"
    TURN= "TURN"
    STRAIGHT = "STRAIGHT"
    FINISH= "FINISH"

class _Movement:
    def __init__(self, movementType: MovementType, movementDirection: MovementDirection, distance: int):
        self.movementType: MovementType = movementType
        self.movementDirection: MovementDirection = movementDirection
        self.distance: int = distance

    def toJson(self):
        return {
            "movementType": self.movementType.value,
            "movementDirection": self.movementDirection.value,
            "distance": self.distance
        }
    
    def __str__(self):
        return f"{self.movementType.value}_{self.movementDirection.value}_{self.distance}"


class StrainghtMovement(_Movement):
    pass

class _Command:
    def __init__(self, command, value):
        self.command: CommandType = command
        self.value: _Movement | None = value

    def toJson(self):
        if self.command == CommandType.SNAP:
            return {
                "command": "SNAP"
            }
        elif self.command == CommandType.TURN:
            return {
                "command": "MOVEMENT",
                "value": self.value.toJson()
            }
        elif self.command == CommandType.FINISH:
            return {
                "command": "FINISH"
            }
        else:
            raise Exception("Invalid command type")
    
    def __str__(self):
        if self.command == CommandType.SNAP:
            return f"SNAP"
        elif self.command == CommandType.TURN:
            return str(self.value)
        elif self.command == CommandType.FINISH:
            return f"FINISH"
        else:
            raise Exception("Invalid command type")

class Instruction:
    '''
    This class represents the instruction that the robot will follow
    All commands through the server should be passed to this class to be processed
    '''
    def __init__(self, commands: list[_Command], distance, path = []):
        self.commands: list[_Command] = commands
        self.distance: int = distance
        self.path = path

    @staticmethod
    def fromJson(json):
        '''
        This method takes a json object and returns an Instruction object
        '''
        return Instruction(
            [_Command(CommandType[command["command"]], _Movement(MovementType[command["value"]["movementType"]], MovementDirection[command["value"]["movementDirection"]], command["value"]["distance"])) if command["command"] == "MOVEMENT" else _Command(CommandType[command["command"]], None) for command in json["commands"]],
            json["distance"],
            json["path"]
        )

    def toJson(self):
        '''
        This method returns a json object of the Instruction object
        '''
        return {
            "commands": [command.toJson() for command in self.commands],
            "distance": self.distance,
            "path": self.path
        }
    
    def __str__(self):
        return f"commands: {', '.join([str(command) for command in self.commands])}, distance: {self.distance}, path: {self.path}"


def getDummyInstruction():
    return Instruction([
        # straight, right, straight, right, straight, right, straight, left, straight, left, straight, left
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.STRAIGHT, 200)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.RIGHT, np.pi*100)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.STRAIGHT, 200)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.RIGHT, np.pi*100)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.STRAIGHT, 200)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.RIGHT, np.pi*100)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.STRAIGHT, 200)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.LEFT, np.pi*100)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.STRAIGHT, 200)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.LEFT, np.pi*100)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.STRAIGHT, 200)),
        _Command(CommandType.TURN, _Movement(MovementType.FORWARD, MovementDirection.LEFT, np.pi*100)),

        
    ], 120, [])


if __name__ == "__main__":
    print(getDummyInstruction().toJson())

  
    

        