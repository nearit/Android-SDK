package it.near.sdk.geopolis.geofences;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.logging.NearLog;

/**
 * Service for monitoring geofences.
 * Created by cattaneostefano on 18/07/16.
 */
public class GeoFenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeoFenceService";
    private static final String PREF_SUFFIX = "NearGeo";
    private static final String LIST_IDS = "list_ids";
    public static final String GEOFENCES = "geofences";
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> mPendingGeofences = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofencingClient geofencingClient;

    @Override
    public void onCreate() {
        super.onCreate();
        NearLog.v(TAG, "onCreate()");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // can be called multiple times
        NearLog.v(TAG, "onStartCommand()");
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            startGoogleApiClient();
        }

        if (intent != null && intent.hasExtra(GEOFENCES)) {
            List<GeofenceNode> nodes = intent.getParcelableArrayListExtra(GEOFENCES);
            mPendingGeofences = GeofenceNode.toGeofences(nodes);
            if (GeopolisManager.isRadarStarted(this)) {
                setGeoFences(nodes);
                // pingSingleLocation();
            }
        }

        return Service.START_STICKY;
    }

    private void pingSingleLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            NearLog.v(TAG, "location: " + location.getLongitude() + " " + location.getLatitude());
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        NearLog.v(TAG, "onDestroy on geofence service");
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
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Load geofence request ids from disk.
     */
    private List<String> loadIds() {
        Gson gson = new Gson();
        SharedPreferences sp = getSharedPreferences(getSharedPrefName(this), 0);
        String jsonText = sp.getString(LIST_IDS, null);
        // TODO not catching the unchecked exception is dangerous
        return gson.fromJson(jsonText, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * Overwrite geofence request ids.
     */
    private void saveIds(List<String> ids) {
        Gson gson = new Gson();
        SharedPreferences.Editor edit = getSharedPreferences(getSharedPrefName(this), 0).edit();
        edit.putString(LIST_IDS, gson.toJson(ids)).apply();
    }

    /**
     * Reset the listened geofence request ids.
     */
    public static void resetIds(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(getSharedPrefName(context), 0).edit();
        edit.putString(LIST_IDS, null).apply();
    }

    /**
     * Set the geofence to monitor. Filters the geofence to only add the new ones and to
     * remove the geofences it has no longer to monitor. Persists the new ids.
     *
     * @param geofenceNodes
     */
    private void setGeoFences(List<GeofenceNode> geofenceNodes) {
        // get the ids of the geofence to monitor
        List<String> newIds = idsFromGeofences(geofenceNodes);

        // subtracting the new ids to the old ones, find the geofence to stop monitoring
        List<String> idsToremove = loadIds();
        if (idsToremove != null) {
            idsToremove.removeAll(newIds);
            stopGeofencing(idsToremove);
        }

        // from the old and the new sets of ids, find the geofence to add
        List<Geofence> geofencesToAdd = fetchNewGeofence(geofenceNodes, newIds, loadIds());
        startGeoFencing(getGeofencingRequest(geofencesToAdd));
        saveIds(newIds);
    }

    private List<Geofence> fetchNewGeofence(List<GeofenceNode> geofenceNodes, List<String> newIds, List<String> oldIds) {
        // create copy of the new ids
        List<String> idsToAdd = new ArrayList<>(newIds);
        // subtract the old ids to the new
        if (oldIds != null) {
            idsToAdd.removeAll(oldIds);
        }

        // for each id, fetch the geofence and return them all
        List<Geofence> geoFenceNodesToAdd = new ArrayList<>();
        for (String id : idsToAdd) {
            GeofenceNode geofenceNode = getGeofenceFromId(geofenceNodes, id);
            if (geofenceNode != null) {
                geoFenceNodesToAdd.add(geofenceNode.toGeofence());
            }
        }
        return geoFenceNodesToAdd;
    }

    /**
     * Stop all geofences
     */
    public void stopAllGeofences() {
        stopGeofencing(loadIds());
        saveIds(new ArrayList<String>());
    }

    /**
     * Find a geofence from a geofence list, given its id
     *
     * @param geofenceNodes
     * @param id
     * @return
     */
    private GeofenceNode getGeofenceFromId(List<GeofenceNode> geofenceNodes, String id) {
        for (GeofenceNode geofenceNode : geofenceNodes) {
            if (geofenceNode.getId().equals(id)) {
                return geofenceNode;
            }
        }
        return null;
    }

    private List<String> idsFromGeofences(List<GeofenceNode> geofenceNodes) {
        List<String> ids = new ArrayList<>();
        for (GeofenceNode geofenceNode : geofenceNodes) {
            ids.add(geofenceNode.getId());
        }
        return ids;
    }

    /**
     * Stop geofencing on request ids.
     *
     * @param idsToremove
     */
    public void stopGeofencing(List<String> idsToremove) {
        if (idsToremove == null || idsToremove.size() == 0) return;
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) return;

        geofencingClient.removeGeofences(idsToremove)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        NearLog.v(TAG, "Geofences removed");
                    }
                });
    }

    /**
     * Start geofencing from a geofence request. If the google api client is not yet connected,
     * store the geofecing as pending to be started once the client is connected.
     *
     * @param request
     */
    private void startGeoFencing(GeofencingRequest request) {
        if (request == null) return;
        if (!mGoogleApiClient.isConnected()) {
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
        NearLog.v(TAG, "startGeofencing");
        geofencingClient.addGeofences(
                request,
                getGeofencePendingIntent()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pingSingleLocation();
            }
        });
    }


    /**
     * Builds the geofence request from a list of geofences
     *
     * @return
     */
    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        if (geofences == null || geofences.size() == 0) {
            return null;
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    /**
     * Build the geofence pending intent.
     *
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
        NearLog.v(TAG, "onConnected");
        // If we have pending geofences (that need to be started)
        // we start the geofence now that the client is connected.
        if (mPendingGeofences != null) {
            startGeoFencing(getGeofencingRequest(mPendingGeofences));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        NearLog.v(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        NearLog.v(TAG, "onConnectionFailed");
    }

    @Override
    public void onResult(@NonNull Status status) {
        NearLog.v(TAG, "onResult: " + status.getStatusMessage());
    }

    public static String getSharedPrefName(Context context) {
        String PACK_NAME = context.getApplicationContext().getPackageName();
        return PACK_NAME + PREF_SUFFIX;
    }
}
