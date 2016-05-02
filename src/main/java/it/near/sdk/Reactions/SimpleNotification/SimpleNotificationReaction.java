package it.near.sdk.Reactions.SimpleNotification;

import android.content.Context;

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
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano
 */
public class SimpleNotificationReaction extends Reaction {
    private static final String INGREDIENT_NAME = "simple-notification";
    private static final String SHOW_NOTIFICATION_FLAVOR_NAME = "show_notification";
    private static final String TAG = "SimpleNotificationReaction";
    public static final String PREFS_SUFFIX = "NearSimpleNot";
    private List<SimpleNotification> notificationList;

    public SimpleNotificationReaction(Context context, NearNotifier nearNotifier) {
        super(context, nearNotifier);
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
    public void handleReaction(String reaction_flavor, String reaction_slice, Recipe recipe) {
        switch(reaction_flavor){
            case SHOW_NOTIFICATION_FLAVOR_NAME:
                showNotification(reaction_slice, recipe);
                break;
        }
    }

    private void showNotification(String reaction_slice, Recipe recipe) {
        ULog.d(TAG, "Show notification: " + reaction_slice);
        SimpleNotification notification = getNotification(reaction_slice);
        if (notification == null) return;
        nearNotifier.deliverReaction(notification, recipe);
    }

    private SimpleNotification getNotification(String reaction_slice){
        if (notificationList==null) return null;
        for (SimpleNotification sn : notificationList){
            if (sn.getId().equals(reaction_slice)){
                return sn;
            }
        }
        return null;
    }

    public void refreshConfig() {
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, Constants.API.PLUGINS.simple_notification + "/notifications", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    ULog.d(TAG, response.toString());
                    notificationList = NearUtils.parseList(morpheus, response, SimpleNotification.class);
                    persistList(TAG, notificationList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ULog.d(TAG, "Error: " + error.toString());
                try {
                    notificationList = loadList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private ArrayList<SimpleNotification> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString , new TypeToken<Collection<SimpleNotification>>(){}.getType());
    }

    @Override
    public String getIngredientName() {
        return INGREDIENT_NAME;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put("notifications", SimpleNotification.class);
        return map;
    }

    @Override
    public void buildFlavors() {
        supportedFlavors = new ArrayList<String>();
        supportedFlavors.add(SHOW_NOTIFICATION_FLAVOR_NAME);
    }

    JSONObject testObject;
    String test = "{\"data\":[{\"id\":\"ca16db48-e0ed-455d-803c-b8293943b8de\",\"type\":\"notifications\",\"attributes\":{\"text\":\"Prova notifica di regione.\",\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\"}}]}";

}
