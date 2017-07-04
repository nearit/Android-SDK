package it.near.sdk.reactions;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
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
    protected static final String KEY_LIST = "list";
    /**
     * Gson object to serialize and de-serialize the cache.
     */
    protected static Gson gson = null;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    protected NearAsyncHttpClient httpClient;
    protected List<T> reactionList;
    protected final Class<T> type;

    /**
     * Morpheur object for JsonAPI parsing.
     */
    protected Morpheus morpheus;

    public CoreReaction(SharedPreferences sharedPreferences, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, Class<T> type) {
        super(nearNotifier);
        // static GSON object for de/serialization of objects to/from JSON
        this.sp = sharedPreferences;
        this.editor = sp.edit();
        this.type = type;
        this.httpClient = httpClient;
        gson = new Gson();
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
                    persistList(reactionList);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error: " + statusCode);
                    try {
                        reactionList = loadList();
                    } catch (JSONException e) {
                        NearLog.d(TAG, "Data format error");
                    }
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }
    }

    /**
     * Utility to persist lists in the SharedPreferences.
     *
     * @param list
     */
    protected void persistList(List list) {
        String persistedString = gson.toJson(list);
        editor.putString(KEY_LIST, persistedString);
        editor.apply();
    }

    protected List<T> loadList() throws JSONException {
        String cachedString = loadCachedString(KEY_LIST);
        return gson.fromJson(cachedString, new TypeToken<Collection<T>>() {
        }.getType());
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

    /**
     * Returns a String stored in SharedPreferences.
     * It was not possible to write a generic method already returning a list because of Java type erasure
     *
     * @param key
     * @return
     */
    protected String loadCachedString(String key) {
        return sp.getString(key, "");
    }
    /**
     * Returns the list of POJOs and the jsonAPI resource type string for this plugin.
     *
     * @return
     */
    protected abstract HashMap<String, Class> getModelHashMap();

    /**
     * Return the resource type.
     *
     * @return the name of the resource handled by the plugin.
     */
    protected abstract String getResTypeName();

    /**
     * Download a single request.
     */
    protected abstract void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler);


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
