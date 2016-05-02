package it.near.sdk.Beacons;

import android.content.Context;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;

import it.near.sdk.R;

/**
 * Craft a list of region from the test_beacon resource file
 * @author cattaneostefano
 */
public class TestRegionCrafter {

    public static ArrayList<Region> getTestRegions(Context context){

        /*ArrayList<Region> testRegions = new ArrayList<>();
        testRegions.add(new Region("Region" , Identifier.parse("ACFD065E-C3C0-11E3-9BBE-1A514932AC01"), null, null));
        return testRegions;*/

        ArrayList<Region> testRegions = new ArrayList<>();
        String[] unparsedStrings = context.getResources().getStringArray(R.array.test_beacons);
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(unparsedStrings));
        for (String beaconString : list){
            String[] identifiers = beaconString.split(";");
            Region testRegion;
            int major = Integer.parseInt(identifiers[1]);
            int minor = Integer.parseInt(identifiers[2]);
            String uniqueId = "Region" + Integer.toString(major) + Integer.toString(minor);
            testRegion = new Region(uniqueId, Identifier.parse(identifiers[0]), Identifier.fromInt(major), Identifier.fromInt(minor) );
            testRegions.add(testRegion);
        }
        return testRegions;
    }
}
