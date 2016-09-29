package it.near.sdk.Geopolis.GeoFence;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.near.sdk.Geopolis.GeopolisManager;
import it.near.sdk.Geopolis.Node;

/**
 * Created by cattaneostefano on 28/09/2016.
 */

public class GeoFenceMonitor {

    private Context mContext;
    private List<GeoFenceNode> currentGeofences;
    GeoFenceService geoFenceService;
    private boolean isBound;

    public GeoFenceMonitor(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Set a list of geofence and start the geofence radar
     * @param nodes
     */
    public void setUpMonitor(List<Node> nodes){
        isBound = false;
        currentGeofences = filterGeofence(nodes);
        if (GeopolisManager.isRadarStarted(mContext) && currentGeofences.size()>0){
            startGFRadar();
        }
    }

    private void startGFRadar() {
        Intent serviceIntent = new Intent(mContext, GeoFenceService.class);
        if (!isBound){
            mContext.bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
            isBound = true;
            mContext.startService(serviceIntent);
        }
    }

    private void stopGFRadar(){
        if (isBound){
            isBound = false;
            mContext.unbindService(myConnection);
            mContext.stopService(new Intent(mContext, GeoFenceService.class));
        }
    }

    /**
     * From a list of node, filters the top level geofences in the list
     * @param nodes
     * @return
     */
    private List<GeoFenceNode> filterGeofence(List<Node> nodes) {
        List<GeoFenceNode> geoFenceNodeList = new ArrayList<>();
        for (Node node : nodes) {
            if (node instanceof GeoFenceNode){
                geoFenceNodeList.add((GeoFenceNode) node);
            }
        }
        return geoFenceNodeList;
    }

    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            geoFenceService = ((GeoFenceService.MyLocalBinder) service).getService();
            isBound = true;
            geoFenceService.setGeoFences(currentGeofences);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
