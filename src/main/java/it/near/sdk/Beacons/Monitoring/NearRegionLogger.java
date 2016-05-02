package it.near.sdk.Beacons.Monitoring;

import org.altbeacon.beacon.Region;

import java.util.ArrayList;

/**
 * @author cattaneostefano
 */
public interface NearRegionLogger {

    public abstract void log(String logString, ArrayList<Region> insideRegions);

}
