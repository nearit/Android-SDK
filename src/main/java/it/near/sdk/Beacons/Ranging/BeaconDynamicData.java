package it.near.sdk.Beacons.Ranging;

import android.content.Context;
import android.content.Intent;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author cattaneostefano
 */
public class BeaconDynamicData implements Comparable<BeaconDynamicData> {

    public static final int INDETERMINED = 0;
    public static final int IMMEDIATE = 1;
    public static final int NEAR = 2;
    public static final int FAR = 3;
    public static final String NEW_PROXIMITY_EVENT = "new_proximity_event";
    public static final String PROXIMITY = "proximity";
    private final Context mContext;
    private final ProximityListener proximityListener;

    private ArrayList<Integer> proximityValues;
    private double average;
    // private NearBeacon beaconConfig;
    private Beacon altBeacon;

    private int currentProximity = 0;

    public int getCurrentProximity() {
        return currentProximity;
    }

    public void setCurrentProximity(int newProximity) {
        if (currentProximity != newProximity){
            notifiyEvent(newProximity);
            if (currentProximity == INDETERMINED || newProximity < currentProximity){
                proximityListener.enterBeaconRange(getAltBeacon(), newProximity);
            }
        }
        /*if (currentProximity < newProximity) {
            notifiyEvent(newProximity);
        }*/
        currentProximity = newProximity;
    }

    private void notifiyEvent(int newProximity) {
        // TODO notifiy, track and trigger
        // Console.writeLine("----------- NUOVO STATO: " + newProximity);
        Intent intent = new Intent();
        intent.setAction(NEW_PROXIMITY_EVENT);
        intent.putExtra(PROXIMITY, newProximity);
        mContext.sendBroadcast(intent);
    }

    public BeaconDynamicData(Context context, ProximityListener proximityListener) {
        mContext = context;
        this.proximityListener = proximityListener;
        proximityValues = new ArrayList<>();
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }


    public Beacon getAltBeacon() {
        return altBeacon;
    }

    public void setAltBeacon(Beacon altBeacon) {
        this.altBeacon = altBeacon;
    }

    public void initializeCycleData() {

        proximityValues.add(INDETERMINED);
        if (proximityValues.size() > 4)
            proximityValues.remove(0);
    }


    public void saveDistance(double _distance) {

        int proximity = distanceToProximity(_distance);

        if (_distance>0) {
            proximityValues.remove(proximityValues.size()-1);
            proximityValues.add(proximity);
        }

        computeProximity();
    }

    private void computeProximity() {
        int numberOfValid = 0;
        for (Integer proximityValue : proximityValues) {
            if (proximityValue != INDETERMINED)
                numberOfValid++;
        }

        if (numberOfValid != 0) {
            HashMap<Integer, Integer> scoreboard = buildScoreboard(proximityValues);
            if (scoreboard.get(FAR) >= 3){
                setCurrentProximity(FAR);
            } else if (scoreboard.get(NEAR) >= 3){
                setCurrentProximity(NEAR);
            } else if (scoreboard.get(IMMEDIATE) >= 3){
                setCurrentProximity(IMMEDIATE);
            }
        }
    }

    private HashMap<Integer, Integer> buildScoreboard(ArrayList<Integer> proximityValues) {
        HashMap<Integer, Integer> scoreboard = new HashMap<>();
        scoreboard.put(FAR, 0);
        scoreboard.put(NEAR, 0);
        scoreboard.put(IMMEDIATE, 0);
        for(int dist : proximityValues) {
            if (dist != INDETERMINED){
                scoreboard.put(dist, scoreboard.get(dist) + 1);
            }
        }
        return scoreboard;
    }

    public boolean hasMinumumData() {
        int numberOfValid = 0;
        for (Integer proximityValue : proximityValues) {
            if (proximityValue != INDETERMINED)
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

    public static int distanceToProximity(double distance) {
        if (distance<=0)
            // negative distance, FAR
            return INDETERMINED;

        else if (distance<=0.3)
            // IMMEDIATE
            return IMMEDIATE;

        else if (distance<=1.5)
            // NEAR
            return NEAR;

        else if (distance>1.5)
            // FAR
            return FAR;

        return INDETERMINED;

    }


}
