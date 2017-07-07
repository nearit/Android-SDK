package it.near.sdk.reactions.contentplugin;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.contentplugin.model.Audio;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.contentplugin.model.Upload;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

public class ContentReaction extends CoreReaction<Content> {
    // ---------- content notification plugin ----------
    public static final String PLUGIN_NAME = "content-notification";
    private static final String INCLUDE_RESOURCES = "images,audio,upload";
    private static final String CONTENT_NOTIFICATION_PATH = "content-notification";
    private static final String CONTENT_NOTIFICATION_RESOURCE = "contents";
    private static final String SHOW_CONTENT_ACTION = "show_content";
    private static final String TAG = "ContentReaction";
    private static final String PREFS_NAME = "NearContentNot";

    ContentReaction(Cacher<Content> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier) {
        super(cacher, httpClient, nearNotifier, Content.class);
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected String getRefreshUrl() {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(CONTENT_NOTIFICATION_PATH)
                .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                .appendQueryParameter(Constants.API.INCLUDE_PARAMETER, INCLUDE_RESOURCES).build();
        return url.toString();
    }

    @Override
    protected String getSingleReactionUrl(String bundleId) {
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(CONTENT_NOTIFICATION_PATH)
                .appendPath(CONTENT_NOTIFICATION_RESOURCE)
                .appendPath(bundleId)
                .appendQueryParameter(Constants.API.INCLUDE_PARAMETER, INCLUDE_RESOURCES).build();
        return url.toString();
    }

    @Override
    protected String getDefaultShowAction() {
        return SHOW_CONTENT_ACTION;
    }

    @Override
    protected void injectRecipeId(Content element, String recipeId) {
        // left intentionally blank
    }

    @Override
    protected void normalizeElement(Content element) {
        List<Image> images = element.images;
        List<ImageSet> imageSets = new ArrayList<>();
        for (Image image : images) {
            imageSets.add(image.toImageSet());
        }
        element.setImages_links(imageSets);
    }

    @Override
    protected void getContent(String reactionBundleId, Recipe recipe, final ContentFetchListener listener) {
        if (reactionList == null) {
            try {
                reactionList = cacher.loadList();
            } catch (JSONException e) {
                NearLog.d(TAG, "Data format error");
            }
        }
        for (Content cn : safe(reactionList)) {
            if (cn.getId().equals(reactionBundleId)) {
                listener.onContentFetched(cn, true);
                return;
            }
        }
        requestSingleReaction(reactionBundleId, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Content content = NearJsonAPIUtils.parseElement(morpheus, response, Content.class);
                normalizeElement(content);
                listener.onContentFetched(content, false);
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                listener.onContentFetchError("Error: " + statusCode + " : " + responseString);
            }
        });
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        try {
            JSONObject toParse = new JSONObject(reactionBundleString);
            Content content = NearJsonAPIUtils.parseElement(morpheus, toParse, Content.class);
            if (content == null) return false;
            normalizeElement(content);
            nearNotifier.deliverBackgroundPushReaction(content, recipeId, notificationText, getReactionPluginName());
            return true;
        } catch (JSONException e) {
            return false;
        }
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

    public static ContentReaction obtain(Context context, NearNotifier nearNotifier) {
        return new ContentReaction(
                new Cacher<Content>(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context),
                nearNotifier);
    }
}
