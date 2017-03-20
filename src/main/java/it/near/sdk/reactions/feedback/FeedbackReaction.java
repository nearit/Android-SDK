package it.near.sdk.Reactions.Feedback;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

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
import it.near.sdk.GlobalConfig;
import it.near.sdk.Reactions.ContentFetchListener;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.NearITEventHandler;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.NearJsonAPIUtils;

import static it.near.sdk.Utils.NearUtils.safe;

/**
 * Created by cattaneostefano on 11/10/2016.
 */

public class FeedbackReaction extends CoreReaction {

    public static final String PLUGIN_NAME = "feedbacks";
    public static final String PREFS_SUFFIX = "NearFeedbackNot";
    private static final String ASK_FEEDBACK_ACTION_NAME = "ask_feedback";
    private static final String FEEDBACKS_NOTIFICATION_RESOURCE =  "feedbacks";
    private static final String TAG = "FeedbackReaction";
    private static final String ANSWERS_RESOURCE = "answers";

    private List<Feedback> feedbackList;
    
    public FeedbackReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
    }

    @Override
    public void refreshConfig() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE).build();

        try {
            httpClient.nearGet(mContext, url.toString(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, response.toString());
                    feedbackList = NearJsonAPIUtils.parseList(morpheus, response, Feedback.class);
                    persistList(TAG, feedbackList);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, "Error: " + statusCode);
                    try {
                        feedbackList = loadList();
                    } catch (JSONException e) {
                        Log.d(TAG, "Data format error");
                    }
                }
            });
        } catch (AuthenticationException e) {
            Log.d(TAG, "Auth error");
        }
    }

    private ArrayList<Feedback> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString , new TypeToken<Collection<Feedback>>(){}.getType());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    public void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            Log.d(TAG, "Auth error");
        }
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch (reaction_action){
            case ASK_FEEDBACK_ACTION_NAME:
                showContent(reaction_bundle.getId(), recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reaction_bundle) {
        Feedback feedback = (Feedback) reaction_bundle;
        feedback.setRecipeId(recipe.getId());
        nearNotifier.deliverBackgroundPushReaction(feedback, recipe, push_id);
    }

    public void sendEvent(FeedbackEvent event, final NearITEventHandler handler){
        if (event.getRating() < 1 || event.getRating() > 5){
            handler.onFail(422, "Rating must be between 1 and 5");
            return;
        }
        try {
            String answerBody = event.toJsonAPI(GlobalConfig.getInstance(mContext));
            Log.d(TAG, "Answer" + answerBody);
            Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(PLUGIN_NAME)
                    .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE)
                    .appendPath(event.getFeedbackId())
                    .appendPath(ANSWERS_RESOURCE).build();
            try {
                httpClient.nearPost(mContext, url.toString(), answerBody, new NearJsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, "Feedback sent successfully");
                        handler.onSuccess();
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        Log.d(TAG, "Error in sending answer: " + statusCode);
                        handler.onFail(statusCode, responseString);
                    }
                });
            } catch (AuthenticationException | UnsupportedEncodingException e) {
                handler.onFail(422, "request was malformed");
            }
        } catch (JSONException e) {
            handler.onFail(422, "request was malformed");
        }
    }

    @Override
    protected void getContent(String reaction_bundle, final Recipe recipe, final ContentFetchListener listener) {
        if (feedbackList == null) {
            try {
                feedbackList = loadList();
            } catch (JSONException e) {
                Log.d(TAG, "Data format error");
            }
        }
        for ( Feedback fb : safe(feedbackList)){
            if (fb.getId().equals(reaction_bundle)){
                fb.setRecipeId(recipe.getId());
                listener.onContentFetched(fb, true);
                return;
            }
        }
        requestSingleReaction(reaction_bundle, new NearJsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Feedback fb = NearJsonAPIUtils.parseElement(morpheus, response, Feedback.class);
                fb.setRecipeId(recipe.getId());
                listener.onContentFetched(fb, false);
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                listener.onContentFetchError("Error: " + statusCode + " : " + responseString);
            }
        });
    }

    @Override
    public String getPrefSuffix() {
        return PREFS_SUFFIX;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(FEEDBACKS_NOTIFICATION_RESOURCE, Feedback.class);
        return map;
    }

    @Override
    protected String getResTypeName() {
        return FEEDBACKS_NOTIFICATION_RESOURCE;
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<String>();
        supportedActions.add(ASK_FEEDBACK_ACTION_NAME);
    }
}
