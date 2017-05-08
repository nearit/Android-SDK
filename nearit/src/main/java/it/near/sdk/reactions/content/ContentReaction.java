package it.near.sdk.reactions.content;

import android.content.Context;
import android.net.Uri;


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
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

/**
 * @author cattaneostefano
 */
public class ContentReaction extends CoreReaction {
    // ---------- content notification plugin ----------
    public static final String PLUGIN_NAME = "content-notification";
    private static final String INCLUDE_RESOURCES = "images,audio,upload";
    private static final String CONTENT_NOTIFICATION_PATH = "content-notification";
    private static final String CONTENT_NOTIFICATION_RESOURCE = "contents";
    private static final String SHOW_CONTENT_ACTION_NAME = "show_content";
    private static final String TAG = "ContentReaction";
    private static final String PREFS_SUFFIX = "NearContentNot";
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
        switch (reaction_action) {
            case SHOW_CONTENT_ACTION_NAME:
                showContent(reaction_bundle.getId(), recipe);
                break;
        }
    }

    @Override
    protected void getContent(String reactionBundleId, Recipe recipe, final ContentFetchListener listener) {
        if (contentList == null) {
            try {
                contentList = loadList();
            } catch (JSONException e) {
                NearLog.d(TAG, "Data format error");
            }
        }
        for (Content cn : safe(contentList)) {
            if (cn.getId().equals(reactionBundleId)) {
                listener.onContentFetched(cn, true);
                return;
            }
        }
        requestSingleReaction(reactionBundleId, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Content content = NearJsonAPIUtils.parseElement(morpheus, response, Content.class);
                formatLinks(content);
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
                .appendQueryParameter(Constants.API.INCLUDE_PARAMETER, INCLUDE_RESOURCES).build();
        try {
            httpClient.get(mContext, url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    contentList = NearJsonAPIUtils.parseList(morpheus, response, Content.class);
                    formatLinks(contentList);
                    persistList(TAG, contentList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error: " + statusCode);
                    try {
                        contentList = loadList();
                    } catch (JSONException e) {
                        NearLog.d(TAG, "Data format error");
                    }
                }

            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }

    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reactionBundle) {
        Content content = (Content) reactionBundle;
        if (content.hasContentToInclude()) {
            requestSingleReaction(reactionBundle.getId(), new NearJsonHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Content content = NearJsonAPIUtils.parseElement(morpheus, response, Content.class);
                    formatLinks(content);
                    nearNotifier.deliverBackgroundPushReaction(content, recipe, push_id);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "couldn't fetch content for push recipe");
                }
            });
        } else {
            nearNotifier.deliverBackgroundPushReaction(content, recipe, push_id);
        }

    }


    private void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(CONTENT_NOTIFICATION_PATH)
                .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                .appendPath(bundleId)
                .appendQueryParameter(Constants.API.INCLUDE_PARAMETER, INCLUDE_RESOURCES).build();
        try {
            httpClient.get(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    private ArrayList<Content> loadList() throws JSONException {
        String cachedString = loadCachedString(TAG);
        return gson.fromJson(cachedString, new TypeToken<Collection<Content>>() {
        }.getType());
    }

    private void formatLinks(List<Content> notifications) {
        for (Content notification : notifications) {
            formatLinks(notification);
        }
    }

    private void formatLinks(Content notification) {
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
        map.put("audios", Audio.class);
        map.put("uploads", Upload.class);
        return map;
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<>();
        supportedActions.add(SHOW_CONTENT_ACTION_NAME);
    }

}
