import numpy as np
from movement import *
import unittest

class TestMovement(unittest.TestCase):
    def test_move_forward(self):
        curr_x, curr_y, curr_dir = 0, 0, 0
        curr_x, curr_y, curr_dir = move_forward(curr_x, curr_y, curr_dir, 10)
        self.assertAlmostEqual(curr_x, 10)
        self.assertAlmostEqual(curr_y, 0)
        self.assertAlmostEqual(curr_dir, 0)

        curr_x, curr_y, curr_dir = 2, 3, np.pi/2
        curr_x, curr_y, curr_dir = move_forward(curr_x, curr_y, curr_dir, 5)
        self.assertAlmostEqual(curr_x, 2)
        self.assertAlmostEqual(curr_y, 8)
        self.assertAlmostEqual(curr_dir, np.pi/2)

        curr_x, curr_y, curr_dir = 0, 0, 30*np.pi/180
        curr_x, curr_y, curr_dir = move_forward(curr_x, curr_y, curr_dir, 5)
        self.assertAlmostEqual(curr_x, 4.330127018922194)
        self.assertAlmostEqual(curr_y, 2.5)
        self.assertAlmostEqual(curr_dir, 30*np.pi/180)

    def test_turn(self):
        curr_x, curr_y, curr_dir = 0, 0, 0
        curr_x, curr_y, curr_dir = turn(curr_x, curr_y, curr_dir, True, False, 1, np.pi/2)
        self.assertAlmostEqual(curr_x, 1)
        self.assertAlmostEqual(curr_y, 1)
        self.assertAlmostEqual(curr_dir, 0, 3)


        curr_x, curr_y, curr_dir = 0,


if __name__ == '__main__':
    unittest.main()