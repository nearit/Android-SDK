package it.near.sdk.Beacons.Ranging;

import it.near.sdk.Models.NearBeacon;

/**
 * Created by cattaneostefano on 23/03/16.
 */
public interface ProximityListener {

    public abstract void enterBeaconRange(NearBeacon beacon);
    public abstract void exitBeaconRange(NearBeacon beacon);

}
