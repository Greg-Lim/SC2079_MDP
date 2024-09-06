from typing import List
from entities.Entity import CellState
from consts import Direction


class Robot:
    def __init__(self, center_x: int, center_y: int, start_direction: Direction):
        """Robot object class

        Args:
            center_x (int): x coordinate of center of robot
            center_y (int): y coordinate of center of robot
            start_direction (Direction): Direction robot is facing at the start

        Internals:
            states: List of cell states of the robot's historical path
        """
        self.states: List[CellState] = [
            CellState(center_x, center_y, start_direction)]

    def get_start_state(self):
        """Returns the starting cell state of the robot

        Returns:
            CellState: starting cell state of robot (x,y,d)
        """
        return self.states[0]
    
    def set_direction(self, direction: Direction):
        """Sets a new direction for the robot

        Args:
            direction (Direction): The new direction to set for the robot
        """
        # Update the direction in the current state
        self.states[-1].direction = direction

    def set_position(self, x_new, y_new):
        self.states[-1].x = x_new
        self.states[-1].y = y_new
