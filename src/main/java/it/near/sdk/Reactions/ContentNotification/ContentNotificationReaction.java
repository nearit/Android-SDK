package it.near.sdk.Reactions.ContentNotification;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.ArrayMap;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.internal.LinkedTreeMap;
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
public class ContentNotificationReaction extends Reaction {
    // ---------- content notification plugin ----------
    public static final String CONTENT_NOTIFICATION_PATH =      "content-notification";
    public static final String CONTENT_NOTIFICATION_RESOURCE =  "contents";
    private static final String PLUGIN_NAME = "content-notification";
    private static final String SHOW_CONTENT_ACTION_NAME = "show_content";
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
    protected String getResTypeName() {
        return "contents";
    }


    @Override
    public void handleReaction(String reaction_action, String reaction_bundle, Recipe recipe) {
        switch (reaction_action){
            case SHOW_CONTENT_ACTION_NAME:
                showContent(reaction_bundle, recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, String bundle_id) {
        //TODO download single resource
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(CONTENT_NOTIFICATION_PATH)
                .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                .appendPath(bundle_id)
                .appendQueryParameter("include", "images").build();
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, url.toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        ContentNotification content = NearUtils.parseElement(morpheus, response, ContentNotification.class);
                        formatLinks(content);
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

    private void showContent(String reaction_bundle, Recipe recipe) {
        ULog.d(TAG, "Show content: " + reaction_bundle);
        ContentNotification notification = getNotification(reaction_bundle);
        if (notification == null) return;
        nearNotifier.deliverBackgroundRegionReaction(notification, recipe);
    }

    private ContentNotification getNotification(String reaction_bundle) {
        if (contentNotificationList == null) return null;
        for ( ContentNotification cn : contentNotificationList){
            if (cn.getId().equals(reaction_bundle)){
                return cn;
            }
        }
        return null;
    }

    public void refreshConfig() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(CONTENT_NOTIFICATION_PATH)
                    .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                    .appendQueryParameter("include", "images").build();
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, url.toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        contentNotificationList = NearUtils.parseList(morpheus, response, ContentNotification.class);
                        formatLinks(contentNotificationList);
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
            formatLinks(notification);
        }
    }

    private void formatLinks(ContentNotification notification){
        List<Image> images = notification.getImages();
        List<ImageSet> imageSets = new ArrayList<>();
        for (Image image : images) {
            ImageSet imageSet = new ImageSet();
            HashMap<String, Object> map = image.getImage();
            imageSet.setFullSize((String) map.get("url"));
            imageSet.setBigSize(((LinkedTreeMap<String, Object>)map.get("max_1920_jpg")).get("url").toString());
            imageSet.setSmallSize(((LinkedTreeMap<String, Object>)map.get("square_300")).get("url").toString());
            imageSets.add(imageSet);
        }
        notification.setImages_links(imageSets);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put("contents", ContentNotification.class);
        map.put("images", Image.class);
        return map;
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<String>();
        supportedActions.add(SHOW_CONTENT_ACTION_NAME);
    }

    JSONObject testObject;
    String test = "{\"data\":[{\"id\":\"2b2bf055-e432-40a4-a5b8-7ea5f0fdd506\",\"type\":\"notifications\",\"attributes\":{\"text\":\"Tap to see content\",\"content\":null,\"images_ids\":[],\"video_link\":null,\"app_id\":\"cda5b1bd-e5b7-4ca7-8930-5bedcad449f6\",\"owner_id\":\"1bff22d9-3abc-43ed-b51b-764440c65865\",\"updated_at\":\"2016-04-13T14:59:18.171Z\",\"created_at\":\"2016-04-13T14:59:18.171Z\"}}]}";


}
