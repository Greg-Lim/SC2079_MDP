# SYMBOL_MAP = {
#   0: 'A', 1: 'B', 2: 'Bullseye', 3: 'C', 4: 'D', 5: 'Down Arrow',
#   6: 'E', 7: 'Eight', 8: 'F', 9: 'Five', 10: 'Four', 11: 'G',
#   12: 'H', 13: 'Left Arrow', 14: 'Nine', 15: 'One', 16: 'Right Arrow',
#   17: 'S', 18: 'Seven', 19: 'Six', 20: 'Stop', 21: 'T', 22: 'Three',
#   23: 'Two', 24: 'U', 25: 'Up Arrow', 26: 'V', 27: 'W', 28: 'X', 29: 'Y', 30: 'Z'
# }

from typing import List, Tuple

RESOLUTIONS: List[Tuple[float, float, float, float]] = [(0.0, 0.1, 1.0, 1.0), 
                                                        (0.1, 0.2, 0.85, 0.85),
                                                        (0.2, 0.4, 0.75, 0.75) 
                                                       ]

SYMBOL_MAP = {
    "0": "Left Arrow",
    "10": "Bullseye",
    "11": "One",
    "12": "Two",
    "13": "Three",
    "14": "Four",
    "15": "Five",
    "16": "Six",
    "17": "Seven",
    "18": "Eight",
    "19": "Nine",
    "20": "A",
    "21": "B",
    "22": "C",
    "23": "D",
    "24": "E",
    "25": "F",
    "26": "G",
    "27": "H",
    "28": "S",
    "29": "T",
    "30": "U",
    "31": "V",
    "32": "W",
    "33": "X",
    "34": "Y",
    "35": "Z",
    "36": "Up Arrow",
    "37": "Down Arrow",
    "38": "Right Arrow",
    "39": "Left Arrow",
    "40": "Stop"
}
