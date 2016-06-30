package it.near.sdk.Reactions;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.Communication.NearAsyncHttpClient;
import it.near.sdk.MorpheusNear.JsonApiObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.ULog;

/**
 * Superclass for NearIT core-content reactions. Adds jsonAPI parsing, simple caching.
 * @author cattaneostefano.
 */
public abstract class CoreReaction extends Reaction {
    /**
     * Gson object to serialize and de-serialize the cache.
     */
    protected static Gson gson = null;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    protected NearAsyncHttpClient httpClient;
    /**
     * Cache prefix based on app package name.
     */
    protected static String PACK_NAME;
    /**
     * Morpheur object for JsonAPI parsing.
     */
    protected Morpheus morpheus;

    public CoreReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
        // static GSON object for de/serialization of objects to/from JSON
        gson = new Gson();
        PACK_NAME = mContext.getApplicationContext().getPackageName();
        setUpMorpheus();
        initSharedPreferences(getPrefSuffix());
        httpClient = new NearAsyncHttpClient();
        refreshConfig();
    }

    /**
     * Set up the jsonapi parser for parsing only related to this plugin.
     */
    public void setUpMorpheus(){
        HashMap<String, Class> classes = getModelHashMap();
        morpheus = new Morpheus();
        for (Map.Entry<String, Class> entry : classes.entrySet()){
            morpheus.getFactory().getDeserializer().registerResourceClass(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Initialize SharedPreferences.
     * The preference name is formed by our plugin name and the package name of the app, to avoid conflicts.
     * @param prefsNameSuffix suffix for shared parameters
     */
    protected void initSharedPreferences(String prefsNameSuffix) {
        String PREFS_NAME = PACK_NAME + prefsNameSuffix;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();
    }

    /**
     * Utility to persist lists in the SharedPreferences.
     * @param key
     * @param list
     */
    protected void persistList(String key, List list){
        String persistedString = gson.toJson(list);
        ULog.d(key, "Persist: " + persistedString);
        editor.putString(key, persistedString);
        editor.apply();
    }

    /**
     * Returns a String stored in SharedPreferences.
     * It was not possible to write a generic method already returning a list because of Java type erasure
     * @param key
     * @return
     * @throws JSONException
     */
    protected String loadCachedString(String key) throws JSONException {
        return sp.getString(key,"");
    }

    /**
     * Return the suffix for the local cache of this specific plugin. This string will be prefixxed by the app package name to avoid collision
     * with other nearit-powered apps.
     * @return the cache suffix. Use a suffix unique for the plugin.
     */
    public abstract String getPrefSuffix();

    /**
     * Returns the list of POJOs and the jsonAPI resource type string for this plugin.
     * @return
     */
    protected abstract HashMap<String,Class> getModelHashMap();

    /**
     * Return the resource type.
     * @return the name of the resource handled by the plugin.
     */
    protected abstract String getResTypeName();

}
