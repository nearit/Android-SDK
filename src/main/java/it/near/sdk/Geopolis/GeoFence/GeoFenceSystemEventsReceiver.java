package it.near.sdk.Geopolis.GeoFence;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Geopolis.GeopolisManager;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 04/10/2016.
 */

public class GeoFenceSystemEventsReceiver extends BroadcastReceiver {

    private static final String TAG = "GeoFenceSystemEventsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ULog.d(TAG, "received intent: " + intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            sendResetIntent(context);
        }
        if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")){
            boolean anyLocationProv = false;
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            anyLocationProv |= locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            anyLocationProv |= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (anyLocationProv){
                sendResetIntent(context);
            }
        }
    }

    private void sendResetIntent(Context context) {
        Intent resetIntent = new Intent();
        String packageName = context.getPackageName();
        resetIntent.setAction(packageName + "." + GeopolisManager.RESET_MONITOR_ACTION_SUFFIX);
        context.sendBroadcast(resetIntent);
    }



}
