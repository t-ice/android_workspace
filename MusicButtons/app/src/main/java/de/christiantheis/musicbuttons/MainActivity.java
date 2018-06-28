package de.christiantheis.musicbuttons;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BlueCanService blueCanService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            blueCanService = ((BlueCanService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            blueCanService = null;
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            EditText text = (EditText) findViewById(R.id.editText);
            switch (action) {
                case BlueCanService.ACTION_GATT_CONNECTED:
                    text.append("Gatt connected\n");
                    break;
                case BlueCanService.ACTION_GATT_DISCONNECTED:
                    text.append("Gatt disconnected\n");
                    break;
                case BlueCanService.ACTION_GATT_SERVICES_DISCOVERED:
                    text.append("Services discovered\n");
                    break;
                case BlueCanService.ACTION_DATA_AVAILABLE:
                    String command = intent.getStringExtra(BlueCanService.EXTRA_DATA);
                    text.append("Received data: " + command + "\n");
                    handleCommand(command);
                    break;
                default:
                    log("No supported intent action detected");
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLocationServiceGrants();
        bindBlueCanService();
    }

    private void bindBlueCanService() {
        Intent gattServiceIntent = new Intent(this, BlueCanService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }


    private void checkLocationServiceGrants() {
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


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (blueCanService != null) {
            blueCanService.connect();

        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BlueCanService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BlueCanService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BlueCanService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BlueCanService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        blueCanService = null;
    }


    public void onNextTrackBottonClick(View view) {
        MusicController.getInstance(this).nextTrack();
    }

    public void onPreviousTrackBottonClick(View view) {
        MusicController.getInstance(this).previousTrack();
    }

    public void onPlayPauseBottonClick(View view) {
        MusicController.getInstance(this).togglePause();
    }




    private void startEnableBluetoothActivity() {
        final int REQUEST_ENABLE_BT = 1;
        Intent enableBtIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }




    /**
     * Called when the user taps the Send button
     */
    public void onConnectBottonClick(View view) {
        blueCanService.initialize();
        if (!blueCanService.isBluetoothEnabled()) {
            startEnableBluetoothActivity();
        } else {
            blueCanService.connect();
        }
    }


    public void log(String text) {
        if (text != null && !text.isEmpty())
            Log.d(Constants.LOG_TAG, text);
    }


    public void handleCommand(String command) {
        MusicController musicController = MusicController.getInstance(this);
        switch (command) {
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
                log("Command not understood: " + command);
                break;
        }
    }

}
