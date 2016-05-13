package it.near.sdk.Beacons.BeaconForest;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.near.sdk.Beacons.BeaconForest.ForestManager;
import it.near.sdk.Utils.ULog;

/**
 * Monitor for AltBeacon regions. It sets the format of the bluetooth package and holds the background powersaver.
 *
 * @author cattaneostefano.
 */
public class AltBeaconMonitor implements BeaconConsumer, BootstrapNotifier, RangeNotifier {

    private static final String TAG = "AltBeaconMonitor";
    private static final float DEFAULT_THRESHOLD = 0.5f;
    private static final String INSIDE_STATE = "inside_state";
    private final BeaconManager beaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    private Context mContext;
    private RegionBootstrap regionBootstrap;
    private List<Region> regionsToRange = new ArrayList<>();
    private List<Region> regionsImIn = new ArrayList<>();
    private BootstrapNotifier outerNotifier;
    private float threshold = DEFAULT_THRESHOLD;
    private boolean areWeInside;
    private String prefsNameSuffix = "AltMonitor";
    private SharedPreferences sp;

    public AltBeaconMonitor(Context context) {
        this.mContext = context;

        beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
        beaconManager.getBeaconParsers().clear();
        // set beacon layout for iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        // TODO turn back off
        BeaconManager.setDebug(true);

        String PACK_NAME = mContext.getApplicationContext().getPackageName();
        String PREFS_NAME = PACK_NAME + prefsNameSuffix;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);

        areWeInside = loadInsideState();

        //backgroundPowerSaver = new BackgroundPowerSaver(context.getApplicationContext());
    }

    private boolean loadInsideState() {
        return sp.getBoolean(INSIDE_STATE, false);
    }

    private void setInsideState(boolean insideState){
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(INSIDE_STATE, insideState).apply();
    }

    /**
     * Starts the Near region scanner. The AltBeacon backgroundMode won't match the app background status.
     * They are simply used to set two different scan parameter sets.
     * The Scanning will be in background mode when we are outside all super region and in foreground mode when we are
     * inside one super region.
     *
     * @param backBetweenPeriod period between background scans.
     * @param backScanPeriod background scan length.
     * @param foreBetweenPeriod period between foreground scans.
     * @param foreScanPeriod foreground scan length.
     * @param regionExitPeriod time to wait before notifying a region exits.
     * @param threshold minimum "distance" in ranging before considering the device inside the region.
     * @param superRegions list of super regions to always monitor.
     * @param regions list of normal regions.
     * @param outerNotifier monitor notifier for normal region entry
     */
    public void startRadar(long backBetweenPeriod, long backScanPeriod, long foreBetweenPeriod, long foreScanPeriod, long regionExitPeriod, float threshold, List<Region> superRegions, List<Region> regions, BootstrapNotifier outerNotifier){
        this.outerNotifier = outerNotifier;
        if (threshold != 0) this.threshold = threshold;
        // resetMonitoring();
        // setMonitoring(superRegions);
        ULog.d(TAG, "startRadar");
        regionsToRange = regions;
        beaconManager.setBackgroundBetweenScanPeriod(backBetweenPeriod);
        beaconManager.setBackgroundScanPeriod(backScanPeriod);
        beaconManager.setForegroundBetweenScanPeriod(foreBetweenPeriod);
        beaconManager.setForegroundScanPeriod(foreScanPeriod);
        beaconManager.setRegionExitPeriod(regionExitPeriod);
        beaconManager.setBackgroundMode(!loadInsideState());
        regionBootstrap = new RegionBootstrap(this, superRegions);
    }

    /**
     * Set regions to range and connects to beaconservice.
     */
    public void startExpBGRanging(){
        ULog.d(TAG, "startExpRanging");
        setInsideState(true);
        beaconManager.setBackgroundMode(false);
        setRanging(regionsToRange);
        beaconManager.setRangeNotifier(this);
        beaconManager.bind(this);

    }

    private void setRanging(List<Region> regionsToRange) {
        for (Region region : regionsToRange) {
            try {
                beaconManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop monitoring all regions previously registered.
     * @param alsoSuper
     */
    private void resetMonitoring(boolean alsoSuper) {
        ArrayList<Region> monitoredRegions = (ArrayList<Region>) beaconManager.getMonitoredRegions();
        for (Region region : monitoredRegions){
            if (region.getUniqueId().startsWith("super")){
                if (!alsoSuper){
                    continue;
                }
            }
            try {
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        ULog.d(TAG, "onBeacpnServiceConnect()");
        // Since we probably just entered a super region we stop monitoring all normal regions
        resetMonitoring(false);
        // We stop all previous ranging
        resetRanging();
        // and start ranging normal regions
        for (Region region : regionsToRange) {
            try {
                beaconManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop ranging all regions.
     */
    private void resetRanging() {
        List<Region> regions = (List<Region>) beaconManager.getRangedRegions();
        for (Region region : regions) {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        mContext.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return mContext.bindService(intent,serviceConnection,i);
    }

    /**
     * Start monitoring regions.
     * @param regions regions to monitor.
     */
    public void setMonitoring(List<Region> regions) {
        for (Region region : regions) {
            try {
                beaconManager.startMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        if (region.getUniqueId().startsWith("super")){
            // We enterd a super regions so we start ranging for normal regions
            ULog.d(TAG, "enter in superRegion");
            // Multiple calls to this shouldn't be dangerous
            startExpBGRanging();
        } else {
            setInsideState(true);
            beaconManager.setBackgroundMode(false);
            // We entered a normal region
            String regionString = region.getUniqueId();
            ULog.d(TAG, "enter in " + regionString);
            if (!regionsImIn.contains(region)){
                // we add it to the regions we are in, and notify the outside
                regionsImIn.add(region);
                outerNotifier.didEnterRegion(region);
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (region.getUniqueId().startsWith("super")){
            // We exited a super region, so we stop ranging the normal regions
            // TODO what if there are multiple super regions? maintaing a list of super regions we are in brings back all V1 problems
            ULog.d(TAG, "exit from super region");
            // stop ranging
            resetRanging();
            for(int i=regionsImIn.size()-1;i>=0;i--){
                didExitRegion(regionsImIn.get(i));
            }
            regionsImIn.clear();
            setInsideState(false);
            beaconManager.setBackgroundMode(true);
        } else {
            // We exited a normal region
            ULog.d(TAG, "exit from region " + region.getUniqueId());
            // we exit from the region
            // TODO mantain this list too?
            regionsImIn.remove(region);
            try {
                // we stop monitoring the region we just exited
                // we will start monitoring this region once the ranging set the region as near
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            outerNotifier.didExitRegion(region);
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        ULog.d(TAG, "beacons ranged: " + collection.size() + " data: " + region.toString());
        for (org.altbeacon.beacon.Beacon beacon : collection) {
            ULog.d(TAG, "distance: " + beacon.getDistance());
            if (beacon.getDistance() < threshold)
                ULog.d(TAG, "start Monitoring normal region " + region.getUniqueId());
                try {
                    // we are close to the region so we start monitoring
                    beaconManager.startMonitoringBeaconsInRegion(region);
                    // and we immediately trigger the entry
                    beaconManager.getMonitoringNotifier().didEnterRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    }
}
