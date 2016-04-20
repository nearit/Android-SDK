package it.near.sdk.Reactions.PollNotification;

import android.content.Context;

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
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class PollNotificationReaction extends Reaction {
    private static final String INGREDIENT_NAME = "poll-notification";
    private static final String SHOW_POLL_FLAVOR_NAME = "show_poll";
    private static final String TAG = "PollNotificationReaction";
    public static final String PREFS_SUFFIX = "NearPollNot";
    private List<PollNotification> pollList;

    public PollNotificationReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
        setUpMorpheus();

        initSharedPreferences(PREFS_SUFFIX);

        try {
            testObject = new JSONObject(test);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshConfig();
    }

    @Override
    protected void handleReaction(String reaction_flavor, String reaction_slice, Recipe recipe) {
        switch(reaction_flavor){
            case SHOW_POLL_FLAVOR_NAME:
                showPoll(reaction_slice, recipe);
                break;
        }
    }

    private void showPoll(String reaction_slice, Recipe recipe) {
        ULog.d(TAG , "Show poll: " + reaction_slice);
        PollNotification notification = getNotification(reaction_slice);
        if (notification==null) return;
        nearNotifier.deliverReaction(notification, recipe);
    }

    private PollNotification getNotification(String reaction_slice) {
        for (PollNotification pn : pollList){
            if (pn.getId().equals(reaction_slice)){
                return pn;
            }
        }
        return null;
    }

    @Override
    public void refreshConfig() {
        requestQueue.add(
                new CustomJsonRequest(mContext, Constants.API.PLUGINS.poll_notification + "/notifications", new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        pollList = parseList(response, PollNotification.class);
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

    private ArrayList<PollNotification> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString , new TypeToken<Collection<PollNotification>>(){}.getType());
    }

    @Override
    public void buildFlavors() {
        supportedFlavors = new ArrayList<String>();
        supportedFlavors.add(SHOW_POLL_FLAVOR_NAME);
    }

    @Override
    public String getIngredientName() {
        return INGREDIENT_NAME;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put("notifications", PollNotification.class);
        return map;
    }

    JSONObject testObject;
    String test = "{\"data\":[{\"id\":\"5db0d8a4-d17a-4c2d-8d48-cf459aeab4e5\",\"type\":\"notifications\",\"attributes\":{\"text\":\"Poll - Tap to answer\",\"question\":\"How you doing?\",\"choice_1\":\"Fine, thx\",\"choice_2\":\"No good..\",\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\"}}]}";

    public void sendAction(PollAction action) {
        try {
            String answerBody = action.toJsonAPI();
            ULog.d(TAG, "Answer" + answerBody);
            requestQueue.add(new CustomJsonRequest(mContext, Request.Method.POST, Constants.API.PLUGINS.poll_notification + "/notifications/" +
                    action.getId() + "/answers", answerBody , new Response.Listener<JSONObject>() {
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
