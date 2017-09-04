package it.near.sdk.geopolis.geofences;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.geopolis.Node;
import it.near.sdk.logging.NearLog;
import it.near.sdk.utils.AppVisibilityDetector;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 * Created by cattaneostefano on 28/09/2016.
 */

public class GeoFenceMonitor implements AppVisibilityDetector.AppVisibilityCallback {

    private static final String TAG = "GeoFenceMonitor";
    private static final String CURRENT_GEOFENCES = "current_geofences";
    private Context mContext;
    private List<GeofenceNode> currentGeofences;
    private static final String PREFS_SUFFIX = "NearGeoMonitor";
    private FusedLocationProviderClient mFusedLocationClient;

    public GeoFenceMonitor(Context mContext) {
        this.mContext = mContext;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    /**
     * Set a list of geofence and start the geofence radar
     *
     * @param nodes
     */
    public void setUpMonitor(List<GeofenceNode> nodes) {
        currentGeofences = nodes;
        persistCurrentGeofences(mContext, currentGeofences);
        if (GeopolisManager.isRadarStarted(mContext) && currentGeofences.size() > 0) {
            startGFRadar();
        }
    }


    public void startGFRadar() {
        Intent serviceIntent = new Intent(mContext, GeoFenceService.class);
        serviceIntent.putParcelableArrayListExtra(GeoFenceService.GEOFENCES, (ArrayList<? extends Parcelable>) currentGeofences);
        mContext.startService(serviceIntent);
    }

    public void stopGFRadar() {
        mContext.stopService(new Intent(mContext, GeoFenceService.class));
    }

    /**
     * From a list of node, filters the top level geofences in the list
     *
     * @param nodes
     * @return
     */
    public static List<GeofenceNode> filterGeofence(List<Node> nodes) {
        List<GeofenceNode> geofenceNodeList = new ArrayList<>();
        if (nodes == null) return geofenceNodeList;
        for (Node node : nodes) {
            if (node instanceof GeofenceNode) {
                geofenceNodeList.add((GeofenceNode) node);
            }
        }
        return geofenceNodeList;
    }

    public static void persistCurrentGeofences(Context context, List<GeofenceNode> currentGeofences) {
        String PACK_NAME = context.getApplicationContext().getPackageName();
        SharedPreferences.Editor edit = context.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0).edit();
        Gson gson = new GsonBuilder().setExclusionStrategies(GeofenceNode.getExclusionStrategy()).create();
        String json = gson.toJson(currentGeofences);
        edit.putString(CURRENT_GEOFENCES, json).apply();
    }

    public static List<GeofenceNode> getCurrentGeofences(Context context) {
        String PACK_NAME = context.getApplicationContext().getPackageName();
        SharedPreferences sp = context.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0);
        Gson gson = new Gson();
        String json = sp.getString(CURRENT_GEOFENCES, null);
        Type type = new TypeToken<ArrayList<GeofenceNode>>() {
        }.getType();
        return gson.<ArrayList<GeofenceNode>>fromJson(json, type);
    }

    public static List<GeofenceNode> geofencesOnEnter(List<Node> nodes, Node node) {
        if (nodes == null || node == null) {
            return new ArrayList<>();
        }
        List<GeofenceNode> toListen = new ArrayList<>();
        // add children
        toListen.addAll(filterGeofence(node.children));
        if (node.parent != null) {
            toListen.addAll(filterGeofence(node.parent.children));
        } else {
            toListen.addAll(filterGeofence(nodes));
        }
        return toListen;
    }

    public static List<GeofenceNode> geofencesOnExit(List<Node> nodes, Node node) {
        if (nodes == null || node == null) {
            return new ArrayList<>();
        }
        List<Node> toListen = new ArrayList<>();
        if (filterGeofence(nodes).contains(node)) {
            // node is top level so we add all top level nodes
            toListen.addAll(nodes);
        } else {
            // node has a parent
            if (node.parent.parent != null) {
                // node has a grand parent
                // so we add all the grand parent children, aka parent sibilings
                toListen.addAll(node.parent.parent.children);
            } else {
                // node is a child of a top level node, so we add top level nodes, aka parent sibilings
                toListen.addAll(nodes);
            }
            // we add the node sibilings
            toListen.addAll(node.parent.children);
        }
        return filterGeofence(toListen);
    }

    public void initAppLifecycleMonitor(Application application) {
        AppVisibilityDetector.init(application, this);
    }

    @Override
    public void onAppGotoForeground() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NearLog.i(TAG, "Requesting single location");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000L);
        mLocationRequest.setFastestInterval(2000L);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onAppGotoBackground() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            NearLog.i(TAG, "Got location update");
        }
    };
}
