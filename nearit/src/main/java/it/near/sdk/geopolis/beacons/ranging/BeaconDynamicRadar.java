package it.near.sdk.geopolis.beacons.ranging;

import android.content.Context;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.geopolis.beacons.BeaconNode;

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

    public BeaconDynamicRadar(Context context, List<BeaconNode> beacons) {
        this.context = context;
        beaconsDistances = new ArrayList<>();

        if (beacons != null) {
            initBeaconDynamicData(beacons);
        }
    }

    /**
     * For every beacon create beacon dynamic data
     * @param beacons
     */
    public void initBeaconDynamicData(List<BeaconNode> beacons) {
        // for every beacon, create a BeaconDynamicData
        for (BeaconNode beacon : beacons) {
            BeaconDynamicData dynData = new BeaconDynamicData(context);
            dynData.setBeaconNode(beacon);
            beaconsDistances.add(dynData);
        }
    }

    /**
     * Handle a list of beacons being detected. Note that this is called about every second.
     *
     * @param beacons
     */
    public void beaconsDiscovered(List<Beacon> beacons) {
        initializeCycleDistance();

        for (Beacon beacon : beacons) {
            // for every beacon we save the new distance
            BeaconDynamicData dynBeacon = findDynamicBeacon(beacon);

            if (dynBeacon != null) {
                dynBeacon.saveDistance(beacon.getDistance());
            }
        }
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
            if (_beacon.getId2().toInt() == data.getBeaconNode().major
                    && _beacon.getId3().toInt() == data.getBeaconNode().minor
                    && _beacon.getId1().toString().equals(data.getBeaconNode().proximityUUID))
                return data;
        }
        return null;
    }

    public void resetData() {
        for (BeaconDynamicData beaconsDistance : beaconsDistances) {
            beaconsDistance.resetData();
        }
    }
}
