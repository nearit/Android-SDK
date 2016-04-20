package it.near.sdk.Reactions;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

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
 * Created by cattaneostefano on 14/04/16.
 */
public abstract class Reaction {
    public List<String> supportedFlavors = null;
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
     * @param prefsNameSuffix
     */
    protected void initSharedPreferences(String prefsNameSuffix) {
        String PREFS_NAME = PACK_NAME + prefsNameSuffix;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();
    }


    public List<String> getSupportedFlavors() {
        if (supportedFlavors == null){
            buildFlavors();
        }
        return supportedFlavors;
    }

    /**
     * Method called by the recipe manager to trigger a reaction.
     * @param recipe
     */
    public void handleReaction(Recipe recipe){
        if (!getIngredientName().equals(recipe.getReaction_ingredient_id())){
            return;
        }
        handleReaction(recipe.getReaction_flavor().getId(), recipe.getReaction_slice_id(), recipe);
    }

    /**
     * Utility for parsing lists
     * @param json
     * @param clazz
     * @param <T>
     * @return
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
     * Build supported flavors
     */
    public abstract void buildFlavors();
    public abstract void refreshConfig();
    public abstract String getIngredientName();

    /**
     * Returns the list of POJOs and the jsonAPI resource type string for this plugin.
     * @return
     */
    protected abstract HashMap<String,Class> getModelHashMap();
    protected abstract void handleReaction(String reaction_flavor, String reaction_slice, Recipe recipe);

}
