package it.near.sdk.Beacons.BeaconForest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.Beacons.Ranging.BeaconDynamicRadar;
import it.near.sdk.Beacons.Ranging.ProximityListener;
import it.near.sdk.GlobalConfig;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.OnLifecycleEventListener;
import it.near.sdk.Utils.ULog;

/**
 * Monitor for AltBeacon regions. It sets the format of the bluetooth package and holds the background powersaver.
 *
 * @author cattaneostefano.
 */
public class AltBeaconMonitor extends OnLifecycleEventListener implements BeaconConsumer, BootstrapNotifier, RangeNotifier {

    private static final String TAG = "AltBeaconMonitor";
    private static final float DEFAULT_THRESHOLD = 0.5f;
    private static final String INSIDE_STATE = "inside_state";
    private static final long BACKGROUND_BETWEEN_SCAN_PERIODS = 30000;
    private static final long BACKGROUND_SCAN_PERIOD = 1500;
    private static final long FOREGROUND_SCAN_PERIOD = 3000;
    private static final long REGION_EXIT_PERIOD = 30000;
    private final BeaconManager beaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    private Application mApplication;
    private RegionBootstrap regionBootstrap;
    private List<Region> regionsToRange = new ArrayList<>();
    private List<Region> regionsImIn = new ArrayList<>();
    private BootstrapNotifier outerNotifier;
    private float threshold = DEFAULT_THRESHOLD;
    private boolean areWeInside;
    private String prefsNameSuffix = "AltMonitor";
    private SharedPreferences sp;
    private List<Region> regions;
    private ProximityListener proximityListener;
    private Map<Region, BeaconDynamicRadar> rangingRadars;

    public AltBeaconMonitor(Application application) {
        this.mApplication = application;
        this.rangingRadars = new HashMap<>();

        initAppLifecycleMonitor(application);

        beaconManager = BeaconManager.getInstanceForApplication(application.getApplicationContext());
        beaconManager.getBeaconParsers().clear();
        // set beacon layout for iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        BeaconManager.setDebug(true);

        String PACK_NAME = application.getApplicationContext().getPackageName();
        String PREFS_NAME = PACK_NAME + prefsNameSuffix;
        sp = application.getSharedPreferences(PREFS_NAME, 0);

    }


    public void startRadar(List<Region> regions, ProximityListener proximityListener){

        beaconManager.setBackgroundBetweenScanPeriod(BACKGROUND_BETWEEN_SCAN_PERIODS);
        beaconManager.setBackgroundScanPeriod(BACKGROUND_SCAN_PERIOD);
        beaconManager.setForegroundScanPeriod(FOREGROUND_SCAN_PERIOD);
        BeaconManager.setRegionExitPeriod(REGION_EXIT_PERIOD);

        beaconManager.setBackgroundMode(true);

        this.regions = regions;
        this.proximityListener = proximityListener;
        resetRanging();
        resetMonitoring();
        regionBootstrap = new RegionBootstrap(this, regions);

    }

    public void stopRadar(){
        regions.clear();
        beaconManager.setBackgroundMode(true);
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
        resetRanging();
        resetMonitoring();
        regionBootstrap.disable();
    }

    /**
     * Initialize app lifecycle monitor to detect the app going to the background/foreground
     * @param application
     */
    private void initAppLifecycleMonitor(Application application) {
        new AppLifecycleMonitor(application, this);
    }

    public void addRegions(List<Region> regions){
        for (Region region : regions) {
            addRegion(region);
        }
    }

    public void addRegion(Region region){
        regionBootstrap.addRegion(region);
    }

    public void removeRegions(List<Region> regions){
        for (Region region : regions) {
            removeRegion(region);
        }
    }

    public void removeRegion(Region region) {
        regionBootstrap.removeRegion(region);
    }

    /**
     * Switch to ranging mode
     */
    private void startRanging() {
        beaconManager.setBackgroundMode(false);
        beaconManager.addRangeNotifier(this);
    }

    /**
     * Stop ranging mode
     */
    private void stopRanging() {
        beaconManager.setBackgroundMode(true);
        beaconManager.removeRangeNotifier(this);
    }

    @Override
    public void onBeaconServiceConnect() {

    }

    @Override
    public Context getApplicationContext() {
        return mApplication.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        mApplication.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return mApplication.bindService(intent,serviceConnection,i);
    }

    private void resetRanging() {
        for (Region region : beaconManager.getRangedRegions()) {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetMonitoring() {
        for (Region region : beaconManager.getMonitoredRegions()) {
            try {
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onForeground() {
        ULog.wtf(TAG, "onForeground");
        // When going to the foreground, if we have regions to range, start ranging
        if (beaconManager.getRangedRegions().size() > 0) {
            startRanging();
        }
    }

    @Override
    public void onBackground() {
        ULog.wtf(TAG, "onBackground");
        // Console.clear();
        // When going to the background stop ranging, in an idempotent way (we might haven't been ranging)
        stopRanging();
    }

    @Override
    public void didEnterRegion(Region region) {
        String msg = "enter region: " + region.toString();
        ULog.wtf(TAG, msg);

        logRangedRegions();
        // nearit trigger
        proximityListener.enterRegion(region);

    }

    @Override
    public void didExitRegion(Region region) {
        String msg = "exit region: " + region.toString();
        ULog.wtf(TAG, msg);

        logRangedRegions();
        if (beaconManager.getRangedRegions().size() == 0){
            // if the list of ranged regions is empty, we stop ranging
            stopRanging();
        }

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

        // This is called both on region entry/exit and when the the monitor gets rebooted to re-inforce in which regions we are in and out
        // so we don't want to trigger and track a recipe here, but we still handle the region ranging in this callback.
        // basically, idempotent logic lives here

        ULog.wtf(TAG, "determine state " + i + " for region: " + region.toString());

        try {
            if (i == MonitorNotifier.INSIDE){
                // region enter
                startRangingRegion(region);
                if (AppLifecycleMonitor.isApplicationInForeground()){
                    // switch to ranging mode only if we are in foreground
                    startRanging();
                }
            } else {
                // region exit
                stopRangingRegion(region);
                beaconManager.stopRangingBeaconsInRegion(region);
                if (beaconManager.getRangedRegions().size() == 0){
                    // if the list of ranged regions is empty, we stop ranging
                    stopRanging();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ULog.wtf(TAG, "regions ranged: " + beaconManager.getRangedRegions().size());
        logRangedRegions();

    }


    private void startRangingRegion(Region region) throws RemoteException {
        rangingRadars.put(region, new BeaconDynamicRadar(mApplication, rangingBeaconsFor(region), proximityListener));
        beaconManager.startRangingBeaconsInRegion(region);
    }

    private List<Beacon> rangingBeaconsFor(Region region) {
        // TODO obviously fake implementation
        List<Beacon> rangingBeacons = new ArrayList<>();
        for (int i = 1 ; i <= 4 ; i++) {
            rangingBeacons.add(new Beacon.Builder()
                    .setId1(region.getId1().toString())
                    .setId2(region.getId2().toString())
                    .setId3(String.valueOf(i))
                    .build());
        }
        return rangingBeacons;
    }

    private void stopRangingRegion(Region region) throws RemoteException {
        rangingRadars.remove(region);
        beaconManager.stopRangingBeaconsInRegion(region);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        String msg = "For region: " + region.getUniqueId() + " found " + collection.size() + " beacons. Distance: " + (collection.iterator().hasNext() ? collection.iterator().next().getDistance() : "none");
        ULog.wtf(TAG, msg);

        BeaconDynamicRadar radar = rangingRadars.get(region);
        radar.beaconsDiscovered((List<Beacon>) collection);

    }

    private void logRangedRegions() {
        String msg1 = "regions ranged: " + beaconManager.getRangedRegions().size();
        ULog.wtf(TAG, msg1);
    }
}
