package it.near.sdk.Models;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 17/03/16.
 */
public class Configuration {

    private static final String TAG = "Configuration";
    List<Matching> matchingList;
    List<Beacon> beaconList;

    public List<Matching> getMatchingList() {
        return matchingList;
    }

    public void setMatchingList(List<Matching> matchingList) {
        this.matchingList = matchingList;
    }

    public List<Beacon> getBeaconList() {
        return beaconList;
    }

    public void setBeaconList(List<Beacon> beaconList) {
        this.beaconList = beaconList;
    }

    public void addBeacon(Beacon beacon) {
        if (beaconList == null) {
            beaconList = new ArrayList<Beacon>();
        }
        beaconList.add(beacon);
    }

    public boolean hasBeacon(org.altbeacon.beacon.Beacon beacon) {
        if ( beaconList == null || beaconList.size()==0 ){
            return false;
        }
        for (Beacon appBeacon : beaconList){
            if (appBeacon.isLike(beacon))
                return true;
        }
        return false;
    }
}
