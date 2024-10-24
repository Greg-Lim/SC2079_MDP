import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from consts import Direction, TURN_FACTOR, EXPANDED_CELL, SAFE_COST, SCREENSHOT_COST, TURN_RADIUS
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
    x, y = pos
    if not (0 <= x < grid_w and 0 <= y < grid_h):
        return False
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

# Update the MOVE_DIRECTION list to include backward movement
MOVE_DIRECTION = [
    (0, 1, Direction.NORTH),  # Move forward to the north
    (0, -1, Direction.SOUTH),  # Move forward to the south
    (1, 0, Direction.EAST),    # Move forward to the east
    (-1, 0, Direction.WEST),   # Move forward to the west
    (0, -1, Direction.NORTH),  # Move backward to the south when facing north
    (0, 1, Direction.SOUTH),   # Move backward to the north when facing south
    (-1, 0, Direction.EAST),   # Move backward to the west when facing east
    (1, 0, Direction.WEST)     # Move backward to the east when facing west
]

# A* search algorithm implementation
def a_star_search(grid_w: int, grid_h: int, obstacles: List[List[int]], robot_pos: List[int], robot_d: Direction, target_pos: List[int], target_d: Direction, verbose = False) -> Optional[Tuple[List[Tuple[Tuple[int, int], Direction]], float]]:
    # Expand the obstacles to accommodate the robot's 3x3 size
    expanded_obstacles = expand_obstacles(obstacles, grid_w, grid_h)
    if verbose: print("Expanded Obstacles:", expanded_obstacles)  # Debugging log

    start = tuple(robot_pos)
    target = tuple(target_pos)
    start_dir = robot_d
    target_dir = target_d

    start_node = Node(start, start_dir, 0, heuristic(start, target, start_dir, target_dir))
    open_list = []
    heapq.heappush(open_list, start_node)
    closed_set = set()

    # Add a counter to track how many iterations we go through
    iteration_counter = 0

    while open_list:
        iteration_counter += 1
        current_node = heapq.heappop(open_list)
        if verbose: print(f"Iteration {iteration_counter}: Current node: {current_node.position} facing {current_node.direction}")  # Debugging log

        # Check if the current node is the goal
        if current_node.position == target and current_node.direction == target_dir:
            path = []
            total_cost = current_node.g
            while current_node:
                path.append((current_node.position, current_node.direction))
                current_node = current_node.parent
            if verbose: print(f"Goal reached with total cost: {total_cost}")  # Debugging log
            return path[::-1], total_cost

        # Create a unique key for the node (position + direction)
        closed_key = (current_node.position, current_node.direction)

        # Check if the node has already been expanded (to avoid revisiting)
        if closed_key in closed_set:
            if verbose: print(f"Already visited: {current_node.position}, {current_node.direction}")  # Debugging log
            continue
        closed_set.add(closed_key)

        # Explore all possible movements (neighbors)
        for move_x, move_y, move_dir in MOVE_DIRECTION:
            new_x = current_node.position[0] + move_x
            new_y = current_node.position[1] + move_y
            new_pos = (new_x, new_y)
            new_direction = move_dir

            # Check validity against expanded obstacles
            if not is_valid(grid_w, grid_h, new_pos, expanded_obstacles):
                if verbose: print(f"Position {new_pos} blocked by obstacle.")  # Debugging log
                continue

            # Calculate rotation cost
            rotation_cost = Direction.rotation_cost(current_node.direction, new_direction)

            # Handle turn logic (e.g., adjust position if turning)
            if current_node.direction != new_direction:
                turn_direction = 'right' if new_direction in [Direction.EAST, Direction.NORTH] else 'left'
                new_pos, new_direction = perform_turn(current_node.position, current_node.direction, turn_direction)

                if not is_valid(grid_w, grid_h, new_pos, expanded_obstacles):
                    if verbose: print(f"Turned position {new_pos} is blocked by an obstacle.")  # Debugging log
                    continue

            # Calculate g cost (movement cost)
            g_cost = current_node.g + 1 + rotation_cost * TURN_FACTOR

            # Heuristic
            h_cost = heuristic(new_pos, target, new_direction, target_dir)

            neighbor = Node(new_pos, new_direction, g_cost, h_cost)
            neighbor.parent = current_node

            neighbor_key = (neighbor.position, neighbor.direction)
            if neighbor_key in closed_set:
                if verbose: print(f"Neighbor {neighbor.position} facing {neighbor.direction} already visited.")  # Debugging log
                continue

            if verbose: print(f"Adding neighbor {neighbor.position} facing {neighbor.direction} to open list.")  # Debugging log
            heapq.heappush(open_list, neighbor)

        # If the loop runs for too long, break out
        if iteration_counter > 10000:
            print("Exceeded iteration limit, aborting search.")
            return None  # Avoid infinite loop

    print("No path found.")  # Debugging log
    return None


# Example Usage
if __name__ == "__main__":
    grid_w = 20
    grid_h = 20
    obstacles = [
        [10, 5],
        [6, 1]
    ]
    robot_pos = [1, 1]
    robot_d = Direction.NORTH
    target_pos = [10, 6]
    target_d = Direction.NORTH

    result = a_star_search(grid_w, grid_h, obstacles, robot_pos, robot_d, target_pos, target_d)
    print("result:",result)
    if result:
        path, total_cost = result
        print(f"Path found with total cost: {total_cost}")
        for step in path:
            print(f"Position: {step[0]}, Direction: {step[1].name}")
    else:
        print("No path found.")
