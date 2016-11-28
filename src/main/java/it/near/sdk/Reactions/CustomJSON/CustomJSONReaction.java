package it.near.sdk.Reactions.CustomJSON;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;

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
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearJsonHttpResponseHandler;
import it.near.sdk.GlobalConfig;
import it.near.sdk.Reactions.ContentFetchListener;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.NearJsonAPIUtils;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano.
 */
public class CustomJSONReaction extends CoreReaction {


    private static final String PREFS_SUFFIX = "NearJSON";
    private static final String PLUGIN_NAME = "json-sender";
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
            httpClient.nearGet(mContext, url.toString(), new NearJsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    jsonList = NearJsonAPIUtils.parseList(morpheus, response, CustomJSON.class);
                    persistList(TAG, jsonList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    ULog.d(TAG, "Error: " + statusCode);
                    if (statusCode == 0){
                        ULog.d(TAG, throwable.toString());
                    }
                    try {
                        jsonList = loadList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<CustomJSON> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString, new TypeToken<Collection<CustomJSON>>(){}.getType());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch (reaction_action){
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
                e.printStackTrace();
            }
        }
        for (CustomJSON json : jsonList){
            if (json.getId().equals(reaction_bundle)){
                listener.onContentFetched(json, true);
            }
        }
        requestSingleReaction(reaction_bundle, new NearJsonHttpResponseHandler(){
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


    public void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(JSON_CONTENT_RES)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }
}
