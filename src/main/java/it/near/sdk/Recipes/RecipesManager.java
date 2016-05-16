package it.near.sdk.Recipes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.Communication.Filter;
import it.near.sdk.GlobalState;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.Models.OperationAction;
import it.near.sdk.Recipes.Models.PulseAction;
import it.near.sdk.Recipes.Models.PulseBundle;
import it.near.sdk.Recipes.Models.ReactionAction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * Menage recipes download, caching and direct calling.
 *
 * @author cattaneostefano
 */
public class RecipesManager {
    private static final String TAG = "RecipesManager";
    public static final String PREFS_SUFFIX = "NearRecipes";
    public final String PREFS_NAME;
    private final SharedPreferences sp;
    private Context mContext;
    private Morpheus morpheus;
    private List<Recipe> recipes = new ArrayList<>();
    private HashMap<String, Reaction> reactions = new HashMap<>();
    SharedPreferences.Editor editor;

    public RecipesManager(Context context) {
        this.mContext = context;

        String PACK_NAME = mContext.getApplicationContext().getPackageName();
        PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();
        try {
            loadChachedList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setUpMorpheusParser();
        refreshConfig();
    }

    /**
     * Set up Morpheus parser. Morpheus parses jsonApi encoded resources
     * https://github.com/xamoom/Morpheus
     * We didn't actually use this library due to its minSdkVersion. We instead imported its code and adapted it.
     */
    private void setUpMorpheusParser() {
        morpheus = new Morpheus();
        // register your resources
        morpheus.getFactory().getDeserializer().registerResourceClass("recipes", Recipe.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_actions", PulseAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("operation_actions", OperationAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_actions", ReactionAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_bundles", PulseBundle.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_bundles", ReactionBundle.class);
    }

    public void addReaction(String plugin, Reaction reaction){
        reactions.put(plugin, reaction);
    }

    /**
     * return the list of recipes
     * @return the list of recipes
     */
    public List<Recipe> getRecipes() {
        return recipes;
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     */
    public void refreshConfig(){
        // TODO turn strings to constants
        final Uri uri = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendQueryParameter("filter[active]", "true")
                .appendQueryParameter("include", "pulse_action,operation_action,reaction_action").build();
        GlobalState.getInstance(mContext).getRequestQueue().add(
                new CustomJsonRequest(mContext, uri.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ULog.d(TAG, uri.toString());
                ULog.d(TAG, response.toString());
                recipes = NearUtils.parseList(morpheus, response, Recipe.class);
                persistList(recipes);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ULog.d(TAG , "Error " + error.toString());
                try {
                    recipes = loadChachedList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private void persistList(List<Recipe> recipes) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(recipes);
        ULog.d(TAG , "Persist: " + listStringified);
        editor.putString(TAG , listStringified);
        editor.apply();
    }

    private List<Recipe> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
        ArrayList<Recipe> recipes = gson.fromJson(sp.getString(TAG, ""), collectionType);
        return recipes;
    }

    /**
     * Tries to trigger a recipe, stating the plugin, action and bundle of the pulse.
     * If nothing matches, nothing happens.
     *
     * @param pulse_plugin the plugin of the pulse.
     * @param pulse_action the action of the pulse.
     * @param pulse_bundle the bundle of the pulse.
     */
    public void gotPulse(String pulse_plugin, String pulse_action, String pulse_bundle){
        List<Recipe> matchingRecipes = new ArrayList<>();
        for (Recipe recipe : recipes){
             if ( recipe.getPulse_plugin_id().equals(pulse_plugin) &&
                  recipe.getPulse_action().getId().equals(pulse_action) &&
                  recipe.getPulse_bundle().getId().equals(pulse_bundle) ) {
                 matchingRecipes.add(recipe);
             }
        }
        if (matchingRecipes.isEmpty()){return;}
        Recipe winnerRecipe = matchingRecipes.get(0);
        gotRecipe(winnerRecipe);
    }

    /**
     * Tries to trigger a recipe. If no reaction plugin can handle the recipe, nothing happens.
     *
     * @param recipe the recipe to trigger.
     */
    public void gotRecipe(Recipe recipe){
        String stringRecipe = recipe.getName();
        ULog.d(TAG , stringRecipe);
        Reaction reaction = reactions.get(recipe.getReaction_plugin_id());
        reaction.handleReaction(recipe);
    }

    /**
     * Process a recipe from it's id. Typically called for processing a push recipe.
     * @param id recipe id.
     * @return true if the recipe was found, false otherwise.
     */
    public boolean processRecipe(String id) {
        // todo download recipe
        Uri uri = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(id).build();

        // inside receiver, parse the response to know what reaction plugin to use
        // than fire the reaction
        // if we got a network error, return false
        return true;
    }
}
