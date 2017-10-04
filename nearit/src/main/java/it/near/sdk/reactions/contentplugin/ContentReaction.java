package it.near.sdk.reactions.contentplugin;

import android.content.Context;
import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.contentplugin.model.Audio;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.contentplugin.model.ContentLink;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.contentplugin.model.Upload;
import it.near.sdk.recipes.NearNotifier;

import static it.near.sdk.reactions.contentplugin.model.ContentLink.CTA_LABEL_KEY;
import static it.near.sdk.reactions.contentplugin.model.ContentLink.CTA_URL_KEY;
import static it.near.sdk.utils.NearUtils.safe;

public class ContentReaction extends CoreReaction<Content> {
    // ---------- content notification plugin ----------
    public static final String PLUGIN_NAME = "content-notification";
    private static final String INCLUDE_RESOURCES = "images,audio,upload";
    private static final String CONTENT_NOTIFICATION_PATH = "content-notification";
    private static final String CONTENT_NOTIFICATION_RESOURCE = "contents";
    static final String SHOW_CONTENT_ACTION = "show_content";
    private static final String PREFS_NAME = "NearContentNot";
    public static final String RES_CONTENTS = "contents";
    public static final String RES_IMAGES = "images";
    public static final String RES_AUDIOS = "audios";
    public static final String RES_UPLOADS = "uploads";


    ContentReaction(Cacher<Content> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, Type cacheType) {
        super(cacher, httpClient, nearNotifier, Content.class, cacheType);
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
        for (Image image : safe(images)) {
            try {
                imageSets.add(image.toImageSet());
            } catch (Image.MissingImageException ignored) {

            }
        }
        element.setImages_links(imageSets);

        try {
            if (element.link != null &&
                    element.link.containsKey(CTA_LABEL_KEY) &&
                    element.link.containsKey(CTA_URL_KEY)) {
                String label = (String) element.link.get(CTA_LABEL_KEY);
                String url = (String) element.link.get(CTA_URL_KEY);

                ContentLink contentLink = new ContentLink(label, url);
                element.setCta(contentLink);
            }
        } catch (Throwable ignored) {}

    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(RES_CONTENTS, Content.class);
        map.put(RES_IMAGES, Image.class);
        map.put(RES_AUDIOS, Audio.class);
        map.put(RES_UPLOADS, Upload.class);
        return map;
    }

    public static ContentReaction obtain(Context context, NearNotifier nearNotifier) {
        return new ContentReaction(
                new Cacher<Content>(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context),
                nearNotifier,
                new TypeToken<List<Content>>() {}.getType());
    }
}
