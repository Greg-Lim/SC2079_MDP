import pygame
import sys

# Initialize Pygame
pygame.init()

# Constants
WINDOW_SIZE = (1000, 650)
GRID_SIZE = 20
CELL_SIZE = 25
MARGIN = 50
CONTROL_PANEL_HEIGHT = 100

START_POS = [0, 0]
START_DIRECTION = 'U'
OBSTACLES = []
OBSTACLE_DIRECTIONS = []

# Colors
WHITE = (255, 255, 255)
GRAY = (200, 200, 200)
GRAY_L = (225, 225, 225)
GREEN = (0, 255, 0)
RED = (255, 0, 0)
BLUE = (0, 0, 255)
BLACK = (0, 0, 0)

# Set up display
screen = pygame.display.set_mode(WINDOW_SIZE)
pygame.display.set_caption('Robot Simulator')

# Fonts
font = pygame.font.Font(None, 36)
font_input_box = pygame.font.Font(None, 24)
label_font = pygame.font.Font(None, 24)

# Input fields
input_boxes = {
    'x_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 45, 80, 30), 'text': '0', 'active': False},
    'y_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 105, MARGIN + 45, 80, 30), 'text': '0', 'active': False},
    'direction_o': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 190, MARGIN + 45, 30, 30), 'text': 'U', 'active': False},

    'x_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 185, 80, 30), 'text': '0', 'active': False},
    'y_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 105, MARGIN + 185, 80, 30), 'text': '0', 'active': False},
    'direction_p': {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 190, MARGIN + 185, 30, 30), 'text': 'U', 'active': False}
}

buttons = {
    'add' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 80, 125, 30), 'color': GREEN, 'text': 'ADD', 'active': False},
    'reset_o' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 150, MARGIN + 80, 70, 30), 'color': RED, 'text': 'RESET', 'active': False},

    'set' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 220, 125, 30), 'color': GREEN, 'text': 'SET', 'active': False},
    'reset_p' : {'rect': pygame.Rect(GRID_SIZE * CELL_SIZE + MARGIN + 150, MARGIN + 220, 70, 30), 'color': RED, 'text': 'RESET', 'active': False}
}

# Global variables
start_pos = START_POS
start_direction = START_DIRECTION
obstacles = OBSTACLES
obstacle_directions = OBSTACLE_DIRECTIONS

# Functions
def draw_grid():
    for x in range(GRID_SIZE):
        for y in range(GRID_SIZE):
            rect = pygame.Rect(MARGIN + x * CELL_SIZE, MARGIN + y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
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

    pygame.draw.rect(screen, GRAY, (GRID_SIZE * CELL_SIZE + MARGIN + 20, MARGIN + 140, 200, 40))
    text = font.render('Robot Position', True, BLACK)
    screen.blit(text, (GRID_SIZE * CELL_SIZE + MARGIN + 32, MARGIN + 150))

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

def draw_control_panel():
    # Draw background
    panel_rect = pygame.Rect(0, GRID_SIZE * CELL_SIZE + MARGIN + 50, WINDOW_SIZE[0], CONTROL_PANEL_HEIGHT)
    pygame.draw.rect(screen, WHITE, panel_rect)

    # Draw buttons and input boxes
    draw_titles()
    draw_input_boxes()
    draw_buttons()

def main():
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
                            
                            print(box['text'])
                            
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
                            for o in obstacles:
                                if o == obstacle:
                                    print("Failed to add new obstacle: location occupied")
                                    add_status = 0
                            
                            if add_status == 1:
                                obstacles.append(obstacle)
                                obstacle_directions.append(input_boxes['direction_o']['text'])
                                print("New obstacle added to", obstacles[-1], "\nNew obstacle's direction set to", obstacle_directions[-1])
                        
                        elif key == 'reset_o':
                            obstacles = OBSTACLES
                            obstacle_directions = OBSTACLE_DIRECTIONS
                            print("Obstacle(s) cleared")
                        
                        elif key == 'set':
                            start_pos[0] = int(input_boxes['x_p']['text'])
                            start_pos[1] = int(input_boxes['y_p']['text'])
                            start_direction = input_boxes['direction_p']['text']
                            print("Start position set to", start_pos, "\nStart direction set to", start_direction)
                        
                        elif key == 'reset_p':
                            start_pos = START_POS
                            start_direction = START_DIRECTION
                            print("Start position reset to", start_pos, "\nStart direction reset to", start_direction)

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
                                if box['text'][0] == '0' and len(box['text']) > 1:
                                    box['text'] = box['text'][1]
                                if int(box['text']) > 19:
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
