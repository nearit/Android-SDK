package it.near.sdk.Beacons.Ranging;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import it.near.sdk.Models.NearBeacon;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    void enterBeaconRange(Beacon beacon, int proximity);
    void exitBeaconRange(Beacon beacon, int proximity);

    void enterRegion(Region region);
    void exitRegion(Region region);

}
