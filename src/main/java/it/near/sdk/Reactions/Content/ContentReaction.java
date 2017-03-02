package it.near.sdk.Reactions.Content;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearJsonHttpResponseHandler;
import it.near.sdk.Reactions.ContentFetchListener;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearJsonAPIUtils;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano
 */
public class ContentReaction extends CoreReaction {
    // ---------- content notification plugin ----------
    public static final String PLUGIN_NAME = "content-notification";
    private static final String CONTENT_NOTIFICATION_PATH =      "content-notification";
    private static final String CONTENT_NOTIFICATION_RESOURCE =  "contents";
    private static final String SHOW_CONTENT_ACTION_NAME = "show_content";
    private static final String TAG = "ContentReaction";
    public static final String PREFS_SUFFIX = "NearContentNot";
    private List<Content> contentList;

    public ContentReaction(Context context, NearNotifier nearNotifier) {
        super(context, nearNotifier);
    }


    @Override
    protected String getResTypeName() {
        return CONTENT_NOTIFICATION_RESOURCE;
    }

    @Override
    public void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch (reaction_action){
            case SHOW_CONTENT_ACTION_NAME:
                showContent(reaction_bundle.getId(), recipe);
                break;
        }
    }

    @Override
    protected void getContent(String reaction_bundle, Recipe recipe, final ContentFetchListener listener) {
        if (contentList == null){
            try {
                contentList = loadList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for ( Content cn : contentList){
            if (cn.getId().equals(reaction_bundle)){
                listener.onContentFetched(cn, true);
                return;
            }
        }
        requestSingleReaction(reaction_bundle, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Content content = NearJsonAPIUtils.parseElement(morpheus, response, Content.class);
                listener.onContentFetched(content, false);
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                listener.onContentFetchError("Error: " + statusCode + " : " + responseString);
            }
        });
    }

    public void refreshConfig() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                    .appendPath(CONTENT_NOTIFICATION_PATH)
                    .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                    .appendQueryParameter("include", "images,audio,upload").build();
        try {
            httpClient.nearGet(mContext, url.toString(), new NearJsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    contentList = NearJsonAPIUtils.parseList(morpheus, response, Content.class);
                    formatLinks(contentList);
                    persistList(TAG, contentList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    ULog.d(TAG, "Error: " + statusCode);
                    try {
                        contentList = loadList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reactionBundle) {
        Content content = (Content) reactionBundle;
        formatLinks(content);
        nearNotifier.deliverBackgroundPushReaction(content, recipe, push_id);
    }


    public void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler){
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(CONTENT_NOTIFICATION_PATH)
                .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                .appendPath(bundleId)
                .appendQueryParameter("include", "images").build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Content> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString , new TypeToken<Collection<Content>>(){}.getType());
    }

    private void formatLinks(List<Content> notifications){
        for (Content notification : notifications) {
            formatLinks(notification);
        }
    }

    private void formatLinks(Content notification){
        List<Image> images = notification.getImages();
        List<ImageSet> imageSets = new ArrayList<>();
        for (Image image : images) {
            imageSets.add(image.toImageSet());
        }
        notification.setImages_links(imageSets);
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
        map.put("contents", Content.class);
        map.put("images", Image.class);
        return map;
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<String>();
        supportedActions.add(SHOW_CONTENT_ACTION_NAME);
    }

}
