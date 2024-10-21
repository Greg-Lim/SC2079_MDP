package com.example.mdp_android.fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothPopUpFragment extends DialogFragment {

    private static final String TAG = "BluetoothPopUpFragment";
    private boolean toConnect = false;
    private BluetoothHelper bluetoothHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> deviceList;
    private ListView listViewDevices;
    private BluetoothAdapter bluetoothAdapter;
    private OnConnectedListener btListener;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //
    BluetoothSocket existingSocket;
    MainActivity mainActivity;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                    deviceList.add(deviceName + "\n" + deviceAddress);
                    adapter.notifyDataSetChanged();
            }
        }
    };
    private final BroadcastReceiver bondingReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "Device successfully bonded: " + device.getName());
                    // Now you can connect to the device
                    connectToDevice(device);
                } else if (bondState == BluetoothDevice.BOND_NONE) {
                    Log.e(TAG, "Bonding failed for device: " + device.getName());
                }
            }
        }
    };

    // Unified interface
    public interface OnConnectedListener {
        void onConnected(BluetoothSocket socket);
    }

    public BluetoothPopUpFragment() {
        // Empty constructor
    }
    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.activity_bluetooth_setting, container, false);

        // Initialize BluetoothHelper
        bluetoothHelper = new BluetoothHelper();
        enableBluetooth(); //Get default adapter and activate bluetooth


        // Initialize device list and adapter
        deviceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceList);
        listViewDevices = dialogView.findViewById(R.id.lv_devices);
        listViewDevices.setAdapter(adapter);

        // Add paired devices to the top of the list
        addPairedDevices();

        // Set up scan button
        Button scanButton = dialogView.findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(v -> startDiscovery());

        // Set up close button
        Button closeButton = dialogView.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> dismiss());

        /*
        String hardcodeMacAddress = "9C:2E:7A:D3:01:13";
        BluetoothDevice dev = bluetoothHelper.getRemoteDevice(hardcodeMacAddress);
        //startBluetoothClient();
        connectToDevice(dev);*/

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            String item = deviceList.get(position);
            String deviceAddress = item.split("\n")[1];
            BluetoothDevice device = bluetoothHelper.getRemoteDevice(deviceAddress);

            Log.d(TAG, "Device: " + item + ", Addr: " + deviceAddress);

            connectToDevice(device);
        });

        // Register the BroadcastReceiver for device discovery
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        assert getActivity() != null;
        getActivity().registerReceiver(receiver, filter);

        return dialogView;
    }
    @SuppressLint("MissingPermission")
    private void addPairedDevices() {
         Set<BluetoothDevice> pairedDevices = mainActivity.getBluetoothAdapter().getBondedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                deviceList.add(deviceName + "\n" + deviceAddress); // Add to the top of the list
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null) {
            getDialog().getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        assert getActivity() != null;
        getActivity().unregisterReceiver(receiver); // Unregister the receiver when the dialog is destroyed
    }

    private void startDiscovery() {
        if (bluetoothHelper.isBluetoothEnabled()) {
            bluetoothHelper.startDiscovery();
            Toast.makeText(getContext(), "Scanning for devices...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "Starting connectToDevice method");
        bluetoothHelper.cancelDiscovery();

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            Log.d(TAG, "Device not bonded, attempting to bond...");
            //device.createBond();
            return;
        }

        Log.d(TAG, "Device already bonded, connecting...");
        printDeviceInfo(device);
        if(device.getAddress().contains("AA:AA:AA:AA:AA:AA")){
            if(device.getName().contains("Group27SMB")){
                this.toConnect = true;
                Log.d("BluetoothPopUpFragment", "It is our group !!!!!!!!!!!!!");
            }
            else{
                Log.d("BluetoothPopUpFragment", "It is NOT our group !!!!!!!!!!!!!");
            }
        }
        else{
            this.toConnect = true;
        }
        if(toConnect) {
            this.toConnect = false;
            new Thread(() -> {
                BluetoothSocket socket = null;
                boolean connected = false;
                int retryCount = 0;
                final int MAX_RETRY = 1;

                while (!connected && retryCount < MAX_RETRY) {
                    try {
                        Thread.sleep(1000);
                        Log.d(TAG, "Attempt " + (retryCount + 1) + " to connect");

                        // Try to get all the UUID from the device
                        ParcelUuid[] uuidCollection = device.getUuids();
                        // If there is at least 1 uuid available, select the device uuid orelse just use default SSP uuid
                        UUID deviceUUID = (uuidCollection != null && uuidCollection.length > 0) ? uuidCollection[0].getUuid() : MY_UUID;
                        Log.d(TAG, "Using UUID: " + deviceUUID);

                        // Try the standard method first
                        try {
                            Log.d(TAG, "Attempting to create socket with createRfcommSocketToServiceRecord");
                            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to create socket with createRfcommSocketToServiceRecord: " + e.getMessage());

                        }

                        Log.d(TAG, "Attempting to connect...");
                        socket.connect();

                        if (socket.isConnected()) {
                            connected = true;
                            Log.d(TAG, "Successfully connected to device");
                            final BluetoothSocket connectedSocket = socket;
                            mainActivity.manageConnectedSocket(socket);
                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    if (btListener != null) {
                                        btListener.onConnected(connectedSocket);
                                    }
                                    Toast.makeText(getContext(), "Connected successfully", Toast.LENGTH_SHORT).show();
                                });
                            }else {
                                Log.e("BluetoothPopUpFragment", "Fragment not attached or activity is null. Cannot update UI.");
                            }
                        } else {
                            Log.d(TAG, "Socket connected() method returned, but isConnected() is false");
                        }
                    } catch (IOException | InterruptedException e) {
                        Log.e(TAG, "Error connecting to device: " + e.getMessage());
                        e.printStackTrace();
                        retryCount++;
                        if (retryCount < MAX_RETRY) {
                            Log.d(TAG, "Retrying connection, attempt " + (retryCount + 1));
                        } else if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error connecting: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }
                        try {
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException closeException) {
                            Log.e(TAG, "Could not close the client socket", closeException);
                        }
                    }
                }

                if (!connected) {
                    Log.e(TAG, "Failed to connect after " + MAX_RETRY + " attempts");
                    if((getActivity() != null && isAdded())) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Failed to connect after multiple attempts", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }).start();
        }
    }

    @SuppressLint("MissingPermission")
    private void printDeviceInfo(BluetoothDevice device) {
        Log.d(TAG, "Device Name: " + device.getName());
        Log.d(TAG, "Device Address: " + device.getAddress());
        Log.d(TAG, "Device Class: " + device.getBluetoothClass());
        Log.d(TAG, "Device Bond State: " + bondStateToString(device.getBondState()));
        Log.d(TAG, "Device Type: " + deviceTypeToString(device.getType()));

        ParcelUuid[] uuids = device.getUuids();
        if (uuids != null) {
            for (ParcelUuid uuid : uuids) {
                Log.d(TAG, "Device UUID: " + uuid);
            }
        } else {
            Log.d(TAG, "No UUIDs found for device");
        }
    }

    private String bondStateToString(int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_NONE: return "BOND_NONE";
            case BluetoothDevice.BOND_BONDING: return "BOND_BONDING";
            case BluetoothDevice.BOND_BONDED: return "BOND_BONDED";
            default: return "UNKNOWN";
        }
    }

    private String deviceTypeToString(int type) {
        switch (type) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC: return "DEVICE_TYPE_CLASSIC";
            case BluetoothDevice.DEVICE_TYPE_LE: return "DEVICE_TYPE_LE";
            case BluetoothDevice.DEVICE_TYPE_DUAL: return "DEVICE_TYPE_DUAL";
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN: return "DEVICE_TYPE_UNKNOWN";
            default: return "UNKNOWN";
        }
    }


    public void setOnConnectedListener(OnConnectedListener listener) {
        this.btListener = listener;
    }

    @SuppressLint("MissingPermission")
    private void enableBluetooth(){
        try{
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(!bluetoothAdapter.isEnabled()){
                bluetoothAdapter.enable();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

}
