
import os, sys
import json as JSON
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
# sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from Instructions import Instruction

testJson = {"commands": [{"command": "MOVEMENT", "value": {"movementType": "FORWARD", "movementDirection": "STRAIGHT", "distance": 20}}, {"command": "MOVEMENT", "value": {"movementType": "FORWARD", "movementDirection": "LEFT", "distance": 20}}, {"command": "MOVEMENT", "value": {"movementType": "FORWARD", "movementDirection": "RIGHT", "distance": 20}}, {"command": "MOVEMENT", "value": {"movementType": "BACKWARD", "movementDirection": "STRAIGHT", "distance": 20}}, {"command": "MOVEMENT", "value": {"movementType": "BACKWARD", "movementDirection": "LEFT", "distance": 20}}, {"command": "MOVEMENT", "value": {"movementType": "BACKWARD", "movementDirection": "RIGHT", "distance": 20}}, {"command": "FINISH"}], "distance": 120, "path": []}

instruction = Instruction.fromJson(JSON.loads(JSON.dumps(testJson)))

print(instruction)