package it.near.sdk.Geopolis.GeoFence;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Geopolis.GeopolisManager;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 18/07/16.
 */
public class GeoFenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeoFenceService";
    private static final String PREF_SUFFIX = "NearGeo";
    private static final String GEO_LIST = "GeofenceList";
    private static final String LIST_IDS = "list_ids";
    public static final String GEOFENCES = "geofences";
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private List<GeoFenceNode> mNearGeoList;
    private SharedPreferences sp;
    private List<Geofence> mPendingGeofences = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        // loadGeofenceList();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // can be called multiple times
        Log.d(TAG, "onStartCommand()");
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            startGoogleApiClient();
        }

        if (intent != null && intent.hasExtra(GEOFENCES)){
            List<GeoFenceNode> nodes = intent.getParcelableArrayListExtra(GEOFENCES);
            mPendingGeofences = GeoFenceNode.toGeofences(nodes);
            if (GeopolisManager.isRadarStarted(this)){
                setGeoFences(nodes);
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        ULog.d(TAG, "onDestroy on geofence service");
        super.onDestroy();
        stopAllGeofences();
        resetIds(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Create and start the google api client for the geofences.
     * Set this service as the listener for the connection callback methods.
     */
    private void startGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Load geofence request ids from disk.
     * @return
     */
    private List<String> loadIds() {
        Gson gson = new Gson();
        sp = getSharedPreferences(getSharedPrefName(this),0);
        String jsonText = sp.getString(LIST_IDS, null);
        return gson.fromJson(jsonText, new TypeToken<List<String>>(){}.getType());
    }

    /**
     * Overwrite geofence request ids.
     * @param ids
     */
    private void saveIds(List<String> ids){
        Gson gson = new Gson();
        SharedPreferences.Editor edit = getSharedPreferences(getSharedPrefName(this),0).edit();
        edit.putString(LIST_IDS, gson.toJson(ids)).apply();
    }

    /**
     * Reset the listened geofence request ids.
     * @param context
     */
    public static void resetIds(Context context){
        SharedPreferences.Editor edit = context.getSharedPreferences(getSharedPrefName(context),0).edit();
        edit.putString(LIST_IDS, null).apply();
    }

    /**
     *
     */
    private void loadGeofenceList() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<GeoFenceNode>>(){}.getType();
        SharedPreferences sp = getSharedPreferences(getSharedPrefName(this), 0);
        String json = sp.getString(GEO_LIST, "");
        if (!json.equals("")){
            mNearGeoList = gson.fromJson(json, type);
        }

    }

    /**
     * Set the geofence to monitor. Filters the geofence to only add the new ones and to
     * remove the geofences it has no longer to monitor. Persists the new ids.
     * @param geoFenceNodes
     */
    private void setGeoFences(List<GeoFenceNode> geoFenceNodes) {
        // get the ids of the geofence to monitor
        List<String> newIds = idsFromGeofences(geoFenceNodes);

        // subtracting the new ids to the old ones, find the geofence to stop monitoring
        List<String> idsToremove = loadIds();
        if (idsToremove != null){
            idsToremove.removeAll(newIds);
            stopGeofencing(idsToremove);
        }

        // from the old and the new sets of ids, find the geofence to add
        List<Geofence> geofencesToAdd = fetchNewGeofence(geoFenceNodes, newIds, loadIds());
        startGeoFencing(getGeofencingRequest(geofencesToAdd));
        saveIds(newIds);
    }

    private List<Geofence> fetchNewGeofence(List<GeoFenceNode> geoFenceNodes, List<String> newIds, List<String> oldIds) {
        // create copy of the new ids
        List<String> idsToAdd = new ArrayList<>(newIds);
        // subtract the old ids to the new
        if (oldIds!=null){
            idsToAdd.removeAll(oldIds);
        }

        // for each id, fetch the geofence and return them all
        List<Geofence> geoFenceNodesToAdd = new ArrayList<>();
        for (String id : idsToAdd) {
            GeoFenceNode geoFenceNode = getGeofenceFromId(geoFenceNodes, id);
            if (geoFenceNode != null){
                geoFenceNodesToAdd.add(geoFenceNode.toGeofence());
            }
        }
        return geoFenceNodesToAdd;
    }

    /**
     * Stop all geofences
     */
    public void stopAllGeofences(){
        stopGeofencing(loadIds());
        saveIds(new ArrayList<String>());
    }

    /**
     * Find a geofence from a geofence list, given its id
     * @param geoFenceNodes
     * @param id
     * @return
     */
    private GeoFenceNode getGeofenceFromId(List<GeoFenceNode> geoFenceNodes, String id) {
        for (GeoFenceNode geoFenceNode : geoFenceNodes) {
            if (geoFenceNode.getId().equals(id)){
                return geoFenceNode;
            }
        }
        return null;
    }

    private List<String> idsFromGeofences(List<GeoFenceNode> geoFenceNodes) {
        List<String> ids = new ArrayList<>();
        for (GeoFenceNode geoFenceNode : geoFenceNodes) {
            ids.add(geoFenceNode.getId());
        }
        return ids;
    }


    /**
     * Stop geofencing on request ids.
     * @param idsToremove
     */
    public void stopGeofencing(List<String> idsToremove){
        if (idsToremove == null || idsToremove.size() == 0) return;

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idsToremove)
                .setResultCallback(this);
    }

    /**
     * Start geofencing from a geofence request. If the google api client is not yet connected,
     * store the geofecing as pending to be started once the client is connected.
     * @param request
     */
    private void startGeoFencing(GeofencingRequest request) {
        if (request == null) return;
        if (!mGoogleApiClient.isConnected()){
            mPendingGeofences = request.getGeofences();
            return;
        }
        mPendingGeofences.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "startGeofencing");
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                request,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }



    /**
     * Builds the geofence request from a list of geofences
     * @return
     */
    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        if (geofences == null || geofences.size() == 0){
            return null;
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        // builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    /**
     * Build the geofence pending intent.
     * @return
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, NearGeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        // If we have pending geofences (that need to be started)
        // we start the geofence now that the client is connected.
        if (mPendingGeofences != null){
            startGeoFencing(getGeofencingRequest(mPendingGeofences));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG, "onResult: " + status.getStatusMessage() );
    }

    public static String getSharedPrefName(Context context) {
        String PACK_NAME = context.getApplicationContext().getPackageName();
        return PACK_NAME + PREF_SUFFIX;
    }

}
