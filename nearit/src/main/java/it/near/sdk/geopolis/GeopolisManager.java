package it.near.sdk.geopolis;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.geopolis.beacons.AltBeaconMonitor;
import it.near.sdk.geopolis.geofences.GeoFenceMonitor;
import it.near.sdk.geopolis.geofences.GeoFenceSystemEventsReceiver;
import it.near.sdk.geopolis.trackings.Events;
import it.near.sdk.geopolis.trackings.GeopolisTrackingsManager;
import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.RecipeEvaluator;
import it.near.sdk.recipes.pulse.TriggerRequest;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CurrentTime;

/**
 * Manages a beacon forest, the plugin for monitoring regions structured in a tree.
 * This region structure was made up to enable background monitoring of more than 20 regions on iOS.
 * In this plugin every region is specified by ProximityUUID, minor and major. It the current implementation every region is a beacon.
 * The AltBeacon altBeaconMonitor is initialized with this setting:
 * - The background period between scans is 8 seconds.
 * - The background length of a scan 1 second.
 * - The period to wait before finalizing a region exit.
 * <p>
 * In our current plugin representation:
 * - this is a "pulse" plugin
 * - the plugin name is: beacon-forest
 * - the only supported action is: enter_region
 * - the bundle is the id of a region
 *
 * @author cattaneostefano
 */
public class GeopolisManager {

    private static final String TAG = "GeopolisManager";
    private static final String PREFS_SUFFIX = "GeopolisManager";
    public static final String PLUGIN_NAME = "geopolis";
    private static final String NODES_RES = "nodes";

    private static final String RADAR_ON = "radar_on";
    public static final String GF_ENTRY_ACTION_SUFFIX = "REGION_ENTRY";
    public static final String GF_EXIT_ACTION_SUFFIX = "REGION_EXIT";
    public static final String BT_ENTRY_ACTION_SUFFIX = "BT_REGION_ENTRY";
    public static final String BT_EXIT_ACTION_SUFFIX = "BT_REGION_EXIT";
    @Deprecated
    public static final String GF_RANGE_FAR_SUFFIX = "RANGE_FAR";
    public static final String GF_RANGE_NEAR_SUFFIX = "RANGE_NEAR";
    public static final String GF_RANGE_IMMEDIATE_SUFFIX = "RANGE_IMMEDIATE";
    public static final String NODE_ID = "identifier";

    private final Context context;
    private final RecipeEvaluator recipeEvaluator;
    private final GeoFenceMonitor geofenceMonitor;
    private final GlobalConfig globalConfig;
    private final SharedPreferences sp;
    private final GeopolisTrackingsManager geopolisTrackingsManager;

    private final AltBeaconMonitor altBeaconMonitor;
    private final NodesManager nodesManager;

    private final NearAsyncHttpClient httpClient;

    public GeopolisManager(Context context, RecipeEvaluator recipeEvaluator, GlobalConfig globalConfig, TrackManager trackManager) {
        this.context = context;
        this.recipeEvaluator = recipeEvaluator;
        this.globalConfig = globalConfig;

        SharedPreferences nodesManagerSP = NodesManager.getSharedPreferences(context);
        this.nodesManager = new NodesManager(nodesManagerSP);

        this.altBeaconMonitor = new AltBeaconMonitor(context, nodesManager);
        if (isRadarStarted(context)) {
            altBeaconMonitor.startRadar();
        }
        this.geofenceMonitor = new GeoFenceMonitor(context);

        this.geopolisTrackingsManager = new GeopolisTrackingsManager(
                trackManager,
                globalConfig,
                new CurrentTime()
        );

        registerProximityReceiver();
        registerResetReceiver();

        String PACK_NAME = this.context.getApplicationContext().getPackageName();
        String PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        httpClient = new NearAsyncHttpClient(context);
        refreshConfig();
    }

    private void registerProximityReceiver() {
        IntentFilter regionFilter = new IntentFilter();
        String packageName = context.getPackageName();
        regionFilter.addAction(packageName + "." + GF_ENTRY_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + GF_EXIT_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + BT_ENTRY_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + BT_EXIT_ACTION_SUFFIX);
        regionFilter.addAction(packageName + "." + GF_RANGE_NEAR_SUFFIX);
        regionFilter.addAction(packageName + "." + GF_RANGE_IMMEDIATE_SUFFIX);
        context.registerReceiver(regionEventsReceiver, regionFilter);
    }

    private void registerResetReceiver() {
        IntentFilter resetFilter = new IntentFilter();
        String packageName = context.getPackageName();
        resetFilter.addAction(packageName + "." + GeoFenceSystemEventsReceiver.RESET_MONITOR_ACTION_SUFFIX);
        context.registerReceiver(resetEventReceiver, resetFilter);
    }

    /**
     * Refresh the configuration of the component. The list of beacons to altBeaconMonitor will be downloaded from the APIs.
     * If there's an error in refreshing the configuration, a cached version will be used instead.
     */
    public void refreshConfig() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(NODES_RES)
                .appendQueryParameter("filter[app_id]", globalConfig.getAppId())
                .appendQueryParameter(Constants.API.INCLUDE_PARAMETER, "**.children")
                .build();
        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());

                    List<Node> nodes = nodesManager.parseAndSetNodes(response);
                    startRadarOnNodes(nodes);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error " + statusCode);
                    // load the config
                    startRadarOnNodes(nodesManager.getNodes());
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    public void startRadarOnNodes(List<Node> nodes) {
        if (nodes == null) return;
        geofenceMonitor.setUpMonitor(GeoFenceMonitor.filterGeofence(nodes));
        // altBeaconMonitor.setUpMonitor(nodes);
    }

    public void startRadar() {
        if (isRadarStarted(context)) return;
        setRadarState(true);
        List<Node> nodes = nodesManager.getNodes();
        // altBeaconMonitor.setUpMonitor(nodes);
        geofenceMonitor.setUpMonitor(GeoFenceMonitor.filterGeofence(nodes));
        geofenceMonitor.startGFRadar();
    }

    public void stopRadar() {
        setRadarState(false);
        altBeaconMonitor.stopRadar();
        geofenceMonitor.stopGFRadar();
    }

    final BroadcastReceiver regionEventsReceiver = new BroadcastReceiver() {
        public static final String TAG = "RegionEventReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            NearLog.d(TAG, "receiverEvent");
            if (!intent.hasExtra(NODE_ID)) return;
            // trim the package name
            String packageName = GeopolisManager.this.context.getPackageName();
            String action = intent.getAction().replace(packageName + ".", "");
            Node node = nodesManager.nodeFromId(intent.getStringExtra(NODE_ID));
            if (node == null) return;
            switch (action) {
                case GF_ENTRY_ACTION_SUFFIX:
                    trackAndFirePulse(node, Events.ENTER_PLACE);
                    if (node.children != null) {
                        geofenceMonitor.setUpMonitor(GeoFenceMonitor.geofencesOnEnter(nodesManager.getNodes(), node));
                        altBeaconMonitor.addRegions(node.children);
                    }
                    break;
                case GF_EXIT_ACTION_SUFFIX:
                    trackAndFirePulse(node, Events.LEAVE_PLACE);
                    geofenceMonitor.setUpMonitor(GeoFenceMonitor.geofencesOnExit(nodesManager.getNodes(), node));
                    altBeaconMonitor.removeRegions(node.children);
                    break;
                case BT_ENTRY_ACTION_SUFFIX:
                    trackAndFirePulse(node, Events.ENTER_REGION);
                    break;
                case BT_EXIT_ACTION_SUFFIX:
                    trackAndFirePulse(node, Events.LEAVE_REGION);
                    break;
                case GF_RANGE_NEAR_SUFFIX:
                    trackAndFirePulse(node, Events.RANGE_NEAR);
                    break;
                case GF_RANGE_IMMEDIATE_SUFFIX:
                    trackAndFirePulse(node, Events.RANGE_IMMEDIATE);
                    break;
            }
        }
    };

    private void trackAndFirePulse(Node node, Events.GeoEvent event) {
        if (node != null && node.identifier != null) {
            try {
                geopolisTrackingsManager.trackEvent(node.identifier, event.event);
            } catch (JSONException ignored) {
            }
            firePulse(event, node.tags, node.identifier);
        }
    }

    private void firePulse(Events.GeoEvent event, List<String> tags, String pulseBundle) {
        NearLog.d(TAG, "firePulse!");

        TriggerRequest triggerRequest = new TriggerRequest();
        triggerRequest.plugin_name = PLUGIN_NAME;
        triggerRequest.plugin_action = event.event;
        triggerRequest.bundle_id = pulseBundle;
        triggerRequest.plugin_tag_action = event.fallback;
        triggerRequest.tags = tags;
        Map<String, String> metadata = new HashMap<>();
        // TODO build correct format
        metadata.put("node", pulseBundle);
        TrackingInfo trackingInfo = new TrackingInfo();
        trackingInfo.metadata = metadata;
        triggerRequest.trackingInfo = trackingInfo;
        recipeEvaluator.handleTriggerRequest(triggerRequest);
    }

    private final BroadcastReceiver resetEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NearLog.d(TAG, "reset intent received");
            if (intent.getBooleanExtra(GeoFenceSystemEventsReceiver.LOCATION_STATUS, false)) {
                startRadarOnNodes(nodesManager.getNodes());
            } else {
                altBeaconMonitor.stopRadar();
                geofenceMonitor.stopGFRadar();
            }
        }
    };

    /**
     * Returns whether the app started the location radar.
     *
     * @param context the context object
     * @return whether the app started the location radar
     */
    public static boolean isRadarStarted(Context context) {
        String PACK_NAME = context.getApplicationContext().getPackageName();
        SharedPreferences sp = context.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0);
        return sp.getBoolean(RADAR_ON, false);
    }

    private void setRadarState(boolean b) {
        sp.edit().putBoolean(RADAR_ON, b).apply();
    }

    public void initLifecycle(Application application) {
        altBeaconMonitor.initAppLifecycleMonitor(application);
    }
}
