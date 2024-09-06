import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from entities.Entity import *
from entities.Robot import *
from entities.Instructions import *
from consts import *
from algo.a_star_search import a_star_search

def optimal_path(grid: Grid, robot: Robot):
    grid_W = grid.size_x
    grid_h = grid.size_y
    obstacles = grid.get_obstacles()
    robot_pos = (robot.get_start_state().x, robot.get_start_state().y)
    robot_d = robot.get_start_state().direction

    obs = []
    targets = []
    for obstacle in obstacles: 
        obs.append([obstacle.x, obstacle.y])
        
        if obstacle.direction == Direction.NORTH:
            targets.append(obstacle.x, obstacle.y + OBSERVATION_DISTANCE + 1)
        elif obstacle.direction == Direction.SOUTH:
            targets.append(obstacle.x, obstacle.y - OBSERVATION_DISTANCE - 1)
        elif obstacle.direction == Direction.WEST:
            targets.append(obstacle.x - OBSERVATION_DISTANCE - 1, obstacle.y)
        elif obstacle.direction == Direction.EAST:
            targets.append(obstacle.x + OBSERVATION_DISTANCE + 1, obstacle.y)