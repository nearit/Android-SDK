package it.near.sdk.geopolis.geofences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

/**
 * Receiver for the BOOT_COMPLETED and PROVIDERS_CHANGED system events.
 * It sends intent to start or stop radar
 * Created by cattaneostefano on 04/10/2016.
 */

public class GeoFenceSystemEventsReceiver extends BroadcastReceiver {

    private static final String TAG = "GFSystemEventsReceiver";
    public static final String LOCATION_STATUS = "location_status";
    public static final String RESET_MONITOR_ACTION_SUFFIX = "RESET_SCAN";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received intent: " + intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            sendResetIntent(context, true);
        }
        if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            boolean anyLocationProv = false;
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            anyLocationProv |= locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            anyLocationProv |= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            sendResetIntent(context, anyLocationProv);
        }
    }

    /**
     * Send the reset intent to start or stop the radar.
     *
     * @param context
     * @param startRadar indicates if the radar should start.
     */
    private void sendResetIntent(Context context, boolean startRadar) {
        Intent resetIntent = new Intent();
        String packageName = context.getPackageName();
        resetIntent.setAction(packageName + "." + RESET_MONITOR_ACTION_SUFFIX);
        resetIntent.putExtra(LOCATION_STATUS, startRadar);
        context.sendBroadcast(resetIntent);
    }
}
