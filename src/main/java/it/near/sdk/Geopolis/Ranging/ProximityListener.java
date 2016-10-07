package it.near.sdk.Geopolis.Ranging;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    void enterBeaconRange(Beacon beacon, int proximity);

}
