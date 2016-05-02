package it.near.sdk.Beacons.Monitoring;

import android.content.Context;
import android.os.RemoteException;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Beacons.BeaconForest.Beacon;
import it.near.sdk.Beacons.TestRegionCrafter;
import it.near.sdk.GlobalState;
import it.near.sdk.Utils.ULog;

/**
 * Monitor for AltBeacon regions. It sets the format of the bluetooth package and holds the background powersaver.
 *
 * @author cattaneostefano.
 */
public class AltBeaconMonitor {

    private static final String TAG = "AltBeaconMonitor";
    private final BeaconManager beaconManager;
    private final BackgroundPowerSaver backgroundPowerSaver;
    private Context mContext;
    private RegionBootstrap regionBootstrap;

    public AltBeaconMonitor(Context context) {
        this.mContext = context;

        beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
        beaconManager.getBeaconParsers().clear();
        // set beacon layout for iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        // TODO turn back off
        BeaconManager.setDebug(true);

        backgroundPowerSaver = new BackgroundPowerSaver(context.getApplicationContext());
    }

    /**
     * Start monitoring on the given regions and sets the notifier object to be notified on region enter and exit.
     * When doing this, we stop monitoring on the region we were previously monitoring and we set the given notifier
     * as the only notifier. The notifier will be called even after app termination and device restart, as soon as
     * a registered region is scanned.
     *
     * @param backBetweenPeriod period between scans in milliseconds
     * @param backScanPeriod scan length in milliseconds
     * @param regionExitPeriod milliseconds to wait before confirming region exit
     * @param regions list of regions to monitor
     * @param notifier background region notifier
     */
    public void startRadar(long backBetweenPeriod, long backScanPeriod, long regionExitPeriod, List<Region> regions, BootstrapNotifier notifier){
        resetMonitoring();

        beaconManager.setBackgroundBetweenScanPeriod(backBetweenPeriod);
        beaconManager.setBackgroundScanPeriod(backScanPeriod);
        beaconManager.setRegionExitPeriod(regionExitPeriod);
        regionBootstrap = new RegionBootstrap(notifier, regions);
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

}
