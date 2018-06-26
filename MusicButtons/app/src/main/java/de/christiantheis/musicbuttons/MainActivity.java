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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLocationServiceGrants();
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


    public void onNextTrackBottonClick(View view) {
        MusicController.getInstance(this).nextTrack();
    }

    public void onPreviousTrackBottonClick(View view) {
        MusicController.getInstance(this).previousTrack();
    }

    public void onPlayPauseBottonClick(View view) {
        MusicController.getInstance(this).togglePause();
    }


    public void onStartDiscoverBottonClick(View view) {
        BlueCanController blueCanController = BlueCanController.getInstance(this);

        if (!blueCanController.isBluetoothEnabled()) {
            startEnableBluetoothActivity();
        } else {
            blueCanController.startDiscovery();
        }
    }

    private void startEnableBluetoothActivity() {
        final int REQUEST_ENABLE_BT = 1;
        Intent enableBtIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    public void onStopDiscoverBottonClick(View view) {
        BlueCanController.getInstance(this).stopDiscovery();
    }


    /**
     * Called when the user taps the Send button
     */
    public void onConnectBottonClick(View view) {
        BlueCanController.getInstance(this).connect();
    }


    public void log(String text) {
        if (text != null && !text.isEmpty())
            Log.d(Constants.LOG_TAG, text);
    }


}
