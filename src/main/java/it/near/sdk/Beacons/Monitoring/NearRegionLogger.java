package it.near.sdk.Beacons.Monitoring;

import org.altbeacon.beacon.Region;

import java.util.ArrayList;

/**
 * Created by cattaneostefano on 06/04/16.
 */
public interface NearRegionLogger {

    public abstract void log(String logString, ArrayList<Region> insideRegions);

}
