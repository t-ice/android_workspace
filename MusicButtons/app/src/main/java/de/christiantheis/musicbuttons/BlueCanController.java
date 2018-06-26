package de.christiantheis.musicbuttons;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class BlueCanController {

    public static final UUID BLUE_CAN_CUSTOM_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID BLUE_CAN_CUSTOM_CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final String BLUECAN_ADDRESS = "3C:A3:08:92:EB:40";

    private static BlueCanController instance = null;
    private Context androidContext;

    private BluetoothDevice blueCan;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback scanCallback;
    private BluetoothGatt gatt;
    private BluetoothGattCallback gattCallback;

    public static BlueCanController getInstance(Context context) {
        if(instance == null) {
            instance = new BlueCanController(context);
        }
        return instance;
    }

    private BlueCanController() {
    }

    private BlueCanController(Context context) {
        this.androidContext = context;
        initBluetootAdapter();
    }

    private void initBluetootAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) androidContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }


    public void startDiscovery() {

        if (gatt == null) {
            scanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    log(bluetoothDevice.getName() + " : " + bluetoothDevice.getAddress());

                    if (BLUECAN_ADDRESS.equals(bluetoothDevice.getAddress())) {
                        blueCan = bluetoothDevice;
                        log("BlueCan found!");
                    }

                    if (blueCan != null) {
                        stopDiscovery();
                    }
                }
            };
            bluetoothAdapter.startLeScan(scanCallback);
        }
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }


    public void stopDiscovery() {
        if (bluetoothAdapter != null && scanCallback != null) {
            bluetoothAdapter.stopLeScan(scanCallback);
        }
    }


    public void connect() {
        if (gattCallback == null || gatt == null) {
            gattCallback = new BluetoothGattCallback() {
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
                    MusicController musicController = MusicController.getInstance(androidContext);
                    switch (value) {
                        case "PLAY":
                            musicController.togglePause();
                            break;
                        case "NEXT":
                            musicController.nextTrack();
                            break;
                        case "PREVIOUS":
                            musicController.previousTrack();
                            break;
                        default:
                            log("Value not understood: " + value);
                            break;
                    }
                }
            };
            gatt = blueCan.connectGatt(androidContext, true, gattCallback);
        }
    }

    public void sendMessageToBlueCan(String message) {
        BluetoothGattCharacteristic characteristic =
                gatt.getService(BLUE_CAN_CUSTOM_SERVICE_UUID)
                        .getCharacteristic(BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID);
        characteristic.setValue(message.getBytes());
        gatt.writeCharacteristic(characteristic);
    }


    public void log(String text) {
        if (text != null && !text.isEmpty())
            Log.d(Constants.LOG_TAG, text);
    }
}
