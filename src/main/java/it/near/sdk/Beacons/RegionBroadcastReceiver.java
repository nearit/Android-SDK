package it.near.sdk.Beacons;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by cattaneostefano on 11/04/16.
 */
public class RegionBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "RegionBroadcastReceiver";

    /**
     * @param context the current context
     * @param intent the input intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("TAG" , "exit region event received by the wakeful broadcast receiver");

        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), RegionIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
