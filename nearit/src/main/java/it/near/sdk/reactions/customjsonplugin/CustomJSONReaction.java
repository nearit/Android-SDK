package it.near.sdk.reactions.customjsonplugin;

import android.content.Context;
import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.recipes.NearNotifier;

public class CustomJSONReaction extends CoreReaction<CustomJSON> {

    public static final String PLUGIN_NAME = "json-sender";
    private static final String PREFS_NAME = "NearJSON";
    static final String SHOW_JSON_ACTION = "deliver_json";
    static final String JSON_CONTENT_RES = "json_contents";
    private static final String TAG = "CustomJSONReaction";
    private static final String PLUGIN_ROOT_PATH = "json-sender";

    CustomJSONReaction(Cacher<CustomJSON> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, Type cacheType) {
        super(cacher, httpClient, nearNotifier, CustomJSON.class, cacheType);
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected String getRefreshUrl() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(JSON_CONTENT_RES)
                .build();
        return url.toString();
    }

    @Override
    protected String getSingleReactionUrl(String bundleId) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(JSON_CONTENT_RES)
                .appendPath(bundleId).build();
        return url.toString();
    }

    @Override
    protected String getDefaultShowAction() {
        return SHOW_JSON_ACTION;
    }

    @Override
    protected void injectRecipeId(CustomJSON element, String recipeId) {
        // left intentionally blank
    }

    @Override
    protected void normalizeElement(CustomJSON element) {
        // left intentionally empty
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(JSON_CONTENT_RES, CustomJSON.class);
        return map;
    }

    public static CustomJSONReaction obtain(Context context, NearNotifier nearNotifier, GlobalConfig globalConfig) {
        return new CustomJSONReaction(
                new Cacher<CustomJSON>(
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context, globalConfig),
                nearNotifier,
                new TypeToken<List<CustomJSON>>() {}.getType()
        );
    }
}
