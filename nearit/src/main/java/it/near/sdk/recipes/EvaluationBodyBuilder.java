package it.near.sdk.recipes;

import android.support.annotation.Nullable;

import org.json.JSONException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import it.near.sdk.GlobalConfig;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class EvaluationBodyBuilder {

    static final String PULSE_PLUGIN_ID_KEY = "pulse_plugin_id";
    static final String PULSE_ACTION_ID_KEY = "pulse_action_id";
    static final String PULSE_BUNDLE_ID_KEY = "pulse_bundle_id";
    static final String KEY_CORE = "core";
    static final String KEY_EVALUATION = "evaluation";
    static final String KEY_PROFILE_ID = "profile_id";
    static final String KEY_INSTALLATION_ID = "installation_id";
    static final String KEY_APP_ID = "app_id";
    static final String KEY_UTC_OFFSET = "utc_offset";
    static final String KEY_COOLDOWN = "cooldown";
    static final String KEY_LAST_NOTIFIED_AT = "last_notified_at";
    static final String KEY_RECIPES_NOTIFIED_AT = "recipes_notified_at";

    private final GlobalConfig globalConfig;
    private final RecipesHistory recipesHistory;
    private final CurrentTime currentTime;

    public EvaluationBodyBuilder(GlobalConfig globalConfig,
                                 RecipesHistory recipesHistory,
                                 CurrentTime currentTime) {
        this.globalConfig = checkNotNull(globalConfig);
        this.recipesHistory = checkNotNull(recipesHistory);
        this.currentTime = checkNotNull(currentTime);
    }

    public String buildEvaluateBody() throws JSONException {
        return buildEvaluateBody(null, null, null);
    }

    public String buildEvaluateBody(@Nullable String pulse_plugin,
                                    @Nullable String pulse_action,
                                    @Nullable String pulse_bundle) throws JSONException {
        if (globalConfig.getProfileId() == null ||
                globalConfig.getAppId() == null) {
            throw new JSONException("missing data");
        }
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(KEY_CORE, buildCoreObject());
        if (pulse_plugin != null) attributes.put(PULSE_PLUGIN_ID_KEY, pulse_plugin);
        if (pulse_action != null) attributes.put(PULSE_ACTION_ID_KEY, pulse_action);
        if (pulse_bundle != null) attributes.put(PULSE_BUNDLE_ID_KEY, pulse_bundle);
        return NearJsonAPIUtils.toJsonAPI(KEY_EVALUATION, attributes);
    }

    private HashMap<String, Object> buildCoreObject() {
        HashMap<String, Object> coreObj = new HashMap<>();
        coreObj.put(KEY_PROFILE_ID, globalConfig.getProfileId());
        coreObj.put(KEY_INSTALLATION_ID, globalConfig.getInstallationId());
        coreObj.put(KEY_APP_ID, globalConfig.getAppId());
        coreObj.put(KEY_UTC_OFFSET, buildUtcOffset());
        if (recipesHistory != null) {
            coreObj.put(KEY_COOLDOWN, buildCooldownBlock());
        }

        return coreObj;
    }

    private String buildUtcOffset() {
        Calendar currentCalendar = currentTime.currentCalendar();
        int offsetInMillis = currentCalendar.getTimeZone().getOffset(currentTime.currentTimestamp());
        String offset = String.format(Locale.US, "%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;
        return offset;
    }

    private HashMap<String, Object> buildCooldownBlock() {
        HashMap<String, Object> block = new HashMap<>();
        block.put(KEY_LAST_NOTIFIED_AT, recipesHistory.getLatestLogEntry());
        block.put(KEY_RECIPES_NOTIFIED_AT, recipesHistory.getRecipeLogMap());
        return block;
    }

    public static EvaluationBodyBuilder obtain(GlobalConfig globalConfig, RecipesHistory recipesHistory) {
        return new EvaluationBodyBuilder(globalConfig, recipesHistory, new CurrentTime());
    }
}
