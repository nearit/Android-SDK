package it.near.sdk.Beacons.Ranging;

import android.content.Context;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.near.sdk.Beacons.Ranging.BeaconDynamicRadar;
import it.near.sdk.GlobalState;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 18/03/16.
 */
public class NearRangeNotifier implements RangeNotifier {
    private static final String TAG = "NearRangeNotifier";
    private final Context context;

    public NearRangeNotifier(Context context) {
        this.context = context;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        beacons = filterBeacons(beacons);

        Collections.sort((List<Beacon>) beacons, distanceComparator);
        ULog.d(TAG, "didRangeBeaconsInRegion " + beacons.size() + " region: " + region.toString());
        if (beacons.size() > 0) {
            logList(beacons);
            traceList(beacons);

            BeaconDynamicRadar radar = GlobalState.getInstance(context).getBeaconDynamicRadar();
            if (radar!=null){
                radar.beaconsDiscovered((List<Beacon>) beacons);
            }
            /*Beacon firstBeacon = beacons.iterator().next();
            ULog.d(TAG, "The first beacon I see is about " + firstBeacon.getDistance() + " meters away.");
            ULog.d(TAG, "Major: " + firstBeacon.getId2().toString() + "  Minor: " + firstBeacon.getId3().toString() );*/
        }
    }

    private void traceList(Collection<Beacon> beacons) {
        int i = 0;
        String trace = "Rankings: ";
        for (Beacon beacon : beacons){
            trace = trace.concat("\nPosition: " + i + " Major: " + beacon.getId2().toString() + " Minor: " + beacon.getId3().toString() + " Distance: " + beacon.getDistance());
            i++;
        }
        GlobalState.getInstance(context).getTraceNotifier().trace(trace);
    }


    private void logList(Collection<Beacon> beacons) {
        int i = 0;
        ULog.d("Rankings", "--------------------------------------");
        for (Beacon beacon : beacons){
            ULog.d("Rankings", "Position:" + i + " Major: " + beacon.getId2().toString() + " Minor: " + beacon.getId3().toString() + " Distance: " + beacon.getDistance());
            i++;
        }
    }

    /**
     * Filter beacons based on Near configuration
     *
     * @param beacons
     * @return
     */
    private List<Beacon> filterBeacons(Collection<Beacon> beacons) {
        Configuration configuration = GlobalState.getInstance(context).getConfiguration();
        List<Beacon> appBeacons = new ArrayList<>();
        for (Beacon beacon : beacons){
            /*if (configuration.hasBeacon(beacon)){
                appBeacons.add(beacon);
            }*/

        }
        return appBeacons;
    }

    Comparator<Beacon> distanceComparator = new Comparator<Beacon>() {
        @Override
        public int compare(Beacon lhs, Beacon rhs) {
            return Double.compare(lhs.getDistance(),rhs.getDistance());
        }
    };
}
