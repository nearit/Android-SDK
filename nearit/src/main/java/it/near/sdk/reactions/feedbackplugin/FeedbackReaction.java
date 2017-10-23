package it.near.sdk.reactions.feedbackplugin;

import android.content.Context;
import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.NearNotifier;

public class FeedbackReaction extends CoreReaction<Feedback> {

    public static final String PLUGIN_NAME = "feedbacks";
    private static final String PREFS_NAME = "NearFeedbackNot";
    static final String ASK_FEEDBACK_ACTION_NAME = "ask_feedback";
    static final String FEEDBACKS_NOTIFICATION_RESOURCE = "feedbacks";
    private static final String TAG = "FeedbackReaction";
    private static final String ANSWERS_RESOURCE = "answers";

    private final GlobalConfig globalConfig;

    FeedbackReaction(Cacher<Feedback> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, GlobalConfig globalConfig, Type cacheType) {
        super(cacher, httpClient, nearNotifier, Feedback.class, cacheType);
        this.globalConfig = globalConfig;
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected String getRefreshUrl() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE).build();
        return url.toString();
    }

    @Override
    protected String getSingleReactionUrl(String bundleId) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(FEEDBACKS_NOTIFICATION_RESOURCE)
                .appendPath(bundleId).build();
        return url.toString();
    }

    @Override
    protected String getDefaultShowAction() {
        return ASK_FEEDBACK_ACTION_NAME;
    }

    @Override
    protected void injectRecipeId(Feedback element, String recipeId) {
        element.setRecipeId(recipeId);
    }

    @Override
    protected void normalizeElement(Feedback element) {
        // left intentionally empty
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
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(FEEDBACKS_NOTIFICATION_RESOURCE, Feedback.class);
        return map;
    }

    public static FeedbackReaction obtain(Context context, NearNotifier nearNotifier, GlobalConfig globalConfig) {
        return new FeedbackReaction(
                new Cacher<Feedback>(
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context, globalConfig),
                nearNotifier,
                globalConfig,
                new TypeToken<List<Feedback>>() {}.getType()
        );
    }
}
