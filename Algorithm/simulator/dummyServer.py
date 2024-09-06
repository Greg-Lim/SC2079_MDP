
import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import json as JSON
from flask import Flask
from flask import request

import entities.Instructions as Instructions

# flask --app Algorithm/simulator/dummySever.py run
# flask --app dummyServer run


app = Flask(__name__)

# @app.route("/", methods=['GET'])
# def hello_world():
#     return "<p>Hello, World!</p>"

@app.route("/test", methods=['GET'])
def hello_world():
    return "<p>Test work</p>"

class MazeSolver:
    def __init__(self):
        self.instruction = Instructions.getDummyInstruction()

    def run(self) -> Instructions.Instruction:
        return self.instruction
    

@app.route("/dummyInstruction", methods=['POST', 'GET'])
def getDummyInstruction():
    # print data from the request
    print(request.data)
    
    # print(Instructions.getDummyInstruction().toJson())
    return JSON.dumps(Instructions.getDummyInstruction().toJson())
