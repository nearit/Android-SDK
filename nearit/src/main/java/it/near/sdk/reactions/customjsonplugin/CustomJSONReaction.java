package it.near.sdk.reactions.customjsonplugin;

import android.content.Context;
import android.net.Uri;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

public class CustomJSONReaction extends CoreReaction<CustomJSON> {

    public static final String PLUGIN_NAME = "json-sender";
    private static final String PREFS_NAME = "NearJSON";
    private static final String SHOW_JSON_ACTION = "deliver_json";
    private static final String JSON_CONTENT_RES = "json_contents";
    private static final String TAG = "CustomJSONReaction";
    private static final String PLUGIN_ROOT_PATH = "json-sender";

    public CustomJSONReaction(Cacher<CustomJSON> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier) {
        super(cacher, httpClient, nearNotifier, CustomJSON.class);
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(JSON_CONTENT_RES, CustomJSON.class);
        return map;
    }

    @Override
    protected void normalizeElement(CustomJSON element) {
        // left intentionally empty
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch (reaction_action) {
            case SHOW_JSON_ACTION:
                showContent(reaction_bundle.getId(), recipe);
                break;
        }
    }

    @Override
    protected void getContent(String reaction_bundle, Recipe recipe, final ContentFetchListener listener) {
        if (reactionList == null) {
            try {
                reactionList = cacher.loadList();
            } catch (JSONException e) {
                NearLog.d(TAG, "Data format error");
            }
        }
        for (CustomJSON json : safe(reactionList)) {
            if (json.getId().equals(reaction_bundle)) {
                listener.onContentFetched(json, true);
                return;
            }
        }
        requestSingleReaction(reaction_bundle, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                CustomJSON json = NearJsonAPIUtils.parseElement(morpheus, response, CustomJSON.class);
                listener.onContentFetched(json, false);
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                listener.onContentFetchError("Error: " + statusCode + " : " + responseString);
            }
        });
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
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reactionBundle) {
        CustomJSON customJSON = (CustomJSON) reactionBundle;
        nearNotifier.deliverBackgroundPushReaction(customJSON, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
    }

    @Override
    public void handlePushReaction(final String recipeId, final String notificationText, String reactionAction, String reactionBundleId) {
        requestSingleReaction(reactionBundleId, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        CustomJSON json = NearJsonAPIUtils.parseElement(morpheus, response, CustomJSON.class);
                        nearNotifier.deliverBackgroundPushReaction(json, recipeId, notificationText, getReactionPluginName());
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        NearLog.d(TAG, "Couldn't fetch content");
                    }
                },
                new Random().nextInt(1000));
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        try {
            JSONObject toParse = new JSONObject(reactionBundleString);
            CustomJSON json = NearJsonAPIUtils.parseElement(morpheus, toParse, CustomJSON.class);
            if (json == null) return false;
            nearNotifier.deliverBackgroundPushReaction(json, recipeId, notificationText, getReactionPluginName());
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    protected void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(JSON_CONTENT_RES)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    public static CustomJSONReaction obtain(Context context, NearNotifier nearNotifier) {
        return new CustomJSONReaction(
                new Cacher<CustomJSON>(
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context),
                nearNotifier
        );
    }
}
