package it.near.sdk.Reactions.ContentNotification;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Beacons.BeaconForest.Beacon;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.NearItManager;
import it.near.sdk.Reactions.ContentNotification.ContentNotification;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Recipe;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class ContentNotificationReaction extends Reaction {
    private static final String INGREDIENT_NAME = "content-notification";
    private static final String SHOW_CONTENT_FLAVOR_NAME = "show_content";
    private static final String TAG = "ContentNotificationReaction";
    public static final String PREFS_SUFFIX = "NearContentNot";
    private final String PREFS_NAME;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;
    private List<ContentNotification> contentNotificationList;

    public ContentNotificationReaction(Context context, NearNotifier nearNotifier) {
        super(context, nearNotifier);
        setUpMorpheus();

        String PACK_NAME = mContext.getApplicationContext().getPackageName();
        PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();

        try {
            testObject = new JSONObject(test);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshConfig();
    }

    @Override
    public void handleReaction(String reaction_flavor, String reaction_slice, Recipe recipe) {
        switch (reaction_flavor){
            case SHOW_CONTENT_FLAVOR_NAME:
                showContent(reaction_slice, recipe);
                break;
        }
    }

    private void showContent(String reaction_slice, Recipe recipe) {
        ULog.d(TAG, "Show content: " + reaction_slice);
        ContentNotification notification = getNotification(reaction_slice);
        if (notification == null) return;
        nearNotifier.deliverReaction(notification, recipe);
    }

    private ContentNotification getNotification(String reaction_slice) {
        for ( ContentNotification cn : contentNotificationList){
            if (cn.getId().equals(reaction_slice)){
                return cn;
            }
        }
        return null;
    }

    public void refreshConfig() {
        requestQueue.add(
                new CustomJsonRequest(mContext, Constants.API.PLUGINS.content_notification + "/notifications", new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        contentNotificationList = parseList(response, ContentNotification.class);
                        persistList(contentNotificationList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ULog.d(TAG, "Error: " + error.toString());
                        try {
                            contentNotificationList = loadChachedList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
        );
    }

    private void persistList(List<ContentNotification> contentNotificationList) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(contentNotificationList);
        ULog.d(TAG , "Persist: " + listStringified);
        editor.putString(TAG , listStringified);
        editor.apply();
    }

    private List<ContentNotification> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<ContentNotification>>(){}.getType();
        ArrayList<ContentNotification> contents = gson.fromJson(sp.getString(TAG, ""), collectionType);
        return contents;
    }

    @Override
    public String getIngredientName() {
        return INGREDIENT_NAME;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put("notifications", ContentNotification.class);
        return map;
    }

    @Override
    public void buildFlavors() {
        supportedFlavors = new ArrayList<String>();
        supportedFlavors.add(SHOW_CONTENT_FLAVOR_NAME);
    }

    JSONObject testObject;
    String test = "{\"data\":[{\"id\":\"2b2bf055-e432-40a4-a5b8-7ea5f0fdd506\",\"type\":\"c-notifications\",\"attributes\":{\"text\":\"Tap to see content\",\"content\":\"Aut est et perspiciatis eos iure consectetur. Culpa dignissimos excepturi modi doloremque possimus. Porro a molestiae ut omnis amet ipsa quaerat.\",\"images_ids\":[],\"video_link\":null,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\"}}]}";


}
