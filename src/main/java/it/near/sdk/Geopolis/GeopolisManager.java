package it.near.sdk.Geopolis;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Geopolis.Beacons.AltBeaconMonitor;
import it.near.sdk.Geopolis.Beacons.BeaconNode;
import it.near.sdk.Geopolis.GeoFence.GeoFenceMonitor;
import it.near.sdk.Geopolis.GeoFence.GeoFenceNode;
import it.near.sdk.Geopolis.BeaconForest.NearBeacon;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearAsyncHttpClient;
import it.near.sdk.Communication.NearNetworkUtil;
import it.near.sdk.Geopolis.GeoFence.GeoFenceSystemEventsReceiver;
import it.near.sdk.GlobalConfig;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.Reactions.Event;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Trackings.Events;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * Manages a beacon forest, the plugin for monitoring regions structured in a tree.
 * This region structure was made up to enable background monitoring of more than 20 regions on iOS.
 * In this plugin every region is specified by ProximityUUID, minor and major. It the current implementation every region is a beacon.
 * The AltBeacon altBeaconMonitor is initialized with this setting:
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
public class GeopolisManager {

    // ---------- beacon forest ----------
    public static final String BEACON_FOREST_PATH =         "beacon-forest";
    public static final String BEACON_FOREST_TRACKINGS =    "trackings";
    public static final String BEACON_FOREST_BEACONS =      "beacons";

    private static final String TAG = "GeopolisManager";
    private static final String PREFS_SUFFIX = "GeopolisManager";
    private static final String PLUGIN_NAME = "geopolis";

    private static final String RADAR_ON = "radar_on";
    private static final String GEOPOLIS_CONFIG = "cached_config";
    public static final String GF_ENTRY_ACTION_SUFFIX = "REGION_ENTRY";
    public static final String GF_EXIT_ACTION_SUFFIX = "REGION_EXIT";
    public static final String BT_ENTRY_ACTION_SUFFIX = "BT_REGION_ENTRY";
    public static final String BT_EXIT_ACTION_SUFFIX = "BT_REGION_EXIT";
    public static final String NODE_ID = "identifier";
    public static final String RESET_MONITOR_ACTION_SUFFIX = "RESET_SCAN";

    private final RecipesManager recipesManager;
    private final String PREFS_NAME;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    private List<NearBeacon> beaconList;
    private List<Region> regionList;
    private Application mApplication;
    private Morpheus morpheus;
    private AltBeaconMonitor altBeaconMonitor;
    private final GeoFenceMonitor geofenceMonitor;

    private NearAsyncHttpClient httpClient;
    private List<Node> nodes;

    public GeopolisManager(Application application, RecipesManager recipesManager) {
        this.mApplication = application;
        this.recipesManager = recipesManager;
        this.altBeaconMonitor = new AltBeaconMonitor(application);
        this.geofenceMonitor = new GeoFenceMonitor(application);
        setUpMorpheusParser();
        registerProximityReceiver();
        registerResetReceiver();

        String PACK_NAME = mApplication.getApplicationContext().getPackageName();
        PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = mApplication.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();

        httpClient = new NearAsyncHttpClient();
        refreshConfig();
    }

    private void registerProximityReceiver() {
        IntentFilter regionFilter = new IntentFilter();
        String packageName = mApplication.getPackageName();
        regionFilter.addAction(packageName + "." + GF_ENTRY_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + GF_EXIT_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + BT_ENTRY_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + BT_EXIT_ACTION_SUFFIX);
        mApplication.registerReceiver(regionEventsReceiver, regionFilter);
    }

    private void registerResetReceiver() {
        IntentFilter resetFilter = new IntentFilter();
        String packageName = mApplication.getPackageName();
        resetFilter.addAction(packageName + "." + RESET_MONITOR_ACTION_SUFFIX);
        mApplication.registerReceiver(resetEventReceiver, resetFilter);
    }


    /**
     * Set up Morpheus parser. Morpheus parses jsonApi encoded resources
     * https://github.com/xamoom/Morpheus
     * We didn't actually use this library due to its minSdkVersion. We instead imported its code and adapted it. And then fixed it.
     */
    private void setUpMorpheusParser() {
        morpheus = new Morpheus();
        // register your resources

        morpheus.getFactory().getDeserializer().registerResourceClass("nodes", Node.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("beacon_nodes", BeaconNode.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("geofence_nodes", GeoFenceNode.class);
    }

    /**
     * Refresh the configuration of the component. The list of beacons to altBeaconMonitor will be downloaded from the APIs.
     * If there's an error in refreshing the configuration, a cached version will be used instead.
     *
     */
    public void refreshConfig(){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(BEACON_FOREST_PATH)
                    .appendPath(BEACON_FOREST_BEACONS).build();
        url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath("geopolis")
                .appendPath("nodes")
                .appendQueryParameter("filter[app_id]",GlobalConfig.getInstance(mApplication).getAppId())
                .appendQueryParameter("include", "children.*.children")
                .build();
        try {
            httpClient.nearGet(mApplication, url.toString(), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    saveConfir(response.toString());

                    nodes = NearUtils.parseList(morpheus, response, Node.class);
                    startRadarOnNodes(nodes);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    ULog.d(TAG, "Error " + statusCode);
                    // load the config
                    if (nodes == null){
                        try {
                            nodes = loadNodes();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    startRadarOnNodes(nodes);
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }

    public void startRadarOnNodes(List<Node> nodes) {
        if (nodes == null) return;
        geofenceMonitor.setUpMonitor(GeoFenceMonitor.filterGeofence(nodes));
        // altBeaconMonitor.setUpMonitor(nodes);
    }

    public void startRadar(){
        if (isRadarStarted(mApplication)) return;
        setRadarState(true);
        if (nodes == null){
            try {
                nodes = loadNodes();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // TODO reset geopolis on radar start?
        // altBeaconMonitor.setUpMonitor(nodes);
        geofenceMonitor.setUpMonitor(GeoFenceMonitor.filterGeofence(nodes));
        geofenceMonitor.startGFRadar();
    }

    public void stopRadar(){
        setRadarState(false);
        altBeaconMonitor.stopRadar();
        geofenceMonitor.stopGFRadar();
    }

    private List<Node> loadNodes() throws JSONException {
        String config = getSavedConfig();
        if (config == null) return null;
        JSONObject configJson = new JSONObject(config);
        return NearUtils.parseList(morpheus, configJson, Node.class);
    }

    private String getSavedConfig(){
        return sp.getString(GEOPOLIS_CONFIG, null);
    }

    private void saveConfir(String json){
        editor.putString(GEOPOLIS_CONFIG, json).apply();
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
        // altBeaconMonitor.startRadar(backgroundBetweenScanPeriod, backgroundScanPeriod, regionExitPeriod, regionsToMonitor, this);
        if (sp.getBoolean(RADAR_ON, false)){
            // altBeaconMonitor.startRadar(regionsToMonitor, this);
        }
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


    BroadcastReceiver regionEventsReceiver = new BroadcastReceiver() {
        public static final String TAG = "RegionEventReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            ULog.wtf(this.TAG, "receiverEvent");
            if (!intent.hasExtra(NODE_ID)) return;
            // trim the package name
            String packageName = mApplication.getPackageName();
            String action = intent.getAction().replace(packageName + ".", "");
            Node node = nodeFromId(intent.getStringExtra(NODE_ID));

            if (node == null) return;
            switch (action){
                case GF_ENTRY_ACTION_SUFFIX:
                    trackAndFirePulse(node.getIdentifier(), Events.ENTER_PLACE);
                    if (node.getChildren() != null){
                        geofenceMonitor.setUpMonitor(GeoFenceMonitor.geofencesOnEnter(nodes, node));
                        altBeaconMonitor.addRegions(node.getChildren());
                    }
                    break;
                case GF_EXIT_ACTION_SUFFIX:
                    trackAndFirePulse(node.getIdentifier(), Events.LEAVE_PLACE);
                    geofenceMonitor.setUpMonitor(GeoFenceMonitor.geofencesOnExit(nodes, node));
                    altBeaconMonitor.removeRegions(node.getChildren());

                    break;
                case BT_ENTRY_ACTION_SUFFIX:
                    trackAndFirePulse(node.getIdentifier(), Events.ENTER_REGION);
                    break;
                case BT_EXIT_ACTION_SUFFIX:
                    trackAndFirePulse(node.getIdentifier(), Events.LEAVE_REGION);
                    break;
            }
        }
    };

    /**
     * Tracks the geographical interaction and fires the proper pulse. It does nothing if the identifier is null.
     * @param identifier
     * @param event
     */
    private void trackAndFirePulse(String identifier, String event) {
        if (identifier != null){
            trackEvent(identifier, event);
            firePulse(event, identifier);
        }
    }

    private BroadcastReceiver resetEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ULog.wtf(TAG, "reset intent received");
            if (intent.getBooleanExtra(GeoFenceSystemEventsReceiver.LOCATION_STATUS, false)){
                if (nodes == null) {
                    try {
                        nodes = loadNodes();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                startRadarOnNodes(nodes);
            } else {
                altBeaconMonitor.stopRadar();
                geofenceMonitor.stopGFRadar();
            }

        }
    };

    private Node nodeFromId(String id) {
        if (nodes == null) {
            try {
                nodes = loadNodes();
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return findNode(nodes, id);
    }

    private Node findNode(List<Node> nodes, String id) {
        if (nodes == null){
            try {
                nodes = loadNodes();
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            if (nodes == null) return null;
        }
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().equals(id)) return node;
            Node foundNode = findNode(node.getChildren(), id);
            if (foundNode != null) return foundNode;
        }
        return null;
    }

    /**
     * Send tracking data to the Forest beacon APIs about a region enter (every beacon is a region).
     */
    private void trackEvent(String identifier, String event) {
        try {
            Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(BEACON_FOREST_PATH)
                    .appendPath(BEACON_FOREST_TRACKINGS).build();
            NearNetworkUtil.sendTrack(mApplication, url.toString(), buildTrackBody(identifier, event));
        } catch (JSONException e) {
            ULog.d(TAG, "Unable to send track: " +  e.toString());
        }
    }


    /**
     * Compute the HTTP request body from the region identifier in jsonAPI format.
     * @param identifier the node identifier
     * @param event the event
     * @return the correctly formed body
     * @throws JSONException
     */
    private String buildTrackBody(String identifier, String event) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("identifier" , identifier);
        map.put("event", event);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date now = new Date(System.currentTimeMillis());
        String formatted = sdf.format(now);
        map.put("tracked_at", formatted);
        map.put("profile_id", GlobalConfig.getInstance(mApplication).getProfileId());
        map.put("installation_id", GlobalConfig.getInstance(mApplication).getInstallationId());
        return NearUtils.toJsonAPI("trackings", map);
    }



    /**
     * Returns wether the app started the location radar.
     * @param context
     * @return
     */
    public static boolean isRadarStarted(Context context){
        String PACK_NAME = context.getApplicationContext().getPackageName();
        SharedPreferences sp = context.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0);
        return sp.getBoolean(RADAR_ON, false);
    }

    public void setRadarState(boolean b){
        String PACK_NAME = mApplication.getApplicationContext().getPackageName();
        SharedPreferences.Editor edit = mApplication.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0).edit();
        edit.putBoolean(RADAR_ON, b).apply();

    }

}
