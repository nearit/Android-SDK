package it.near.sdk.Beacons;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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
import java.util.List;

import it.near.sdk.GlobalState;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 14/03/16.
 */
public class AltBeaconWrapper extends Service implements BeaconConsumer {

    private static final String TAG = "AltBeaconWrapper";
    private BeaconManager beaconManager;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public AltBeaconWrapper getService() {
            // Return this instance of LocalService so clients can call public methods
            return AltBeaconWrapper.this;
        }
    }

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
        beaconManager.setRangeNotifier(GlobalState.getInstance(this).getNearRangeNotifier());
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
        return mBinder;
    }

    @Override
    public void onBeaconServiceConnect() {
        ULog.d(TAG, "onBeaconServiceConnect, startRanging");

        /*beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                ULog.d(TAG, "didRangeBeaconsInRegion " + beacons.size() + " region: " + region.toString());
                if (beacons.size() > 0) {
                    ULog.d(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                }
            }
        });*/
        configureScanner(GlobalState.getInstance(getApplicationContext()).getConfiguration());
    }

    public void configureScanner(Configuration configuration){
        stopRangingAll();
        List<it.near.sdk.Models.Beacon> beaconList = configuration.getBeaconList();
        if ( beaconList == null  ||  beaconList.size() == 0 ) return;

        for (it.near.sdk.Models.Beacon b : beaconList){
            try {
                ULog.d(TAG, "startRanging beacon: " + b.getMajor() + " " + b.getMinor());
                beaconManager.startRangingBeaconsInRegion(new Region("Region", null, null, null));
                //beaconManager.startRangingBeaconsInRegion(new Region("Region" + b.getMinor() + b.getMajor(), Identifier.parse(b.getProximity_uuid()), Identifier.fromInt(b.getMajor()), Identifier.fromInt(b.getMinor())));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRangingAll(){
        Collection<Region> regionList = beaconManager.getRangedRegions();
        for (Region r : regionList){
            try {
                beaconManager.stopRangingBeaconsInRegion(r);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    public void setBackgroundMode(boolean isInBackground){
        beaconManager.setBackgroundMode(isInBackground);
    }
}
