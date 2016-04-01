package it.near.sdk.Beacons;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;

import it.near.sdk.GlobalState;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Models.NearBeacon;
import it.near.sdk.Utils.ULog;

/**
 * Wrapper around AltBeacon. It's a Service and it's used by the SDK with both startservice and bind.
 *
 * Created by cattaneostefano on 14/03/16.
 */
public class AltBeaconWrapper implements BeaconConsumer {

    private static final String TAG = "AltBeaconWrapper";
    private BeaconManager beaconManager;
    private Context mContext;

    public AltBeaconWrapper(Context context) {
        ULog.d(TAG , "Constructor called");
        mContext = context;
        beaconManager = BeaconManager.getInstanceForApplication(mContext);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
    }

    public void startRanging() {
        ULog.d(TAG , "startRanging");
        beaconManager.bind(this);
        beaconManager.setBackgroundMode(false);
        beaconManager.setRangeNotifier(GlobalState.getInstance(mContext).getNearRangeNotifier());
    }

    public void stopRangingAll(){
        ULog.d(TAG, "stopRangingAll");
        resetRanging();
        beaconManager.unbind(this);
        beaconManager.setRangeNotifier(null);
        beaconManager.setBackgroundMode(true);
    }

    private void resetRanging(){
        ULog.d(TAG, "resetRanging");
        Collection<Region> regionList = beaconManager.getRangedRegions();
        for (Region r : regionList){
            try {
                beaconManager.stopRangingBeaconsInRegion(r);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        ULog.d(TAG, "onBeaconServiceConnect, startRanging");

        // after we connected with the beacon service, we use the configuration (if we ahve it yet) to configure our radar
        configureScanner(GlobalState.getInstance(getApplicationContext()).getConfiguration());
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        mContext.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return mContext.bindService(intent, serviceConnection, i);
    }


    /**
     * Reset configuration.
     * Stop current ranging and range new configuration beacons.
     *
     * @param configuration
     */
    public void configureScanner(Configuration configuration){
        ULog.d(TAG , "configureScanner");
        resetRanging();
        List<NearBeacon> beaconList = configuration.getBeaconList();

        BeaconDynamicRadar radar = new BeaconDynamicRadar(this.getApplicationContext(), beaconList, proximityListener);
        GlobalState.getInstance(this.getApplicationContext()).setBeaconDynamicRadar(radar);

        if ( beaconList == null  ||  beaconList.size() == 0 ) return;

        try {
            // Since every beacon can have completely different identifiers, we don't range for specific regions, we range all beacons
            // when we will have actual regions we will range regions
            beaconManager.startRangingBeaconsInRegion(new Region("Region", null, null, null));
            //beaconManager.startRangingBeaconsInRegion(new Region("Region" + b.getMinor() + b.getMajor(), Identifier.parse(b.getProximity_uuid()), Identifier.fromInt(b.getMajor()), Identifier.fromInt(b.getMinor())));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }




    public void setBackgroundMode(boolean isInBackground){
        beaconManager.setBackgroundMode(isInBackground);
    }

    private GlobalState getGlobalState(){
        return GlobalState.getInstance(getApplicationContext());
    }

    private void trace(String trace){
        getGlobalState().getTraceNotifier().trace(trace);
    }

    private ProximityListener proximityListener = new ProximityListener() {
        @Override
        public void enterBeaconRange(NearBeacon beacon) {
            Matching matching = getGlobalState().getConfiguration().getMatchingFromBeacon(beacon);
            getGlobalState().getMatchingNotifier().onRuleFullfilled(matching);
        }

        @Override
        public void exitBeaconRange(NearBeacon beacon) {
        }
    };


}
