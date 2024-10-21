    package com.example.mdp_android.map;

    import android.util.Log;

    import com.example.mdp_android.MainActivity;
    import com.example.mdp_android.map.MapView;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    public class MapModel {

        private MainActivity mainActivity;

        private MapView maps;
        private String imageID;
        private float coordX, coordY;
        private int robotX, robotY;
        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        public void setMaps(MapView maps) {
            this.maps = maps;
        }

        //Actions received by robot and send back Acknowledgement
        public void receivedActions(String [] receivedInstruction) throws JSONException {

            // 0: ROBOT
            // 1: Forward, Reverse, Rotate Left, Rotate Right
            // 2: X coordinate (INT)
            // 3: Y coordinate (INT)
            // 4: Direction (String)
            maps.setReceived(true);
            switch(receivedInstruction[1]){
                case "Forward":
                    maps.moveRobotForward();
                    break;
                case "Reverse":
                    maps.moveRobotBackward();
                    break;
                case "r_left":
                    maps.rotateLeft();
                    break;
                case "r_right":
                    maps.rotateRight();
                    break;
            }

        }
        // When CNN detects image ID and passed from Algorithm and send back ACK
        public void receivedObstacle(int [] receivedObstacle){

            // 0: Image_ID
            // 1: Obstacle ID result
            maps.setNewObstacleID(receivedObstacle[1], Integer.toString(receivedObstacle[0]));
        }

        public void receivedObstacleActions(String [] receivedObstacle) throws JSONException {

            // 0: OBSTACLE
            // 1: Remove, Add
            // 2: X coordinate (INT)
            // 3: Y coordinate (INT)
            // 4: Direction (String)
            /*
            for(int i = 0; i < receivedObstacle.length; i++){
                Log.d("MapModel", receivedObstacle[i]);
            }*/

            this.coordX = Integer.parseInt(receivedObstacle[2]) * maps.getCellSize();
            this.coordY = Integer.parseInt(receivedObstacle[3]) * maps.getCellSize();
            MapView.Obstacle obstacles = maps.findObstacle(coordX, coordY);
            if(obstacles != null) {
                if(receivedObstacle[1].contains("Add")) {
                    // ADD Obstacle
                    maps.addObstacle(coordX, coordY);
                }
                else{
                    // Remove Obstacle
                    maps.removeObstacle(coordX, coordY);
                }
            }
            else {
                Log.d("MapModel", "obstacles is NULL");
            }
        }

        public void receivedRobotLocation(float rX, float rY, int direction){

            String d = "";
            switch (direction){
                case 0: d = "N"; break;
                case 2: d = "E"; break;
                case 4: d = "S"; break;
                case 6: d = "W"; break;
            }
            if(d != "") {
                int calculationX = (int) Math.ceil((rX - 5) / 10);
                int calculationY = (int) Math.ceil((rY - 5) / 10);
                Log.d("MapModel", "X: " + calculationX + " Y: " + calculationY);
                maps.setRobotX(calculationX);
                maps.setRobotY(calculationY);
                maps.setRobotDirection(d);
                maps.invalidate();
            }
        }

        public void handleJsonFormat(String message) throws JSONException {
            JSONObject jsonMessage = new JSONObject(message);
            String cat = null;
            String robot = null;

            if(jsonMessage.has("error")){
                jsonMessage.getString("error");
                // FLAG OUT ERROR
            }
            if(jsonMessage.has("cat")){
                cat = jsonMessage.getString("cat");
            }

            // Handling robot-related actions
            assert cat != null;
            if (cat.equals("ROBOT")) {
                String action = jsonMessage.getString("Action");
                int x = jsonMessage.getInt("x");
                int y = jsonMessage.getInt("y");
                String direction = jsonMessage.getString("Direction");

                // Create an array of strings to pass to the receivedActions method
                String[] robotCommands = {"ROBOT", action, String.valueOf(x), String.valueOf(y), direction};
                receivedActions(robotCommands);


            } else if (cat.equals("OBSTACLE")) { // Handling obstacle-related actions

                String action = jsonMessage.getString("Action");
                int x = jsonMessage.getInt("x");
                int y = jsonMessage.getInt("y");
                String direction = jsonMessage.getString("Direction");

                // Create an array of strings to pass to the receivedObstacleActions method
                String[] obstacleCommands = {"OBSTACLE", action, String.valueOf(x), String.valueOf(y), direction};
                receivedObstacleActions(obstacleCommands);


            }else if(cat.equals("imgreg")) {
                // Retrieve the nested "value" object from the JSON message
                JSONObject valueObject = jsonMessage.getJSONObject("value");

                // Extract data from the nested object
                int detected = valueObject.getInt("detected");
                int img_id = valueObject.getInt("id");
                String img_name = valueObject.getString("name");
                int obstacle_id = valueObject.getInt("obstacle_id");

                Log.d("MapModel", "Number of Detection: " + detected + " Image Detected: " + img_name);

                int[] imageRecResult = {img_id, obstacle_id};
                receivedObstacle(imageRecResult);
            }
            else if(cat.equals("location")){
                Log.d("MapModel", "Result : " + jsonMessage);
                JSONObject valueObject = jsonMessage.getJSONObject("value");
                float x = (float) valueObject.getDouble("x");
                float y = (float) valueObject.getDouble("y");
                int d = valueObject.getInt("d");
                Log.d("MapModel", "X: " + x + " Y: " + y);
                receivedRobotLocation(x,y,d);
            }
            else if (cat.equals("status")) {  // HANDLE robot status
                String value = jsonMessage.getString("value");
                mainActivity.robotStatusText.setText(value);
            }

        }
    }
