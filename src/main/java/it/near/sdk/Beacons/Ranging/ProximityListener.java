package it.near.sdk.Beacons.Ranging;

import it.near.sdk.Models.NearBeacon;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    void enterBeaconRange(NearBeacon beacon);
    void exitBeaconRange(NearBeacon beacon);

}
