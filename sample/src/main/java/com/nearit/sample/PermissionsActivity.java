package com.nearit.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class PermissionsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private static final int BLUETOOTH_SETTINGS_CODE = 4000;
    private static final int LOCATION_SETTINGS_CODE = 5000;
    public static final int PERMISSION_REQUEST_FINE_LOCATION = 6000;

    private Button enableBtn;
    private boolean permissionGiven = false;

    public static Intent createIntent(Context context) {
        return new Intent(context, PermissionsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_permissions);

        enableBtn = (Button) findViewById(R.id.ask_permissions);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!permissionGiven) {
                    askPermissions();
                } else {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        if (allPermissionGranted()) {
            onPermissionsReady();
        }
    }

    public boolean allPermissionGranted() {
        boolean anyLocationProv = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        anyLocationProv |= locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        anyLocationProv |= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        boolean bluetoothOn = false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothOn = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();

        int permissionCheck = PackageManager.PERMISSION_DENIED;
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        return anyLocationProv &&
                bluetoothOn &&
                permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks and asks for missing permissions for Android 23+ devices.
     * Otherwise request for enabling system wise location services.
     */
    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isPermissionGranted = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (isPermissionGranted) {
                openLocationSettings();
            } else {
                requestFineLocationPermission();
            }
        } else {
            openLocationSettings();
        }
    }

    /**
     * Asks to enable location services.
     */
    private void openLocationSettings() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    /**
     * Asks for location permissions.
     */
    private void requestFineLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openLocationSettings();
            } else {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                openBluetoothSettings();
            }
        } else if (requestCode == BLUETOOTH_SETTINGS_CODE) {
            //Nothing to do
            if (resultCode == Activity.RESULT_OK) {
                onPermissionsReady();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setNeedBle(true);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // The bluetooth permissions are strictly necessary for beacons,
                        // but not for geofences
                        openBluetoothSettings();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    PermissionsActivity.this,
                                    LOCATION_SETTINGS_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });

    }

    private void openBluetoothSettings() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_SETTINGS_CODE);
        } else {
            Log.d(TAG, "All permission available");
            onPermissionsReady();
        }

    }

    private void onPermissionsReady() {
        // You have all the right permissions to start the NearIT radar
        permissionGiven = true;
        enableBtn.setText("You are set to go!");
        enableBtn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
    }

    @Override
    public void onBackPressed() {
        if (permissionGiven) {
            setResult(Activity.RESULT_OK);
        }
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Handle connection suspended error
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Handle connection failed error
    }
}
