package it.near.sdk.reactions;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

/**
 * Superclass for NearIT core-content reactions. Adds jsonAPI parsing, simple caching.
 *
 * @author cattaneostefano.
 */
public abstract class CoreReaction<T extends ReactionBundle> extends Reaction {
    private static final String TAG = "CoreReaction";
    /**
     * Gson object to serialize and de-serialize the cache.
     */
    protected final NearAsyncHttpClient httpClient;
    protected final Cacher<T> cacher;
    protected final Class<T> type;
    protected List<T> reactionList;
    protected Type cacheType;

    /**
     * Morpheus object for JsonAPI parsing.
     */
    protected Morpheus morpheus;

    public CoreReaction(Cacher<T> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, Class<T> type, Type cacheType) {
        super(nearNotifier);
        // static GSON object for de/serialization of objects to/from JSON
        this.cacher = cacher;
        this.type = type;
        this.httpClient = httpClient;
        this.cacheType = cacheType;
        setUpMorpheus();
        refreshConfig();
    }

    /**
     * Set up the jsonapi parser for parsing only related to this plugin.
     */
    private void setUpMorpheus() {
        HashMap<String, Class> classes = getModelHashMap();
        morpheus = new Morpheus();
        for (Map.Entry<String, Class> entry : classes.entrySet()) {
            morpheus.getFactory().getDeserializer().registerResourceClass(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void refreshConfig() {
        String url = getRefreshUrl();
        try {
            httpClient.nearGet(url, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    reactionList = NearJsonAPIUtils.parseList(morpheus, response, type);
                    normalizeList(reactionList);
                    cacher.persistList(reactionList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error: " + statusCode);
                    try {
                        reactionList = cacher.loadList(cacheType);
                    } catch (JSONException e) {
                        NearLog.d(TAG, "Data format error");
                    }
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    protected void normalizeList(List<T> reactionList) {
        for (T element : reactionList) {
            normalizeElement(element);
        }
    }

    protected abstract void normalizeElement(T element);

    private void showContent(String reaction_bundle, final Recipe recipe) {
        getContent(reaction_bundle, recipe, new ContentFetchListener<ReactionBundle>() {
            @Override
            public void onContentFetched(ReactionBundle content, boolean cached) {
                if (content == null) return;
                if (recipe.isForegroundRecipe()) {
                    nearNotifier.deliverForegroundReaction(content, recipe);
                } else {
                    nearNotifier.deliverBackgroundReaction(content, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
                }
            }

            @Override
            public void onContentFetchError(String error) {
                NearLog.d(TAG, "Content not found");
            }
        });
    }

    protected void getContent(String reaction_bundle_id, final Recipe recipe, final ContentFetchListener<ReactionBundle> listener) {
        if (reactionList == null) {
            try {
                reactionList = cacher.loadList(cacheType);
            } catch (JSONException e) {
                NearLog.d(TAG, "Data format error");
            }
        }
        for (T element : safe(reactionList)) {
            if (element.getId().equals(reaction_bundle_id)) {
                injectRecipeId(element, recipe.getId());
                listener.onContentFetched(element, true);
                return;
            }
        }
        downloadSingleReaction(reaction_bundle_id, new ContentFetchListener<T>() {
            @Override
            public void onContentFetched(T element, boolean cached) {
                injectRecipeId(element, recipe.getId());
                listener.onContentFetched(element, false);
            }

            @Override
            public void onContentFetchError(String error) {
                listener.onContentFetchError(error);
            }
        });
    }

    protected abstract String getRefreshUrl();

    protected abstract String getSingleReactionUrl(String bundleId);

    protected abstract String getDefaultShowAction();

    protected abstract void injectRecipeId(T element, String recipeId);

    /**
     * Returns the list of POJOs and the jsonAPI resource type string for this plugin.
     */
    protected abstract HashMap<String, Class> getModelHashMap();

    /**
     * Download a single request.
     */
    protected void downloadSingleReaction(String bundleId, final ContentFetchListener<T> contentFetchListener) {
        try {
            httpClient.nearGet(getSingleReactionUrl(bundleId), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    T element = NearJsonAPIUtils.parseElement(morpheus, response, type);
                    normalizeElement(element);
                    contentFetchListener.onContentFetched(element, false);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    contentFetchListener.onContentFetchError("Couldn't fetch reaction");
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        if (reaction_action.equals(getDefaultShowAction())) {
            showContent(reaction_bundle.getId(), recipe);
        }
    }

    @Override
    public void handlePushReaction(final String recipeId, final String notificationText, String reactionAction, String reactionBundleId) {
        downloadSingleReaction(reactionBundleId, new ContentFetchListener<T>() {
            @Override
            public void onContentFetched(T element, boolean cached) {
                injectRecipeId(element, recipeId);
                nearNotifier.deliverBackgroundPushReaction(element, recipeId, notificationText, getReactionPluginName());
            }

            @Override
            public void onContentFetchError(String error) {
                NearLog.d(TAG, "Couldn't fetch content");
            }
        }, new Random().nextInt(1000));
    }

    @Override
    public void handlePushReaction(final Recipe recipe, String push_id, ReactionBundle reactionBundle) {
        T element = (T) reactionBundle;
        if (element.hasContentToInclude()) {
            downloadSingleReaction(element.getId(), new ContentFetchListener<T>() {
                @Override
                public void onContentFetched(T element, boolean cached) {
                    injectRecipeId(element, recipe.getId());
                    nearNotifier.deliverBackgroundPushReaction(element, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
                }

                @Override
                public void onContentFetchError(String error) {
                    NearLog.d(TAG, "Error: " + error);
                }
            });
        } else {
            injectRecipeId(element, recipe.getId());
            normalizeElement(element);
            nearNotifier.deliverBackgroundPushReaction(element, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
        }
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        try {
            JSONObject toParse = new JSONObject(reactionBundleString);
            T element = NearJsonAPIUtils.parseElement(morpheus, toParse, type);
            if (element == null) return false;
            injectRecipeId(element, recipeId);
            normalizeElement(element);
            nearNotifier.deliverBackgroundPushReaction(element, recipeId, notificationText, getReactionPluginName());
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private void downloadSingleReaction(final String bundleId, final ContentFetchListener<T> listener, int i) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                downloadSingleReaction(bundleId, listener);
            }
        }, i);
    }
}
