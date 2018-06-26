package de.christiantheis.musicbuttons;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_ENABLE_BT = 1;
    public static final String TAG = "musicbuttons";

    public static final UUID BLUE_CAN_CUSTOM_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID BLUE_CAN_CUSTOM_CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    BluetoothDevice blueCan;
    BluetoothAdapter bluetoothAdapter;
    BluetoothAdapter.LeScanCallback scanCallback;
    BluetoothGatt gatt;

    MusicController musicController = new MusicController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Called when the user taps the Send button
     */
    public void onNextTrackBottonClick(View view) {
        musicController.next();
    }

    /**
     * Called when the user taps the Send button
     */
    public void onPreviousTrackBottonClick(View view) {
        musicController.previousTrack();
    }


    /**
     * Called when the user taps the Send button
     */
    public void onPlayPauseBottonClick(View view) {
        musicController.togglePause();
    }


    /**
     * Called when the user taps the Send button
     */
    public void onStartDiscoverBottonClick(View view) {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

                log(bluetoothDevice.getName() + " : " + bluetoothDevice.getAddress());

                if ("3C:A3:08:92:EB:40".equals(bluetoothDevice.getAddress())) {
                    blueCan = bluetoothDevice;
                }
            }
        };
        bluetoothAdapter.startLeScan(scanCallback);
    }


    public void onStopDiscoverBottonClick(View view) {

        if (bluetoothAdapter != null && scanCallback != null) {
            bluetoothAdapter.stopLeScan(scanCallback);
        }

    }

    /**
     * Called when the user taps the Send button
     */
    public void onConnectBottonClick(View view) {

        BluetoothGattCallback gattCallback =
                new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == STATE_CONNECTED) {
                            gatt.discoverServices();
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        BluetoothGattCharacteristic characteristic =
                                gatt.getService(BLUE_CAN_CUSTOM_SERVICE_UUID)
                                        .getCharacteristic(BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLUE_CAN_CUSTOM_CLIENT_CHARACTERISTIC_CONFIG_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.setCharacteristicNotification(characteristic, true);
                        gatt.writeDescriptor(descriptor);
                    }


                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        final String value = new String(characteristic.getValue());
                        log("ValueChanged: " + value);
                        switch (value) {
                            case "PLAY":

                                break;
                            case "NEXT":
                                break;
                            case "PREVIOUS":
                                break;
                            default:
                                log("Value not understood: " + value);
                                break;
                        }
                    }
                };

        gatt = blueCan.connectGatt(this, true, gattCallback);
    }

    private void sendMessageToBlueCan(String message) {
        BluetoothGattCharacteristic characteristic =
                gatt.getService(BLUE_CAN_CUSTOM_SERVICE_UUID)
                        .getCharacteristic(BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID);
        characteristic.setValue(message.getBytes());
        gatt.writeCharacteristic(characteristic);
    }


    public void log(String text) {
        //TextView textElement = findViewById(R.id.editText);
        //textElement.append(text);
        if (text != null && !text.isEmpty())
            Log.d(TAG, text);
    }


}
