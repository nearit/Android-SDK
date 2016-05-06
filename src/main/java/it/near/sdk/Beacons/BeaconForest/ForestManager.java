package it.near.sdk.Beacons.BeaconForest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Beacons.Monitoring.AltBeaconMonitor;
import it.near.sdk.Beacons.Ranging.BeaconDynamicData;
import it.near.sdk.Beacons.Ranging.BeaconDynamicRadar;
import it.near.sdk.Beacons.Ranging.NearRangeNotifier;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.Communication.NearNetworkUtil;
import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.Models.NearBeacon;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * Manages a beacon forest, the plugin for monitoring regions structured in a tree.
 * This region structure was made up to enable background monitoring of more than 20 regions on iOS.
 * In this plugin every region is specified by ProximityUUID, minor and major. It the current implementation every region is a beacon.
 * The AltBeacon monitor is initialized with this setting:
 * - The background period between scans is 8 seconds.
 * - The background length of a scan 1 second.
 * - The period to wait before finalizing a region exit.
 *
 * In our current plugin representation:
 * - this is a "pulse" plugin
 * - the ingredient name (the name of the plugin) is: beacon-forest
 * - the only supported flavor is: enter_region
 * - the slice is the id of a region
 *
 * @author cattaneostefano
 */
public class ForestManager implements BootstrapNotifier {

    private static final String TAG = "ForestManager";
    private static final String PREFS_SUFFIX = "NearBeacons";
    private static final String INGREDIENT_NAME = "beacon-forest";
    private static final String ENTER_REGION = "enter_region";

    private static ForestManager mInstance = null;
    private final RecipesManager recipesManager;
    private final String PREFS_NAME;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    private static final long backgroundBetweenScanPeriod = 10000l;
    private static final long backgroundScanPeriod = 1500l;
    private static final long regionExitPeriod = 30000l;

    private List<Beacon> beaconList;
    private Context mContext;
    private Morpheus morpheus;
    private AltBeaconMonitor monitor;
    private NearRangeNotifier rangeNotifier;

    /**
     * Constructor.
     *
     * @param context App context.
     * @param monitor SDK beacon monitor.
     * @param recipesManager SDK global RECIPES_PATH manager.
     */
    public ForestManager(Context context, AltBeaconMonitor monitor, RecipesManager recipesManager) {
        this.mContext = context;
        this.monitor = monitor;
        this.recipesManager = recipesManager;
        rangeNotifier = new NearRangeNotifier(context);
        setUpMorpheusParser();

        String PACK_NAME = context.getApplicationContext().getPackageName();
        PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = context.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();

        try {
            testObject = new JSONObject(test);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshConfig();
    }
 

    /**
     * Set up Morpheus parser. Morpheus parses jsonApi encoded resources
     * https://github.com/xamoom/Morpheus
     * We didn't actually use this library due to its minSdkVersion. We instead imported its code and adapted it. And then fixed it.
     */
    private void setUpMorpheusParser() {
        morpheus = new Morpheus();
        // register your resources

        morpheus.getFactory().getDeserializer().registerResourceClass("beacons", Beacon.class);
    }

    /**
     * Refresh the configuration of the component. The list of beacons to monitor will be downloaded from the APIs.
     * If there's an error in refreshing the configuration, a cached version will be used instead.
     *
     */
    public void refreshConfig(){
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, Constants.API.PLUGINS.BEACON_FOREST_BEACONS, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    ULog.d(TAG, response.toString());
                    List<Beacon> beacons = NearUtils.parseList(morpheus, response, Beacon.class);
                    beaconList = parseTree(beacons);
                    startRadarOnBeacons(beaconList);
                    persistList(beaconList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "Error " + error);
                    try {
                        beaconList = loadChachedList();
                        if (beaconList!=null){
                            startRadarOnBeacons(beaconList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }));

    }

    /**
     * Save the beacon list on disk
     * @param beaconList the list of beacons.
     */
    private void persistList(List<Beacon> beaconList) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(beaconList);
        ULog.d(TAG , "Persist: " + listStringified);
        editor.putString(TAG , listStringified);
        editor.apply();
    }

    /**
     * Load the beacon list from disk
     * @return the beacon list
     * @throws JSONException if the cached list is not properly formatted
     */
    private List<Beacon> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Beacon>>(){}.getType();
        ArrayList<Beacon> beacons = gson.fromJson(sp.getString(TAG, ""), collectionType);
        return beacons;
    }

    /**
     * Creates a list of AltBeacon regions from Near Regions and starts the radar.
     *
     * @param beacons the list to convert
     */
    private void startRadarOnBeacons(List<Beacon> beacons) {
        List<Region> regionsToMonitor = new ArrayList<>();
        for (Beacon beacon : beacons){
            String uniqueId = "Region" + Integer.toString(beacon.getMajor()) + Integer.toString(beacon.getMinor());
            Region region = new Region(uniqueId, Identifier.parse(beacon.getUuid()),
                    Identifier.fromInt(beacon.getMajor()), Identifier.fromInt(beacon.getMinor()));
            regionsToMonitor.add(region);
        }
        // BeaconDynamicRadar radar = new BeaconDynamicRadar(getApplicationContext(), beacons, null);
        // monitor.startRadar(backgroundBetweenScanPeriod, backgroundScanPeriod, regionExitPeriod, regionsToMonitor, this);
        List<Region> superRegions = computeSuperRegions(regionsToMonitor);
        float threshold = GlobalConfig.getInstance(getApplicationContext()).getThreshold();
        monitor.startRadar(90000l, 2000l, 20000l, 2000l, regionExitPeriod, threshold, superRegions, regionsToMonitor, this);
    }

    /**
     * Creates the superRegions from the regions. There is one super region for each distinct proximityUUID
     *
     * @param regionsToMonitor all the regions to monitor.
     * @return the list of super regions.
     */
    private List<Region> computeSuperRegions(List<Region> regionsToMonitor) {
        ArrayList<String> proximityUUIDs = new ArrayList<>();
        for (Region region : regionsToMonitor) {
            String UUID = region.getId1().toString();
            if (!proximityUUIDs.contains(UUID)){
                proximityUUIDs.add(UUID);
            }
        }
        List<Region> superRegions = new ArrayList<Region>();
        for (String proximityUUID : proximityUUIDs) {
            superRegions.add(new Region("super-" + proximityUUID, Identifier.parse(proximityUUID),null,null));
        }
        return superRegions;
    }

    /**
     * Walk through every root beacon and returns a list of all beacons.
     * @param beacons the root beacons to parse
     * @return all beacons under any root beacon
     */
    private List<Beacon> parseTree(List<Beacon> beacons) {
        List<Beacon> allBeacons = new ArrayList<>();
        for (Beacon beacon : beacons){
            allBeacons.addAll(parseTree(beacon));
        }
        return allBeacons;
    }

    /**
     * Walk through the tree under a node and returns all beacons in the tree (including the root)
     * @param node the beacon from which to start parsing
     * @return a flattened list of the tree beacons
     */
    private List<Beacon> parseTree(Beacon node){
        List<Beacon> list = new ArrayList<>();
        list.add(node);
        for (Beacon beacon : node.getChildren()){
            list.addAll(parseTree(beacon));
        }
        return list;
    }


    String test = "{\"data\":[{\"id\":\"6a3375ed-6072-42e0-8af0-755094f5884a\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":1001,\"minor\":14,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"root\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":null},\"children\":{\"data\":[{\"id\":\"c2313d76-ede3-4950-a352-58efd0315849\",\"type\":\"beacons\"},{\"id\":\"97bc9f29-b341-4ac7-bf18-73ae868ddae1\",\"type\":\"beacons\"}]}}},{\"id\":\"eeff61aa-4d1d-48cd-858f-3bee6e901290\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":2001,\"minor\":10,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"root2\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":null},\"children\":{\"data\":[{\"id\":\"baa9fac9-1ce6-4b7e-bfd4-a909ac32d8f0\",\"type\":\"beacons\"},{\"id\":\"511606b8-5f19-45af-acff-bba5e0d5cb0b\",\"type\":\"beacons\"}]}}}],\"included\":[{\"id\":\"c2313d76-ede3-4950-a352-58efd0315849\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":1001,\"minor\":14,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlio1\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"6a3375ed-6072-42e0-8af0-755094f5884a\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}},{\"id\":\"97bc9f29-b341-4ac7-bf18-73ae868ddae1\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":1001,\"minor\":16,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlio2\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"6a3375ed-6072-42e0-8af0-755094f5884a\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}},{\"id\":\"baa9fac9-1ce6-4b7e-bfd4-a909ac32d8f0\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":2001,\"minor\":12,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlia1\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"eeff61aa-4d1d-48cd-858f-3bee6e901290\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}},{\"id\":\"511606b8-5f19-45af-acff-bba5e0d5cb0b\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":2001,\"minor\":15,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlia2\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"eeff61aa-4d1d-48cd-858f-3bee6e901290\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}}]}";
    JSONObject testObject;

    @Override
    public Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    @Override
    public void didEnterRegion(Region region) {
        String pulseSlice = getPulseFromRegion(region);
        trackRegionEnter(pulseSlice);
        if (pulseSlice != null){
            firePulse(ENTER_REGION, pulseSlice);
        }
    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d(TAG, "determine region:" + region.toString() + " state: " + i);
    }

    /**
     * Send tracking data to the Forest beacon APIs about a region enter (every beacon is a region).
     * @param regionSlice the beacon identifier
     */
    private void trackRegionEnter(String regionSlice) {
        try {
            NearNetworkUtil.sendTrack(mContext, Constants.API.PLUGINS.BEACON_FOREST_TRACKINGS, buildTrackBody(regionSlice));
        } catch (JSONException e) {
            ULog.d(TAG, "Unable to send track: " +  e.toString());
        }
    }

    /**
     * Compute the HTTP request body from the region identifier in jsonAPI format.
     * @param regionSlice the region identifier
     * @return the correctly formed body
     * @throws JSONException
     */
    private String buildTrackBody(String regionSlice) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("beacon_id" , regionSlice);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date now = new Date(System.currentTimeMillis());
        String formatted = sdf.format(now);
        map.put("tracked_at", formatted);
        map.put("platform", "android");
        return NearUtils.toJsonAPI("trackings", map);
    }

    /**
     * Return the region identifier form an AltBeacon region
     * @param region the AltBeacon region
     * @return the region identifier of the region if such region exists in the configuration, <code>null</code> otherwise
     */
    private String getPulseFromRegion(Region region) {
        for (Beacon beacon : beaconList){
            if (beacon.getUuid().equals(region.getId1().toString()) &&
                    beacon.getMajor() == region.getId2().toInt() &&
                    beacon.getMinor() == region.getId3().toInt()){
                return beacon.getId();
            }
        }
        return null;
    }

    /**
     * Notify the RECIPES_PATH manager of the occurance of a registered pulse.
     * @param flavor the flavor of the pulse to notify
     * @param pulseSlice the region identifier of the pulse
     */
    private void firePulse(String flavor, String pulseSlice) {
        ULog.d(TAG, "firePulse!");
        recipesManager.gotPulse(INGREDIENT_NAME, flavor, pulseSlice);
    }

}
