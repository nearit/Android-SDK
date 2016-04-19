package it.near.sdk.Reactions.ContentNotification;

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
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class ContentNotificationReaction extends Reaction {
    private static final String INGREDIENT_NAME = "content-notification";
    private static final String SHOW_CONTENT_FLAVOR_NAME = "show_content";
    private static final String TAG = "ContentNotificationReaction";
    public static final String PREFS_SUFFIX = "NearContentNot";
    private List<ContentNotification> contentNotificationList;

    public ContentNotificationReaction(Context context, NearNotifier nearNotifier) {
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
                        // formatLinks(contentNotificationList);
                        persistList(TAG, contentNotificationList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ULog.d(TAG, "Error: " + error.toString());
                        try {
                            contentNotificationList = loadList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
        );
    }


    private ArrayList<ContentNotification> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString , new TypeToken<Collection<ContentNotification>>(){}.getType());
    }

    private void formatLinks(List<ContentNotification> notifications){
        for (ContentNotification notification : notifications) {
            List<Image> images = notification.getImages();
            List<ImageSet> imageSets = new ArrayList<>();
            for (Image image : images) {
                ImageSet imageSet = new ImageSet();
                HashMap<String, Object> map = image.getImage();
                imageSet.setFullSize((String) map.get("url"));
                try {
                    imageSet.setBigSize(((JSONObject)map.get("max_1920_jpg")).getString("url"));
                    imageSet.setSmallSize(((JSONObject)map.get("square_300")).getString("url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                imageSets.add(imageSet);
            }
            notification.setImages_links(imageSets);
        }
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
    String test = "{\"data\":[{\"id\":\"2b2bf055-e432-40a4-a5b8-7ea5f0fdd506\",\"type\":\"notifications\",\"attributes\":{\"text\":\"Tap to see content\",\"content\":null,\"images_ids\":[],\"video_link\":null,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"updated_at\":\"2016-04-13T14:59:18.171Z\",\"created_at\":\"2016-04-13T14:59:18.171Z\"}}]}";


}
