package it.near.sdk.reactions.customjson;

import android.content.Context;
import android.net.Uri;


import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.GlobalConfig;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

/**
 * @author cattaneostefano.
 */
public class CustomJSONReaction extends CoreReaction {

    public static final String PLUGIN_NAME = "json-sender";
    private static final String PREFS_SUFFIX = "NearJSON";
    private static final String SHOW_JSON_ACTION = "deliver_json";
    private static final String JSON_CONTENT_RES = "json_contents";
    private static final String TAG = "CustomJSONReaction";
    private static final String PLUGIN_ROOT_PATH = "json-sender";
    private List<CustomJSON> jsonList;

    public CustomJSONReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
    }

    @Override
    public String getPrefSuffix() {
        return PREFS_SUFFIX;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(JSON_CONTENT_RES, CustomJSON.class);
        return map;
    }

    @Override
    protected String getResTypeName() {
        return JSON_CONTENT_RES;
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<>();
        supportedActions.add(SHOW_JSON_ACTION);
    }

    @Override
    public void refreshConfig() {
        String appId = GlobalConfig.getInstance(mContext).getAppId();
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(JSON_CONTENT_RES)
                .appendQueryParameter("filter[app_id]", appId).build();
        try {
            httpClient.nearGet(mContext, url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    jsonList = NearJsonAPIUtils.parseList(morpheus, response, CustomJSON.class);
                    persistList(TAG, jsonList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error: " + statusCode);
                    if (statusCode == 0) {
                        NearLog.d(TAG, throwable.toString());
                    }
                    try {
                        jsonList = loadList();
                    } catch (JSONException e) {
                        NearLog.d(TAG, "Data format error");
                    }
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }

    }

    private ArrayList<CustomJSON> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString, new TypeToken<Collection<CustomJSON>>() {
        }.getType());
    }

    @Override
    public String getPluginName() {
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
        if (jsonList == null) {
            try {
                jsonList = loadList();
            } catch (JSONException e) {
                NearLog.d(TAG, "Data format error");
            }
        }
        for (CustomJSON json : safe(jsonList)) {
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
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reactionBundle) {
        CustomJSON customJSON = (CustomJSON) reactionBundle;
        nearNotifier.deliverBackgroundPushReaction(customJSON, recipe, push_id);
    }


    public void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(JSON_CONTENT_RES)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }
}
