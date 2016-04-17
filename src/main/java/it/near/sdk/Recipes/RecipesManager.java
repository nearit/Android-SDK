package it.near.sdk.Recipes;

import android.content.Context;
import android.content.SharedPreferences;

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
import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.Models.OperationFlavor;
import it.near.sdk.Recipes.Models.PulseFlavor;
import it.near.sdk.Recipes.Models.ReactionFlavor;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 13/04/16.
 */
public class RecipesManager {
    private static final String TAG = "RecipesManager";
    public static final String PREFS_SUFFIX = "NearRecipes";
    public final String PREFS_NAME;
    private final RequestQueue requestQueue;
    private final SharedPreferences sp;
    private Context mContext;
    private Morpheus morpheus;
    private List<Recipe> recipes = new ArrayList<>();
    private HashMap<String, Reaction> reactions = new HashMap<>();
    SharedPreferences.Editor editor;

    public RecipesManager(Context context) {
        this.mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.start();

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
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_flavors", PulseFlavor.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("operation_flavor", OperationFlavor.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_flavors", ReactionFlavor.class);
    }

    public void addReaction(String ingredient, Reaction reaction){
        reactions.put(ingredient, reaction);
    }

    public void refreshConfig(){
        Filter filter = Filter.build().addFilter("active","true");
        requestQueue.add(new CustomJsonRequest(mContext, Constants.API.recipes_include_flavors + filter.print(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ULog.d(TAG, response.toString());
                recipes = parseList(response, Recipe.class);
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

    private <T> List<T> parseList(JSONObject json, Class<T> clazz) {
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

    public void gotPulse(String pulse_ingredient, String pulse_flavor, String pulse_slice){
        List<Recipe> matchingRecipes = new ArrayList<>();
        for (Recipe recipe : recipes){
             if ( recipe.getPulse_ingredient_id().equals(pulse_ingredient) &&
                  recipe.getPulse_flavor().getId().equals(pulse_flavor) &&
                  recipe.getPulse_slice_id().equals(pulse_slice) ) {
                 matchingRecipes.add(recipe);
             }
        }
        if (matchingRecipes.isEmpty()){return;}
        Recipe winnerRecipe = matchingRecipes.get(0);
        gotRecipe(winnerRecipe);
    }

    private void gotRecipe(Recipe recipe){
        String stringRecipe = recipe.getName();
        ULog.d(TAG , stringRecipe);
        Reaction reaction = reactions.get(recipe.getReaction_ingredient_id());
        reaction.handleReaction(recipe);
    }
}
