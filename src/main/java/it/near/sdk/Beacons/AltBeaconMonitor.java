package it.near.sdk.Beacons;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;

import it.near.sdk.Models.Content;
import it.near.sdk.R;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 05/04/16.
 */
public class AltBeaconMonitor implements BootstrapNotifier{

    private static final String TAG = "AltBeaconMonitor";
    private final BeaconManager beaconManager;
    Context mContext;
    private RegionBootstrap regionBootstrap;
    private NearRegionLogger nearRegionLogger;
    private ArrayList<Region> insideRegions;

    public AltBeaconMonitor(Context context) {
        this.mContext = context;

        ULog.d(TAG, "Constructor called");
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        resetMonitoring();
        insideRegions = new ArrayList<>();

        ArrayList<Region> testRegions = TestRegionCrafter.getTestRegions(mContext);

//        Region emitterRegion = new Region("Region", Identifier.parse("ACFD065E-C3C0-11E3-9BBE-1A514932AC01"), Identifier.fromInt(2001),
//                                            Identifier.fromInt(10));
//        Region kontaktRegion = new Region("Kontakt", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(452), null);
//        testRegions.add(emitterRegion);
//        testRegions.add(kontaktRegion);
        regionBootstrap = new RegionBootstrap(this, testRegions);

    }

    public void setLogger(NearRegionLogger logger){
        this.nearRegionLogger = logger;
    }

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
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void didEnterRegion(Region region) {
        String msg = "didEnterRegion: " + region.toString();
        ULog.d(TAG , msg);
        insideRegions.add(region);
        safeLog(msg, insideRegions);

    }

    @Override
    public void didExitRegion(Region region) {
        String msg = "didExitRegion: " + region.toString();
        ULog.d(TAG, msg);
        insideRegions.remove(region);
        safeLog(msg, insideRegions);
    }

    private void safeLog(String msg, ArrayList<Region> insideRegions) {
        if (nearRegionLogger != null){
            nearRegionLogger.log(msg, insideRegions);
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        ULog.d(TAG, "didDetermineStateForRegion");
    }
}
