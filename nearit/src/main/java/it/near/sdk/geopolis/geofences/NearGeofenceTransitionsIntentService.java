package it.near.sdk.geopolis.geofences;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.R;
import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.logging.NearLog;

/**
 * Created by cattaneostefano on 14/07/16.
 */

public class NearGeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransService";
    private static final String PREF_SUFFIX = "NearGeo";
    private static final String STATUS_LIST = "Status";
    private SharedPreferences mSharedPreferences;

    public NearGeofenceTransitionsIntentService() {
        super("NearGeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            /*String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());*/
            NearLog.e(TAG, "Error");
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT /*||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL*/) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence triggeringGeofence : triggeringGeofences) {
                notifyEventOnGeofence(triggeringGeofence, geofenceTransition);
            }

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            NearLog.d(TAG, geofenceTransitionDetails);
            // Send notification and log the transition details.
        } else {
            NearLog.d(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private void notifyEventOnGeofence(Geofence triggeringGeofence, int geofenceTransition) {
        Intent intent = new Intent();
        String packageName = this.getPackageName();
        String actionSuffix = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ?
                GeopolisManager.GF_ENTRY_ACTION_SUFFIX : GeopolisManager.GF_EXIT_ACTION_SUFFIX;
        intent.setAction(packageName + "." + actionSuffix);
        intent.putExtra(GeopolisManager.NODE_ID, triggeringGeofence.getRequestId());
        sendBroadcast(intent);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return getString(R.string.geofence_transition_dwell);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

}
