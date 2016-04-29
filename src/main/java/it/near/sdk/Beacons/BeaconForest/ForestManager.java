package it.near.sdk.Beacons.BeaconForest;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

import it.near.sdk.Beacons.Monitoring.AltBeaconMonitor;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.Communication.NearNetworkUtil;
import it.near.sdk.GlobalState;
import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * Manages a beacon forest, the plugin for monitoring regions structured in a tree.
 * This region structure was made up to enable background monitoring of more than 20 regions on iOS.
 * In this plugin every region is specified by ProximityUUID, minor and major. It the current implementation every region is a beacon.
 *
 * In our current plugin representation:
 * - this is a "pulse" plugin
 * - the ingredient name (the name of the plugin) is: beacon-forest
 * - the only supported flavor is: enter_region
 * - the slice is the id of a region
 *
 * Created by cattaneostefano on 13/04/16.
 */
public class ForestManager implements BootstrapNotifier {

    private static final String TAG = "ForestManager";
    public static final String PREFS_SUFFIX = "NearBeacons";
    private static final String INGREDIENT_NAME = "beacon-forest";
    private static final String ENTER_REGION = "enter_region";

    private static ForestManager mInstance = null;
    private final RecipesManager recipesManager;
    private final String PREFS_NAME;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    private long backgroundBetweenScanPeriod = 8000l;
    private long backgroundScanPeriod = 1000l;
    private long regionExitPeriod = 30000l;

    List<Beacon> beaconList;
    Context mContext;
    private Morpheus morpheus;
    private AltBeaconMonitor monitor;

    public ForestManager(Context mContext, AltBeaconMonitor monitor, RecipesManager recipesManager) {
        this.mContext = mContext;
        this.monitor = monitor;
        this.recipesManager = recipesManager;
        setUpMorpheusParser();

        String PACK_NAME = mContext.getApplicationContext().getPackageName();
        PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
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


    public void refreshConfig(){

        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, Constants.API.PLUGINS.beacon_forest + "/beacons", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    ULog.d(TAG, response.toString());
                    List<Beacon> beacons = NearUtils.parseList(morpheus, response, Beacon.class);
                    beaconList = extractLeafs(beacons);
                    monitorBeacons(beaconList);
                    persistList(beaconList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "Error " + error);
                    try {
                        beaconList = loadChachedList();
                        if (beaconList!=null){
                            monitorBeacons(beaconList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }));

    }

    private void persistList(List<Beacon> beaconList) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(beaconList);
        ULog.d(TAG , "Persist: " + listStringified);
        editor.putString(TAG , listStringified);
        editor.apply();
    }

    private List<Beacon> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Beacon>>(){}.getType();
        ArrayList<Beacon> beacons = gson.fromJson(sp.getString(TAG, ""), collectionType);
        return beacons;
    }

    /**
     * Creates a list of AltBeacon regions from Near Regions
     * @param beacons
     */
    private void monitorBeacons(List<Beacon> beacons) {
        List<Region> regionsToMonitor = new ArrayList<>();
        for (Beacon beacon : beacons){
            String uniqueId = "Region" + Integer.toString(beacon.getMajor()) + Integer.toString(beacon.getMinor());
            Region region = new Region(uniqueId, Identifier.parse(beacon.getUuid()),
                    Identifier.fromInt(beacon.getMajor()), Identifier.fromInt(beacon.getMinor()));
            regionsToMonitor.add(region);
        }
        monitor.startRadar(backgroundBetweenScanPeriod, backgroundScanPeriod, regionExitPeriod, regionsToMonitor, this);
    }

    /**
     * Extract leaf regions from a list of root regions with references to children.
     * For now, leafs are only at 1 level of depth
     * @param beacons
     * @return
     */
    private List<Beacon> extractLeafs(List<Beacon> beacons) {
        List<Beacon> allBeacons = new ArrayList<>();
        for (Beacon beacon : beacons){
            allBeacons.addAll(parseTree(beacon));
        }
        return allBeacons;
    }

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

    private void trackRegionEnter(String regionSlice) {
        try {
            NearNetworkUtil.sendTrack(mContext, Constants.API.PLUGINS.beacon_forest + "/trackings" ,buildTrackBody(regionSlice));
        } catch (JSONException e) {
            ULog.d(TAG, "Unable to send track: " +  e.toString());
        }
    }

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

    private void firePulse(String flavor, String pulseSlice) {
        ULog.d(TAG, "firePulse!");
        recipesManager.gotPulse(INGREDIENT_NAME, flavor, pulseSlice);
    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
