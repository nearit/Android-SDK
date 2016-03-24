package it.near.sdk.Beacons;

import android.content.Context;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.near.sdk.GlobalState;
import it.near.sdk.Models.NearBeacon;

/**
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

    public void initBeaconDynamicData(List<NearBeacon> beacons){
        // for every beacon, create a BeaconDynamicData
        for (NearBeacon beacon : beacons){
            BeaconDynamicData dynData = new BeaconDynamicData();
            dynData.setBeaconConfig(beacon);
            beaconsDistances.add(dynData);
        }
    }

    public void beaconsDiscovered(List<Beacon> beacons) {
        initializeCycleDistance();

        for (Beacon beacon : beacons){
            BeaconDynamicData dynBeacon = findDynamicBeacon(beacon);

            if (dynBeacon!=null){
                dynBeacon.setAltBeacon(beacon);
                dynBeacon.saveDistance(beacon.getDistance());
            }
        }

        List<BeaconDynamicData> inRangeBeacons = new ArrayList<>();
        for (BeaconDynamicData dynBeacon : beaconsDistances) {
            if (dynBeacon.hasMinumumData()) {

                int avgProximityValue = NearBeacon.distanceToProximity(dynBeacon.getAverage()); // trasformo la distanza media in metri in proximity
                int requestedProximityValue = Integer.valueOf(dynBeacon.getBeaconConfig().getRange());
                dynBeacon.setCurrentProximity(avgProximityValue);

                if ( avgProximityValue <= requestedProximityValue )
                    inRangeBeacons.add(dynBeacon);
            }
        }

        BeaconDynamicData selectedBeacon = null;
        Collections.sort(inRangeBeacons);
        if (inRangeBeacons.size() > 0){
            selectedBeacon = inRangeBeacons.get(0);
        }

        if ( selectedBeacon==null && currentDynamicBeacon != null) {
            leaveBeacon(currentDynamicBeacon.getBeaconConfig());
            currentDynamicBeacon = null;
        } else if (currentDynamicBeacon != null
                && selectedBeacon != null
                && selectedBeacon.getAltBeacon().getId3().toInt() != currentDynamicBeacon.getAltBeacon().getId3().toInt()) {
            double actualDifference = currentDynamicBeacon.getAverage() - selectedBeacon.getAverage();
            if (actualDifference > minDifference) {
                leaveBeacon(currentDynamicBeacon.getBeaconConfig());
                currentDynamicBeacon = null;
            }
        }

        if (inRangeBeacons.size() > 0 && currentDynamicBeacon == null) {
            currentDynamicBeacon = selectedBeacon;
            enterBeacon(currentDynamicBeacon.getBeaconConfig());
        }

    }

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
