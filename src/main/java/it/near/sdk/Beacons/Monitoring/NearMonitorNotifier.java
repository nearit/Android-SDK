package it.near.sdk.Beacons.Monitoring;

import android.content.Context;

import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano
 */
public class NearMonitorNotifier implements MonitorNotifier, BootstrapNotifier {
    private static final String TAG = "NearMonitorNotifier";
    private final Context mContext;
    private RegionListener regionListener;

    public NearMonitorNotifier(Context context, RegionListener regionListener) {
        this.mContext = context;
        this.regionListener = regionListener;
    }

    @Override
    public void didEnterRegion(Region region) {
        ULog.d(TAG , "didEnterRegion: " + region.toString());
        regionListener.enterRegion(region);
    }

    @Override
    public void didExitRegion(Region region) {
        ULog.d(TAG , "didExitRegion: " + region.toString());
        regionListener.exitRegion(region);
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        ULog.d(TAG , "didDetermineStateForRegion: " + region.toString());
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }
}