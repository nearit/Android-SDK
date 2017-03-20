package it.near.sdk.Reactions.Poll;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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
import it.near.sdk.Reactions.ContentFetchListener;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.NearITEventHandler;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearJsonAPIUtils;

import static it.near.sdk.Utils.NearUtils.safe;

/**
 * @author cattaneostefano
 */
public class PollReaction extends CoreReaction {
    // ---------- poll notification plugin ----------
    public static final String PLUGIN_NAME = "poll-notification";
    private static final String POLL_NOTIFICATION =          "poll-notification";
    private static final String POLL_NOTIFICATION_RESOURCE = "polls";
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
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reactionBundle) {
        Poll poll = (Poll) reactionBundle;
        poll.setRecipeId(recipe.getId());
        nearNotifier.deliverBackgroundPushReaction(poll, recipe, push_id);
    }

    public void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(POLL_NOTIFICATION)
                .appendPath(POLL_NOTIFICATION_RESOURCE)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            Log.d(TAG, "Auth error");
        }
    }

    @Override
    protected void getContent(String reaction_bundle, final Recipe recipe, final ContentFetchListener listener) {
        if (pollList == null) {
            try {
                pollList = loadList();
            } catch (JSONException e) {
                Log.d(TAG, "Data format error");
            }
        }
        for (Poll pn : safe(pollList)){
            if (pn.getId().equals(reaction_bundle)){
                pn.setRecipeId(recipe.getId());
                listener.onContentFetched(pn, true);
                return;
            }
        }
        requestSingleReaction(reaction_bundle, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Poll poll = NearJsonAPIUtils.parseElement(morpheus, response, Poll.class);
                poll.setRecipeId(recipe.getId());
                listener.onContentFetched(poll, false);
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                listener.onContentFetchError("Error: " + statusCode + " : " + responseString);
            }
        });
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
                    Log.d(TAG, response.toString());
                    pollList = NearJsonAPIUtils.parseList(morpheus, response, Poll.class);
                    persistList(TAG, pollList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    Log.d(TAG, "Error: " + statusCode);
                    try {
                        pollList = loadList();
                    } catch (JSONException e) {
                        Log.d(TAG, "Data format error");
                    }
                }
            });
        } catch (AuthenticationException e) {
            Log.d(TAG, "Auth error");
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


    public void sendEvent(PollEvent event, final NearITEventHandler handler) {
        try {
            String answerBody = event.toJsonAPI(mContext);
            Log.d(TAG, "Answer" + answerBody);
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
                        Log.d(TAG, "Answer sent successfully");
                        handler.onSuccess();
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        Log.d(TAG, "Error in sending answer: " + statusCode);
                        handler.onFail(statusCode, responseString);
                    }
                });
            } catch (AuthenticationException | UnsupportedEncodingException e) {
                handler.onFail(422, "Incorrect format");
            }
        } catch (JSONException e) {;
            Log.d(TAG, "Error: incorrect format " + e.toString());
            handler.onFail(422, "Incorrect format");
        }
    }
}
