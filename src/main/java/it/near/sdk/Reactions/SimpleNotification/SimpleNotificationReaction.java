package it.near.sdk.Reactions.SimpleNotification;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class SimpleNotificationReaction extends Reaction {
    private static final String INGREDIENT_NAME = "simple-notification";
    private static final String SHOW_NOTIFICATION_FLAVOR_NAME = "show_notification";
    private static final String TAG = "SimpleNotification";
    private List<SimpleNotification> notificationList;

    public SimpleNotificationReaction(Context context) {
        super(context);
        setUpMorpheus();
        try {
            testObject = new JSONObject(test);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshConfig();
    }


    @Override
    public void handleReaction(String reaction_flavor, String reaction_slice) {
        switch(reaction_flavor){
            case SHOW_NOTIFICATION_FLAVOR_NAME:
                showNotification(reaction_slice);
                break;
        }
    }

    private void showNotification(String reaction_slice) {
        ULog.d(TAG, "Show notification: " + reaction_slice);
    }

    public void refreshConfig() {
        requestQueue.add(new CustomJsonRequest(mContext, Constants.API.PLUGINS.simple_notification + "/notifications", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ULog.d(TAG, response.toString());
                notificationList = parseList(response, SimpleNotification.class);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ULog.d(TAG, "Error: " + error.toString());
            }
        }));
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
    String test = "{\"data\":[{\"id\":\"ca16db48-e0ed-455d-803c-b8293943b8de\",\"type\":\"s-notifications\",\"attributes\":{\"text\":\"Prova notifica di regione.\",\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\"}}]}";

}
