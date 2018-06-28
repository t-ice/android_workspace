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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
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

    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;
    private Handler mHandler = new Handler();
    private BluetoothDevice blueCan;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private ScanCallback scanCallback;
    private BluetoothGatt bluetoothGatt;
    private final IBinder mBinder = new LocalBinder();

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
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
            Log.i(Constants.LOG_TAG, "Connected to GATT server and attempting to start service discovery");
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

    public void initialize() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    private void startDiscovery() {
        if (bluetoothGatt == null) {
            postDelayStopDiscovery();
            scanCallback = createScanCallback();
            leScanner.startScan(getScanFilters(), getScanSettings(), scanCallback);
        }
    }



    private void stopDiscovery() {
        if (bluetoothAdapter != null && scanCallback != null) {
            leScanner.stopScan(scanCallback);
        }
    }

    /**
     * Stops scanning after a pre-defined scan period.
     */
    private void postDelayStopDiscovery() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log("Device not discovered in scan period!");
                stopDiscovery();
            }
        };
        mHandler.postDelayed(runnable, SCAN_PERIOD);
    }

    @NonNull
    private ScanCallback createScanCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                blueCan = result.getDevice();
                log(blueCan.getName() + " : " + blueCan.getAddress());
                stopDiscovery();
                connect();
            }

            @Override
            public void onScanFailed(int errorCode) {
                log("Scan failed!");
            }
        };
    }

    @NonNull
    private List<ScanFilter> getScanFilters() {
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(BLUECAN_ADDRESS).build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);
        return filters;
    }

    private ScanSettings getScanSettings() {
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }


    public void connect() {
        if(blueCan == null){
            startDiscovery();
        } else{
            if (gattCallback == null || bluetoothGatt == null) {
                bluetoothGatt = blueCan.connectGatt(this, true, gattCallback);
            }else{
                //reconnect
                bluetoothGatt.connect();
            }
        }
    }

    public void sendMessageToBlueCan(String message) {
        BluetoothGattCharacteristic characteristic =
                bluetoothGatt.getService(BLUE_CAN_CUSTOM_SERVICE_UUID)
                        .getCharacteristic(BLUE_CAN_CUSTOM_CHARACTERISTIC_UUID);
        characteristic.setValue(message.getBytes());
        bluetoothGatt.writeCharacteristic(characteristic);
    }


    public void log(String text) {
        if (text != null && !text.isEmpty())
            Log.d(Constants.LOG_TAG, text);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BlueCanService getService() {
            return BlueCanService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }


    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }
}
