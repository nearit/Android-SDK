package it.near.sdk.Beacons.Ranging;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

/**
 * @author cattaneostefano
 */
public class BeaconDynamicData implements Comparable<BeaconDynamicData>{

    private ArrayList<Double> distances;
    private double average;
    // private NearBeacon beaconConfig;
    private Beacon altBeacon;

    private int currentProximity;

    public int getCurrentProximity() {
        return currentProximity;
    }

    public void setCurrentProximity(int currentProximity) {
        this.currentProximity = currentProximity;
    }

    public BeaconDynamicData() {
        distances = new ArrayList<Double>();
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

//    public NearBeacon getBeaconConfig() {
//        return beaconConfig;
//    }
//
//    public void setBeaconConfig(NearBeacon beaconConfig) {
//        this.beaconConfig = beaconConfig;
//    }

    public Beacon getAltBeacon() {
        return altBeacon;
    }

    public void setAltBeacon(Beacon altBeacon) {
        this.altBeacon = altBeacon;
    }

    public void initializeCycleData() {

        distances.add((double) -1);
        if (distances.size() > 4)
            distances.remove(0);
    }


    public void saveDistance(double _distance) {

        if (_distance>0) {
            distances.remove(distances.size()-1);
            distances.add(_distance);
        }

        average = makeAverage();
    }

    private double makeAverage() {
        int numberOfValid = 0;
        for (double dist : distances) {
            if(dist >= 0)
                numberOfValid++;
        }

        double average = 0;
        if (numberOfValid != 0) {
            for(double dist : distances) {
                if (dist >= 0)
                    average += dist / numberOfValid;
            }
        }

        return average;
    }

    public boolean hasMinumumData() {
        int numberOfValid = 0;
        for (double dist : distances) {
            if (dist >= 0)
                numberOfValid++;
        }

        return numberOfValid >= 2;

    }


    @Override
    public int compareTo(BeaconDynamicData that) {
        double diff = this.getAverage() - that.getAverage();

        if (diff<0)
            return -1;
        else if (diff>0)
            return 1;
        else
            return 0;
    }
}
