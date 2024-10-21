package com.example.mdp_android.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.fragment.BluetoothPopUpFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private MainActivity mainActivity;

    public BluetoothHelper() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void enableBluetooth(Context context) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }

    @SuppressLint("MissingPermission")
    public void disableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }
    public BluetoothAdapter getBluetoothAdapter(){
        return this.bluetoothAdapter;
    }

    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    public void cancelDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public BluetoothDevice getRemoteDevice(String address) {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getRemoteDevice(address);
        }
        return null;
    }



}
