package it.near.sdk.Reactions;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.ULog;

/**
 * Superclass for plugins of type "reaction".
 * @author cattaneostefano
 */
public abstract class Reaction {
    private static final String TAG = "Reaction";
    public List<String> supportedActions = null;
    protected static Gson gson = null;
    protected SharedPreferences sp;
    protected SharedPreferences.Editor editor;
    protected static String PACK_NAME;
    protected Context mContext;
    protected NearNotifier nearNotifier;
    protected Morpheus morpheus;

    public Reaction(Context mContext, NearNotifier nearNotifier) {
        this.mContext = mContext;
        this.nearNotifier = nearNotifier;
        // static GSON object for de/serialization of objects to/from JSON
        gson = new Gson();
        PACK_NAME = mContext.getApplicationContext().getPackageName();
    }

    /**
     * set up the jsonapi parser for parsing only related to this plugin
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


    public List<String> getSupportedActions() {
        if (supportedActions == null){
            buildActions();
        }
        return supportedActions;
    }

    /**
     * Method called by the recipe manager to trigger a reaction.
     * @param recipe matched recipe
     */
    public void handleReaction(Recipe recipe){
        if (!getPluginName().equals(recipe.getReaction_plugin_id())){
            return;
        }
        handleReaction(recipe.getReaction_action().getId(), recipe.getReaction_bundle().getId(), recipe);
    }

    public void handlePushReaction(Recipe recipe,String push_id, JSONObject response){
        JSONObject reaction_bundle = fetchReactionBundle(response);
        try {
            reaction_bundle.put("type", getResTypeName());
            ULog.d(TAG, "");
            JSONObject outerObject = new JSONObject();
            outerObject.put("data", reaction_bundle);
            handlePushReaction(recipe,push_id, outerObject, response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private JSONObject fetchReactionBundle(JSONObject response) {
        try {
            JSONArray includedObject = response.getJSONArray("included");
            for (int i = 0; i < includedObject.length(); i++){
                JSONObject obj = (JSONObject) includedObject.get(i);
                if (obj.getString("type").equals("reaction_bundles")){
                    return obj;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Utility for parsing lists
     * @param json json to parse
     * @param clazz Object class of list objects
     * @param <T> generic type
     * @return List of Objects
     */
    protected <T> List<T> parseList(JSONObject json, Class<T> clazz) {
        JSONAPIObject jsonapiObject = null;
        try {
            jsonapiObject = morpheus.parse(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<T> returnList = new ArrayList<T>();

        for (Resource r : jsonapiObject.getResources()){
            returnList.add((T) r);
        }

        return returnList;
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
     * Build supported actions
     */
    public abstract void buildActions();
    public abstract void refreshConfig();
    public abstract String getPluginName();

    /**
     * Returns the list of POJOs and the jsonAPI resource type string for this plugin.
     * @return
     */
    protected abstract HashMap<String,Class> getModelHashMap();
    protected abstract void handleReaction(String reaction_action, String reaction_bundle, Recipe recipe);
    protected abstract void handlePushReaction(Recipe recipe,String push_id, JSONObject reaction_bundle, JSONObject response);
    protected abstract String getResTypeName();


}
