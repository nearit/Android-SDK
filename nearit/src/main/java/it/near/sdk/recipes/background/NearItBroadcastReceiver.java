package it.near.sdk.recipes.background;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Deprecated
 */
public class NearItBroadcastReceiver extends WakefulBroadcastReceiver {

    /**
     * Starts @RegionIntentService in wakeful mode.
     * @param context the current context
     * @param intent the input intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Explicitly specify that RegionIntentService will handle the intent.
        ComponentName comp = new ComponentName(context, NearItIntentService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
