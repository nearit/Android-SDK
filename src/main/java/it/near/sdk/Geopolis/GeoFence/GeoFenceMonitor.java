package it.near.sdk.Geopolis.GeoFence;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.near.sdk.Geopolis.GeopolisManager;
import it.near.sdk.Geopolis.Node;

/**
 * Created by cattaneostefano on 28/09/2016.
 */

public class GeoFenceMonitor {

    private static final String TAG = "GeoFenceMonitor";
    private static final String CURRENT_GEOFENCES = "current_geofences";
    private Context mContext;
    private List<GeoFenceNode> currentGeofences;
    GeoFenceService geoFenceService;
    private static final String PREFS_SUFFIX = "NearGeoMonitor";

    public GeoFenceMonitor(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Set a list of geofence and start the geofence radar
     * @param nodes
     */
    public void setUpMonitor(List<GeoFenceNode> nodes){
        currentGeofences = nodes;
        persistCurrentGeofences(mContext, currentGeofences);
        if (GeopolisManager.isRadarStarted(mContext) && currentGeofences.size()>0){
            startGFRadar();
        }
    }


    public void startGFRadar() {
        Intent serviceIntent = new Intent(mContext, GeoFenceService.class);
        serviceIntent.putParcelableArrayListExtra(GeoFenceService.GEOFENCES, (ArrayList<? extends Parcelable>) currentGeofences);
        mContext.startService(serviceIntent);
    }

    public void stopGFRadar(){
        mContext.stopService(new Intent(mContext, GeoFenceService.class));
    }

    /**
     * From a list of node, filters the top level geofences in the list
     * @param nodes
     * @return
     */
    public static List<GeoFenceNode> filterGeofence(List<Node> nodes) {
        List<GeoFenceNode> geoFenceNodeList = new ArrayList<>();
        for (Node node : nodes) {
            if (node instanceof GeoFenceNode){
                geoFenceNodeList.add((GeoFenceNode) node);
            }
        }
        return geoFenceNodeList;
    }

    public static void persistCurrentGeofences(Context context, List<GeoFenceNode> currentGeofences) {
        String PACK_NAME = context.getApplicationContext().getPackageName();
        SharedPreferences.Editor edit = context.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0).edit();
        Gson gson = new GsonBuilder().setExclusionStrategies(GeoFenceNode.getExclusionStrategy()).create();
        String json = gson.toJson(currentGeofences);
        edit.putString(CURRENT_GEOFENCES, json).apply();
    }

    public static List<GeoFenceNode> getCurrentGeofences(Context context){
        String PACK_NAME = context.getApplicationContext().getPackageName();
        SharedPreferences sp = context.getSharedPreferences(PACK_NAME + PREFS_SUFFIX, 0);
        Gson gson = new Gson();
        String json = sp.getString(CURRENT_GEOFENCES, null);
        Type type = new TypeToken<ArrayList<GeoFenceNode>>() {}.getType();
        ArrayList<GeoFenceNode> nodes = gson.fromJson(json, type);
        return nodes;
    }

    public static List<GeoFenceNode> geofencesOnEnter(List<Node> nodes, Node node) {
        if (nodes == null || node == null){
            return new ArrayList<>();
        }
        List<GeoFenceNode> toListen = new ArrayList<>();
        // add children
        toListen.addAll(filterGeofence(node.getChildren()));
        if (node.getParent() != null){
            toListen.addAll(filterGeofence(node.getParent().getChildren()));
        } else {
            toListen.addAll(filterGeofence(nodes));
        }
        return toListen;
    }

    public static List<GeoFenceNode> geofencesOnExit(List<Node> nodes, Node node){
        if (nodes == null || node == null){
            return new ArrayList<>();
        }
        List<Node> toListen = new ArrayList<>();
        if (filterGeofence(nodes).contains(node)){
            // node is top level so we add all top level nodes
            toListen.addAll(nodes);
        } else {
            // node has a parent
            if (node.getParent().getParent()!=null){
                // node has a grand parent
                // so we add all the grand parent children, aka parent sibilings
                toListen.addAll(node.getParent().getParent().getChildren());
            } else {
                // node is a child of a top level node, so we add top level nodes, aka parent sibilings
                toListen.addAll(nodes);
            }
            // we add the node sibilings
            toListen.addAll(node.getParent().getChildren());
        }
        return filterGeofence(toListen);
    }

}
