package it.near.sdk.recipes;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import it.near.sdk.GlobalConfig;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackRequest;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class RecipeTrackSender {

    static final String TRACKINGS_PATH = "trackings";
    static final String TRACKING_PROFILE_ID = "profile_id";
    static final String TRACKING_INSTALLATION_ID = "installation_id";
    static final String TRACKING_APP_ID = "app_id";
    static final String TRACKING_RECIPE_ID = "recipe_id";
    static final String TRACKING_EVENT = "event";
    static final String TRACKING_TRACKED_AT = "tracked_at";
    static final String TRACKING_METADATA = "metadata";
    static final String TRACKINGS_TYPE = "trackings";
    private static final String TRACK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final GlobalConfig globalConfig;
    private final RecipesHistory recipesHistory;
    private final TrackManager trackManager;
    private final CurrentTime currentTime;

    public RecipeTrackSender(GlobalConfig globalConfig,
                             RecipesHistory recipesHistory,
                             TrackManager trackManager,
                             CurrentTime currentTime) {
        this.globalConfig = checkNotNull(globalConfig);
        this.recipesHistory = checkNotNull(recipesHistory);
        this.trackManager = checkNotNull(trackManager);
        this.currentTime = checkNotNull(currentTime);
    }

    void sendTracking(TrackingInfo trackingInfo, String trackingEvent) throws JSONException {
        if (trackingInfo == null ||
                trackingInfo.recipeId == null ||
                trackingEvent == null) return;

        if (trackingEvent.equals(Recipe.NOTIFIED_STATUS)) {
            recipesHistory.markRecipeAsShown(trackingInfo.recipeId);
        }

        String trackingBody = buildTrackingBody(
                trackingInfo,
                trackingEvent
        );

        trackManager.sendTracking(new TrackRequest(TRACKINGS_PATH, trackingBody));
    }

    private String buildTrackingBody(TrackingInfo trackingInfo, String trackingEvent) throws JSONException {
        String profileId = globalConfig.getProfileId();
        String appId = globalConfig.getAppId();
        String installationId = globalConfig.getInstallationId();
        if (appId == null ||
                profileId == null ||
                installationId == null) {
            throw new JSONException("missing data");
        }
        DateFormat sdf = new SimpleDateFormat(TRACK_DATE_FORMAT, Locale.US);
        Date now = new Date(currentTime.currentTimestamp());
        String formattedDate = sdf.format(now);
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(TRACKING_PROFILE_ID, profileId);
        attributes.put(TRACKING_INSTALLATION_ID, installationId);
        attributes.put(TRACKING_APP_ID, appId);
        attributes.put(TRACKING_RECIPE_ID, trackingInfo.recipeId);
        // TODO unit test for this
        attributes.put(TRACKING_METADATA, trackingInfo.metadata);
        attributes.put(TRACKING_EVENT, trackingEvent);
        attributes.put(TRACKING_TRACKED_AT, formattedDate);
        return NearJsonAPIUtils.toJsonAPI(TRACKINGS_TYPE, attributes);
    }

}
