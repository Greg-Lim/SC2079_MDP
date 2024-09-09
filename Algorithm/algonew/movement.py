from math import cos, sin, pi

def move_forward(curr_x, curr_y, curr_dir, distance):
    """
    curr_x, y: current position
    direction: current direction in radians
    distance: distance to move forward
    """
    curr_x += distance * cos(curr_dir)
    curr_y += distance * sin(curr_dir)
    return curr_x, curr_y, curr_dir

def turn(x, y, direction, is_forward: bool, is_left: bool, turning_radius, turn_angle):
    """
    x, y: current position
    direction: current direction in radians
    forward: True if the robot is moving forward, False if the robot is moving backward
    left: True if the robot is turning left, False if the robot is turning right
    turning_radius: turning radius of the robot
    turn_angle: angle to turn in radians

    The robot turns in an arc 
    """

    if is_left:
        turn_angle = -turn_angle
    # if turing left, pivot should be 90 degrees to the left of the robot
    # if turning right, pivot should be 90 degrees to the right of the robot
    rotate = 90 if is_left else -90
    pivot = [x + turning_radius * cos(direction + rotate*pi/180), y + turning_radius * sin(direction + rotate*pi/180)]

    if is_forward:
        new_direction = direction - turn_angle
    else:
        new_direction = direction + turn_angle

    print(new_direction)

    new_x = pivot[0] - turning_radius * cos(new_direction + rotate*pi/180)
    new_y = pivot[1] - turning_radius * sin(new_direction + rotate*pi/180)
    return new_x, new_y, new_direction, pivot
