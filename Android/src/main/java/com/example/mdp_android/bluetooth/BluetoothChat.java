package com.example.mdp_android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.map.MapModel;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothChat extends Thread {
    private static final String TAG = "BluetoothChat";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothDevice mmDevice;
    private OnMessageReceivedListener messageReceivedListener;
    private StringBuilder messageBuilder;
    private final UUID MY_APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Example UUID
    private final List<String> messageBuffer = new ArrayList<>(); // Buffer for messages
    private MapModel mappingInstructions;
    private MainActivity mainActivity;
    private Handler handler = new Handler(Looper.getMainLooper()); // For cross-thread communication

    public interface OnConnectedListener {
        void onConnected(BluetoothSocket socket);
    }

    private OnConnectedListener onConnectedListener;

    public BluetoothChat(BluetoothSocket socket, MainActivity mainActivity) {
        this.mmSocket = socket;
        this.mainActivity = mainActivity;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input and output streams", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        mmDevice = socket.getRemoteDevice();
        messageBuilder = new StringBuilder();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024]; // Buffer for storing received data
        int numBytes; // Number of bytes received

        while (true) {
            synchronized (this) {
                if (messageReceivedListener == null) {
                    Log.e(TAG, "MessageReceivedListener is NULL");
                } else {
                    Log.e(TAG, "MessageReceivedListener is ON");
                }
                try {
                    // Read from the input stream
                    numBytes = mmInStream.read(buffer);
                    String receivedMessage = new String(buffer, 0, numBytes);
                    Log.d(TAG, "Received data: " + receivedMessage);
                    // Append the received message to the message builder
                    messageBuilder.append(receivedMessage);

                    String fullMessage = messageBuilder.toString().trim();
                    messageBuilder.setLength(0); // Clear the builder for the next message
                    Log.d(TAG, "Full message received: " + fullMessage);

                    // Trigger the message received callback if the listener is set
                    if (messageReceivedListener != null) {
                        messageReceivedListener.onMessageReceived(fullMessage);
                    } else {
                        Log.e(TAG, "MessageReceivedListener is NULL, buffering the message");
                        // Buffer the message until the listener is set
                        messageBuffer.add(fullMessage);
                    }

                } catch (IOException e) {
                    Log.e("BluetoothChat", "Input stream was disconnected", e);

                    // Notify MainActivity of the disconnection and attempt reconnection
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, "Bluetooth connection lost. Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                    });
                    break;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // Method to send data
    public void write(byte[] bytes) {
        try {
            Log.d(TAG, "Sending data: " + new String(bytes));
            mmOutStream.write(bytes);
            mmOutStream.flush();  // Make sure data is immediately sent
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            //mainActivity.reconnectToLastDevice();
        }
    }

    // Interface for message received callback
    public interface OnMessageReceivedListener {
        void onMessageReceived(String message) throws JSONException;
    }

    // Set the message listener and handle any buffered messages
    public synchronized void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;

        // Process buffered messages if any exist
        if (!messageBuffer.isEmpty()) {
            for (String bufferedMessage : messageBuffer) {
                try {
                    listener.onMessageReceived(bufferedMessage);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            messageBuffer.clear(); // Clear the buffer after delivering messages
        }
    }

    // Call this method to shut down the connection
    public void cancel() {
        try {
            if (mmInStream != null) {
                mmInStream.close();
            }
            if (mmOutStream != null) {
                mmOutStream.close();
            }
            if (mmSocket != null && mmSocket.isConnected()) {
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.e("BluetoothChat", "Error closing the socket", e);
        }
    }

    public synchronized void setMainActivity(MainActivity main) {
        this.mainActivity = main;
    }

    public void setMapReceiver(MapModel mapping) {
        this.mappingInstructions = mapping;
    }

    public void setOnConnectedListener(OnConnectedListener listener) {
        this.onConnectedListener = listener;
    }
}
