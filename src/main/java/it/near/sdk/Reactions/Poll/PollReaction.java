package it.near.sdk.Reactions.Poll;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.GlobalState;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearUtils;
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
    protected void handleReaction(String reaction_action, String reaction_bundle, Recipe recipe) {
        switch(reaction_action){
            case SHOW_POLL_ACTION_NAME:
                showPoll(reaction_bundle, recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, String bundle_id) {
        // Download the single resource
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(POLL_NOTIFICATION)
                .appendPath(POLL_NOTIFICATION_RESOURCE)
                .appendPath(bundle_id).build();
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, url.toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        Poll content = NearUtils.parseElement(morpheus, response, Poll.class);
                        nearNotifier.deliverBackgroundPushReaction(content, recipe, push_id);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ULog.d(TAG, "Error downloading push content: " + error.toString());
                    }
                })
        );
    }

    private void showPoll(String reaction_bundle, Recipe recipe) {
        ULog.d(TAG , "Show poll: " + reaction_bundle);
        Poll notification = getNotification(reaction_bundle);
        if (notification==null) return;
        nearNotifier.deliverBackgroundRegionReaction(notification, recipe);
    }

    private Poll getNotification(String reaction_bundle) {
        if (pollList == null) return null;
        for (Poll pn : pollList){
            if (pn.getId().equals(reaction_bundle)){
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
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, url.toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        pollList = NearUtils.parseList(morpheus, response, Poll.class);
                        persistList(TAG, pollList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ULog.d(TAG, "Error: " + error.toString());
                        try {
                            pollList = loadList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
        );
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
            String answerBody = event.toJsonAPI();
            ULog.d(TAG, "Answer" + answerBody);
            Uri path = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(POLL_NOTIFICATION)
                    .appendPath(POLL_NOTIFICATION_RESOURCE)
                    .appendPath(event.getId())
                    .appendPath("answers").build();
            GlobalState.getInstance(mContext).getRequestQueue().add(new CustomJsonRequest(mContext, Request.Method.POST, path.toString(), answerBody , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ULog.d(TAG, "Answer sent successfully");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "Error in sending answer: " + error.toString());
                }
            }));

        } catch (JSONException e) {
            e.printStackTrace();
            ULog.d(TAG, "Error: incorrect format " + e.toString());
        }
    }
}
