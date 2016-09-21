package it.near.sdk.Beacons.BeaconForest;

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
import it.near.sdk.Beacons.Ranging.ProximityListener;
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
                    List<NearBeacon> beacons = NearUtils.parseList(morpheus, response, NearBeacon.class);
                    beaconList = parseTree(beacons);
                    startRadarOnBeacons(beaconList);
                    persistList(beaconList);
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


    /**
     * Save the beacon list on disk
     * @param beaconList the list of beacons.
     */
    private void persistList(List<NearBeacon> beaconList) {
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
            monitor.startRadar(regionsToMonitor, this);
        }
    }

    public void startRadar(){
        editor.putBoolean(RADAR_ON, true).commit();
        monitor.startRadar(regionList, this);
    }


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
            "      \"id\": \"01124d4d-d7be-4612-b8a2-4c3a5a718f67\",\n" +
            "      \"type\": \"geofence_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"02d131db-e990-4bd2-925c-395a0f7713c8\",\n" +
            "        \"latitude\": \"36.234545\",\n" +
            "        \"longitude\": \"-73.845465\",\n" +
            "        \"radius\": 50\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": null\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"c07ee801-2049-416f-859d-a61be2571739\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"abce5b10-2e7f-4e20-91e9-a3352dfec220\",\n" +
            "      \"type\": \"geofence_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"e9a447e7-d696-48dd-ac20-d96e808deb09\",\n" +
            "        \"latitude\": \"41.5244\",\n" +
            "        \"longitude\": \"-81.611008\",\n" +
            "        \"radius\": 50\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": null\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"cc022ca6-a5a8-4091-b0b2-38a7461330ed\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"included\": [\n" +
            "    {\n" +
            "      \"id\": \"c07ee801-2049-416f-859d-a61be2571739\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": null,\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": null,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"01124d4d-d7be-4612-b8a2-4c3a5a718f67\",\n" +
            "            \"type\": \"geofence_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"ed4495ac-36f2-47db-a89a-926e89a63c70\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"ed4495ac-36f2-47db-a89a-926e89a63c70\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"7ec3176e-57f7-4c57-aa9d-073da1fe3117\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 10897,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"c07ee801-2049-416f-859d-a61be2571739\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"c6a6c4af-e181-4e48-984e-70a31cc6727e\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"c5005ed2-2c38-45ed-b83b-657b502ae2a7\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"c6a6c4af-e181-4e48-984e-70a31cc6727e\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"a97c4921-eafc-4e78-885d-02c8f6c04e4d\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 10897,\n" +
            "        \"minor\": 45354\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ed4495ac-36f2-47db-a89a-926e89a63c70\",\n" +
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
            "      \"id\": \"c5005ed2-2c38-45ed-b83b-657b502ae2a7\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"2763cf09-c3f6-4aeb-943f-dadb7a6c9861\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 10897,\n" +
            "        \"minor\": 22326\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"ed4495ac-36f2-47db-a89a-926e89a63c70\",\n" +
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
            "      \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"cc703614-7bed-4305-9013-07f3731750d8\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 2472,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"c07ee801-2049-416f-859d-a61be2571739\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"2a5583ba-40f8-4a30-a8c1-ed37b923bf0f\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"6cc3ce59-e4bb-4ef2-a379-6ee9fe1b19bd\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"58db5fef-d4f0-4e60-b602-faecbcfe5a65\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"df313157-024b-43a8-aa3a-1403301eca68\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"e081fb79-320d-4098-ae71-f02ed50056f2\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2a5583ba-40f8-4a30-a8c1-ed37b923bf0f\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"2aaba6f5-cbb2-4095-be64-04cb70ce0cdf\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 2472,\n" +
            "        \"minor\": 5736\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
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
            "      \"id\": \"6cc3ce59-e4bb-4ef2-a379-6ee9fe1b19bd\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"f5e5afed-69d5-4166-bf87-ed4b1e5e413c\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 2472,\n" +
            "        \"minor\": 42423\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
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
            "      \"id\": \"58db5fef-d4f0-4e60-b602-faecbcfe5a65\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"566647e6-fb54-4f6b-a256-ef7c2c0d7cf7\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 2472,\n" +
            "        \"minor\": 45691\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
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
            "      \"id\": \"df313157-024b-43a8-aa3a-1403301eca68\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"abbd49e1-d1e1-41f3-bf10-7e243eefe993\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 2472,\n" +
            "        \"minor\": 48231\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
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
            "      \"id\": \"e081fb79-320d-4098-ae71-f02ed50056f2\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"dcc1e701-10f3-46e8-bbe4-8eda0fa9f751\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 2472,\n" +
            "        \"minor\": 51667\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"94c61341-3715-4964-8c60-69e9884a9869\",\n" +
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
            "      \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"08d023cc-2558-4413-851d-d68fa68e49a1\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"c07ee801-2049-416f-859d-a61be2571739\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"4e305252-d965-442e-9fa4-287b8d406695\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"47b871cf-d136-4312-8125-71da39a73d7c\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"46f47fdc-1d5a-4a4b-91d7-01c354290166\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"d4b440fd-3638-4095-910b-5ba0e1c9bdc6\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"c4686a43-7440-4d41-8e90-e8544029d960\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"d146b07f-7b42-4e4e-9472-c491019b1390\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"4e305252-d965-442e-9fa4-287b8d406695\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"71f75174-3f16-4443-bf76-fdf66622eb91\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": 33458\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
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
            "      \"id\": \"47b871cf-d136-4312-8125-71da39a73d7c\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"487f762e-36e1-47d9-8a92-9db396db37f7\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": 20610\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
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
            "      \"id\": \"46f47fdc-1d5a-4a4b-91d7-01c354290166\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"e2e63004-f85b-4ea2-9561-42fb264606e0\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": 49320\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
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
            "      \"id\": \"d4b440fd-3638-4095-910b-5ba0e1c9bdc6\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"53356ea5-8b7f-4363-99c6-88c7726f27ca\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": 37862\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
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
            "      \"id\": \"c4686a43-7440-4d41-8e90-e8544029d960\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"91b3658b-ee71-43f0-94a2-315f79a7247f\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": 50878\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
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
            "      \"id\": \"d146b07f-7b42-4e4e-9472-c491019b1390\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"873ebb57-b76c-40ef-84ce-4a4cfbe2e8ba\",\n" +
            "        \"proximity_uuid\": \"d79c92b0-6ec8-4906-9bc4-74fd82ddf415\",\n" +
            "        \"major\": 44521,\n" +
            "        \"minor\": 58681\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"493c62ad-dcb0-47dd-9e12-8644b79439b4\",\n" +
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
            "      \"id\": \"cc022ca6-a5a8-4091-b0b2-38a7461330ed\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": null,\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": null,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"abce5b10-2e7f-4e20-91e9-a3352dfec220\",\n" +
            "            \"type\": \"geofence_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"e226994f-06dd-44c0-bfd5-e634bf6aa3cb\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"cb12e8c4-45b6-48d1-8678-7d0ee30c9495\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"e226994f-06dd-44c0-bfd5-e634bf6aa3cb\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"e19d6754-f95a-4271-99d4-3e4f59ae8754\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 23515,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cc022ca6-a5a8-4091-b0b2-38a7461330ed\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"a55b37f5-fd0c-4ed9-9341-234f042b7efc\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"4cf6e9a3-491a-4671-8aaf-439af904b29c\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"a55b37f5-fd0c-4ed9-9341-234f042b7efc\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"ccad671b-abfb-4cf1-94e1-070ae665de81\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 23515,\n" +
            "        \"minor\": 21455\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"e226994f-06dd-44c0-bfd5-e634bf6aa3cb\",\n" +
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
            "      \"id\": \"4cf6e9a3-491a-4671-8aaf-439af904b29c\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"2d0c4598-59d5-4fd0-9732-2fe1ec176a2f\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 23515,\n" +
            "        \"minor\": 316\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"e226994f-06dd-44c0-bfd5-e634bf6aa3cb\",\n" +
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
            "      \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"dfe528e5-4f54-4b34-b55b-488194d44852\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cc022ca6-a5a8-4091-b0b2-38a7461330ed\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"910665f8-4989-407c-84f1-e70dad06a300\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"2b7164a7-4b30-47e5-b28a-0b75f65cc482\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"411d725f-5a0c-4f95-8166-d7a881309ac4\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"d2540e3a-ed3b-4916-8107-08923b3449c9\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"823c60c4-de6c-4780-9afd-8da8435a2076\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"73306e5e-a5fe-4df7-a779-50dc414c8632\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"5794f7e4-2b56-4a96-927d-5064352f82a0\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"97c79548-016e-4d63-881b-3bab8615b24d\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"935c05e0-b69b-491e-be61-45861912650c\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"2b06e51c-5e4a-44b8-b585-135c6cd3b8bf\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"910665f8-4989-407c-84f1-e70dad06a300\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"36a2c914-3bbb-4c72-a0cf-728acbc00e33\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 51065\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"2b7164a7-4b30-47e5-b28a-0b75f65cc482\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"d134725b-1b0f-4449-8da5-880730233497\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 45443\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"411d725f-5a0c-4f95-8166-d7a881309ac4\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"b62c453e-57f1-482b-a2e0-2a630075fe93\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 6060\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"d2540e3a-ed3b-4916-8107-08923b3449c9\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"f811cc8a-74db-4d78-a49e-9fc8e34e5498\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 29191\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"823c60c4-de6c-4780-9afd-8da8435a2076\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"f79f2f99-2a26-44b4-b1df-9c92fb53fb09\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 37092\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"73306e5e-a5fe-4df7-a779-50dc414c8632\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"8be8bf64-dfe1-4c08-977c-5ac622f5a380\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 888\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"5794f7e4-2b56-4a96-927d-5064352f82a0\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"d900c439-ad04-4721-ae20-93ded7e16984\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 12381\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"97c79548-016e-4d63-881b-3bab8615b24d\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"a941ffe8-c895-4239-bb9c-c33db127c5dc\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 41918\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"935c05e0-b69b-491e-be61-45861912650c\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"13c5dd5f-9445-4300-b9d7-6067e458635b\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 3034\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"2b06e51c-5e4a-44b8-b585-135c6cd3b8bf\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"cb34e962-2f58-49ff-924c-e5662b239daf\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 14550,\n" +
            "        \"minor\": 64901\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"71e00ecd-f82e-4367-8dde-fa185515d719\",\n" +
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
            "      \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"70ec7173-51c4-47ad-bc51-9d1e6b9dc4a3\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cc022ca6-a5a8-4091-b0b2-38a7461330ed\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"2cdb0eca-5407-4f2d-8664-79973ba7a523\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"bbd718a9-b575-4766-a92a-2cb1623eec97\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"c97110f8-24da-4b13-a87e-e9dfd481eb8f\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"a9318d6c-2056-487a-8f8e-6931a5f912a9\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"a5e94b7e-46a3-4e42-9908-21141d7ae9eb\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"08bf9c3b-f56e-4d6c-9374-8c57803eedb8\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"2123d908-f76a-47c5-8b64-8f7aa6f7cabc\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"f95c16bc-604a-49eb-9ed1-d751b57b7483\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"de2a5ee3-bf97-46a8-8ed3-9d792c5f7143\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2cdb0eca-5407-4f2d-8664-79973ba7a523\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"850ba168-4b75-4c2f-aad4-6747f3199ed9\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 27438\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"bbd718a9-b575-4766-a92a-2cb1623eec97\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"3755b878-47eb-45ea-b962-1030f0ea8843\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 48535\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"c97110f8-24da-4b13-a87e-e9dfd481eb8f\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"9f8c7feb-caff-4705-91fe-686e8956e971\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 15584\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"a9318d6c-2056-487a-8f8e-6931a5f912a9\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"110e7144-a43d-4f03-81a7-f31414ed88bb\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 63539\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"a5e94b7e-46a3-4e42-9908-21141d7ae9eb\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"2854cd93-7f46-419c-9d18-69203169805a\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 13931\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"08bf9c3b-f56e-4d6c-9374-8c57803eedb8\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"dc7f7633-8fed-40b9-8fb1-7fd6b71a7c51\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 54500\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"2123d908-f76a-47c5-8b64-8f7aa6f7cabc\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"d7371ef7-7cee-4db4-ad2e-4ed3d9836311\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 37069\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"f95c16bc-604a-49eb-9ed1-d751b57b7483\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"965ca5c9-d574-46c4-a653-faa76fbe0362\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 27937\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"de2a5ee3-bf97-46a8-8ed3-9d792c5f7143\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"10482e6a-96eb-46c1-97e9-16ff362d1bb4\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 53866,\n" +
            "        \"minor\": 26214\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"77cf68e8-826f-403e-8cd7-be03a5bae533\",\n" +
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
            "      \"id\": \"cb12e8c4-45b6-48d1-8678-7d0ee30c9495\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"bb6a0516-3275-4631-8b5e-a18ea356424a\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 16149,\n" +
            "        \"minor\": null\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cc022ca6-a5a8-4091-b0b2-38a7461330ed\",\n" +
            "            \"type\": \"beacon_nodes\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"children\": {\n" +
            "          \"data\": [\n" +
            "            {\n" +
            "              \"id\": \"bfcdf359-cf68-4ae2-a95d-6c267f7c1962\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"4861652a-09f7-4d9b-a017-c21db424fd44\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"79d484ec-fa2b-4871-be28-e61e9d3b4969\",\n" +
            "              \"type\": \"beacon_nodes\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"bfcdf359-cf68-4ae2-a95d-6c267f7c1962\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"756951b2-94ec-414b-b422-2cc20e8db363\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 16149,\n" +
            "        \"minor\": 57810\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cb12e8c4-45b6-48d1-8678-7d0ee30c9495\",\n" +
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
            "      \"id\": \"4861652a-09f7-4d9b-a017-c21db424fd44\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"cc2fc97c-f6fb-49a3-9781-4da1a14090e9\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 16149,\n" +
            "        \"minor\": 48022\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cb12e8c4-45b6-48d1-8678-7d0ee30c9495\",\n" +
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
            "      \"id\": \"79d484ec-fa2b-4871-be28-e61e9d3b4969\",\n" +
            "      \"type\": \"beacon_nodes\",\n" +
            "      \"attributes\": {\n" +
            "        \"app_id\": \"86c640e0-146b-4221-b219-71918f94153f\",\n" +
            "        \"identifier\": \"7a1a9c12-bd45-4189-bc00-a678a07b605e\",\n" +
            "        \"proximity_uuid\": \"6c2ae267-d638-42ba-9fce-3524a3968f24\",\n" +
            "        \"major\": 16149,\n" +
            "        \"minor\": 47820\n" +
            "      },\n" +
            "      \"relationships\": {\n" +
            "        \"parent\": {\n" +
            "          \"data\": {\n" +
            "            \"id\": \"cb12e8c4-45b6-48d1-8678-7d0ee30c9495\",\n" +
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
