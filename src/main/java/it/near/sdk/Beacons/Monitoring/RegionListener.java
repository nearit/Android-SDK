package it.near.sdk.Beacons.Monitoring;

import org.altbeacon.beacon.Region;

/**
 * @author cattaneostefano
 */
public interface RegionListener {

    public abstract void enterRegion(Region region);
    public abstract void exitRegion(Region region);
}
