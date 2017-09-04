package it.near.sdk.geopolis.geofences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.near.sdk.logging.NearLog;

public class LocationUpdatesReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationUpdatesReceiver";
    public static final String ACTION_PROCESS_UPDATES = "kjhdbfds";

    @Override
    public void onReceive(Context context, Intent intent) {
        NearLog.d(TAG, "got location update");
    }
}
