package it.near.sdk.Reactions.CustomJSON;

import android.content.Context;
import android.net.Uri;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.GlobalConfig;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.NearUtils;
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
            httpClient.nearGet(mContext, url.toString(), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    jsonList = NearUtils.parseList(morpheus, response, CustomJSON.class);
                    persistList(TAG, jsonList);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    ULog.d(TAG, "Error: " + statusCode);
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
    protected void handleReaction(String reaction_action, String reaction_bundle, Recipe recipe) {
        switch (reaction_action){
            case SHOW_JSON_ACTION:
                showJSON(reaction_bundle, recipe);
                break;
        }
    }

    private void showJSON(String reaction_bundle, Recipe recipe) {
        ULog.d(TAG, "Show json:" + reaction_bundle);
        CustomJSON customJSON = getJSONContent(reaction_bundle);
        if (customJSON == null) return;
        nearNotifier.deliverBackgroundReaction(customJSON, recipe);
    }

    private CustomJSON getJSONContent(String reaction_bundle) {
        if (jsonList == null) return null;
        for (CustomJSON json : jsonList){
            if (json.getId().equals(reaction_bundle)){
                return json;
            }
        }
        return null;
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, String bundle_id) {
        requestSingleReaction(bundle_id, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ULog.d(TAG, response.toString());
                CustomJSON customJson = NearUtils.parseElement(morpheus, response, CustomJSON.class);
                nearNotifier.deliverBackgroundPushReaction(customJson, recipe, push_id);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ULog.d(TAG, "Error downloading push content: " + statusCode);
            }
        });
    }

    @Override
    public void handleEvaluatedReaction(final Recipe recipe, String bundle_id) {
        requestSingleReaction(bundle_id, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ULog.d(TAG, response.toString());
                CustomJSON customJSON = NearUtils.parseElement(morpheus, response, CustomJSON.class);
                nearNotifier.deliverBackgroundReaction(customJSON, recipe);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ULog.d(TAG, "Error downloading content: " + statusCode);
            }
        });
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
