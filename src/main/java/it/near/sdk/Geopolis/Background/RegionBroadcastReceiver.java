package it.near.sdk.Geopolis.Background;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * WakefulBroadcastReceiver to receive region messages. This receiver should be initiated in the manifest to use the default implementation
 * or extended to create a custom implementation (In this case the extended receiver it's the one to include in the app manifest).
 * The action to register in the manifest is <code>it.near.sdk.permission.REGION_MESSAGE</code>
 *
 * <pre>
 *     {@code
 *           <!-- MESSAGGI INGRESSO/USCITA DALLA REGION -->
 *           <receiver android:name="it.near.sdk.Beacons.Monitoring.RegionBroadcastReceiver"
 *           android:exported="false">
 *              <intent-filter>
 *                  <action android:name="it.near.sdk.permission.REGION_MESSAGE" />
 *                  <category android:name="android.intent.category.DEFAULT" />
 *              </intent-filter>
 *           </receiver>
 *     }
 * </pre>
 *
 * @author cattaneostefano
 */
public class RegionBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "RegionBroadcastReceiver";

    /**
     * Starts @RegionIntentService in wakeful mode.
     * @param context the current context
     * @param intent the input intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("TAG" , "exit region event received by the wakeful broadcast receiver");

        // Explicitly specify that RegionIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), RegionIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }




}
