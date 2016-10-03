package it.near.sdk.Geopolis.GeoFence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.R;

/**
 * Created by cattaneostefano on 14/07/16.
 */

// TODO rename this class, it's too standard (NearGeofenceTransitionsIntentService)
public class GeofenceTransitionsIntentService extends IntentService {


    private static final String TAG = "GeofenceIntentService";
    private static final String PREF_SUFFIX = "NearGeo";
    private static final String STATUS_LIST = "Status";
    private SharedPreferences mSharedPreferences;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            /*String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());*/
            Log.e(TAG, "Error");
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            Log.wtf(TAG, geofenceTransitionDetails);
            // Send notification and log the transition details.
            loadSharedPrefs();
            if(!alreadySameState(triggeringGeofences, geofenceTransition)){
                sendNotification(geofenceTransitionDetails, geofenceTransition);
            } else {
                Log.d(TAG, "transition ignored");
            }
            registerEvent(geofencingEvent.getTriggeringGeofences(), geofenceTransition);
        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private void sendNotification(String geofenceTransitionDetails, int geofenceTransition) {
        Toast.makeText(this, "Geofence event", Toast.LENGTH_SHORT).show();
    }

    private boolean alreadySameState(List<Geofence> triggeringGeofences, int geofenceTransition) {
        return (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) == alreadyInside(triggeringGeofences);
    }

    private boolean alreadyInside(List<Geofence> triggeringGeofences) {
        for (Geofence geofence : triggeringGeofences) {
            if (mSharedPreferences.contains(geofence.getRequestId())){
                return true;
            }
        }
        return false;
    }

    private void registerEvent(List<Geofence> triggeringGeofences, int geofenceTransition) {
        boolean hasEntered = (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER);
        for (Geofence triggeringGeofence : triggeringGeofences) {
            persistEnterEvent(triggeringGeofence.getRequestId(), hasEntered);
        }
    }

    private void persistEnterEvent(String requestId, boolean hasEntered) {
        if (hasEntered){
            writeStatus(requestId, Long.valueOf(System.currentTimeMillis()));
        } else {
            removeStatus(requestId);
        }
    }

    private void removeStatus(String requestId) {
        mSharedPreferences.edit().remove(requestId).apply();
    }

    private void writeStatus(String requestId, Long timestamp) {
        mSharedPreferences.edit().putLong(requestId, timestamp).apply();
    }

    private void loadSharedPrefs() {
        mSharedPreferences = getSharedPreferences(getSharedPrefName(), 0);
    }


    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    /*private void sendNotification(String notificationDetails, int geofenceTransition) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent notificationPendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(geofenceTransition, builder.build());
    }*/

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
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

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


    public String getSharedPrefName() {
        String PACK_NAME = getApplicationContext().getPackageName();
        return PACK_NAME + PREF_SUFFIX;
    }
}
