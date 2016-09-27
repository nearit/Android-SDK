package it.near.sdk.Geopolis;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Geopolis.BeaconForest.AltBeaconMonitor;
import it.near.sdk.Geopolis.BeaconForest.BeaconNode;
import it.near.sdk.Geopolis.BeaconForest.GeoFenceNode;
import it.near.sdk.Geopolis.BeaconForest.NearBeacon;
import it.near.sdk.Geopolis.BeaconForest.Node;
import it.near.sdk.Geopolis.Ranging.ProximityListener;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearAsyncHttpClient;
import it.near.sdk.Communication.NearNetworkUtil;
import it.near.sdk.GlobalConfig;
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
 * - the plugin name is: beacon-forest
 * - the only supported action is: enter_region
 * - the bundle is the id of a region
 *
 * @author cattaneostefano
 */
public class GeopolisManager implements BootstrapNotifier, ProximityListener {

    // ---------- beacon forest ----------
    public static final String BEACON_FOREST_PATH =         "beacon-forest";
    public static final String BEACON_FOREST_TRACKINGS =    "trackings";
    public static final String BEACON_FOREST_BEACONS =      "beacons";

    private static final String TAG = "GeopolisManager";
    private static final String PREFS_SUFFIX = "NearBeacons";
    // TODO change
    private static final String PLUGIN_NAME = "beacon-forest";
    private static final String ENTER_REGION = "enter_region";

    private static final String RADAR_ON = "radar_on";

    private final RecipesManager recipesManager;
    private final String PREFS_NAME;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    private List<NearBeacon> beaconList;
    private List<Region> regionList;
    private Application mApplication;
    private Morpheus morpheus;
    private AltBeaconMonitor monitor;

    private NearAsyncHttpClient httpClient;

    public GeopolisManager(Application application, RecipesManager recipesManager) {
        this.mApplication = application;
        this.recipesManager = recipesManager;
        this.monitor = new AltBeaconMonitor(application);
        setUpMorpheusParser();

        String PACK_NAME = mApplication.getApplicationContext().getPackageName();
        PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = mApplication.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();

        try {
            testObject = new JSONObject(test);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpClient = new NearAsyncHttpClient();
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

        morpheus.getFactory().getDeserializer().registerResourceClass("beacons", NearBeacon.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("nodes", Node.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("beacon_nodes", BeaconNode.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("geofence_nodes", GeoFenceNode.class);
    }

    /**
     * Refresh the configuration of the component. The list of beacons to monitor will be downloaded from the APIs.
     * If there's an error in refreshing the configuration, a cached version will be used instead.
     *
     */
    public void refreshConfig(){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(BEACON_FOREST_PATH)
                    .appendPath(BEACON_FOREST_BEACONS).build();
        try {
            httpClient.nearGet(mApplication, url.toString(), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    JSONObject testNodesObj = null;
                    try {
                        testNodesObj = new JSONObject(testnodes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    List<Node> nodes = NearUtils.parseList(morpheus, testNodesObj, Node.class);
                    startRadar(nodes.get(0).getChildren());

                    List<NearBeacon> beacons = NearUtils.parseList(morpheus, response, NearBeacon.class);
                    beaconList = parseTree(beacons);

                    startRadarOnBeacons(beaconList);
                    // persistList(beaconList);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    ULog.d(TAG, "Error " + statusCode);
                    try {
                        beaconList = loadChachedList();
                        if (beaconList!=null){
                            startRadarOnBeacons(beaconList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }

    private void startRadar(List<Node> nodes) {
        monitor.setUpMonitor(nodes, this);
    }

    private void persistList(List<Node> nodes) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(nodes);
        editor.putString(TAG, listStringified).apply();
    }

    private List<Node> loadList(){
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Node>>(){}.getType();
        return gson.<ArrayList<Node>>fromJson(sp.getString(TAG, ""), collectionType);
    }


    /**
     * Save the beacon list on disk
     * @param beaconList the list of beacons.
     */
    /*private void persistList(List<NearBeacon> beaconList) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(beaconList);
        ULog.d(TAG , "Persist: " + listStringified);
        editor.putString(TAG , listStringified);
        editor.apply();
    }*/

    /**
     * Load the beacon list from disk
     * @return the beacon list
     * @throws JSONException if the cached list is not properly formatted
     */
    private List<NearBeacon> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<NearBeacon>>(){}.getType();
        return gson.<ArrayList<NearBeacon>>fromJson(sp.getString(TAG, ""), collectionType);
    }

    /**
     * Creates a list of AltBeacon regions from Near Regions and starts the radar.
     *
     * @param beacons the list to convert
     */
    private void startRadarOnBeacons(List<NearBeacon> beacons) {
        List<Region> regionsToMonitor = new ArrayList<>();
        for (NearBeacon beacon : beacons){
            String uniqueId = "Region" + Integer.toString(beacon.getMajor()) + Integer.toString(beacon.getMinor());
            Region region = new Region(uniqueId, Identifier.parse(beacon.getUuid()),
                    Identifier.fromInt(beacon.getMajor()), Identifier.fromInt(beacon.getMinor()));
            regionsToMonitor.add(region);
        }
        regionList = regionsToMonitor;
        // BeaconDynamicRadar radar = new BeaconDynamicRadar(getApplicationContext(), beacons, null);
        // monitor.startRadar(backgroundBetweenScanPeriod, backgroundScanPeriod, regionExitPeriod, regionsToMonitor, this);
        if (sp.getBoolean(RADAR_ON, false)){
            // monitor.startRadar(regionsToMonitor, this);
        }
    }

    /*public void startRadar(){
        editor.putBoolean(RADAR_ON, true).commit();
        monitor.startRadar(regionList, this);
    }*/


    /**
     * Walk through every root beacon and returns a list of all beacons.
     * @param beacons the root beacons to parse
     * @return all beacons under any root beacon
     */
    private List<NearBeacon> parseTree(List<NearBeacon> beacons) {
        List<NearBeacon> allBeacons = new ArrayList<>();
        for (NearBeacon beacon : beacons){
            allBeacons.addAll(parseTree(beacon));
        }
        return allBeacons;
    }

    /**
     * Walk through the tree under a node and returns all beacons in the tree (including the root)
     * @param node the beacon from which to start parsing
     * @return a flattened list of the tree beacons
     */
    private List<NearBeacon> parseTree(NearBeacon node){
        List<NearBeacon> list = new ArrayList<>();
        list.add(node);
        for (NearBeacon beacon : node.getChildren()){
            list.addAll(parseTree(beacon));
        }
        return list;
    }


    String test = "{\"data\":[{\"id\":\"6a3375ed-6072-42e0-8af0-755094f5884a\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":1001,\"minor\":14,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"root\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":null},\"children\":{\"data\":[{\"id\":\"c2313d76-ede3-4950-a352-58efd0315849\",\"type\":\"beacons\"},{\"id\":\"97bc9f29-b341-4ac7-bf18-73ae868ddae1\",\"type\":\"beacons\"}]}}},{\"id\":\"eeff61aa-4d1d-48cd-858f-3bee6e901290\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":2001,\"minor\":10,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"root2\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":null},\"children\":{\"data\":[{\"id\":\"baa9fac9-1ce6-4b7e-bfd4-a909ac32d8f0\",\"type\":\"beacons\"},{\"id\":\"511606b8-5f19-45af-acff-bba5e0d5cb0b\",\"type\":\"beacons\"}]}}}],\"included\":[{\"id\":\"c2313d76-ede3-4950-a352-58efd0315849\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":1001,\"minor\":14,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlio1\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"6a3375ed-6072-42e0-8af0-755094f5884a\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}},{\"id\":\"97bc9f29-b341-4ac7-bf18-73ae868ddae1\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":1001,\"minor\":16,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlio2\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"6a3375ed-6072-42e0-8af0-755094f5884a\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}},{\"id\":\"baa9fac9-1ce6-4b7e-bfd4-a909ac32d8f0\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":2001,\"minor\":12,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlia1\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"eeff61aa-4d1d-48cd-858f-3bee6e901290\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}},{\"id\":\"511606b8-5f19-45af-acff-bba5e0d5cb0b\",\"type\":\"beacons\",\"attributes\":{\"uuid\":\"acfd065e-c3c0-11e3-9bbe-1a514932ac01\",\"major\":2001,\"minor\":15,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"name\":\"figlia2\",\"map_placement\":null},\"relationships\":{\"parent\":{\"data\":{\"id\":\"eeff61aa-4d1d-48cd-858f-3bee6e901290\",\"type\":\"beacons\"}},\"children\":{\"data\":[]}}}]}";
    JSONObject testObject;

    @Override
    public Context getApplicationContext() {
        return mApplication.getApplicationContext();
    }

    @Override
    public void didEnterRegion(Region region) {
        String pulseBundle = getPulseFromRegion(region);
        trackRegionEnter(pulseBundle);
        if (pulseBundle != null){
            firePulse(ENTER_REGION, pulseBundle);
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
     * @param regionBundle the beacon identifier
     */
    private void trackRegionEnter(String regionBundle) {
        try {
            Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                        .appendPath(BEACON_FOREST_PATH)
                        .appendPath(BEACON_FOREST_TRACKINGS).build();
            NearNetworkUtil.sendTrack(mApplication, url.toString(), buildTrackBody(regionBundle));
        } catch (JSONException e) {
            ULog.d(TAG, "Unable to send track: " +  e.toString());
        }
    }

    /**
     * Compute the HTTP request body from the region identifier in jsonAPI format.
     * @param regionBundle the region identifier
     * @return the correctly formed body
     * @throws JSONException
     */
    private String buildTrackBody(String regionBundle) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("beacon_id" , regionBundle);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date now = new Date(System.currentTimeMillis());
        String formatted = sdf.format(now);
        map.put("tracked_at", formatted);
        map.put("platform", "android");
        map.put("profile_id", GlobalConfig.getInstance(mApplication).getProfileId());
        map.put("installation_id", GlobalConfig.getInstance(mApplication).getInstallationId());
        /*String installId = GlobalConfig.getInstance(getApplicationContext()).getInstallationId();
        map.put("installation_id", installId);*/
        return NearUtils.toJsonAPI("trackings", map);
    }

    /**
     * Return the region identifier form an AltBeacon region
     * @param region the AltBeacon region
     * @return the region identifier of the region if such region exists in the configuration, <code>null</code> otherwise
     */
    private String getPulseFromRegion(Region region) {
        // TODO this needs ot be changed
        for (NearBeacon beacon : beaconList){
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
     * @param pulseAction the action of the pulse to notify
     * @param pulseBundle the region identifier of the pulse
     */
    private void firePulse(String pulseAction, String pulseBundle) {
        ULog.d(TAG, "firePulse!");
        recipesManager.gotPulse(PLUGIN_NAME, pulseAction, pulseBundle);
    }

    @Override
    public void enterBeaconRange(Beacon beacon, int proximity) {
        ULog.wtf(TAG, "Enter in range: " + proximity + " for beacon: " + beacon.toString());
    }

    @Override
    public void exitBeaconRange(Beacon beacon, int proximity) {
        ULog.wtf(TAG, "Exit from range: " + proximity + " for beacon: " + beacon.toString());
    }

    @Override
    public void enterRegion(Region region) {
        String pulseBundle = getPulseFromRegion(region);
        trackRegionEnter(pulseBundle);
        if (pulseBundle != null){
            firePulse(ENTER_REGION, pulseBundle);
        }
    }

    @Override
    public void exitRegion(Region region) {

    }

    String testnodes = "{\n" +
            "  \"data\": [\n" +
            "    {\n" +
            "      \"id\": \"a27fb650-19c9-403c-8df9-e2f4e5d8fe3a\",\n" +
            "      \"type\": \"geofence_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"a37a8543-355c-4825-857b-5624031f8d2a\",\n" +
            "        \"latitude\": 40.78595,\n" +
            "        \"longitude\": -73.667595,\n" +
            "        \"radius\": 50.0\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": null\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"5618708a-5ae8-4794-8a43-95e617db3088\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"1fe492ae-3d53-4642-ba5b-7daa46378320\",\n" +
            "      \"type\": \"geofence_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"060f7b11-5302-4440-bb46-62f1a65d612a\",\n" +
            "        \"latitude\": 38.996207,\n" +
            "        \"longitude\": -122.012045,\n" +
            "        \"radius\": 50.0\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": null\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"18a36337-392f-41d7-84dc-b09ba14cf09b\",\n" +
            "      \"type\": \"geofence_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"60af1b03-e03c-4c97-9b25-23c869a8cd8f\",\n" +
            "        \"latitude\": 37.575803,\n" +
            "        \"longitude\": -81.611008,\n" +
            "        \"radius\": 50.0\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": null\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"309dc29d-1899-489e-a058-bf04547c1d73\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"included\": [\n" +
            "    {\n" +
            "      \"id\": \"5618708a-5ae8-4794-8a43-95e617db3088\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": null,\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": null,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"a27fb650-19c9-403c-8df9-e2f4e5d8fe3a\",\n" +
            "            \"type\": \"geofence_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"9363d258-aa0d-4f76-ac5e-ad1fb1ef444a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"cb958ac8-a8a8-4308-bd32-25ce4638fe9e\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"5618708a-5ae8-4794-8a43-95e617db3088\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"06546666-eeef-41f7-8564-60f8d9195bef\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"6a570b4c-e751-430e-8a8d-e77f26e43554\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"fd44e515-39bc-4b98-8f8b-ba2f26c2abda\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"458f6256-d1fc-443b-b6ad-28c51c5daf0d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"06eadb09-b858-4a61-afd2-c3c6457dff86\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"02659ecd-8ae0-4bad-8b00-96a8b97b402a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"d92691a4-cbea-42c0-a031-1fc536d1eebf\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"9e92e24c-a4b8-417d-bf8c-e500c66a247e\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"2b655817-ecd5-4dd4-9638-7a162fb3131f\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"05492e1f-ab32-4b5a-87dc-64d16be8ff3f\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"06546666-eeef-41f7-8564-60f8d9195bef\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"8f71e8ea-078f-49e3-96b0-1ebbf520c91a\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 20796\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"6a570b4c-e751-430e-8a8d-e77f26e43554\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"7a40ff3c-2d2c-4eb7-9aba-1870520d9c97\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 63141\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"fd44e515-39bc-4b98-8f8b-ba2f26c2abda\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"2bfaecf1-c120-4db6-99ff-f8768600a728\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 3154\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"458f6256-d1fc-443b-b6ad-28c51c5daf0d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"e4e2eace-e8a5-41c9-8774-0c263b87cf8a\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 22865\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"06eadb09-b858-4a61-afd2-c3c6457dff86\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"7f745711-d477-4290-9d62-9288459726cb\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 45134\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"02659ecd-8ae0-4bad-8b00-96a8b97b402a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"3920020b-552a-4b19-9877-be9649707692\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 42819\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"d92691a4-cbea-42c0-a031-1fc536d1eebf\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"2d638ab9-db76-4530-9975-e99642d26f19\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 25825\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"9e92e24c-a4b8-417d-bf8c-e500c66a247e\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"d2fa45de-5714-4f6f-ae27-abca83bc7e1c\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 40075\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2b655817-ecd5-4dd4-9638-7a162fb3131f\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"485285db-905b-49ee-a03c-d93b9775d2a7\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 50243\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"05492e1f-ab32-4b5a-87dc-64d16be8ff3f\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"6ab5e674-4d8f-4abb-8aaa-e7612b1a97f6\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 39357,\n" +
            "        \"minor\": 12090\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"50c2805e-6402-4d28-abd7-28faf5be2b8a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"9363d258-aa0d-4f76-ac5e-ad1fb1ef444a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f27f5530-412d-46b1-88db-88e971bde1c4\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 47147,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"5618708a-5ae8-4794-8a43-95e617db3088\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"3cda8e2d-daf8-48e2-a903-bc8dded3c275\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"471c47c3-8d1f-4507-a9cc-6a49e948b964\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"3cda8e2d-daf8-48e2-a903-bc8dded3c275\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"1fe214cf-583c-4167-b201-b2026b28f4a9\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 47147,\n" +
            "        \"minor\": 599\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"9363d258-aa0d-4f76-ac5e-ad1fb1ef444a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"471c47c3-8d1f-4507-a9cc-6a49e948b964\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"101a2e4f-c908-4621-b67a-0210ffed9275\",\n" +
            "        \"proximity_uuid\": \"9f1e4c18-87c6-47c3-8518-7e3269280d53\",\n" +
            "        \"major\": 47147,\n" +
            "        \"minor\": 46188\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"9363d258-aa0d-4f76-ac5e-ad1fb1ef444a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": null,\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": null,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"1fe492ae-3d53-4642-ba5b-7daa46378320\",\n" +
            "            \"type\": \"geofence_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"52698f55-00da-4517-9334-3daba5597f1a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"a927913a-0c20-4ee1-8628-28688392882d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"97f81865-04eb-4ff2-9ffb-0f4809c08edb\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"85a190fa-676e-4793-ba7c-99b9b07d084e\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"62c9b3ae-fe65-4254-be4f-f75e76919c2d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"cdb48b04-867c-45ac-8fef-1c3b3bc11d33\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"58181576-85ca-4280-8d56-085f265aee5a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"5ae1506d-d63b-414f-a1a1-4c64f2061bcd\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"43e0528d-d50d-46cf-8d53-dbd5440157a6\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"d535c9b6-e4a0-4466-8811-612f4f558e7d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"2ad73740-e860-4b2d-80f3-f5192ff6bd05\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"85a190fa-676e-4793-ba7c-99b9b07d084e\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f5a469aa-9ce8-467a-8f60-ba69b66e4bae\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 54946\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"62c9b3ae-fe65-4254-be4f-f75e76919c2d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"67e8ba1a-4a6a-4675-a9d9-6b1990540b35\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 9471\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"cdb48b04-867c-45ac-8fef-1c3b3bc11d33\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"305ac6c0-de67-46e7-9b86-8e4ebd7ff695\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 41778\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"58181576-85ca-4280-8d56-085f265aee5a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"1944883a-077a-430f-a0b7-1644b68af018\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 8414\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"5ae1506d-d63b-414f-a1a1-4c64f2061bcd\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"8e395ba7-75fa-466f-a141-a5968ef269c1\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 36496\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"43e0528d-d50d-46cf-8d53-dbd5440157a6\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"2948a4b4-0b2f-4612-85dc-bf5e512a4f4b\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 18448\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"d535c9b6-e4a0-4466-8811-612f4f558e7d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"05f22c1c-04c8-4766-b891-e9e8df315416\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 34309\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2ad73740-e860-4b2d-80f3-f5192ff6bd05\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f2257451-7e13-41fa-ae63-99ab7fc955be\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 19014,\n" +
            "        \"minor\": 16123\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ae3da4dc-c89c-4b43-b49c-dbc9f14daf73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"52698f55-00da-4517-9334-3daba5597f1a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"d74ba1c3-e3a4-4327-be27-77920765d1bd\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 33773,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"57c7adaf-07f6-4efb-9e4f-125db1eaadac\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"197f024c-59aa-42ad-8d94-ead4786c2104\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"57c7adaf-07f6-4efb-9e4f-125db1eaadac\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"a933b035-71c6-4d34-b6a6-d8786a857188\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 33773,\n" +
            "        \"minor\": 6158\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"52698f55-00da-4517-9334-3daba5597f1a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"197f024c-59aa-42ad-8d94-ead4786c2104\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"43101554-000c-45e6-a5da-52ebe9073fe6\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 33773,\n" +
            "        \"minor\": 40481\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"52698f55-00da-4517-9334-3daba5597f1a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f16bb26e-e8dd-4b21-81aa-e18b0173a9b4\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"efb60e52-3899-4bbc-a123-144929920f3a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"40c4da06-a600-45e9-a447-fcf5e3046038\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"a34b3a19-991c-42ba-93b5-438c9f4af4f7\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"88b76860-062b-4690-b619-26b9843f2704\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"4e61205b-e1cc-4aa6-aed5-b212aa6cf755\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"9524b11f-37bc-4795-8d9b-5e484297b5ed\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"52f091a0-9faf-47cb-8ad6-f2be6515edc9\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"26092d51-d2fc-4065-8183-3521f92b850b\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"c187cf6e-e8d8-4402-8c2f-c9ac15d2febf\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"efb60e52-3899-4bbc-a123-144929920f3a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"91c9e714-3da3-4f27-873f-3a1afe551fb9\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 51149\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"40c4da06-a600-45e9-a447-fcf5e3046038\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"877dd79e-5a6a-4a58-981c-b6d0dd2e8077\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 28747\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"a34b3a19-991c-42ba-93b5-438c9f4af4f7\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"ceaff267-d752-43fe-8c9e-afb4d9cf1ed0\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 39245\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"88b76860-062b-4690-b619-26b9843f2704\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"6b94644d-9322-42f3-b708-afbc904b7e3b\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 40647\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"4e61205b-e1cc-4aa6-aed5-b212aa6cf755\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"03d49c28-0fb2-4eb4-ae9f-3f1be48ccb26\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 15616\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"9524b11f-37bc-4795-8d9b-5e484297b5ed\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"9c09403e-2d68-4917-99bd-0210386587bf\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 4471\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"52f091a0-9faf-47cb-8ad6-f2be6515edc9\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"5c5e5d4c-724f-4ef9-9c2d-56f3e7844dbf\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 11287\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"26092d51-d2fc-4065-8183-3521f92b850b\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"c27e7e75-f858-443f-8224-794ff484a7d6\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 62249\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"c187cf6e-e8d8-4402-8c2f-c9ac15d2febf\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"fd54f25b-2a58-43b1-914b-7c8567411dd9\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 50083,\n" +
            "        \"minor\": 31307\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"6b112a5a-fcbf-4147-aa79-e35265cc9759\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"a927913a-0c20-4ee1-8628-28688392882d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"0b2a320d-3669-429e-ba36-14a276535f17\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 29614,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"6dd15c13-f842-4054-88f0-515144a2799d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"e78c7e63-dc1a-4a05-8b3f-19b731cb5382\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"e194cf9a-78ef-44fe-8a0a-4a037b31d27d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"6dd15c13-f842-4054-88f0-515144a2799d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"d7fc36f0-1d82-4444-bd14-817cf198123c\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 29614,\n" +
            "        \"minor\": 19532\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"a927913a-0c20-4ee1-8628-28688392882d\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"e78c7e63-dc1a-4a05-8b3f-19b731cb5382\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"8237bb95-22bc-4212-8699-a46903d28206\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 29614,\n" +
            "        \"minor\": 17878\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"a927913a-0c20-4ee1-8628-28688392882d\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"e194cf9a-78ef-44fe-8a0a-4a037b31d27d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"5540c06e-933d-4673-a055-82b4bdf74d65\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 29614,\n" +
            "        \"minor\": 26425\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"a927913a-0c20-4ee1-8628-28688392882d\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"99b28063-0d3c-4f84-a280-f9a1d5569a3b\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"c2eecad5-7929-457d-b7df-9398572fa6dc\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"cf46c7f1-8be6-483c-86e9-1e02f46df658\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"31cec406-023b-44c0-9b36-da3078d7ca2b\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"83e916a1-117b-4e59-bd64-c6d4514f9dcb\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"36c2911f-373a-4640-a187-6657a2af6f4c\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"b5c651d6-1c93-463a-b630-597e2cde1bfe\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"99467624-9ff3-49a6-8964-301190577bb7\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"94e58c51-2b30-4816-9428-0dbe1e53abb6\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"7be860c1-d42e-4cca-a0da-49611bcc1656\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"c2eecad5-7929-457d-b7df-9398572fa6dc\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"118d70cf-c2f8-49a5-8f44-f80295db91a6\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 62396\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"cf46c7f1-8be6-483c-86e9-1e02f46df658\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"c5a72e85-156f-42c3-bc0f-cbe39dab79df\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 42816\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"31cec406-023b-44c0-9b36-da3078d7ca2b\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"6679cd65-0cd6-43dd-b0ba-cdcdc395ba3a\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 42841\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"83e916a1-117b-4e59-bd64-c6d4514f9dcb\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"a489f779-3a09-4e94-aa2a-301f889622ea\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 27551\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"36c2911f-373a-4640-a187-6657a2af6f4c\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"755f9fdd-4955-44a1-b6e4-aa7c0b77f504\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 13212\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"b5c651d6-1c93-463a-b630-597e2cde1bfe\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f7a4d440-5870-4a52-b7cf-b8d9a500889f\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 19816\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"99467624-9ff3-49a6-8964-301190577bb7\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"3147a591-bffc-4255-a832-0c4ce87c67c4\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 5325\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"94e58c51-2b30-4816-9428-0dbe1e53abb6\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f3954e10-c108-43f9-9e16-aa0035418f2c\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 35740\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"7be860c1-d42e-4cca-a0da-49611bcc1656\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"ef5ac1c8-ec70-48f7-82c2-ad536939a569\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 35025,\n" +
            "        \"minor\": 8660\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"4a51333e-4873-46e5-a4e6-45d5798ad750\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"095a1b3a-742e-4a0e-9a92-f6d1ae59f2af\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 13544,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"deef15bc-fcd8-4edd-96a6-dce5b4df9f45\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"005a6015-abe9-4333-9940-a5226eac0f4b\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"725be32e-f64f-47e1-95f7-e717beffab21\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"e45725ba-c64b-414f-8bbe-adf25545abdc\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"c777b678-2cb7-450f-9a36-0288fdbdd321\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"d674d3fa-7484-4cc6-b6e8-2ece15dd44b6\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"005a6015-abe9-4333-9940-a5226eac0f4b\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"2cfab8fc-54a8-491e-8921-2600cd462d2a\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 13544,\n" +
            "        \"minor\": 28899\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"725be32e-f64f-47e1-95f7-e717beffab21\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"8f927d36-2a0f-41d2-854b-3c323f78dc52\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 13544,\n" +
            "        \"minor\": 54410\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"e45725ba-c64b-414f-8bbe-adf25545abdc\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"ff3f85a0-5608-4caf-b8e4-59da8fca6831\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 13544,\n" +
            "        \"minor\": 54153\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"c777b678-2cb7-450f-9a36-0288fdbdd321\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"df7c5692-3247-4b1e-a463-81eb5868a1dd\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 13544,\n" +
            "        \"minor\": 65203\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"d674d3fa-7484-4cc6-b6e8-2ece15dd44b6\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"bc6e2bd0-bcca-4308-8f5f-b0c661a747a0\",\n" +
            "        \"proximity_uuid\": \"3551a71e-015f-448a-8281-2784225d7cca\",\n" +
            "        \"major\": 13544,\n" +
            "        \"minor\": 50249\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"354df2c7-0dc3-41d9-85cf-5a002a5df426\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"309dc29d-1899-489e-a058-bf04547c1d73\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": null,\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": null,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"18a36337-392f-41d7-84dc-b09ba14cf09b\",\n" +
            "            \"type\": \"geofence_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"5cd0fee6-d8fb-4d0f-aac0-34bd23e0c11e\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"5cd0fee6-d8fb-4d0f-aac0-34bd23e0c11e\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"609a8416-b3b5-484e-8107-051f93abb801\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 3975,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"309dc29d-1899-489e-a058-bf04547c1d73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"f7308d22-12e1-43f4-957f-2294ab8e3cde\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"b2124ef6-5f11-4a64-8289-6f68f2bc8acc\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"f7308d22-12e1-43f4-957f-2294ab8e3cde\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f34d4da5-7ee3-4a3c-9fd8-d65b9fac3148\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 3975,\n" +
            "        \"minor\": 24895\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"5cd0fee6-d8fb-4d0f-aac0-34bd23e0c11e\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"b2124ef6-5f11-4a64-8289-6f68f2bc8acc\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"b8f8bac1-29ef-4574-8d3d-5b6be70c641c\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 3975,\n" +
            "        \"minor\": 39771\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"5cd0fee6-d8fb-4d0f-aac0-34bd23e0c11e\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"e53f73b2-03a6-4f85-b9d6-a1e634762aa3\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"309dc29d-1899-489e-a058-bf04547c1d73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"13196c7b-9fb4-4bdb-9411-a8fde1007489\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"333b3e88-b9f6-4090-b161-64672dc295bb\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"ca120aae-10b7-4ec9-846a-baa60d5311fb\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"e6e28990-50c0-48f7-8c79-2fc264d3cfa4\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"84d4af82-021f-4d5d-80e5-3ebc6ed38d27\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"34b57bb2-ad8e-4f92-8c03-01f89d92b710\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"13196c7b-9fb4-4bdb-9411-a8fde1007489\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"3b8b81b1-5a04-4571-99fa-9e28e15e1c70\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": 8044\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"333b3e88-b9f6-4090-b161-64672dc295bb\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"26e698a4-b874-4c79-9d71-0872916047aa\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": 36635\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"ca120aae-10b7-4ec9-846a-baa60d5311fb\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"df03f07e-2238-4def-b4ad-6737b72cc46f\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": 54720\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"e6e28990-50c0-48f7-8c79-2fc264d3cfa4\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"2a208227-6f2d-478e-bf81-4cf1db7e934d\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": 20756\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"84d4af82-021f-4d5d-80e5-3ebc6ed38d27\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"800f78f4-aefc-4107-af90-7c6b4b056820\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": 48166\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"34b57bb2-ad8e-4f92-8c03-01f89d92b710\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"0f6fac1c-03a1-4623-97a3-666ae7b53843\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 54308,\n" +
            "        \"minor\": 19222\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"48ca1022-10e5-4335-bdc4-b8182aaeaf49\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"6d0a0563-7e2e-4211-bfdf-1c1f4dc8618b\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"309dc29d-1899-489e-a058-bf04547c1d73\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"a24d0cd3-1cd6-4de3-9fc5-1e5597b77a13\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"dd8c6a7c-52ef-4e3f-a40e-f5b0b8b292c7\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"808c30c1-66bc-43f1-a4ae-bdde485d374e\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"41b2602d-d718-4d22-9e98-97bed31eeb20\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"5b6b01bd-0026-405c-a58d-98c20b3db962\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"726a57b0-e814-4073-b79c-6152b3548275\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"ddcd2bb2-7944-4f37-bb1e-1c5b8ee8dbf4\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"a24d0cd3-1cd6-4de3-9fc5-1e5597b77a13\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"14415859-cd0a-46e5-8829-64da2d8398e6\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 46116\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"dd8c6a7c-52ef-4e3f-a40e-f5b0b8b292c7\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"836c92c3-b84a-48e4-8405-e5824af6210e\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 6481\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"808c30c1-66bc-43f1-a4ae-bdde485d374e\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"806c4547-eee5-4207-bf7e-14034bbeec0e\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 64270\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"41b2602d-d718-4d22-9e98-97bed31eeb20\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"b6177a90-5f05-4bc4-9e1c-b4d2e54e2b8e\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 7491\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"5b6b01bd-0026-405c-a58d-98c20b3db962\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"f3ac303c-715e-47b8-99e4-bae395dfc67e\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 57438\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"726a57b0-e814-4073-b79c-6152b3548275\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"1789c68d-a166-4024-986d-b4cca3a71e1f\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 33217\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"ddcd2bb2-7944-4f37-bb1e-1c5b8ee8dbf4\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"1d464fa3-a915-434f-8925-bcb5388f559d\",\n" +
            "        \"identifier\": \"9a74370e-902e-496c-9f32-d47b596c2a27\",\n" +
            "        \"proximity_uuid\": \"2b3d915a-fe41-4d32-9c11-d7460cf11351\",\n" +
            "        \"major\": 47400,\n" +
            "        \"minor\": 45075\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"67ffbe39-4951-4503-ac58-51819c20a95a\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
