import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from consts import Direction, MOVE_DIRECTION,TURN_FACTOR, EXPANDED_CELL, SAFE_COST, SCREENSHOT_COST, TURN_RADIUS
import heapq
import math
from typing import List, Optional, Tuple
from enum import Enum

# Node representation for A* algorithm
class Node:
    def __init__(self, position: Tuple[int, int], direction: Direction, g: float, h: float):
        self.position = position
        self.direction = direction
        self.g = g  # Cost from start to this node
        self.h = h  # Heuristic cost to target
        self.f = g + h  # Total estimated cost
        self.parent: Optional['Node'] = None  # Parent node for path reconstruction

    def __lt__(self, other):
        return self.f < other.f

# Heuristic function: Euclidean distance + rotation cost
def heuristic(start: Tuple[int, int], target: Tuple[int, int], start_dir: Direction, target_dir: Direction):
    dx = target[0] - start[0]
    dy = target[1] - start[1]
    euclidean_distance = math.hypot(dx, dy)
    rotation_cost = Direction.rotation_cost(start_dir, target_dir)
    return euclidean_distance + rotation_cost * TURN_FACTOR

# Expand obstacles to 3x3 by marking all surrounding cells
def expand_obstacles(obstacles: List[List[int]], grid_w: int, grid_h: int) -> List[List[int]]:
    expanded_obstacles = set()
    for obs in obstacles:
        x, y = obs
        # Expand the 1x1 obstacle to a 3x3 area
        for dx in range(-EXPANDED_CELL, EXPANDED_CELL + 1):
            for dy in range(-EXPANDED_CELL, EXPANDED_CELL + 1):
                new_x = x + dx
                new_y = y + dy
                if 0 <= new_x < grid_w and 0 <= new_y < grid_h:
                    expanded_obstacles.add((new_x, new_y))
    return list(expanded_obstacles)

# Check if the robot's current position is valid (i.e., not in an obstacle)
def is_valid(grid_w: int, grid_h: int, pos: Tuple[int, int], expanded_obstacles: List[Tuple[int, int]]) -> bool:
    return pos not in expanded_obstacles

# Simulate the robot turning around the center, adjusting the direction and position
def perform_turn(robot_pos: Tuple[int, int], direction: Direction, turn_direction: str) -> Tuple[Tuple[int, int], Direction]:
    x = robot_pos[0]
    y = robot_pos[1]

    if direction == Direction.NORTH:
        if turn_direction == 'right':
            x = x + (2 + TURN_RADIUS)
            y = y + TURN_RADIUS
            d = Direction.EAST
        elif turn_direction == 'left':
            x = x - (2 + TURN_RADIUS)
            y = y + TURN_RADIUS
            d = Direction.WEST
    elif direction == Direction.EAST:
        if turn_direction == 'right':
            x = x + TURN_RADIUS
            y = y - (2 + TURN_RADIUS)
            d = Direction.SOUTH
        elif turn_direction == 'left':
            x = x + TURN_RADIUS
            y = y + (2 + TURN_RADIUS)
            d = Direction.NORTH
    elif direction == Direction.SOUTH:
        if turn_direction == 'right':
            x = x - (2 + TURN_RADIUS)
            y = y - TURN_RADIUS
            d = Direction.WEST
        elif turn_direction == 'left':
            x = x + (2 + TURN_RADIUS)
            y = y - TURN_RADIUS
            d = Direction.EAST
    elif direction == Direction.WEST:
        if turn_direction == 'right':
            x = x - TURN_RADIUS
            y = y + (2 + TURN_RADIUS)
            d = Direction.NORTH
        elif turn_direction == 'left':
            x = x - TURN_RADIUS
            y = y - (2 + TURN_RADIUS)
            d = Direction.SOUTH
        
    return (x, y), d

# A* search algorithm implementation
def a_star_search(grid_w: int, grid_h: int, obstacles: List[List[int]], robot_pos: List[int], robot_d: Direction, target_pos: List[int], target_d: Direction) -> Optional[Tuple[List[Tuple[Tuple[int, int], Direction]], float]]:
    # Expand the obstacles to accommodate the robot's 3x3 size
    expanded_obstacles = expand_obstacles(obstacles, grid_w, grid_h)

    start = tuple(robot_pos)
    target = tuple(target_pos)
    start_dir = robot_d
    target_dir = target_d

    start_node = Node(start, start_dir, 0, heuristic(start, target, start_dir, target_dir))
    open_list = []
    heapq.heappush(open_list, start_node)
    closed_set = set()

    while open_list:
        current_node = heapq.heappop(open_list)

        # Goal check
        if current_node.position == target and current_node.direction == target_dir:
            path = []
            total_cost = current_node.g  # Final cost to reach the target
            while current_node:
                path.append((current_node.position, current_node.direction))
                current_node = current_node.parent
            return path[::-1], total_cost  # Reverse path

        # Add to closed set
        closed_key = (current_node.position, current_node.direction)
        if closed_key in closed_set:
            continue
        closed_set.add(closed_key)

        # Explore all possible movements
        for move_x, move_y, move_dir in MOVE_DIRECTION:
            new_x = current_node.position[0] + move_x
            new_y = current_node.position[1] + move_y
            new_pos = (new_x, new_y)
            new_direction = move_dir

            # Check validity against expanded obstacles
            if not is_valid(grid_w, grid_h, new_pos, expanded_obstacles):
                continue

            # Calculate rotation cost
            rotation_cost = Direction.rotation_cost(current_node.direction, new_direction)

            # Handle the right or left turn based on movement
            if current_node.direction != new_direction:
                if new_direction in [Direction.EAST, Direction.NORTH]:
                    new_pos, new_direction = perform_turn(current_node.position, current_node.direction, 'right')
                else:
                    new_pos, new_direction = perform_turn(current_node.position, current_node.direction, 'left')

                if not is_valid(grid_w, grid_h, new_pos, expanded_obstacles):
                    continue

            # Calculate g cost (movement cost)
            g_cost = current_node.g + 1 + rotation_cost * TURN_FACTOR

            # Heuristic
            h_cost = heuristic(new_pos, target, new_direction, target_dir)

            neighbor = Node(new_pos, new_direction, g_cost, h_cost)
            neighbor.parent = current_node

            neighbor_key = (neighbor.position, neighbor.direction)
            if neighbor_key in closed_set:
                continue

            heapq.heappush(open_list, neighbor)

    return None  # No path found

# Example Usage
if __name__ == "__main__":
    grid_w = 20
    grid_h = 20
    obstacles = [
        [10, 10],
        [6, 1]
    ]
    robot_pos = [1, 1]
    robot_d = Direction.NORTH
    target_pos = [10, 6]
    target_d = Direction.NORTH

    result = a_star_search(grid_w, grid_h, obstacles, robot_pos, robot_d, target_pos, target_d)
    if result:
        path, total_cost = result
        print(f"Path found with total cost: {total_cost}")
        for step in path:
            print(f"Position: {step[0]}, Direction: {step[1].name}")
    else:
        print("No path found.")
