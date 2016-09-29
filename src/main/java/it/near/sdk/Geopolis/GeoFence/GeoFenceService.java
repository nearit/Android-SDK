package it.near.sdk.Geopolis.GeoFence;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cattaneostefano on 18/07/16.
 */
public class GeoFenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeoFenceService";
    private static final String PREF_SUFFIX = "NearGeo";
    private static final String GEO_LIST = "GeofenceList";
    // TODO rename
    public static final String ADD_GEOFENCE_ACTION = "com.example.geofence.add";
    // TODO rename
    public static final String REMOVE_GEOFENCE_ACTION = "com.example.geofence.remove";
    private static final String LIST_IDS = "list_ids";
    List<Geofence> mGeofenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private List<GeoFenceNode> mNearGeoList;
    private final IBinder myBinder = new MyLocalBinder();
    private List<String> currentRequestIds;
    private SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        setUpReceiver();
        loadGeofenceList();
        currentRequestIds = loadIds();
    }


    private void setUpReceiver() {
        Log.d(TAG, "setUpReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ADD_GEOFENCE_ACTION);
        intentFilter.addAction(REMOVE_GEOFENCE_ACTION);
        registerReceiver(geofenceUpdateReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // can be called multiple times
        Log.d(TAG, "onStartCommand()");
        startGoogleApiClient();

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private void startGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private List<String> loadIds() {
        Gson gson = new Gson();
        sp = getSharedPreferences(getSharedPrefName(),0);
        String jsonText = sp.getString(LIST_IDS, null);
        return gson.fromJson(jsonText, new TypeToken<List<String>>(){}.getType());
    }

    private void saveIds(List<String> ids){
        Gson gson = new Gson();
        SharedPreferences.Editor edit = getSharedPreferences(getSharedPrefName(),0).edit();
        edit.putString(LIST_IDS, gson.toJson(ids)).apply();
    }

    private void loadGeofenceList() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<GeoFenceNode>>(){}.getType();
        SharedPreferences sp = getSharedPreferences(getSharedPrefName(), 0);
        String json = sp.getString(GEO_LIST, "");
        if (!json.equals("")){
            mNearGeoList = gson.fromJson(json, type);
        }
        if (mNearGeoList != null)
        {
            for (GeoFenceNode nearGeofence : mNearGeoList) {
                mGeofenceList.add(nearGeofence.toGeofence());
            }
        }

        /*mGeofenceList.add(new Geofence.Builder()
                .setRequestId(GALASSIA_FENCE_ID)
                .setCircularRegion(GALASSIA_LATITUDE, GALASSIA_LONGITUDE, FENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(30000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build()
        );*/
    }

    public void setGeoFences(List<GeoFenceNode> geoFenceNodes) {
        this.mNearGeoList = geoFenceNodes;
        List<String> ids = new ArrayList<>();
        for (GeoFenceNode geoFenceNode : geoFenceNodes) {
            addGeofence(geoFenceNode);
            ids.add(geoFenceNode.getId());
        }
        saveIds(ids);
        startGeoFencing(getGeofencingRequest());
    }

    public void addGeofence(GeoFenceNode nearGeofence) {
        Geofence androidGeofence = nearGeofence.toGeofence();
        mGeofenceList.add(androidGeofence);

        GeofencingRequest request = new GeofencingRequest.Builder()
                .addGeofence(androidGeofence)
                .build();

        mNearGeoList.add(nearGeofence);
        persistGeofenceList();

        startGeoFencing(request);
    }

    public void stopGeofencing(){
        List<String> ids = loadIds();
        if (ids == null) return;
        for (String id : loadIds()) {
            removeGeofence(id);
        }
    }

    public void removeGeofence(String bundleId){
        // using iterator to delete in a loop
        Iterator<GeoFenceNode> i = mNearGeoList.iterator();
        while (i.hasNext()) {
            GeoFenceNode nGeo = i.next();
            if (nGeo.getId().equals(bundleId)){
                i.remove();
            }
        }
        Iterator<Geofence> j = mGeofenceList.iterator();
        while (j.hasNext()) {
            Geofence gFence = j.next();
            if (gFence.getRequestId().equals(bundleId)){
                j.remove();
            }
        }
        persistGeofenceList();
        List<String> gfReqIds = new ArrayList<String>();
        gfReqIds.add(bundleId);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, gfReqIds)
            .setResultCallback(this);
    }

    private void startGeoFencing(GeofencingRequest request) {
        if (request == null) return;
        if (!mGoogleApiClient.isConnected()) return;
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

    private void persistGeofenceList() {
        Gson gson = new Gson();
        String json = gson.toJson(mNearGeoList);
        SharedPreferences.Editor edit = getSharedPreferences(getSharedPrefName(),0).edit();
        edit.putString(GEO_LIST, json).apply();
    }


    private GeofencingRequest getGeofencingRequest() {
        if (mGeofenceList == null || mGeofenceList.size() == 0){
            return null;
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        // builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG , "onConnected");
        if (mNearGeoList == null){
            startGeoFencing(getGeofencingRequest());
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

    public String getSharedPrefName() {
        String PACK_NAME = getApplicationContext().getPackageName();
        return PACK_NAME + PREF_SUFFIX;
    }

    BroadcastReceiver geofenceUpdateReceiver = new BroadcastReceiver() {
        static final String TAG = "GeoUpdate";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received update on regions");
            switch (intent.getAction()){
                case ADD_GEOFENCE_ACTION:
                    GeoFenceNode geofence = intent.getParcelableExtra("geofence");
                    Log.d(TAG, "Start monitoring geofence: " + geofence.getId());
                    addGeofence(geofence);
                    break;
                case REMOVE_GEOFENCE_ACTION:
                    String geofenceBundleId = intent.getStringExtra("geofence_bundle");
                    Log.d(TAG, "Stop monitoring geofence: " + geofenceBundleId);
                    removeGeofence(geofenceBundleId);
                    break;
                default:
            }
        }
    };

    public class MyLocalBinder extends Binder {
        GeoFenceService getService() {
            return GeoFenceService.this;
        }
    }
}
