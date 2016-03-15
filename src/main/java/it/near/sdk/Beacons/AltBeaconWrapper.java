package it.near.sdk.Beacons;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 * Created by cattaneostefano on 14/03/16.
 */
public class AltBeaconWrapper extends Service implements BeaconConsumer {

    private static final String TAG = "AltBeaconWrapper";
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        beaconManager.bind(this);
        beaconManager.setBackgroundMode(false);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        beaconManager.unbind(this);
        beaconManager.setRangeNotifier(null);
        beaconManager.setBackgroundMode(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect, startRanging");

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.d(TAG, "didRangeBeaconsInRegion " + beacons.size() + " region: " + region.toString());
                if (beacons.size() > 0) {
                    Log.d(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                }
            }
        });

        try {
            String proximityUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
            int major = 452;
            beaconManager.startRangingBeaconsInRegion(new Region("Kontact, with Estimote proximityUUID", Identifier.parse(proximityUUID), Identifier.fromInt(major), Identifier.fromInt(111)));
        } catch (RemoteException e) {  Log.d(TAG, "Exception");  }

    }
}
