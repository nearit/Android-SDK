package it.near.sdk.geopolis.trackings;

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
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackRequest;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class GeopolisTrackingsManager {

    static final String TRACKING_RES = "trackings";
    static final String TAG = "GeopolisTrackingManager";
    static final String KEY_IDENTIFIER = "identifier";
    static final String KEY_EVENT = "event";
    static final String KEY_TRACKED_AT = "tracked_at";
    static final String KEY_PROFILE_ID = "profile_id";
    static final String KEY_INSTALLATION_ID = "installation_id";
    static final String KEY_APP_ID = "app_id";
    static final String KEY_TRACKINGS = "trackings";
    private static final String TRACK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final GlobalConfig globalConfig;
    private final TrackManager trackManager;
    private final CurrentTime currentTime;

    public GeopolisTrackingsManager(@NonNull TrackManager trackManager,
                                    @NonNull GlobalConfig globalConfig,
                                    @NonNull CurrentTime currentTime) {
        this.trackManager = checkNotNull(trackManager);
        this.globalConfig = checkNotNull(globalConfig);
        this.currentTime = checkNotNull(currentTime);
    }

    public void trackEvent(String identifier, String event) throws JSONException {
        if (identifier == null ||
                event == null) {
            return;
        }

        String trackingBody = buildTrackBody(identifier, event);

        String url = Constants.API.PLUGINS_ROOT + "/" +
                GeopolisManager.PLUGIN_NAME + "/" +
                TRACKING_RES;

        trackManager.sendTracking(new TrackRequest(url, trackingBody));
    }

    private String buildTrackBody(String identifier, String event) throws JSONException {
        String profileId= globalConfig.getProfileId();
        String appId = globalConfig.getAppId();
        String installationId = globalConfig.getInstallationId();
        if (profileId == null ||
                appId == null ||
                installationId == null) {
            throw new JSONException("missing data");
        }
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(KEY_IDENTIFIER, identifier);
        attributes.put(KEY_EVENT, event);
        DateFormat sdf = new SimpleDateFormat(TRACK_DATE_FORMAT, Locale.US);
        Date now = new Date(currentTime.currentTimestamp());
        String formatted = sdf.format(now);
        attributes.put(KEY_TRACKED_AT, formatted);
        attributes.put(KEY_PROFILE_ID, profileId);
        attributes.put(KEY_INSTALLATION_ID, installationId);
        attributes.put(KEY_APP_ID, appId);
        return NearJsonAPIUtils.toJsonAPI(KEY_TRACKINGS, attributes);
    }
}

