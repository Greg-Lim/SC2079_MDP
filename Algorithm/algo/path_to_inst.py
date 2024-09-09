import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from entities.Entity import *
from entities.Robot import *
from entities.Instructions import *
from consts import *


def path_to_instructions(path):
    '''
        path is a list of tuples, where each tuple is a position and a direction
        returns a list of instructions

        example path = [((1, 1), Direction.NORTH), ((1, 2), Direction.NORTH), ((1, 3), Direction.NORTH)]
        example instructions = [
            "FR00",
            "FW10",
            "SNAP1",
            "FR00",
            "BW50",
            "FL00",
            "FW60",
            "SNAP2",
            ...,
            "FIN"
        ],
    '''

    # Clean up the path
    new_path = []
    curr_dir = path[0][1]
    curr_loc = path[0][0]
    for idx in range(len(path)-1):
        # Check if the robot is moving forward
        if curr_dir == path[idx+1][1]:
            # check if next point is in direction of current direction
            if curr_dir == Direction.NORTH:
                if path[idx+1][0][1] > curr_loc[1]:
                    new_path.append((curr_loc, curr_dir))
                    curr_loc = path[idx+1][0]
            elif curr_dir == Direction.SOUTH:
                if path[idx+1][0][1] < curr_loc[1]:
                    new_path.append((curr_loc, curr_dir))
                    curr_loc = path[idx+1][0]
            elif curr_dir == Direction.EAST:
                if path[idx+1][0][0] > curr_loc[0]:
                    new_path.append((curr_loc, curr_dir))
                    curr_loc = path[idx+1][0]
            elif curr_dir == Direction.WEST:
                if path[idx+1][0][0] < curr_loc[0]:
                    new_path.append((curr_loc, curr_dir))
                    curr_loc = path[idx+1][0]
        
     


if __name__ == "__main__":
    import optimal_path
    grid_w = 20
    grid_h = 20
    obstacles = [
        [10, 5, Direction.NORTH],
        [6, 7, Direction.SOUTH]
    ]
    robot_pos = [1, 1]
    robot_d = Direction.NORTH

    grid = Grid(grid_w, grid_h)
    for idx, obs in enumerate(obstacles):
        # obstacle = Obstacle(obs[0], obs[1], obs[2], idx)
        grid.add_obstacle(Obstacle(obs[0], obs[1], obs[2], idx))

    # grid.show_grid()

    robot = Robot(robot_pos[0], robot_pos[1], robot_d)
    result = optimal_path.optimal_path(grid, robot, True)

    path = result[0]