package it.near.sdk.Beacons.Monitoring;

import org.altbeacon.beacon.Region;

/**
 * Created by cattaneostefano on 04/04/16.
 */
public interface RegionListener {

    public abstract void enterRegion(Region region);
    public abstract void exitRegion(Region region);
}
