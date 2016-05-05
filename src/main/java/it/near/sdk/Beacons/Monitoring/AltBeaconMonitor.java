package it.near.sdk.Beacons.Monitoring;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    private final BeaconManager beaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    private Context mContext;
    private RegionBootstrap regionBootstrap;
    private List<Region> regionsToRange = new ArrayList<>();
    private List<Region> regionsToMonitor;
    private BootstrapNotifier outerNotifier;
    private float threshold = DEFAULT_THRESHOLD;

    public AltBeaconMonitor(Context context) {
        this.mContext = context;

        beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
        beaconManager.getBeaconParsers().clear();
        // set beacon layout for iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        // TODO turn back off
        BeaconManager.setDebug(true);

        //backgroundPowerSaver = new BackgroundPowerSaver(context.getApplicationContext());
    }

    /**
     * Start monitoring on the given regions and sets the notifier object to be notified on region enter and exit.
     * When doing this, we stop monitoring on the region we were previously monitoring and we set the given notifier
     * as the only notifier. The notifier will be called even after app termination and device restart, as soon as
     * a registered region is scanned.
     *  @param backBetweenPeriod period between scans in milliseconds
     * @param backScanPeriod scan length in milliseconds
     * @param regionExitPeriod milliseconds to wait before confirming region exit
     * @param regions list of regions to monitor
     */
    public void startRadar(long backBetweenPeriod, long backScanPeriod, long regionExitPeriod, float threshold, List<Region> superRegions, List<Region> regions, BootstrapNotifier outerNotifier){
        this.outerNotifier = outerNotifier;
        if (threshold != 0) this.threshold = threshold;
        resetMonitoring();
        setMonitoring(superRegions);
        regionsToRange = regions;
        beaconManager.setBackgroundBetweenScanPeriod(backBetweenPeriod);
        beaconManager.setBackgroundScanPeriod(backScanPeriod);
        beaconManager.setForegroundBetweenScanPeriod(backBetweenPeriod);
        beaconManager.setForegroundScanPeriod(backScanPeriod);
        beaconManager.setRegionExitPeriod(regionExitPeriod);
        regionBootstrap = new RegionBootstrap(this, regions);
    }

    public void startExpBGRanging(){
        resetRanging();
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
     */
    private void resetMonitoring() {
        ArrayList<Region> monitoredRegions = (ArrayList<Region>) beaconManager.getMonitoredRegions();
        for (Region region : monitoredRegions){
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
        resetRanging();
        resetMonitoring();
        for (Region region : regionsToRange) {
            try {
                beaconManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

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
            ULog.d(TAG, "enter in superRegion");
            startExpBGRanging();
        } else {
            String regionString = region.getUniqueId();
            ULog.d(TAG, "enter in " + regionString);
            outerNotifier.didEnterRegion(region);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (region.getUniqueId().startsWith("super")){
            resetRanging();
        } else {
            try {
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
                try {
                    beaconManager.startMonitoringBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    }
}
