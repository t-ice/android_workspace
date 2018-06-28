package de.christiantheis.musicbuttons;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

public class BlueCanService extends Service {

    public static final UUID BLUE_CAN_CUSTOM_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID BLUE_CAN_CUSTOM_CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final String BLUECAN_ADDRESS = "3C:A3:08:92:EB:40";


    public final static String ACTION_GATT_CONNECTED =
            "de.bluecan.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "de.bluecan.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "de.bluecan.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "de.bluecan.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "de.bluecan.EXTRA_DATA";

    private BluetoothDevice blueCan;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final IBinder localBinder = new LocalBinder();
    private BluetoothGattCallback gattCallback;


    public void initialize() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        blueCan = bluetoothAdapter.getRemoteDevice(BLUECAN_ADDRESS);
    }

    @NonNull
    private BluetoothGattCallback createBluetoothGattCallback() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcastUpdate(ACTION_GATT_CONNECTED);
                    Log.i(Constants.LOG_TAG, "Connected to GATT server and attempting to start service discovery");
                    bluetoothGatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(Constants.LOG_TAG, "Disconnected from GATT server.");
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(Constants.LOG_TAG, "Service discovered");
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
                final String commandValue = new String(characteristic.getValue());
                log("ValueChanged: " + commandValue);
                final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                intent.putExtra(EXTRA_DATA, commandValue);
                sendBroadcast(intent);
            }
        };
    }


    boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void connect() {
        if (bluetoothGatt == null) {
            bluetoothGatt = blueCan.connectGatt(this, false, createBluetoothGattCallback());
        } else {
            //reconnect
            bluetoothGatt.connect();
        }
    }

    public void sendMessageToBlueCan(String message) {
        BluetoothGattCharacteristic characteristic =
                bluetoothGatt.getService(BLUE_CAN_CUSTOM_SERVICE_UUID)
                        .getCharacteristic(BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID);
        characteristic.setValue(message.getBytes());
        bluetoothGatt.writeCharacteristic(characteristic);
    }


    public class LocalBinder extends Binder {
        BlueCanService getService() {
            return BlueCanService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }


    public void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
        log("Close gatt");
    }


    public void log(String text) {
        if (text != null && !text.isEmpty())
            Log.d(Constants.LOG_TAG, text);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
}
