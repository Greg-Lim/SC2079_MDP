package com.example.mdp_android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ToggleButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_android.bluetooth.BluetoothChat;
import com.example.mdp_android.bluetooth.BluetoothHelper;
import com.example.mdp_android.fragment.BluetoothPopUpFragment;
import com.example.mdp_android.ViewModels.ChatViewModel;
import com.example.mdp_android.fragment.FirstFragment;
import com.example.mdp_android.fragment.SecondFragment;
import com.example.mdp_android.fragment.ThirdFragment;
import com.example.mdp_android.map.MapModel;
import com.example.mdp_android.map.MapView;
import com.google.android.material.tabs.TabLayout;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public ChatViewModel chatViewModel;
    private static final String TAG = "MainActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private ImageButton arrow_Up, arrow_Down, arrow_Left, arrow_Right;
    private TabLayout tablayout;
    private FrameLayout frameLayout;
    private MapView maps;
    private MapModel receivedInstructions;
    private TextView bluetoothButton;
    private TextView coordinateText;
    public TextView robotStatusText;
    private BluetoothHelper bluetoothHelper;
    private BluetoothChat connectedThread;
    private BluetoothPopUpFragment bluetoothDialogFragment;
    private BluetoothSocket bluetoothSocket;
    private BluetoothAdapter bluetoothAdapter;
    private ToggleButton bt_Client_Server;
    private ToggleButton labelBtn;

    //FOR BLUETOOTH disconnect checker
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Device disconnected: " + (device != null ? device.getName() : "Unknown"));
                Toast.makeText(context, "Disconnected from " + (device != null ? device.getName() : "device"), Toast.LENGTH_SHORT).show();

                //reconnectToLastDevice();
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Initialise View Model
        // Initialize ChatViewModel
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Initialize Bluetooth helper and adapter
        bluetoothHelper = new BluetoothHelper();
        bluetoothHelper.setMainActivity(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // MapView for BUTTON actions
        maps = findViewById(R.id.mapView);

        // UI layout textView
        coordinateText = findViewById(R.id.coordinateText);
        robotStatusText = findViewById(R.id.robotStatusText);

        // Initialize Movement Buttons & Actions made
        arrow_Up = findViewById(R.id.arrow_up);
        arrow_Down = findViewById(R.id.arrow_down);
        arrow_Left = findViewById(R.id.arrow_left);
        arrow_Right = findViewById(R.id.arrow_right);

        labelBtn = findViewById(R.id.toggle_axis_labels);

        labelBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle the axis labels on or off
            maps.toggleAxisLabels(!isChecked);
        });

        // UP DOWN LEFT RIGHT button onclick actions
        arrow_Up.setOnClickListener(v -> {
            try {
                maps.moveRobotForward();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        arrow_Down.setOnClickListener(v -> {
            try {
                maps.moveRobotBackward();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        arrow_Left.setOnClickListener(v -> {
            try {
                maps.rotateLeft();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        arrow_Right.setOnClickListener(v -> {
            try {
                maps.rotateRight();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // Initialize the Bluetooth button and toggle Client Server button
        bluetoothButton = findViewById(R.id.bluetooth_btn);
        bt_Client_Server = findViewById(R.id.toggle_server_client_bt);
        bluetoothButton.setOnClickListener(v -> showBluetoothDialog());

        // Start the Bluetooth server
        bt_Client_Server.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Server mode
                bt_Client_Server.setBackgroundColor(Color.parseColor("#FF0000")); // Set to Red
                bt_Client_Server.setTextColor(Color.WHITE);
                Toast.makeText(this, "Server Mode", Toast.LENGTH_SHORT).show();
                offBluetoothClient();
                startBluetoothServer();
            } else {
                // Client mode
                bt_Client_Server.setBackgroundColor(Color.parseColor("#0000FF")); // Set to Blue
                bt_Client_Server.setTextColor(Color.WHITE);
                Toast.makeText(this, "Client Mode", Toast.LENGTH_SHORT).show();
                offBluetoothServer();
                //startBluetoothClient();
            }
        });

        // Receiver that detects Disconnections!!!!
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);


        // Initialize Fragment Tabs
        frameLayout = findViewById(R.id.framePage);
        tablayout = findViewById(R.id.interactiveButtons);

        // Set default fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.framePage, new FirstFragment())
                .addToBackStack(null)
                .commit();

        // Switch to different tabs in frame layout
        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new FirstFragment();
                        break;
                    case 1:
                        fragment = new SecondFragment();
                        break;
                    case 2:
                        fragment = new ThirdFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.framePage, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void updateLogActivity(String message) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.framePage);

        // Update the ChatViewModel with the new message
        if (chatViewModel != null) {
            chatViewModel.appendToLog(message);
        }

    }
    public void updateCoordinate(String message) {
        coordinateText.setText(message);
    }
    public void updateRobotStatus(String message){
        robotStatusText.setText(message);
    }


    ///////////////////// BLUETOOTH FUNCTIONALITIES /////////////////////////////
    @SuppressLint("MissingPermission")
    private void showBluetoothDialog() {
        FragmentManager fm = getSupportFragmentManager();
        bluetoothDialogFragment = new BluetoothPopUpFragment();

        // Pass the MainActivity instance to the fragment
        bluetoothDialogFragment.setMainActivity(this);

        // Set the Bluetooth connection listener
        bluetoothDialogFragment.setOnConnectedListener(socket -> {
            if (socket != null && socket.isConnected()) {
                Log.d(TAG, "Bluetooth connected: " + socket.getRemoteDevice().getName());
                this.bluetoothSocket = socket;
                runOnUiThread(() -> Toast.makeText(this, "Bluetooth connected successfully", Toast.LENGTH_SHORT).show());
            } else {
                Log.e(TAG, "Bluetooth connection failed or socket is null");
                runOnUiThread(() -> Toast.makeText(this, "Bluetooth connection failed", Toast.LENGTH_SHORT).show());
            }
        });

        bluetoothDialogFragment.show(fm, "fragment_bluetooth_dialog");
    }

    @SuppressLint("MissingPermission")
    public void onBluetoothConnected(BluetoothSocket socket) {
        if (socket != null && socket.isConnected()) {
            Log.d(TAG, "Bluetooth connected: " + socket.getRemoteDevice().getName());

            // Save the connected socket so it can be reused on reconnection
            this.bluetoothSocket = socket;

            // Stop any previous thread before creating a new one
            if (connectedThread != null) {
                connectedThread.cancel(); // Cancel the old thread
            }

            // Start the BluetoothChat thread to handle communication
            connectedThread = new BluetoothChat(socket, this);

            // Pass main instance
            connectedThread.setMainActivity(this);

            // INTERPRET instructions in MAP MODEL
            createMapModel();
            connectedThread.setMapReceiver(receivedInstructions);  // In BluetoothChat
            receivedInstructions.setMaps(maps);

            connectedThread.setOnMessageReceivedListener(message -> {
                // Handle the received message here
                Log.d(TAG, "Message received: " + message);
                updateLogActivity(message);
                runOnUiThread(() -> Toast.makeText(this, "Received: " + message, Toast.LENGTH_SHORT).show());

                // MapModel handle ROBOT, OBJECT, STATUS instructions
                receivedInstructions.handleJsonFormat(message);

            });

            // Start the thread
            connectedThread.start();
            Log.d(TAG, "BluetoothChat thread started.");

            runOnUiThread(() -> Toast.makeText(this, "Bluetooth connected successfully", Toast.LENGTH_SHORT).show());

        } else {
            Log.e(TAG, "Bluetooth connection failed or socket is null");
            runOnUiThread(() -> Toast.makeText(this, "Bluetooth connection failed", Toast.LENGTH_SHORT).show());
        }
    }


    public void sendCommand(JSONObject jsonCommand) {
        chatViewModel.appendToLog(jsonCommand.toString());
        if (connectedThread != null) {

            connectedThread.write(jsonCommand.toString().getBytes());
            Log.d(TAG, "Sending JSON: " + jsonCommand);
        } else {
            Log.e(TAG, "Failed to send command, Bluetooth not connected");
            runOnUiThread(() -> Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show());
        }
    }

    public void startBluetoothServer() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            BluetoothServerSocket serverSocket = null;
            try {
                Log.d(TAG, "Starting Bluetooth server...");
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyApp", MY_UUID);
                final BluetoothServerSocket finalServerSocket = serverSocket;
                new Thread(() -> {
                    try {
                        Log.d(TAG, "Waiting for Bluetooth connection...");
                        bluetoothSocket = finalServerSocket.accept();
                        if (bluetoothSocket != null) {
                            Log.d(TAG, "Connection accepted from " + bluetoothSocket.getRemoteDevice().getName());
                            manageConnectedSocket(bluetoothSocket);
                            finalServerSocket.close();
                        } else {
                            Log.e(TAG, "Bluetooth socket is null after accept");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error accepting connection: " + e.getMessage());
                    }
                }).start();
            } catch (SecurityException se) {
                Log.e(TAG, "Permission denied: " + se.getMessage());
                Toast.makeText(this, "Bluetooth permission required to start server", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Error creating server socket: " + e.getMessage());
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
        }
    }

    public void offBluetoothServer() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                bluetoothSocket.close();
                Log.d(TAG, "Bluetooth server socket closed");
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket: " + e.getMessage());
            }
        }
    }

    private void offBluetoothClient() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                bluetoothSocket.close();
                Log.d(TAG, "Bluetooth client socket closed");
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void manageConnectedSocket(BluetoothSocket socket) {
        // Check if the socket is valid and connected
        if (socket != null && socket.isConnected()) {
            Log.d(TAG, "manageConnectedSocket: Socket connected, initializing BluetoothChat thread.");

            // Notify that the Bluetooth is connected
            onBluetoothConnected(socket);
        } else {
            Log.e(TAG, "manageConnectedSocket: Socket is null or not connected.");
        }
    }
    @SuppressLint("MissingPermission")
    public void reconnectToLastDevice() {

        runOnUiThread(() -> {
            if (bluetoothSocket != null && bluetoothSocket.getRemoteDevice() != null) {
                BluetoothDevice lastDevice = bluetoothSocket.getRemoteDevice(); // Get the previously connected device
                Toast.makeText(this, "Reconnecting to " + lastDevice.getName(), Toast.LENGTH_SHORT).show();

                // Reconnect directly to the last device without involving the fragment
                try {
                    BluetoothSocket newSocket = lastDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    newSocket.connect(); // Attempt to reconnect
                    onBluetoothConnected(newSocket); // Handle the connection once successful
                } catch (IOException e) {
                    Log.e(TAG, "Error reconnecting to device: " + e.getMessage());
                    Toast.makeText(this, "Reconnection failed", Toast.LENGTH_SHORT).show();

                    offBluetoothClient();
                    //reconnectToLastDevice();
                }
            } else {
                Log.e(TAG, "No previous device to reconnect to");
                Toast.makeText(this, "No device found to reconnect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            // If permissions are not granted, request them
            Log.d(TAG, "Requesting Bluetooth permissions");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION  // Required for discovering Bluetooth devices
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS
            );
        } else {
            Log.d(TAG, "Bluetooth permissions already granted");
        }
    }

    public BluetoothSocket getBluetoothSocket() {
        Log.d(TAG, "Returning Bluetooth socket");
        if(this.bluetoothSocket != null){
            Log.e("MainActivity", "Bluetooth passing to you is NULL");
        }
        if(!bluetoothSocket.isConnected()){
            Log.e("MainActivity", "Bluetooth is not connected PASSINGGGG");
        }
        return this.bluetoothSocket;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return this.bluetoothAdapter;
    }

    public MapModel getMapModel(){
        return this.receivedInstructions;
    }
    public BluetoothChat getConnectedThread(){
        if(connectedThread != null){
            return this.connectedThread;
        }
        return null;
    }

    private void createMapModel(){
        if(receivedInstructions == null){
            this.receivedInstructions = new MapModel();
            receivedInstructions.setMainActivity(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedThread != null) {
            Log.d(TAG, "Closing Bluetooth connection");
            connectedThread.cancel();
        }
    }


}

/*
public void startBluetoothClient() {

        if (bluetoothAdapter.isEnabled()) {
            // Get the BluetoothDevice object representing the server
            // Samsung tablet Mac address
            BluetoothDevice serverDevice = bluetoothAdapter.getRemoteDevice("48:61:EE:2A:AA:70");
            Toast.makeText(this, "Server Device address: " + serverDevice.getAddress(), Toast.LENGTH_SHORT).show();
            try {
                Log.d(TAG, "Creating socket to connect to server...");
                this.bluetoothSocket = serverDevice.createRfcommSocketToServiceRecord(MY_UUID);
                if (bluetoothSocket == null) {
                    Toast.makeText(this, "Bluetooth Client Creation failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(() -> {
                    try {
                        Log.d(TAG, "Attempting to connect...");
                        bluetoothSocket.connect();
                        if (bluetoothSocket.isConnected()) {
                            Log.d(TAG, "Connected to server");
                            runOnUiThread(() -> Toast.makeText(this, "Bluetooth Socket CONNECTED!", Toast.LENGTH_SHORT).show());
                            manageConnectedSocket(bluetoothSocket);
                        } else {
                            Log.e(TAG, "Failed to connect to server");
                            runOnUiThread(() -> Toast.makeText(this, "Failed to connect to server", Toast.LENGTH_SHORT).show());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error connecting to server: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(this, "Error connecting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        try {
                            bluetoothSocket.close();
                        } catch (IOException closeException) {
                            Log.e(TAG, "Could not close the client socket", closeException);
                        }
                    }
                }).start();
            } catch (IOException e) {
                Log.e(TAG, "Error creating client socket: " + e.getMessage());
                Toast.makeText(this, "Error creating client socket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
        }
        */

