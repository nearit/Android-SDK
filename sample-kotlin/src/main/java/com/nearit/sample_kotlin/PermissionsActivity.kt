package com.nearit.sample_kotlin

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

class PermissionsActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private val BLUETOOTH_SETTINGS_CODE = 4000
    private val LOCATION_SETTINGS_CODE = 5000
    private val PERMISSION_REQUEST_FINE_LOCATION = 6000

    private var permissionGiven = false

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, PermissionsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (!permissionGiven) {
            askPermissions()
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isPermissionGranted = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (isPermissionGranted) {
                openLocationSettings()
            } else {
                requestFineLocationPermission()
            }
        } else {
            openLocationSettings()
        }
    }

    private fun openLocationSettings() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build()
        mGoogleApiClient?.connect()
    }

    private fun requestFineLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openLocationSettings()
            } else finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTINGS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                openBluetoothSettings()
            } else {
                finish()
            }
        } else if (requestCode == BLUETOOTH_SETTINGS_CODE) {
            //  Nothing to do
            if (resultCode == Activity.RESULT_OK) {
                onPermissionsReady()
            } else {
                finish()
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setNeedBle(true)

        val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build())
        result.setResultCallback {
            val status: Status = it.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> openBluetoothSettings()
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        status.startResolutionForResult(this@PermissionsActivity, LOCATION_SETTINGS_CODE)
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> finish()
            }
        }
    }

    private fun openBluetoothSettings() {
        val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter?.let {
            if (!it.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, BLUETOOTH_SETTINGS_CODE)
            } else onPermissionsReady()
        } ?: onPermissionsReady()
    }

    private fun onPermissionsReady() {
        permissionGiven = true
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        if (permissionGiven) {
            setResult(Activity.RESULT_OK)
        }
        finish()
    }

    override fun onConnectionSuspended(i: Int) {
        // Handle connection suspended error
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // Handle connection failed error
    }

}