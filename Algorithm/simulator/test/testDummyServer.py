
import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
# sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

import requests
from entities.Instructions import Instruction
# make a get request to the server
# and print the response

response = requests.get("http://localhost:5000/dummyInstruction")

instruction = Instruction.fromJson(response.json())

print(instruction)