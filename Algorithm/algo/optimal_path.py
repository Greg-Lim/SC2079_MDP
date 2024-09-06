import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from entities.Entity import *
from entities.Robot import *
from entities.Instructions import *
from consts import *
from algo.a_star_search import a_star_search
import itertools

# Helper function to compute the cost and commands for a given route
def compute_route_cost(grid_w, grid_h, robot_pos, robot_d, obs_pos, route_positions, route_directions):
    total_cost = 0
    commands = []

    # Start from the initial robot position and direction
    current_pos = robot_pos
    current_dir = robot_d

    # Go through the target positions in the specified order (route)
    for i, target_pos in enumerate(route_positions):
        target_dir = route_directions[i]

        # Get the commands and cost for this leg of the journey
        result = a_star_search(grid_w, grid_h, obs_pos, current_pos, current_dir, target_pos, target_dir)
        
        # If no valid path found, a_star_search returns None
        if result is None:
            return None, float('inf')  # If no path was found, return infinite cost
        
        leg_commands, leg_cost = result  # Unpack result safely here

        # Add this leg's cost and commands to the total
        total_cost += leg_cost
        commands.extend(leg_commands)

        # Update the current position and direction to the target's position and direction
        current_pos = target_pos
        current_dir = target_dir

    return commands, total_cost


# Function to calculate the optimal path using brute force
def optimal_path(grid, robot):
    grid_w = grid.size_x
    grid_h = grid.size_y
    obstacles = grid.get_obstacles()
    robot_pos = (robot.get_start_state().x, robot.get_start_state().y)
    robot_d = robot.get_start_state().direction

    # Convert obstacles into a list of obstacle positions and directions
    obs_pos = []
    for obstacle in obstacles:
        obs_pos.append([obstacle.x, obstacle.y])

    # Create a list of target positions and their corresponding directions
    targets_pos = []
    targets_d = []

    for obstacle in obstacles:
        if obstacle.direction == Direction.NORTH:
            targets_pos.append((obstacle.x, obstacle.y + OBSERVATION_DISTANCE + 1))
            targets_d.append(Direction.SOUTH)
        elif obstacle.direction == Direction.SOUTH:
            targets_pos.append((obstacle.x, obstacle.y - OBSERVATION_DISTANCE - 1))
            targets_d.append(Direction.NORTH)
        elif obstacle.direction == Direction.WEST:
            targets_pos.append((obstacle.x - OBSERVATION_DISTANCE - 1, obstacle.y))
            targets_d.append(Direction.EAST)
        elif obstacle.direction == Direction.EAST:
            targets_pos.append((obstacle.x + OBSERVATION_DISTANCE + 1, obstacle.y))
            targets_d.append(Direction.WEST)

    # Generate all possible permutations of target positions (brute force approach)
    n_targets = len(targets_pos)
    possible_routes = itertools.permutations(range(n_targets))  # Generates all routes (permutations)

    min_cost = float('inf')
    optimal_commands = None
    route_found = False  # To check if we found any valid route

    # Evaluate each possible route
    for route in possible_routes:
        # Extract the actual positions and directions in this order
        route_positions = [targets_pos[i] for i in route]
        route_directions = [targets_d[i] for i in route]

        # Compute the cost for this route
        route_commands, route_cost = compute_route_cost(grid_w, grid_h, robot_pos, robot_d, obs_pos, route_positions, route_directions)

        # Check if this route has the lowest cost
        if route_cost < min_cost:
            min_cost = route_cost
            optimal_commands = route_commands
            route_found = True  # Valid route found

    if not route_found:
        return "Error: Unable to visit all target positions, no valid path exists", float('inf')

    # Return the optimal commands and total cost
    return optimal_commands, min_cost

if __name__ == "__main__":
    pass
