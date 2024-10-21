package com.example.mdp_android.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Uses legacy RFComm implementation
 */
public class BluetoothClient {

    private static final String TAG = "BluetoothClient";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final Context context;
    public final Set<BluetoothDevice> discoveredDevices = new HashSet<>();
    private final BluetoothReceiver bluetoothReceiver;

    // This is a static string that should be on the server
    private UUID uuidOfServer = null;

    @SuppressLint("StaticFieldLeak")
    private static volatile BluetoothClient thisInstance = null;

    public BluetoothClient(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        this.bluetoothReceiver = new BluetoothReceiver();
    }

    public static BluetoothClient getInstance(Context context){
        if (thisInstance == null) {
            synchronized (BluetoothClient.class) {
                if (thisInstance == null){
                    // getApplicationContext() is key, it keeps you from leaking the
                    // Activity or BroadcastReceiver if someone passes one in.
                    thisInstance = new BluetoothClient(context.getApplicationContext());
                }
            }
        }
        return thisInstance;
    }


    public Boolean setUUID(String uuid){
        try{
            this.uuidOfServer = UUID.fromString(uuid);
            Log.d(TAG, "UUID of server set");
            return true;
        }
        catch(Exception e){
            Log.d(TAG, "UUID of server unable to be set");
            return false;
        }
    }


    public void enableBluetooth() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity) this.context,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_PERMISSIONS
            );
            Log.d(TAG, "Requested Bluetooth scan permission");
        }

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Gets a set of BluetoothDevice(s) that is currently paired
     * @return Set<BluetoothDevice>
     */
    @SuppressLint("MissingPermission")
    public Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }


    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice device) throws IOException {

        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity) this.context,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_PERMISSIONS
            );
            Log.d(TAG, "Requested Bluetooth connect permission");
        }

        if (uuidOfServer == null){
            throw new NullPointerException("UUID of server not set");
        }

        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuidOfServer);
        bluetoothSocket.connect();
        outputStream = bluetoothSocket.getOutputStream();
        inputStream = bluetoothSocket.getInputStream();
        Log.d(TAG, "Connected to " + device.getName());
    }


    // TODO: figure out how to handle midway disconnect
    public void disconnect() throws IOException {
        if (bluetoothSocket != null) {
            bluetoothSocket.close();
        }
    }



    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                (Activity) context,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                REQUEST_BLUETOOTH_PERMISSIONS
                        );
                        Log.d(TAG, "Requested Bluetooth scan permission");
                    }

                    discoveredDevices.add(device);
                    Log.d(TAG, "Discovered device: " + device.getName() + " [" + device.getAddress() + "]");
                }
            }
        }
    }
}