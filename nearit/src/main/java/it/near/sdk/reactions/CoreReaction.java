package it.near.sdk.reactions;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

/**
 * Superclass for NearIT core-content reactions. Adds jsonAPI parsing, simple caching.
 *
 * @author cattaneostefano.
 */
public abstract class CoreReaction<T> extends Reaction {
    private static final String TAG = "CoreReaction";
    /**
     * Gson object to serialize and de-serialize the cache.
     */
    protected final NearAsyncHttpClient httpClient;
    protected final Cacher<T> cacher;
    protected final Class<T> type;
    protected List<T> reactionList;

    /**
     * Morpheur object for JsonAPI parsing.
     */
    protected Morpheus morpheus;

    public CoreReaction(Cacher<T> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, Class<T> type) {
        super(nearNotifier);
        // static GSON object for de/serialization of objects to/from JSON
        this.cacher = cacher;
        this.type = type;
        this.httpClient = httpClient;
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
                        reactionList = cacher.loadList();
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

    protected void showContent(String reaction_bundle, final Recipe recipe) {
        getContent(reaction_bundle, recipe, new ContentFetchListener() {
            @Override
            public void onContentFetched(Parcelable content, boolean cached) {
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

    protected abstract void getContent(String reaction_bundle, Recipe recipe, ContentFetchListener listener);

    protected abstract String getRefreshUrl();

    protected abstract String getSingleReactionUrl(String bundleId);


    /**
     * Returns the list of POJOs and the jsonAPI resource type string for this plugin.
     *
     */
    protected abstract HashMap<String, Class> getModelHashMap();

    /**
     * Download a single request.
     */
    protected void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler) {
        try {
            httpClient.nearGet(getSingleReactionUrl(bundleId), responseHandler);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    protected void requestSingleReaction(final String bundleId, final NearJsonHttpResponseHandler responseHandler, int i) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestSingleReaction(bundleId, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        responseHandler.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        responseHandler.onFailureUnique(statusCode, headers, throwable, responseString);
                    }
                });
            }
        }, i);
    }
}
