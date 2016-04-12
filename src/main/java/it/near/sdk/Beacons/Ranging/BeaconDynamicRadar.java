package it.near.sdk.Beacons.Ranging;

import android.content.Context;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manage and calculate beacon distances from the user device.
 *
 * Created by cattaneostefano on 22/03/16.
 */
public class BeaconDynamicRadar {

    private static final String TAG = "BeaconDynamicRadar";

    private List<BeaconDynamicData> beaconsDistances;
    private BeaconDynamicData currentDynamicBeacon;
    private final double minDifference = 0.5;
    private Context context;
    private ProximityListener proximityListener;

    public BeaconDynamicRadar(Context context, List<NearBeacon> beacons, ProximityListener proximityListener){
        this.context = context;
        this.proximityListener = proximityListener;
        beaconsDistances = new ArrayList<>();

        if (beacons!= null) {
            initBeaconDynamicData(beacons);
        }
    }

    /**
     * For every beacon create beacon dynamic data
     * @param beacons
     */
    public void initBeaconDynamicData(List<NearBeacon> beacons){
        // for every beacon, create a BeaconDynamicData
        for (NearBeacon beacon : beacons){
            BeaconDynamicData dynData = new BeaconDynamicData();
            dynData.setBeaconConfig(beacon);
            beaconsDistances.add(dynData);
        }
    }

    /**
     * Handle a list of beacons being detected. Note that this is called about every second.
     * @param beacons
     */
    public void beaconsDiscovered(List<Beacon> beacons) {
        initializeCycleDistance();

        for (Beacon beacon : beacons){
            // for every beacon we save the new distance
            BeaconDynamicData dynBeacon = findDynamicBeacon(beacon);

            if (dynBeacon!=null){
                dynBeacon.setAltBeacon(beacon);
                dynBeacon.saveDistance(beacon.getDistance());
            }
        }

        // select only beacon for which we stand in their proximity
        ArrayList<BeaconDynamicData> inRangeBeacons = filterInRange();


        BeaconDynamicData closestBeacon = null;
        Collections.sort(inRangeBeacons);
        if (inRangeBeacons.size() > 0){
            closestBeacon = inRangeBeacons.get(0);
        }

        if ( closestBeacon==null && currentDynamicBeacon != null) {
            leaveBeacon(currentDynamicBeacon.getBeaconConfig());
            currentDynamicBeacon = null;
        } else if (currentDynamicBeacon != null
                && closestBeacon != null
                && closestBeacon.getAltBeacon().getId3().toInt() != currentDynamicBeacon.getAltBeacon().getId3().toInt()) {
            double actualDifference = currentDynamicBeacon.getAverage() - closestBeacon.getAverage();
            if (actualDifference > minDifference) {
                leaveBeacon(currentDynamicBeacon.getBeaconConfig());
                currentDynamicBeacon = null;
            }
        }

        if (inRangeBeacons.size() > 0 && currentDynamicBeacon == null) {
            currentDynamicBeacon = closestBeacon;
            enterBeacon(currentDynamicBeacon.getBeaconConfig());
        }

    }

    /**
     * Filter beacons based on us being inside their proximity
     * @return
     */
    private ArrayList<BeaconDynamicData> filterInRange() {
        ArrayList<BeaconDynamicData> inRangeBeacons = new ArrayList<>();
        for (BeaconDynamicData dynBeacon : beaconsDistances) {
            if (dynBeacon.hasMinumumData()) {

                int avgProximityValue = NearBeacon.distanceToProximity(dynBeacon.getAverage()); // trasformo la distanza media in metri in proximity
                int requestedProximityValue = Integer.valueOf(dynBeacon.getBeaconConfig().getRange());
                dynBeacon.setCurrentProximity(avgProximityValue);

                if ( avgProximityValue <= requestedProximityValue )
                    inRangeBeacons.add(dynBeacon);
            }
        }
        return inRangeBeacons;
    }


    /**
     * For every beacon add an empty distance, to take note of the out of range beacons
     */
    private void initializeCycleDistance() {
        for (BeaconDynamicData data : beaconsDistances) {
            data.initializeCycleData();
        }
    }

    BeaconDynamicData findDynamicBeacon(Beacon _beacon) {
        for (BeaconDynamicData data : beaconsDistances) {
            if (_beacon.getId2().toInt() == Integer.valueOf(data.getBeaconConfig().getMajor())
                    && _beacon.getId3().toInt() == Integer.valueOf(data.getBeaconConfig().getMinor())
                    && _beacon.getId1().toString().equals(data.getBeaconConfig().getProximity_uuid()))
                return data;
        }
        return null;
    }

    private void enterBeacon(NearBeacon beacon){
        proximityListener.enterBeaconRange(beacon);
    }

    private void leaveBeacon(NearBeacon beacon){
        proximityListener.exitBeaconRange(beacon);
    }

    public ProximityListener getProximityListener() {
        return proximityListener;
    }

    public void setProximityListener(ProximityListener proximityListener) {
        this.proximityListener = proximityListener;
    }
}
