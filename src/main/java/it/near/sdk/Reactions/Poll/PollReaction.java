package it.near.sdk.Reactions.Poll;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearJsonHttpResponseHandler;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearJsonAPIUtils;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano
 */
public class PollReaction extends CoreReaction {
    // ---------- poll notification plugin ----------
    public static final String POLL_NOTIFICATION =          "poll-notification";
    public static final String POLL_NOTIFICATION_RESOURCE = "polls";
    private static final String PLUGIN_NAME = "poll-notification";
    private static final String SHOW_POLL_ACTION_NAME = "show_poll";
    private static final String TAG = "PollReaction";
    public static final String PREFS_SUFFIX = "NearPollNot";
    private List<Poll> pollList;

    public PollReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
    }

    @Override
    protected String getResTypeName() {
        return "polls";
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch(reaction_action){
            case SHOW_POLL_ACTION_NAME:
                showContent(reaction_bundle.getId(), recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle bundle) {
        // TODO not tested
        requestSingleReaction(bundle.getId(), new NearJsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        ULog.d(TAG, response.toString());
                        Poll content = NearJsonAPIUtils.parseElement(morpheus, response, Poll.class);
                        content.setRecipeId(recipe.getId());
                        nearNotifier.deliverBackgroundPushReaction(content, recipe, push_id);
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        ULog.d(TAG, "Error downloading push content: " + statusCode);
                    }
                }

        );
    }

    public void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(POLL_NOTIFICATION)
                .appendPath(POLL_NOTIFICATION_RESOURCE)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Parcelable getContent(String reaction_bundle, Recipe recipe) {
        if (pollList == null) return null;
        for (Poll pn : pollList){
            if (pn.getId().equals(reaction_bundle)){
                pn.setRecipeId(recipe.getId());
                return pn;
            }
        }
        return null;
    }

    @Override
    public void refreshConfig() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(POLL_NOTIFICATION)
                    .appendPath(POLL_NOTIFICATION_RESOURCE).build();
        // TODO not tested
        try {
            httpClient.nearGet(mContext, url.toString(), new NearJsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    pollList = NearJsonAPIUtils.parseList(morpheus, response, Poll.class);
                    persistList(TAG, pollList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    ULog.d(TAG, "Error: " + statusCode);
                    try {
                        pollList = loadList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Poll> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString , new TypeToken<Collection<Poll>>(){}.getType());
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<String>();
        supportedActions.add(SHOW_POLL_ACTION_NAME);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getPrefSuffix() {
        return PREFS_SUFFIX;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put("polls", Poll.class);
        return map;
    }


    public void sendEvent(PollEvent event) {
        try {
            String answerBody = event.toJsonAPI(mContext);
            ULog.d(TAG, "Answer" + answerBody);
            Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(POLL_NOTIFICATION)
                    .appendPath(POLL_NOTIFICATION_RESOURCE)
                    .appendPath(event.getPollId())
                    .appendPath("answers").build();
            // TODO not tested
            try {
                httpClient.nearPost(mContext, url.toString(), answerBody, new NearJsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        ULog.d(TAG, "Answer sent successfully");
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        ULog.d(TAG, "Error in sending answer: " + statusCode);
                    }
                });
            } catch (AuthenticationException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ULog.d(TAG, "Error: incorrect format " + e.toString());
        }
    }
}
