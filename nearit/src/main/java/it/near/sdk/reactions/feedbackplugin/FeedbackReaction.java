package it.near.sdk.reactions.feedbackplugin;

import android.content.Context;
import android.net.Uri;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

public class FeedbackReaction extends CoreReaction<Feedback> {

    public static final String PLUGIN_NAME = "feedbacks";
    private static final String PREFS_NAME = "NearFeedbackNot";
    private static final String ASK_FEEDBACK_ACTION_NAME = "ask_feedback";
    private static final String FEEDBACKS_NOTIFICATION_RESOURCE = "feedbacks";
    private static final String TAG = "FeedbackReaction";
    private static final String ANSWERS_RESOURCE = "answers";

    private final GlobalConfig globalConfig;

    private List<Feedback> feedbackList;

    public FeedbackReaction(Cacher<Feedback> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, GlobalConfig globalConfig) {
        super(cacher, httpClient, nearNotifier, Feedback.class);
        this.globalConfig = globalConfig;
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE)
                .appendPath(bundleId).build();
        try {
            httpClient.nearGet(url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch (reaction_action) {
            case ASK_FEEDBACK_ACTION_NAME:
                showContent(reaction_bundle.getId(), recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reaction_bundle) {
        Feedback feedback = (Feedback) reaction_bundle;
        feedback.setRecipeId(recipe.getId());
        nearNotifier.deliverBackgroundPushReaction(feedback, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
    }

    @Override
    public void handlePushReaction(final String recipeId, final String notificationText, String reactionAction, String reactionBundleId) {
        requestSingleReaction(reactionBundleId, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Feedback fb = NearJsonAPIUtils.parseElement(morpheus, response, Feedback.class);
                fb.setRecipeId(recipeId);
                nearNotifier.deliverBackgroundPushReaction(fb, recipeId, notificationText, getReactionPluginName());
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
            Feedback fb = NearJsonAPIUtils.parseElement(morpheus, toParse, Feedback.class);
            if (fb == null) return false;
            fb.setRecipeId(recipeId);
            nearNotifier.deliverBackgroundPushReaction(fb, recipeId, notificationText, getReactionPluginName());
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void sendEvent(FeedbackEvent event, final NearITEventHandler handler) {
        if (event.getRating() < 1 || event.getRating() > 5) {
            handler.onFail(422, "Rating must be between 1 and 5");
            return;
        }
        try {
            String answerBody = event.toJsonAPI(globalConfig.getProfileId());
            NearLog.d(TAG, "Answer" + answerBody);
            Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(PLUGIN_NAME)
                    .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE)
                    .appendPath(event.getFeedbackId())
                    .appendPath(ANSWERS_RESOURCE).build();
            try {
                httpClient.nearPost(url.toString(), answerBody, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        NearLog.d(TAG, "Feedback sent successfully");
                        handler.onSuccess();
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        NearLog.d(TAG, "Error in sending answer: " + statusCode);
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
    protected void normalizeElement(Feedback element) {
        // left intentionally empty
    }

    @Override
    protected void getContent(String reaction_bundle, final Recipe recipe, final ContentFetchListener listener) {
        if (feedbackList == null) {
            try {
                feedbackList = cacher.loadList();
            } catch (JSONException e) {
                NearLog.d(TAG, "Data format error");
            }
        }
        for (Feedback fb : safe(feedbackList)) {
            if (fb.getId().equals(reaction_bundle)) {
                fb.setRecipeId(recipe.getId());
                listener.onContentFetched(fb, true);
                return;
            }
        }
        requestSingleReaction(reaction_bundle, new NearJsonHttpResponseHandler() {
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
    protected String getRefreshUrl() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE).build();
        return url.toString();
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

    public static FeedbackReaction obtain(Context context, NearNotifier nearNotifier, GlobalConfig globalConfig) {
        return new FeedbackReaction(
                new Cacher<Feedback>(
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context),
                nearNotifier,
                globalConfig
        );
    }
}
