import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pygame
import sys
from Algorithm.entities.Robot import Robot
from Algorithm.entities.Entity import Obstacle, CellState, Grid
from Algorithm.consts import Direction

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
OBSTACLES = []
OBSTACLE_DIRECTIONS = []

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
    'direction_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 190, MARGIN + 45, 30, 30), 'text': 'U', 'active': False},

    'x_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 215, 80, 30), 'text': '1', 'active': False},
    'y_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 105, MARGIN + 215, 80, 30), 'text': '1', 'active': False},
    'direction_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 190, MARGIN + 215, 30, 30), 'text': 'U', 'active': False}
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
def update_robot_pos(r:Robot):
    s = r.get_start_state()

    p = [s.x, s.y]
    d = s.direction
    robot_position = []
    robot_position.extend([[p[0] + i, p[1] + j] for i in range(-1, 2) for j in range(-1, 2)])

    if d == 2: 
        robot_head = robot_position.pop(7)
    elif d == 6:
        robot_head = robot_position.pop(1)
    elif d == 4:
        robot_head = robot_position.pop(3)
    elif d == 0:
        robot_head = robot_position.pop(5)
    
    return robot_position, robot_head

def get_direction(d):
    if d == 'U':
        return Direction.NORTH
    elif d == 'D':
        return Direction.SOUTH
    elif d == 'L':
        return Direction.WEST
    elif d == 'R':
        return Direction.EAST

# Global variables
robot = Robot(CENTER_X, CENTER_Y, START_DIRECTION)
start_pos = START_POS
start_direction = START_DIRECTION
obstacles = OBSTACLES
obstacle_directions = OBSTACLE_DIRECTIONS
robot_pos, robot_head = update_robot_pos(robot)

# Functions
def draw_grid():
    robot_pos, robot_head = update_robot_pos(robot)

    for x in range(GRID_SIZE):
        for y in range(GRID_SIZE):
            rect = pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
            
            if [x, GRID_SIZE - 1 - y] in obstacles:
                pygame.draw.rect(screen, BLACK, rect)

                i = obstacles.index([x, GRID_SIZE - 1 - y])
                if obstacle_directions[i] == 'U':
                    pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, CELL_SIZE, 5))
                elif obstacle_directions[i] == 'D':
                    pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE + (CELL_SIZE - 5), CELL_SIZE, 5))
                elif obstacle_directions[i] == 'L':
                    pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, 5, CELL_SIZE))
                elif obstacle_directions[i] == 'R':
                    pygame.draw.rect(screen, RED, pygame.Rect(MARGIN + x * CELL_SIZE + (CELL_SIZE - 5), MARGIN + y * CELL_SIZE, 5, CELL_SIZE))

            elif [x, GRID_SIZE - 1 - y] == robot_head:
                pygame.draw.rect(screen, ORANGE, rect)
            
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

def main():
    global robot, start_pos, start_direction, obstacles, obstacle_directions, robot_pos, robot_head
    clock = pygame.time.Clock()
    
    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            elif event.type == pygame.MOUSEBUTTONDOWN:
                # Handle mouse clicks to activate the correct input box
                for key, box in input_boxes.items():
                    if box['rect'].collidepoint(event.pos):
                        box['active'] = True
                        
                        if key in ['direction_o', 'direction_p']:
                            if box['text'] == 'U':
                                box['text'] = 'D'
                            elif box['text'] == 'D':
                                box['text'] = 'L'
                            elif box['text'] == 'L':
                                box['text'] = 'R'
                            elif box['text'] == 'R':
                                box['text'] = 'U'
                            
                            input_boxes[key]['text'] = box['text']
                    else:
                        box['active'] = False
                
                for key, box in buttons.items():
                    if box['rect'].collidepoint(event.pos):
                        box['active'] = True

                        if key == 'add':
                            obstacle = []
                            obstacle.append(int(input_boxes['x_o']['text']))
                            obstacle.append(int(input_boxes['y_o']['text']))

                            add_status = 1
                            if obstacle in obstacles or obstacle in robot_pos:
                                message_1 = "Failed to add new obstacle:"
                                message_2 = "location occupied"
                                print(message_1, message_2)
                                pop_ups['o_1']['text'] = message_1
                                pop_ups['o_2']['text'] = message_2
                                add_status = 0
                            
                            if add_status == 1:
                                obstacles.append(obstacle)
                                obstacle_directions.append(input_boxes['direction_o']['text'])
                                
                                message_1 = f"New obstacle added to {obstacles[-1]}"
                                message_2 = f"New obstacle's direction set to {obstacle_directions[-1]}"
                                print(message_1, message_2)
                                pop_ups['o_1']['text'] = message_1
                                pop_ups['o_2']['text'] = message_2
                        
                        elif key == 'reset_o':
                            obstacles.clear()
                            obstacle_directions.clear()

                            message_1 = "Obstacle(s) cleared"
                            print(message_1)
                            pop_ups['o_1']['text'] = message_1
                            pop_ups['o_2']['text'] = ''
                        
                        elif key == 'set':
                            set_status = 1
                            robot = Robot(int(input_boxes['x_p']['text']), int(input_boxes['y_p']['text']), get_direction(input_boxes['direction_p']['text']))
                            #rs, _ = update_robot_pos([int(input_boxes['x_p']['text']), int(input_boxes['y_p']['text'])], input_boxes['direction_p']['text'])
                            rs, _ = update_robot_pos(robot)
                            for r in rs:
                                if r in obstacles:
                                    message_1 = "Failed to add new obstacle:"
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
                                message_2 = f"Start direction set to {start_direction}"
                                print(message_1, message_2)
                                pop_ups['p_1']['text'] = message_1
                                pop_ups['p_2']['text'] = message_2
                        
                        elif key == 'reset_p':
                            start_pos = [1, 1]
                            start_direction = get_direction('U')
                            robot = Robot(start_pos[0], start_pos[1], start_direction)
                            robot_pos, robot_head = update_robot_pos(robot)

                            message_1 = f"Start position reset to {start_pos}"
                            message_2 = f"Start direction reset to {start_direction}"
                            print(message_1, message_2)
                            pop_ups['p_1']['text'] = message_1
                            pop_ups['p_2']['text'] = message_2
                            
                            cleared = 0
                            for obstacle in obstacles:
                                if obstacle in robot_pos:
                                    obstacles.pop(obstacles.index(obstacle))
                                    cleared = 1

                            if cleared == 1:
                                message_1 = f"Obstacles at start position cleared"
                                message_2 = f""
                                print(message_1, message_2)
                                pop_ups['o_1']['text'] = message_1
                                pop_ups['o_2']['text'] = message_2

                            input_boxes['x_p']['text'] = '1'
                            input_boxes['y_p']['text'] = '1'
                            input_boxes['direction_p']['text'] = 'U'

                        input_boxes['x_o']['text'] = '0'
                        input_boxes['y_o']['text'] = '0'
                        input_boxes['direction_o']['text'] = 'U'
                            
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
