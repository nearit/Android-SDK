package com.nearit.sample;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

import org.json.JSONException;

import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.reactions.content.Content;
import it.near.sdk.reactions.coupon.Coupon;
import it.near.sdk.reactions.customjson.CustomJSON;
import it.near.sdk.reactions.feedback.Feedback;
import it.near.sdk.reactions.poll.Poll;
import it.near.sdk.reactions.simplenotification.SimpleNotification;
import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearItIntentConstants;
import it.near.sdk.utils.NearUtils;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, CoreContentsListener, ProximityListener {

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    public static final int PERMISSION_REQUEST_FINE_LOCATION = 6000;
    private static final int LOCATION_SETTINGS_CODE = 5000;
    private static final int BLUETOOTH_SETTINGS_CODE = 4000;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ForegroundActivity.class));
            }
        });
        permissionCheck();

        MyApplication.getNearItManager().addProximityListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getNearItManager().removeProximityListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        onNewIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null &&
                intent.getExtras() != null &&
                intent.hasExtra(NearItIntentConstants.RECIPE_ID)) {
            // we got a NearIT intent

            // track it as engaged, since we tapped on it
            MyApplication.getNearItManager().sendTracking(
                    intent.getStringExtra(NearItIntentConstants.RECIPE_ID),
                    Recipe.ENGAGED_STATUS
            );

            NearUtils.parseCoreContents(intent, this);
        }
    }

    private void permissionCheck() {
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

    private void requestFineLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                openBluetoothSettings();
                startNearRadar();
            }
        } else if (requestCode == BLUETOOTH_SETTINGS_CODE) {
            //Nothing to do
            if (resultCode == Activity.RESULT_OK) {
                startNearRadar();
            }
        }
    }

    private void openLocationSettings() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).setNeedBle(true);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startNearRadar();
                        openBluetoothSettings();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    MainActivity.this,
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
            Log.d(TAG, "ALl permission availble");
        }
    }

    private void startNearRadar() {
        Log.d(TAG, "Start radar");
        MyApplication.getNearItManager().startRadar();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void foregroundEvent(Parcelable content, Recipe recipe) {
        NearUtils.parseCoreContents(content, recipe, this);
    }

    @Override
    public void gotPollNotification(@Nullable Intent intent, Poll notification, String recipeId) {
        Toast.makeText(this, "You received a poll", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotContentNotification(@Nullable Intent intent, Content notification, String recipeId) {
        Toast.makeText(this, "You received a notification with content", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotCouponNotification(@Nullable Intent intent, Coupon notification, String recipeId) {
        Toast.makeText(this, "You received a coupon", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(notification.getSerial()).create().show();
    }

    @Override
    public void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, String recipeId) {
        Toast.makeText(this, "You received a custom json", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, String recipeId) {
        Toast.makeText(this, "You received a simple notification", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotFeedbackNotification(@Nullable Intent intent, Feedback s_notif, String recipeId) {
        Toast.makeText(this, "You received a feedback request", Toast.LENGTH_SHORT).show();
    }

}
