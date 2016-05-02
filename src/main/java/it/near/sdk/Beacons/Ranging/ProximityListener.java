package it.near.sdk.Beacons.Ranging;

import it.near.sdk.Models.NearBeacon;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    public abstract void enterBeaconRange(NearBeacon beacon);
    public abstract void exitBeaconRange(NearBeacon beacon);

}
