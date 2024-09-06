import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import pygame
import time
from algo.algo import MazeSolver 
from entities.Robot import Robot
from entities.Entity import Obstacle, CellState, Grid
from consts import Direction
from helper import command_generator

# Initialize Pygame
pygame.init()

# Constants
WINDOW_SIZE = (800, 650)
GRID_SIZE = 20
CELL_SIZE = 25
MARGIN = 50
CONTROL_PANEL_HEIGHT = 100

START_POS = [1, 1]
CENTER_X = 1
CENTER_Y = 1
START_DIRECTION = Direction.NORTH

# Initialize Grid
grid = Grid(size_x=GRID_SIZE+1, size_y=GRID_SIZE+1)

# Colors
WHITE = (255, 255, 255)
GRAY_L = (225, 225, 225)
GRAY = (200, 200, 200)
BLACK = (0, 0, 0)
RED = (255, 0, 0)
GREEN = (0, 255, 0)
BLUE = (0, 0, 255)
ORANGE = (255, 165, 0)

# Set up display
screen = pygame.display.set_mode(WINDOW_SIZE)
pygame.display.set_caption('Robot Simulator')

# Fonts
font = pygame.font.Font(None, 36)
font_input_box = pygame.font.Font(None, 24)
font_pop_up = pygame.font.Font(None, 18)
label_font = pygame.font.Font(None, 24)

# Input fields
input_boxes = {
    'x_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 45, 80, 30), 'text': '0', 'active': False},
    'y_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 105, MARGIN + 45, 80, 30), 'text': '0', 'active': False},
    'direction_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 190, MARGIN + 45, 30, 30), 'text': 'N', 'active': False},

    'x_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 215, 80, 30), 'text': '1', 'active': False},
    'y_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 105, MARGIN + 215, 80, 30), 'text': '1', 'active': False},
    'direction_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 190, MARGIN + 215, 30, 30), 'text': 'N', 'active': False}
}

buttons = {
    'add' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 80, 125, 30), 'color': GREEN, 'text': 'ADD', 'active': False},
    'reset_o' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 150, MARGIN + 80, 70, 30), 'color': RED, 'text': 'RESET', 'active': False},

    'set' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 250, 125, 30), 'color': GREEN, 'text': 'SET', 'active': False},
    'reset_p' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 150, MARGIN + 250, 70, 30), 'color': RED, 'text': 'RESET', 'active': False},
    
    'run' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 350, 200, 30), 'color': GREEN, 'text': 'RUN', 'active': False}
}

pop_ups = {
    'o_1': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 110, 125, 20), 'text': '', 'active': False},
    'o_2': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 130, 125, 20), 'text': '', 'active': False},
    
    'p_1': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 280, 125, 20), 'text': '', 'active': False},
    'p_2': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 300, 125, 20), 'text': '', 'active': False}
}

# Functions
def update_robot_pos(robot: Robot):
    s = robot.get_start_state()
    p = [s.x, s.y]
    d = s.direction

    # Create the robot position grid
    robot_position = []
    robot_position.extend([[p[0] + i, p[1] + j] for i in range(-1, 2) for j in range(-1, 2)])

    # Determine the head position based on the direction
    if d == Direction.NORTH:
        robot_head = robot_position.pop(5)
    elif d == Direction.EAST:
        robot_head = robot_position.pop(7)
    elif d == Direction.SOUTH:
        robot_head = robot_position.pop(3)
    elif d == Direction.WEST:
        robot_head = robot_position.pop(1)
    
    return robot_position, robot_head

def get_direction(d):
    if d == 'N':
        return Direction.NORTH
    elif d == 'S':
        return Direction.SOUTH
    elif d == 'W':
        return Direction.WEST
    elif d == 'E':
        return Direction.EAST

def cycle_direction(direction):
    """Cycle through the directions in the order NORTH -> EAST -> SOUTH -> WEST."""
    if direction == Direction.NORTH:
        return Direction.EAST
    elif direction == Direction.EAST:
        return Direction.SOUTH
    elif direction == Direction.SOUTH:
        return Direction.WEST
    elif direction == Direction.WEST:
        return Direction.NORTH

# Initialize Global Variables
robot = Robot(CENTER_X, CENTER_Y, START_DIRECTION)
start_pos = START_POS
start_direction = START_DIRECTION
robot_pos, robot_head = update_robot_pos(robot)

# Functions
def draw_grid():
    robot_pos, robot_head = update_robot_pos(robot)
    for x in range(GRID_SIZE):
        for y in range(GRID_SIZE):
            rect = pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
            
            # Check if the current grid cell matches any obstacle
            for obstacle in grid.get_obstacles():
                if (obstacle.x == x) and (obstacle.y == GRID_SIZE - 1 - y):
                    pygame.draw.rect(screen, BLACK, rect)
                    if obstacle.direction == Direction.NORTH:
                        pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, CELL_SIZE, 5))
                    elif obstacle.direction == Direction.SOUTH:
                        pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE + (CELL_SIZE - 5), CELL_SIZE, 5))
                    elif obstacle.direction == Direction.WEST:
                        pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, 5, CELL_SIZE))
                    elif obstacle.direction == Direction.EAST:
                        pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE + (CELL_SIZE - 5), MARGIN + y * CELL_SIZE, 5, CELL_SIZE))

            # Draw the robot's head
            if [x, GRID_SIZE - 1 - y] == robot_head:
                pygame.draw.rect(screen, ORANGE, rect)
            
            # Draw the robot's body
            elif [x, GRID_SIZE - 1 - y] in robot_pos:
                pygame.draw.rect(screen, BLUE, rect)

            pygame.draw.rect(screen, GRAY, rect, 1)

def draw_labels():
    # Draw X labels (bottom)
    for x in range(GRID_SIZE):
        label = label_font.render(str(x), True, BLACK)
        screen.blit(label, (MARGIN + x * CELL_SIZE + CELL_SIZE // 2 - label.get_width() // 2, MARGIN + GRID_SIZE * CELL_SIZE + 5))
    
    # Draw Y labels (left)
    for y in range(GRID_SIZE): 
        label = label_font.render(str(GRID_SIZE - 1 - y), True, BLACK)
        screen.blit(label, (MARGIN - label.get_width() - 5, MARGIN + y * CELL_SIZE + CELL_SIZE // 2 - label.get_height() // 2))

def draw_titles():
    pygame.draw.rect(screen, GRAY, (GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN, 200, 40))
    text = font.render('Add Obstacle', True, BLACK)
    screen.blit(text, (GRID_SIZE * CELL_SIZE + MARGIN + 40, MARGIN + 10))

    pygame.draw.rect(screen, GRAY, (GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 170, 200, 40))
    text = font.render('Robot Position', True, BLACK)
    screen.blit(text, (GRID_SIZE * CELL_SIZE + MARGIN + 32, MARGIN + 180))

def draw_input_boxes():
    for key, box in input_boxes.items():
        color = GRAY_L if box['active'] else WHITE
        pygame.draw.rect(screen, color, box['rect'])
        pygame.draw.rect(screen, BLACK, box['rect'], 2)
        text_surface = font_input_box.render(box['text'], True, BLACK)
        screen.blit(text_surface, (box['rect'].x + 6, box['rect'].y + 8))

def draw_buttons():
    for key, box in buttons.items():
        pygame.draw.rect(screen, box['color'], box['rect'])
        pygame.draw.rect(screen, BLACK, box['rect'], 2)
        text_surface = font_input_box.render(box['text'], True, BLACK)
        screen.blit(text_surface, (box['rect'].x + 6, box['rect'].y + 8))

def draw_pop_ups():
    for key, box in pop_ups.items():
        pygame.draw.rect(screen, WHITE, box['rect'])
        text_surface = font_pop_up.render(box['text'], True, BLACK)
        screen.blit(text_surface, (box['rect'].x + 6, box['rect'].y + 8))

def draw_control_panel():
    # Draw background
    panel_rect = pygame.Rect(0, GRID_SIZE * CELL_SIZE + MARGIN + 50, WINDOW_SIZE[0], CONTROL_PANEL_HEIGHT)
    pygame.draw.rect(screen, WHITE, panel_rect)

    # Draw buttons and input boxes
    draw_titles()
    draw_input_boxes()
    draw_buttons()
    draw_pop_ups()    

def add_new_obstacle(x, y, direction):
    obstacle = Obstacle(x, y, direction, len(grid.get_obstacles()))
    grid.add_obstacle(obstacle)

def reset_obstacles():
    grid.reset_obstacles()

def remove_obstacle(x, y):
    obstacles = grid.get_obstacles()
    for obstacle in obstacles:
        if obstacle.x == x and obstacle.y == y:
            obstacles.remove(obstacle)
            break

def visualize_run(commands):
    global robot_pos, robot_head

    for command in commands:
        # Parse the command
        action = command[:2]
        value = command[2:]

        if action == "FW":
            # Move Forward
            steps = int(value)
            for _ in range(steps // 10):
                move_robot_forward()  # function to update the robot's position forward
                update_visualization()  # Redraw the grid after moving
                pygame.time.delay(500)  # Delay for visualization
        elif action == "BW":
            # Move Backward
            steps = int(value)
            for _ in range(steps // 10):
                move_robot_backward()  # function to update the robot's position backward
                update_visualization()  # Redraw the grid after moving
                pygame.time.delay(500)
        elif action == "FR":
            # Turn Right
            turn_robot_right()  # function to update robot's direction
            update_visualization()  # Redraw the grid after turning
            pygame.time.delay(500)
        elif action == "FL":
            # Turn Left
            turn_robot_left()  # function to update robot's direction
            update_visualization()  # Redraw the grid after turning
            pygame.time.delay(500)
        elif action.startswith("SNAP"):
            # Take Snapshot
            snap_action = value.split("_")[1]  # Determine if it's _L, _C, or _R
            print(f"Taking snapshot: {snap_action}")
            pygame.time.delay(500)
        elif action == "FIN":
            print("Simulation Finished")
            break

def move_robot_forward():
    """Function to move the robot forward based on its current direction."""
    global robot, robot_pos, robot_head
    # Update robot's position depending on direction
    state = robot.get_start_state()
    if state.direction == Direction.NORTH:
        robot.set_position(state.x, state.y + 1)
    elif state.direction == Direction.EAST:
        robot.set_position(state.x + 1, state.y)
    elif state.direction == Direction.SOUTH:
        robot.set_position(state.x, state.y - 1)
    elif state.direction == Direction.WEST:
        robot.set_position(state.x - 1, state.y)
    robot_pos, robot_head = update_robot_pos(robot)

def move_robot_backward():
    """Function to move the robot backward based on its current direction."""
    global robot, robot_pos, robot_head
    state = robot.get_start_state()
    if state.direction == Direction.NORTH:
        robot.set_position(state.x, state.y - 1)
    elif state.direction == Direction.EAST:
        robot.set_position(state.x - 1, state.y)
    elif state.direction == Direction.SOUTH:
        robot.set_position(state.x, state.y + 1)
    elif state.direction == Direction.WEST:
        robot.set_position(state.x + 1, state.y)
    robot_pos, robot_head = update_robot_pos(robot)

def turn_robot_right():
    """Function to turn the robot 90 degrees to the right."""
    global robot, robot_pos, robot_head
    robot.set_direction(cycle_direction(robot.get_start_state().direction))
    robot_pos, robot_head = update_robot_pos(robot)

def turn_robot_left():
    """Function to turn the robot 90 degrees to the left."""
    global robot, robot_pos, robot_head
    # Cycle the opposite direction
    for _ in range(3):  # Cycle 3 times to go left (as 4 steps go full circle)
        robot.set_direction(cycle_direction(robot.get_start_state().direction))
    robot_pos, robot_head = update_robot_pos(robot)

def update_visualization():
    """Repaint the grid and the robot."""
    screen.fill(WHITE)
    draw_grid()
    draw_labels()
    draw_control_panel()
    pygame.display.flip()

def event_handler(event, robot, start_pos, start_direction, robot_pos, robot_head, grid):
    clock = pygame.time.Clock()

    if event.type == pygame.QUIT:
        pygame.quit()
        sys.exit()
    elif event.type == pygame.MOUSEBUTTONDOWN:
        mouse_x, mouse_y = event.pos
        grid_x = (mouse_x - MARGIN) // CELL_SIZE
        grid_y = GRID_SIZE - 1 - (mouse_y - MARGIN) // CELL_SIZE

        if grid_x >= 0 and grid_x < GRID_SIZE and grid_y >= 0 and grid_y < GRID_SIZE:
            if event.button == 1:  # Left click
                clicked_on_obstacle = False
                clicked_on_robot = False

                # Check if the robot is clicked
                robot_pos, robot_head = update_robot_pos(robot)
                for pos in robot_pos:
                    if pos[0] == grid_x and pos[1] == grid_y:
                        robot.set_direction(cycle_direction(robot.get_start_state().direction))
                        clicked_on_robot = True
                        break

                # Check if the robot's head is clicked
                if not clicked_on_robot and robot_head[0] == grid_x and robot_head[1] == grid_y:
                    robot.set_direction(cycle_direction(robot.get_start_state().direction))
                    clicked_on_robot = True

                if not clicked_on_robot:  # If the robot was not clicked, check obstacles
                    for obstacle in grid.get_obstacles():
                        if obstacle.x == grid_x and obstacle.y == grid_y:
                            # Change direction if clicked on an obstacle
                            obstacle.direction = cycle_direction(obstacle.direction)
                            clicked_on_obstacle = True
                            break

                    if not clicked_on_obstacle:
                        # Add new obstacle with direction 'NORTH' if clicked on a blank cell
                        if grid.is_valid_coord(grid_x, grid_y):
                            add_new_obstacle(grid_x, grid_y, Direction.NORTH)

            elif event.button == 3:  # Right click
                # Remove obstacle if right-clicked
                remove_obstacle(grid_x, grid_y)

        # Handle mouse clicks to activate the correct input box
        for key, box in input_boxes.items():
            if box['rect'].collidepoint(event.pos):
                box['active'] = True
                
                if key in ['direction_o', 'direction_p']:
                    if box['text'] == 'N':
                        box['text'] = 'E'
                    elif box['text'] == 'E':
                        box['text'] = 'S'
                    elif box['text'] == 'S':
                        box['text'] = 'W'
                    elif box['text'] == 'W':
                        box['text'] = 'N'
                    
                    input_boxes[key]['text'] = box['text']
            else:
                box['active'] = False
        
        for key, box in buttons.items():
            if box['rect'].collidepoint(event.pos):
                box['active'] = True

                if key == 'add':
                    x = int(input_boxes['x_o']['text'])
                    y = int(input_boxes['y_o']['text'])
                    direction = get_direction(input_boxes['direction_o']['text'])
                    obstacle = Obstacle(x, y, direction, len(grid.get_obstacles()))
                    
                    if not grid.is_valid_coord(x, y):
                        message_1 = "Invalid coordinates for obstacle."
                        message_2 = ""
                        print(message_1, message_2)
                        pop_ups['o_1']['text'] = message_1
                        pop_ups['o_2']['text'] = message_2
                    else:
                        grid.add_obstacle(obstacle)
                        message_1 = f"New obstacle added at ({x}, {y})"
                        message_2 = f"Direction: {direction}"
                        print(message_1, message_2)
                        pop_ups['o_1']['text'] = message_1
                        pop_ups['o_2']['text'] = message_2
                
                elif key == 'reset_o':
                    reset_obstacles()
                    message_1 = "Obstacle(s) cleared"
                    print(message_1)
                    pop_ups['o_1']['text'] = message_1
                    pop_ups['o_2']['text'] = ''
                
                elif key == 'set':
                    set_status = 1
                    robot = Robot(int(input_boxes['x_p']['text']), int(input_boxes['y_p']['text']), get_direction(input_boxes['direction_p']['text']))
                    rs, _ = update_robot_pos(robot)
                    for r in rs:
                        if any([r == [ob.x, ob.y] for ob in grid.get_obstacles()]):
                            message_1 = "Failed to set robot position:"
                            message_2 = "location occupied"
                            print(message_1, message_2)
                            pop_ups['p_1']['text'] = message_1
                            pop_ups['p_2']['text'] = message_2
                            set_status = 0
                    
                    if set_status == 1:
                        start_pos[0] = int(input_boxes['x_p']['text'])
                        start_pos[1] = int(input_boxes['y_p']['text'])
                        start_direction = get_direction(input_boxes['direction_p']['text'])
                        robot = Robot(start_pos[0], start_pos[1], start_direction)
                        robot_pos, robot_head = update_robot_pos(robot)

                        message_1 = f"Start position set to {start_pos}"
                        message_2 = f"Direction set to {start_direction}"
                        print(message_1, message_2)
                        pop_ups['p_1']['text'] = message_1
                        pop_ups['p_2']['text'] = message_2
                
                elif key == 'reset_p':
                    start_pos = [1, 1]
                    start_direction = Direction.NORTH
                    robot = Robot(start_pos[0], start_pos[1], start_direction)
                    robot_pos, robot_head = update_robot_pos(robot)

                    message_1 = f"Start position reset to {start_pos}"
                    message_2 = f"Direction reset to {start_direction}"
                    print(message_1, message_2)
                    pop_ups['p_1']['text'] = message_1
                    pop_ups['p_2']['text'] = message_2

                    for obstacle in grid.get_obstacles():
                        if [obstacle.x, obstacle.y] in robot_pos:
                            grid.get_obstacles().remove(obstacle)
                            print(f"Obstacle at ({obstacle.x}, {obstacle.y}) removed due to robot position")
                    
                    input_boxes['x_p']['text'] = '1'
                    input_boxes['y_p']['text'] = '1'
                    input_boxes['direction_p']['text'] = 'N'

                elif key == 'run':
                    maze_solver = MazeSolver(GRID_SIZE, GRID_SIZE, robot.states[-1].x, robot.states[-1].y - 1, robot.states[-1].direction, big_turn=None)
                    
                    obstacles = grid.get_obstacles()
                    obs = []
                    for obstacle in obstacles:
                        maze_solver.add_obstacle(obstacle.x, obstacle.y, obstacle.direction, obstacle.obstacle_id)
                        ob = {"x": obstacle.x, "y": obstacle.y, "d": obstacle.direction, "id": obstacle.obstacle_id}
                        obs.append(ob)

                    start = time.time()
                    optimal_path, distance = maze_solver.get_optimal_order_dp(retrying=True)

                    print(f"Time taken to find shortest path using A* search: {time.time() - start}s")
                    print(f"Distance to travel: {distance} units")

                    commands = command_generator(optimal_path, obs)
                    print(commands)

                    visualize_run(commands)

                input_boxes['x_o']['text'] = '0'
                input_boxes['y_o']['text'] = '0'
                input_boxes['direction_o']['text'] = 'N'
                    
            else:
                box['active'] = False

    elif event.type == pygame.KEYDOWN:
        for key, box in input_boxes.items():
            if box['active']:
                if key in ['x_o', 'y_o', 'x_p', 'y_p']:
                    if event.key == pygame.K_BACKSPACE:
                        box['text'] = box['text'][:-1]  # Remove last character
                    elif event.unicode.isdigit():
                        box['text'] += event.unicode  # Add new digit
                        if key in ['x_p', 'y_p'] and int(box['text']) < 1:
                            box['text'] = '1'
                        elif box['text'][0] == '0' and len(box['text']) > 1:
                            box['text'] = box['text'][1]
                        if key in ['x_p', 'y_p'] and int(box['text']) > 18:
                            box['text'] = '18'
                        elif int(box['text']) > 19:
                            box['text'] = '19'

def main():
    global robot, start_pos, start_direction, robot_pos, robot_head, grid
    clock = pygame.time.Clock()
    
    while True:
        for event in pygame.event.get():
            event_handler(event, robot, start_pos, start_direction, robot_pos, robot_head, grid)
                                
        screen.fill(WHITE)
        
        # Draw grid
        draw_grid()
        
        # Draw labels
        draw_labels()
        
        # Draw control panels
        draw_control_panel()
        
        pygame.display.flip()
        clock.tick(30)

if __name__ == "__main__":
    main()
