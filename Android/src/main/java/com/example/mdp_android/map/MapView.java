package com.example.mdp_android.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapView extends View {
    private static final int COL = 20; // Number of columns
    private static final int ROW = 20; // Number of rows
    private int robotX = 1; // Initial X position (column)
    private int robotY = 1; // Initial Y position (row)
    private String robotDirection = "N"; // Initial direction (N, S, E, W)
    private boolean isDraggingRobot = false;
    private float cellSize;
    private final Paint gridPaint = new Paint();
    private final Paint obstaclePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint robotPaint = new Paint();
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private Obstacle selectedObstacle;
    private boolean showAxisLabels = false; // Flag to show/hide axis labels
    // Fragment 2 buttons
    private boolean setObstacle = false;
    private boolean setRemoveObstacle = false;
    private boolean setReceivedInstructions = false;
    private final Rect mapBounds = new Rect(0, 0, 0, 0);

    public class Obstacle {
        int id;
        float startX, startY, endX, endY;
        String text;
        String direction; // N, S, E, W
        Boolean newID = false;
        Drawable imageDrawable;
        Drawable directionDrawable;

        @SuppressLint("UseCompatLoadingForDrawables")
        Obstacle(int id, float startX, float startY, float endX, float endY, String text) {
            this.id = id;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.text = text;
            this.direction = "N"; // Default direction

            this.imageDrawable = null;
            this.directionDrawable = null;
        }
    }

    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(2);
        gridPaint.setStyle(Paint.Style.STROKE);

        obstaclePaint.setColor(Color.RED);
        obstaclePaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(15);
        textPaint.setTextAlign(Paint.Align.CENTER);

        robotPaint.setColor(Color.BLUE);
        robotPaint.setStyle(Paint.Style.FILL);

        mapBounds.set(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("MapView", "onDraw called. Robot position: " + robotX + ", " + robotY);

        // Calculate cell size, leave space for labels outside the grid
        cellSize = (float) getWidth() / COL;  // Grid size remains same
        drawGrid(canvas);
        drawObstacles(canvas);
        drawRobot(canvas);
        if (showAxisLabels) {
            drawAxesLabels(canvas);
        }
    }

    private void drawGrid(Canvas canvas) {
        // Draw the grid without any labels inside it
        for (int x = 0; x <= COL; x++) {
            canvas.drawLine(x * cellSize, 0, x * cellSize, getHeight(), gridPaint);  // Normal grid lines
        }
        for (int y = 0; y <= ROW; y++) {
            float adjustedY = getHeight() - y * cellSize;  // Y-inverted for grid lines
            canvas.drawLine(0, adjustedY, getWidth(), adjustedY, gridPaint);  // Draw the grid
        }
    }

    private void drawAxesLabels(Canvas canvas) {
        Paint axisLabelPaint = new Paint();
        axisLabelPaint.setColor(Color.BLACK);
        axisLabelPaint.setTextSize(cellSize / 3);  // Smaller font for labels
        axisLabelPaint.setTextAlign(Paint.Align.CENTER);

        // Debugging logs to ensure the method is called and bounds are correct
        Log.d("MapView", "Drawing X and Y axis labels");

        // X-axis labels (0, 1, 2,...): Place these below the grid
        for (int i = 0; i < COL; i++) {
            float x = (i + 0.5f) * cellSize;  // Center of each column
            float y = getHeight() - cellSize / 2;  // Slightly above the bottom of the grid
            canvas.drawText(String.valueOf(i), x, y, axisLabelPaint);
        }

        // Y-axis labels (0, 1, 2,...): Place these to the left of the grid
        for (int i = 0; i < ROW; i++) {
            float x = cellSize / 2;  // Slightly to the right of the left edge
            float y = getHeight() - (i + 0.5f) * cellSize;  // Center of each row (inverted Y-axis)
            canvas.drawText(String.valueOf(i), x, y, axisLabelPaint);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void drawObstacles(Canvas canvas) {
        for (Obstacle obstacle : obstacles) {
            // Adjust Y-coordinates for the inverted Y-axis
            float adjustedStartY = getHeight() - obstacle.startY;
            float adjustedEndY = getHeight() - obstacle.endY;

            // Draw obstacle rectangle
            canvas.drawRect(obstacle.startX, adjustedEndY, obstacle.endX, adjustedStartY, obstaclePaint);
            drawObstacleDirectionPadding(canvas, obstacle);

            if (obstacle.newID) {
                // Draw the obstacle image
                obstacle.imageDrawable.setBounds((int) obstacle.startX, (int) adjustedEndY, (int) obstacle.endX, (int) adjustedStartY);
                obstacle.imageDrawable.draw(canvas);

                // Set position for the direction arrow based on the obstacle's direction
                float directionsX = 0;
                float directionsY = 0;
                String imageFile = null;

                switch (obstacle.direction) {
                    case "N":
                        directionsX = obstacle.startX + cellSize / 2;  // Center of the obstacle horizontally
                        directionsY = adjustedEndY - cellSize / 2;    // Move to the cell above the obstacle
                        imageFile = "final_down";  // Arrow pointing up for North
                        break;
                    case "S":
                        directionsX = obstacle.startX + cellSize / 2;  // Center horizontally
                        directionsY = adjustedStartY + cellSize / 2;  // Move to the cell below the obstacle
                        imageFile = "final_up";  // Arrow pointing down for South
                        break;
                    case "E":
                        directionsX = obstacle.endX + cellSize / 2;    // Move to the cell to the right
                        directionsY = (adjustedStartY + adjustedEndY) / 2;  // Center vertically
                        imageFile = "final_left";  // Arrow pointing right for East
                        break;
                    case "W":
                        directionsX = obstacle.startX - cellSize / 2;  // Move to the cell to the left
                        directionsY = (adjustedStartY + adjustedEndY) / 2;  // Center vertically
                        imageFile = "final_right";  // Arrow pointing left for West
                        break;
                }

                // Draw direction arrow based on the calculated direction coordinates
                int resourceId = getContext().getResources().getIdentifier(imageFile, "drawable", getContext().getPackageName());
                obstacle.directionDrawable = getContext().getDrawable(resourceId);

                if (obstacle.directionDrawable != null) {
                    obstacle.directionDrawable.setBounds(
                            (int) (directionsX - cellSize / 2), (int) (directionsY - cellSize / 2),
                            (int) (directionsX + cellSize / 2), (int) (directionsY + cellSize / 2)
                    );
                    obstacle.directionDrawable.draw(canvas);  // Draw the direction arrow
                }

            } else {
                // Draw the text (default ID)
                textPaint.setTextSize(cellSize / 3);  // Default ID size
                canvas.drawText(obstacle.text, (obstacle.startX + obstacle.endX) / 2, (adjustedStartY + adjustedEndY) / 2 + 10, textPaint);
            }

        }
    }


    private void drawRobot(Canvas canvas) {
        Drawable robotDrawable = null;

        // Load the appropriate robot drawable based on direction
        switch (robotDirection) {
            case "N":
                robotDrawable = getContext().getDrawable(R.drawable.robot_up);
                break;
            case "S":
                robotDrawable = getContext().getDrawable(R.drawable.robot_down);
                break;
            case "E":
                robotDrawable = getContext().getDrawable(R.drawable.robot_right);
                break;
            case "W":
                robotDrawable = getContext().getDrawable(R.drawable.robot_left);
                break;
        }

        if (robotDrawable != null) {
            // Calculate the robot's position based on its center (robotX, robotY)
            // The robot's top-left corner will be (robotX - 1, robotY - 1)
            int left = (int) ((robotX - 1) * cellSize);
            int top = (int) ((ROW - robotY - 2) * cellSize);  // 3 cells in height, Y-axis inverted
            int right = (int) ((robotX + 2) * cellSize);       // Robot is 3 cells wide
            int bottom = (int) ((ROW - robotY + 1) * cellSize); // Robot is 3 cells tall

            // Ensure the robot is within the grid boundaries
            if (left < 0) left = 0;
            if (top < 0) top = 0;
            if (right > COL * cellSize) right = (int) (COL * cellSize);
            if (bottom > ROW * cellSize) bottom = (int) (ROW * cellSize);

            // Set bounds and draw the robot
            robotDrawable.setBounds(left, top, right, bottom);
            robotDrawable.draw(canvas);
        }
    }

    public void toggleAxisLabels(boolean showLabels) {
        this.showAxisLabels = showLabels;
        invalidate(); // Redraw the view to apply changes
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = getHeight() - event.getY();  // Invert Y-coordinate for touch

        Obstacle dummyObstacle = findObstacle(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedObstacle = findObstacle(x, y);
                Obstacle neighborObstacle = findObstacleNearClick(x, y);

                // Check if user is trying to drag the robot
                if (isTouchingRobot(x, y)) {
                    isDraggingRobot = true; // Start dragging the robot
                }
                else if (neighborObstacle != null && !getIsAddingObstacle()) {
                    try {
                        handleDirectionSetting(neighborObstacle, x, y);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    invalidate(); // Redraw the view
                } else if (selectedObstacle == null && getIsAddingObstacle()) {
                    try {
                        addObstacle(x, y);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    setIsAddingObstacle(false);
                    Toast.makeText(getContext(), "New obstacle added!", Toast.LENGTH_SHORT).show();
                    invalidate();
                } else if (getIsRemoveObstacle()) {
                    setIsRemoveObstacle(false);
                    try {
                        removeObstacle(x, y);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(getContext(), "Obstacle removed!", Toast.LENGTH_SHORT).show();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDraggingRobot) {
                    updateRobotPosition(x, y); // Update robot position while dragging
                    invalidate();
                } else if (selectedObstacle != null) {
                    updateObstaclePosition(selectedObstacle, x, y); // Update obstacle position while dragging
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDraggingRobot) {
                    snapRobotToGrid(); // Snap the robot to the nearest grid cell after dragging
                    isDraggingRobot = false; // Stop dragging the robot
                    invalidate();
                } else if (selectedObstacle != null) {
                    if (isOutOfMapBounds(selectedObstacle)) {
                        // Remove obstacle if dragged out of the map & update the obstacle ID
                        obstacles.remove(selectedObstacle);

                        // Upon obstacle removed, send data over
                        try {
                            sendObstacleData(dummyObstacle, true, false);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        updateObstacleID();
                        Toast.makeText(getContext(), "Obstacle removed for being out of bounds!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Snap obstacle back to grid if still inside the map
                        snapObstacleToGrid(selectedObstacle);
                        try {
                            sendObstacleData(selectedObstacle, false, false);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    selectedObstacle = null;
                    invalidate();
                }
                break;
        }
        return true;
    }

    private void snapObstacleToGrid(Obstacle obstacle) {
        int col = (int) Math.floor(obstacle.startX / cellSize);
        int row = (int) Math.floor(obstacle.startY / cellSize);
        float startX = col * cellSize;
        float startY = row * cellSize;
        float endX = startX + cellSize;
        float endY = startY + cellSize;

        obstacle.startX = startX;
        obstacle.startY = startY;
        obstacle.endX = endX;
        obstacle.endY = endY;
    }

    private void drawObstacleDirectionPadding(Canvas canvas, Obstacle obstacle) {
        float padding = cellSize / 8; // Set the padding size

        Paint paddingPaint = new Paint();
        paddingPaint.setColor(Color.WHITE);
        paddingPaint.setStyle(Paint.Style.FILL);

        // Adjust Y-coordinates for the inverted axis
        float adjustedStartY = getHeight() - obstacle.startY;
        float adjustedEndY = getHeight() - obstacle.endY;

        switch (obstacle.direction) {
            case "S":  // North: Add padding at the bottom (since Y-axis is inverted)
                canvas.drawRect(obstacle.startX, adjustedStartY - padding, obstacle.endX, adjustedStartY, paddingPaint);
                break;
            case "N":  // South: Add padding at the top (since Y-axis is inverted)
                canvas.drawRect(obstacle.startX, adjustedEndY, obstacle.endX, adjustedEndY + padding, paddingPaint);
                break;
            case "E":  // East: Add padding to the right side
                canvas.drawRect(obstacle.endX - padding, adjustedEndY, obstacle.endX, adjustedStartY, paddingPaint);
                break;
            case "W":  // West: Add padding to the left side
                canvas.drawRect(obstacle.startX, adjustedEndY, obstacle.startX + padding, adjustedStartY, paddingPaint);
                break;
        }
    }
    private void handleDirectionSetting(Obstacle obstacle, float x, float y) throws JSONException {
        int obstacleCol = (int) Math.floor(obstacle.startX / cellSize);
        int obstacleRow = (int) Math.floor(obstacle.startY / cellSize);
        int clickedCol = (int) Math.floor(x / cellSize);
        int clickedRow = (int) Math.floor(y / cellSize);

        Log.e("Obstacle Direction", "Clicked Row: " + clickedRow + " Clicked Col: " + clickedCol);

        // Since the Y-axis is inverted, reverse the logic for north and south directions
        if (clickedCol == obstacleCol && clickedRow == obstacleRow - 1) {
            obstacle.direction = "S"; // Clicked on the cell above the obstacle (inverted Y-axis)
        } else if (clickedCol == obstacleCol && clickedRow == obstacleRow + 1) {
            obstacle.direction = "N"; // Clicked on the cell below the obstacle (inverted Y-axis)
        } else if (clickedCol == obstacleCol + 1 && clickedRow == obstacleRow) {
            obstacle.direction = "E"; // Clicked on the cell to the right of the obstacle
        } else if (clickedCol == obstacleCol - 1 && clickedRow == obstacleRow) {
            obstacle.direction = "W"; // Clicked on the cell to the left of the obstacle
        }

        sendObstacleData(obstacle, false, true);
        Toast.makeText(getContext(), "Obstacle direction set to: " + obstacle.direction, Toast.LENGTH_SHORT).show();
        invalidate(); // Redraw the view to show the new direction
    }


    //////////////////////// ROBOT MOVEMEMENTS ////////////////////////////
    public void moveRobotForward() throws JSONException {
        switch (robotDirection) {
            case "N":
                if (robotY < ROW - 2 && !isAtObstacle(robotX, robotY + 1, "N")) {
                    robotY++;  // Move up
                    appendToLog("Moved Forward to: " + robotX + ", " + robotY);
                    updateCoordinate("Forward", robotX, robotY, "N");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
            case "S":
                if (robotY > 1 && !isAtObstacle(robotX, robotY - 1, "S")) {
                    robotY--;  // Move down
                    appendToLog("Moved Forward to: " + robotX + ", " + robotY);
                    updateCoordinate("Forward", robotX, robotY, "S");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
            case "E":
                if (robotX < COL - 2 && !isAtObstacle(robotX + 1, robotY, "E")) {
                    robotX++;  // Move right
                    appendToLog("Moved Forward to: " + robotX + ", " + robotY);
                    updateCoordinate("Forward", robotX, robotY, "E");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
            case "W":
                if (robotX > 1 && !isAtObstacle(robotX - 1, robotY, "W")) {
                    robotX--;  // Move left
                    appendToLog("Moved Forward to: " + robotX + ", " + robotY);
                    updateCoordinate("Forward", robotX, robotY, "W");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
        }
        updateRobotStatus("Moving Forward");
        updateView();  // Redraw the view
    }


    public void moveRobotBackward() throws JSONException {
        switch (robotDirection) {
            case "N":
                if (robotY > 1 && !isAtObstacle(robotX, robotY - 1, "S")) {
                    robotY--;  // Move down (reverse)
                    appendToLog("Moved Backward to: " + robotX + ", " + robotY);
                    updateCoordinate("Reverse", robotX, robotY, "N");

                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
            case "S":
                if (robotY < ROW - 2 && !isAtObstacle(robotX, robotY + 1, "N")) {
                    robotY++;  // Move up (reverse)
                    appendToLog("Moved Backward to: " + robotX + ", " + robotY);
                    updateCoordinate("Reverse", robotX, robotY, "S");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
            case "E":
                if (robotX > 1 && !isAtObstacle(robotX - 1, robotY, "W")) {
                    robotX--;  // Move left (reverse)
                    appendToLog("Moved Backward to: " + robotX + ", " + robotY);
                    updateCoordinate("Reverse", robotX, robotY, "E");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
            case "W":
                if (robotX < COL - 2 && !isAtObstacle(robotX + 1, robotY, "E")) {
                    robotX++;  // Move right (reverse)
                    appendToLog("Moved Backward to: " + robotX + ", " + robotY);
                    updateCoordinate("Reverse", robotX, robotY, "W");
                } else {
                    appendToLog("Cannot move: Obstruction / out of bounds.");
                }
                break;
        }
        updateRobotStatus("Robot Reversing");
        updateView();  // Redraw the view
    }


    public void rotateLeft() throws JSONException {
        boolean canMove = true;

        switch (robotDirection) {
            case "N":  // Facing north, turning left to west
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX - i, robotY + i, "W")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX -= 2;  // Move 3 units left
                    robotY += 3;  // Move 3 units up
                    robotDirection = "W";  // Now facing west
                    updateCoordinate("left", robotX, robotY, "W");
                } else {
                    appendToLog("Cannot turn left: Obstruction.");
                }
                break;

            case "S":  // Facing south, turning left to east
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX + i, robotY - i, "E")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX += 2;  // Move 3 units right
                    robotY -= 3;  // Move 3 units down
                    robotDirection = "E";  // Now facing east
                    updateCoordinate("left", robotX, robotY, "E");
                } else {
                    appendToLog("Cannot turn left: Obstruction.");
                }
                break;

            case "E":  // Facing east, turning left to north
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX + i, robotY + i, "N")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX += 3;  // Move 3 units right
                    robotY += 2;  // Move 3 units up
                    robotDirection = "N";  // Now facing north
                    updateCoordinate("left", robotX, robotY, "N");
                } else {
                    appendToLog("Cannot turn left: Obstruction.");
                }
                break;

            case "W":  // Facing west, turning left to south
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX - i, robotY - i, "S")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX -= 3;  // Move 3 units left
                    robotY -= 2;  // Move 3 units down
                    robotDirection = "S";  // Now facing south
                    updateCoordinate("left", robotX, robotY, "S");
                } else {
                    appendToLog("Cannot turn left: Obstruction.");
                }
                break;
        }
        updateRobotStatus("Turning Left");
        updateView();  // Redraw the view
    }


    public void rotateRight() throws JSONException {
        boolean canMove = true;

        switch (robotDirection) {
            case "N":  // Facing north, turning right to east
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX + i, robotY + i, "E")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX += 2;  // Move 3 units right
                    robotY += 3;  // Move 3 units up
                    robotDirection = "E";  // Now facing east
                    updateCoordinate("right", robotX, robotY, "E");
                } else {
                    appendToLog("Cannot turn right: Obstruction.");
                }
                break;

            case "S":  // Facing south, turning right to west
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX - i, robotY - i, "W")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX -= 2;  // Move 3 units left
                    robotY -= 3;  // Move 3 units down
                    robotDirection = "W";  // Now facing west
                    updateCoordinate("right", robotX, robotY, "W");
                } else {
                    appendToLog("Cannot turn right: Obstruction.");
                }
                break;

            case "E":  // Facing east, turning right to south
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX - i, robotY - i, "S")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX += 3;  // Move 3 units left
                    robotY -= 2;  // Move 3 units down
                    robotDirection = "S";  // Now facing south
                    updateCoordinate("right", robotX, robotY, "S");
                } else {
                    appendToLog("Cannot turn right: Obstruction.");
                }
                break;

            case "W":  // Facing west, turning right to north
                for (int i = 1; i <= 3; i++) {
                    if (isAtObstacle(robotX + i, robotY + i, "N")) {
                        canMove = false;
                        break;
                    }
                }
                if (canMove) {
                    robotX -= 3;  // Move 3 units right
                    robotY += 2;  // Move 3 units up
                    robotDirection = "N";  // Now facing north
                    updateCoordinate("right", robotX, robotY, "N");
                } else {
                    appendToLog("Cannot turn right: Obstruction.");
                }
                break;
        }
        updateRobotStatus("Turning Right");
        updateView();  // Redraw the view
    }



    ///// DRAGGING ROBOT FUNCTIONALITY
    private boolean isTouchingRobot(float x, float y) {
        // Calculate the robot's bounding box (3x3 grid cells)
        int left = (robotX - 1) * (int) cellSize;  // Left side of the 3x3 robot
        int right = (robotX + 2) * (int) cellSize;  // Right side of the 3x3 robot

        // Correct Y-axis calculations since (0, 0) is bottom-left and Y increases upwards
        int top = (robotY + 2) * (int) cellSize;  // Top side of the 3x3 robot
        int bottom = (robotY - 1) * (int) cellSize;  // Bottom side of the 3x3 robot

        Log.d("MapView", "Robot bounds: left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom);

        // Check if the touch falls within the robot's bounding box
        boolean isTouching = x >= left && x <= right && y >= bottom && y <= top;
        Log.d("MapView", "Touch inside robot bounds: " + isTouching);

        return isTouching;
    }


    // Update robot's position (snapping to grid)
    private void updateRobotPosition(float x, float y) {
        // Snap the robot to the nearest grid cell based on touch
        int newRobotX = Math.round(x / cellSize);  // Update X position
        int newRobotY = Math.round(y / cellSize);  // Update Y position (Y is inverted already)

        // Ensure the robot stays within the grid boundaries
        if (newRobotX - 1 < 0) {
            newRobotX = 1; // Prevent robot from going beyond the left boundary
        } else if (newRobotX + 1 >= COL) {
            newRobotX = COL - 2; // Prevent robot from going beyond the right boundary
        }

        if (newRobotY - 1 < 0) {
            newRobotY = 1; // Prevent robot from going below the bottom boundary
        } else if (newRobotY + 1 >= ROW) {
            newRobotY = ROW - 2; // Prevent robot from going beyond the top boundary
        }

        // Set the new position for the robot
        robotX = newRobotX;
        robotY = newRobotY;

        Log.d("MapView", "Updated robot position to x=" + robotX + " y=" + robotY);
    }


    private void snapRobotToGrid() {
        // Snap robot to the grid based on its current coordinates
        int col = (int) Math.floor(robotX);
        int row = (int) Math.floor(robotY);

        Log.d("MapView", "Snapping robot to grid: col=" + col + " row=" + row);

        robotX = col;
        robotY = row;

        appendToLog("Robot moved to: " + robotX + ", " + robotY);
        try {
            updateCoordinate("Dragged", robotX, robotY, robotDirection);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ////////////////////// UPDATE USER INTERFACE /////////////////////////////
    private void appendToLog(String message) {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).updateLogActivity(message);
        }
    }


    private void updateCoordinate(String action, int x, int y, String direction) throws JSONException {
        Context context = getContext();
        JSONObject jsonMessage = new JSONObject();
        if (context instanceof MainActivity) {

            jsonMessage.put("cat", "manual_control");
            jsonMessage.put("value", action);
            /*
            jsonMessage.put("Type", "ROBOT");
            jsonMessage.put("Action", action);
            jsonMessage.put("x", x);
            jsonMessage.put("y", y);
            jsonMessage.put("Direction", direction);
            */

            String message = "X: " + robotX + " | Y: " + robotY + " | " + direction;
            ((MainActivity) context).updateCoordinate(message);

            // It is own device action
            if(!this.setReceivedInstructions) {
                ((MainActivity) context).sendCommand(jsonMessage);
            }
            else{
                this.setReceivedInstructions = false;
            }
        }
    }

    private void updateRobotStatus(String message){
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).updateRobotStatus(message);
        }
    }

    //To redraw / update map
    private void updateView(){
        Log.d("MapView", "Updating view");
        invalidate();
    }

    /////////////////// OBSTACLE FUNCTIONALITIES /////////////////////////////
    private boolean isAtObstacle(int x, int y, String direction) {
        switch (direction) {
            case "N":
                // Check only the row directly above the robot
                for (int i = -1; i <= 1; i++) { // -1 to 1 for checking the width of the 3x3 robot
                    if (ObstacleChecker(x + i, y + 1)) {
                        return true;
                    }
                }
                break;
            case "S":
                // Check only the row directly below the robot
                for (int i = -1; i <= 1; i++) {
                    if (ObstacleChecker(x + i, y - 1)) {
                        return true;
                    }
                }
                break;
            case "E":
                // Check only the column to the right of the robot
                for (int i = -1; i <= 1; i++) {
                    if (ObstacleChecker(x + 1, y + i)) {
                        return true;
                    }
                }
                break;
            case "W":
                // Check only the column to the left of the robot
                for (int i = -1; i <= 1; i++) {
                    if (ObstacleChecker(x - 1, y + i)) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }
    private boolean ObstacleChecker(int x, int y) {
        for (Obstacle obstacle : obstacles) {
            // Use strict comparison to check if the robot's position is inside the obstacle's grid cell
            int obstacleX = (int) Math.floor(obstacle.startX / cellSize);
            int obstacleY = (int) Math.floor(obstacle.startY / cellSize);

            if (x == obstacleX && y == obstacleY) {
                return true;
            }
        }
        return false;
    }


    private boolean isOutOfMapBounds(Obstacle obstacle) {
        return obstacle.startX < 0 || obstacle.endX > getWidth() ||
                obstacle.startY < 0 || obstacle.endY > getHeight();
    }

    public void addObstacle(float x, float y) throws JSONException {
        // Align the obstacle position to the grid
        int col = (int) Math.floor(x / cellSize);
        int row = (int) Math.floor(y / cellSize);

        float startX = col * cellSize;
        float startY = row * cellSize;
        float endX = startX + cellSize;
        float endY = startY + cellSize;

        // Create an obstacle with a unique ID and set its position to align with the grid
        Obstacle newObstacle = new Obstacle(obstacles.size() + 1, startX, startY, endX, endY, "O" + (obstacles.size() + 1));
        obstacles.add(newObstacle);

        // Transfer Bluetooth data over to robot
        sendObstacleData(newObstacle, false, false);
        Toast.makeText(getContext(), "New obstacle added!", Toast.LENGTH_SHORT).show();
    }

    public void removeObstacle(float x, float y) throws JSONException {
        Obstacle obstacleToRemove = null;

        // Find the obstacle that matches the touched coordinates
        for (Obstacle obstacle : obstacles) {
            if (x >= obstacle.startX && x <= obstacle.endX &&
                    y >= obstacle.startY && y <= obstacle.endY) {
                obstacleToRemove = obstacle;
                break;
            }
        }
        // If an obstacle was found, remove it
        if (obstacleToRemove != null) {
            obstacles.remove(obstacleToRemove);
            updateObstacleID();
            //UPdate Robot on obstacle removal
            sendObstacleData(obstacleToRemove, true, false);
            Toast.makeText(getContext(), "Obstacle removed!", Toast.LENGTH_SHORT).show();
            invalidate(); // Redraw the view
        } else {
            Toast.makeText(getContext(), "No obstacle found at this location!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateObstacleID(){
        int new_id = 1;
        for(Obstacle obstacle : obstacles){
            obstacle.id = new_id;
            obstacle.text = "0" + new_id;
            new_id += 1;
        }
    }

    public Obstacle findObstacle(float x, float y) {
        for (Obstacle obstacle : obstacles) {
            if (x >= obstacle.startX && x <= obstacle.endX && y >= obstacle.startY && y <= obstacle.endY) {
                return obstacle;
            }
        }
        return null;
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    public void setNewObstacleID(int id, String newID){
        for (Obstacle obstacle : obstacles) {
            if (obstacle.id == id) {
                // Update obstacle to show image based on new ID
                obstacle.text = newID;
                obstacle.newID = true;  // Set the flag to show the image

                int resourceId = getContext().getResources().getIdentifier("image_" + newID, "drawable", getContext().getPackageName());
                obstacle.imageDrawable = getContext().getDrawable(resourceId);
                break;
            }
        }
        invalidate();  // Redraw the map after setting the new image
    }

    private Obstacle findObstacleNearClick(float x, float y) {
        int clickedCol = (int) Math.floor(x / cellSize);
        int clickedRow = (int) Math.floor(y / cellSize);
        Log.e("Hello", "X: " + clickedRow + " Y: " + clickedCol);

        for (Obstacle obstacle : obstacles) {
            int obstacleCol = Math.round(obstacle.startX / cellSize);
            int obstacleRow = Math.round(obstacle.startY / cellSize);

            // Check if the clicked cell is a neighbor (N, S, E, W)
            if ((clickedCol == obstacleCol && clickedRow == obstacleRow - 1) ||  // North
                    (clickedCol == obstacleCol && clickedRow == obstacleRow + 1) ||  // South
                    (clickedCol == obstacleCol + 1 && clickedRow == obstacleRow) ||  // East
                    (clickedCol == obstacleCol - 1 && clickedRow == obstacleRow)) {  // West
                return obstacle;  // Found an obstacle with a neighboring click
            }
        }
        return null;  // No neighboring obstacle found
    }

    private void updateObstaclePosition(Obstacle obstacle, float x, float y) {
        // Align the obstacle position to the grid, making it occupy exactly one cell
        int col = (int) Math.floor(x / cellSize);
        int row = (int) Math.floor(y / cellSize);

        obstacle.startX = col * cellSize;
        obstacle.startY = row * cellSize;
        obstacle.endX = obstacle.startX + cellSize;  // Ensure it takes only one cell
        obstacle.endY = obstacle.startY + cellSize;
    }


    public void setIsAddingObstacle(Boolean value){
        Log.e("sss","Add obstacle STATUS CHANGED TO " + value);
        if(getIsRemoveObstacle()){
            setIsRemoveObstacle(false);
        }
        this.setObstacle = value;
    }
    public Boolean getIsAddingObstacle(){
        return this.setObstacle;
    }
    public void setIsRemoveObstacle(boolean value){
        Log.e("sss","Remove obstacle STATUS CHANGED TO " + value);
        if(getIsAddingObstacle()){
            setIsAddingObstacle(false);
        }
        this.setRemoveObstacle = value;
    }
    public Boolean getIsRemoveObstacle(){
        return this.setRemoveObstacle;
    }

    private void sendObstacleData(Obstacle obstacle, Boolean removeObstacle, Boolean changeDirection) throws JSONException {
        // Implement Bluetooth sending logic here
        int coordX = (int) Math.floor(obstacle.startX / cellSize);
        int coordY = (int) Math.floor(obstacle.startY / cellSize);
        JSONObject jsonMessage = new JSONObject();
        //JSONArray
        jsonMessage.put("Type", "OBSTACLE");
        if(removeObstacle){
            jsonMessage.put("Action", "Remove");
            jsonMessage.put("id", obstacle.id);
            jsonMessage.put("Direction", obstacle.direction);
        }
        else if(changeDirection){
            jsonMessage.put("Action", "ChangeDirection");
            jsonMessage.put("id",obstacle.id);
            jsonMessage.put("Direction", obstacle.direction);
        }
        else{
            jsonMessage.put("Action", "Add");
            jsonMessage.put("x",coordX);
            jsonMessage.put("y", coordY);
            jsonMessage.put("Direction", obstacle.direction);
        }
        Context context = getContext();
        if (context instanceof MainActivity) {
            //((MainActivity) context).sendCommand(jsonMessage);
            ((MainActivity) context).chatViewModel.appendToLog(jsonMessage.toString());
        }
        //Toast.makeText(getContext(), "Sending Obstacle Data...", Toast.LENGTH_SHORT).show();
    }

    public void startRobot() throws JSONException{
        JSONObject jsonMessage = new JSONObject();

        jsonMessage.put("cat", "control");
        jsonMessage.put("value", "start");

        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).sendCommand(jsonMessage);
        }
        Toast.makeText(getContext(), "Sending Start Command...", Toast.LENGTH_SHORT).show();
    }
    /////////////////////// SEND ALL MAP DETAILS OVER !!!!! /////////////////////////////
    public void sendAllObstacles() throws JSONException {
        // Create the outermost JSON object
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("cat", "obstacles");
        int direction = 0;

        JSONArray obstaclesArray = new JSONArray();
        /*
            { "cat" : "obstacles",
              "value" : [{ "obstacleID" : id1, "x" : x, "y" : y, "d" : dir},
                             { "obstacleID" : id1, "x" : x, "y" : y, "d" : dir},
                             { "obstacleID" : id1, "x" : x, "y" : y, "d" : dir},
                            ]
           }
         */

        for(Obstacle obs : obstacles){
            int coordX = (int) Math.floor(obs.startX / cellSize);
            int coordY = (int) Math.floor(obs.startY / cellSize);

            // Create the "value" object
            JSONObject eachObstacleData = new JSONObject();

            // Create the "obstacles" array
            eachObstacleData.put("x", (coordX * 10) + 5);
            eachObstacleData.put("y", (coordY * 10) + 5);
            eachObstacleData.put("id", obs.id);
            switch (obs.direction){
                case "N": direction = 0; break;
                case "E": direction = 2; break;
                case "S": direction = 4; break;
                case "W": direction = 6; break;
            }
            eachObstacleData.put("d", direction);

            obstaclesArray.put(eachObstacleData);

        }
        jsonMessage.put("value", obstaclesArray);

        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).sendCommand(jsonMessage);
        }
    }

    public void setReceived(Boolean received){
        // True : don't sendCommand when robot moved
        // False: senCommand when robot moved
        this.setReceivedInstructions = received;
    }

    public float getCellSize(){
        return this.cellSize;
    }

    public void setRobotX(int x){
        this.robotX = x;
    }
    public void setRobotY(int y){
        this.robotY = y;
    }
    public void setRobotDirection(String d){
        this.robotDirection = d;
    }

}
