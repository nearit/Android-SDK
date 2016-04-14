package it.near.sdk.Recipes;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.MorpheusNear.Deserializer;
import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 13/04/16.
 */
public class RecipesManager {
    private static final String TAG = "RecipesManager";
    private final RequestQueue requestQueue;
    private Context mContext;
    private Morpheus morpheus;
    private List<Recipe> recipes = new ArrayList<>();
    private HashMap<String, Reaction> reactions = new HashMap<>();

    public RecipesManager(Context context) {
        this.mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.start();

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
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_flavors", Recipe.PulseFlavor.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("operation_flavor", Recipe.OperationFlavor.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_flavor", Recipe.ReactionFlavor.class);
    }

    public void addReaction(String ingredient, Reaction reaction){
        reactions.put(ingredient, reaction);
    }

    public void refreshConfig(){
        requestQueue.add(new CustomJsonRequest(mContext, Constants.API.recipes, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ULog.d(TAG, response.toString());
                recipes = parseList(response, Recipe.class);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ULog.d(TAG , "Error " + error.toString());
            }
        }));
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
                  recipe.getPulse_flavor().equals(pulse_flavor) &&
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
        reaction.handleReaction(recipe.getReaction_ingredient_id(),
                                recipe.getReaction_flavor().getId(),
                                recipe.getReaction_slice_id());
    }
}
