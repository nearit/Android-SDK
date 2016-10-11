package it.near.sdk.Geopolis.Beacons.Ranging;

import android.content.Context;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Geopolis.Beacons.BeaconNode;
import it.near.sdk.Models.NearBeacon;
import it.near.sdk.Utils.ULog;

/**
 * Manage and calculate beacon distances from the user device.
 *
 * @author cattaneostefano
 */
public class BeaconDynamicRadar {

    private static final String TAG = "BeaconDynamicRadar";
    private List<BeaconDynamicData> beaconsDistances;
    private BeaconDynamicData currentDynamicBeacon;
    private final double minDifference = 0.5;
    private Context context;

    public BeaconDynamicRadar(Context context, List<BeaconNode> beacons){
        this.context = context;
        beaconsDistances = new ArrayList<>();

        if (beacons!= null) {
            initBeaconDynamicData(beacons);
        }
    }

    /**
     * For every beacon create beacon dynamic data
     * @param beacons
     */
    public void initBeaconDynamicData(List<BeaconNode> beacons){
        // for every beacon, create a BeaconDynamicData
        for (BeaconNode beacon : beacons){
            BeaconDynamicData dynData = new BeaconDynamicData(context);
            dynData.setBeaconNode(beacon);
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
                dynBeacon.saveDistance(beacon.getDistance());
            }
        }

        // select only beacon for which we stand in their proximity
        /*ArrayList<BeaconDynamicData> inRangeBeacons = filterInRange();

        BeaconDynamicData closestBeacon = null;
        Collections.sort(inRangeBeacons);
        if (inRangeBeacons.size() > 0){
            closestBeacon = inRangeBeacons.get(0);
        }

        if ( closestBeacon==null && currentDynamicBeacon != null) {
            // leaveBeacon(currentDynamicBeacon.getBeaconConfig());
            currentDynamicBeacon = null;
        } else if (currentDynamicBeacon != null
                && closestBeacon != null
                && closestBeacon.getAltBeacon().getId3().toInt() != currentDynamicBeacon.getAltBeacon().getId3().toInt()) {
            double actualDifference = currentDynamicBeacon.getAverage() - closestBeacon.getAverage();
            if (actualDifference > minDifference) {
                // leaveBeacon(currentDynamicBeacon.getBeaconConfig());
                currentDynamicBeacon = null;
            }
        }

        if (inRangeBeacons.size() > 0 && currentDynamicBeacon == null) {
            currentDynamicBeacon = closestBeacon;
            // enterBeacon(currentDynamicBeacon.getBeaconConfig());
        }

        logInRangeBeacons(inRangeBeacons);*/

    }

    private void logInRangeBeacons(ArrayList<BeaconDynamicData> inRangeBeacons) {
        for (BeaconDynamicData inRangeBeacon : inRangeBeacons) {
            ULog.d(TAG, inRangeBeacon.toString());
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
                // int requestedProximityValue = Integer.valueOf(dynBeacon.getBeaconConfig().getRange());
                dynBeacon.setCurrentProximity(avgProximityValue);
                if (dynBeacon.getAverage() <= 2){
                    inRangeBeacons.add(dynBeacon);
                }

//                if ( avgProximityValue <= requestedProximityValue )
//                    inRangeBeacons.add(dynBeacon);
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
            if (_beacon.getId2().toInt() == data.getBeaconNode().getMajor()
                    && _beacon.getId3().toInt() == data.getBeaconNode().getMinor()
                    && _beacon.getId1().toString().equals(data.getBeaconNode().getProximityUUID()))
                return data;
        }
        return null;
    }

}
