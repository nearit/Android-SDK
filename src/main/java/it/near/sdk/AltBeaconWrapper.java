package it.near.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;

/**
 * Created by cattaneostefano on 14/03/16.
 */
public class AltBeaconWrapper implements BeaconConsumer {

    BeaconManager beaconManager = null;

    private static AltBeaconWrapper mInstance = null;

    private String mString;

    private AltBeaconWrapper(Context context){
        beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
        beaconManager.bind(this);
    }

    public static AltBeaconWrapper getInstance(Context context){
        if(mInstance == null)
        {
            mInstance = new AltBeaconWrapper(context);
        }
        return mInstance;
    }

    @Override
    public void onBeaconServiceConnect() {
        // TODO start beacon interaction
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return false;
    }
}
