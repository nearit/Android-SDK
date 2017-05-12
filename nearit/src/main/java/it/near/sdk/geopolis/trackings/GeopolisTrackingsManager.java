package it.near.sdk.geopolis.trackings;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.logging.NearLog;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackRequest;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class GeopolisTrackingsManager {

    private static final String TRACKING_RES = "trackings";
    private static final String TAG = "GeopolisTrackingManager";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_EVENT = "event";
    private static final String KEY_TRACKED_AT = "tracked_at";
    private static final String KEY_PROFILE_ID = "profile_id";
    private static final String KEY_INSTALLATION_ID = "installation_id";
    private static final String KEY_APP_ID = "app_id";
    private static final String KEY_TRACKINGS = "trackings";
    private static final String TRACK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final GlobalConfig globalConfig;
    private final TrackManager trackManager;

    public GeopolisTrackingsManager(@NonNull TrackManager trackManager,
                                    @NonNull GlobalConfig globalConfig) {
        this.trackManager = checkNotNull(trackManager);
        this.globalConfig = checkNotNull(globalConfig);
    }

    public void trackEvent(String identifier, String event) {
        try {
            Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(GeopolisManager.PLUGIN_NAME)
                    .appendPath(TRACKING_RES).build();
            // TODO caching and retry policy
            trackManager.sendTracking(new TrackRequest(url.toString(), buildTrackBody(identifier, event)));
        } catch (JSONException e) {
            NearLog.d(TAG, "Unable to format tracking body: " + e.toString());
        }
    }

    private String buildTrackBody(String identifier, String event) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY_IDENTIFIER, identifier);
        map.put(KEY_EVENT, event);
        DateFormat sdf = new SimpleDateFormat(TRACK_DATE_FORMAT, Locale.US);
        Date now = new Date(System.currentTimeMillis());
        String formatted = sdf.format(now);
        map.put(KEY_TRACKED_AT, formatted);
        map.put(KEY_PROFILE_ID, globalConfig.getProfileId());
        map.put(KEY_INSTALLATION_ID, globalConfig.getInstallationId());
        map.put(KEY_APP_ID, globalConfig.getAppId());
        return NearJsonAPIUtils.toJsonAPI(KEY_TRACKINGS, map);
    }
}

