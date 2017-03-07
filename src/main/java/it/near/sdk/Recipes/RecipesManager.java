package it.near.sdk.Recipes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearAsyncHttpClient;
import it.near.sdk.Communication.NearJsonHttpResponseHandler;
import it.near.sdk.Communication.NearNetworkUtil;
import it.near.sdk.GlobalConfig;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.Models.OperationAction;
import it.near.sdk.Recipes.Models.PulseAction;
import it.near.sdk.Recipes.Models.PulseBundle;
import it.near.sdk.Recipes.Models.ReactionAction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearJsonAPIUtils;

/**
 * Menage recipes download, caching and direct calling.
 *
 * @author cattaneostefano
 */
public class RecipesManager {
    private static final String TAG = "RecipesManager";
    public static final String PREFS_NAME = "NearRecipes";
    private static final String PROCESS_PATH = "process";
    private static final String EVALUATE = "evaluate";
    private static final String TRACKINGS_PATH = "trackings";
    static final String PULSE_PLUGIN_ID_KEY = "pulse_plugin_id";
    static final String PULSE_ACTION_ID_KEY = "pulse_action_id";
    static final String PULSE_BUNDLE_ID_KEY = "pulse_bundle_id";
    private final SharedPreferences sp;
    private Context mContext;
    private Morpheus morpheus;
    private List<Recipe> recipes = new ArrayList<>();
    private HashMap<String, Reaction> reactions = new HashMap<>();
    SharedPreferences.Editor editor;
    private NearAsyncHttpClient httpClient;
    private static RecipeCooler mRecipeCooler;
    private GlobalConfig globalConfig;

    public RecipesManager(Context context,
                          GlobalConfig globalConfig,
                          RecipeCooler recipeCooler,
                          SharedPreferences sp) {
        this.mContext = context;
        this.globalConfig = globalConfig;
        mRecipeCooler = recipeCooler;
        this.sp = sp;
        editor = sp.edit();

        httpClient = new NearAsyncHttpClient();
        try {
            recipes = loadChachedList();
        } catch (JSONException e) {
            Log.d(TAG, "Recipes format error");
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

    public void addReaction(Reaction reaction){
        reactions.put(reaction.getPluginName(), reaction);
    }

    /**
     * return the list of recipes
     * @return the list of recipes
     */
    public List<Recipe> getRecipes() {
        return recipes!= null ? recipes : new ArrayList<Recipe>();
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     */
    public void refreshConfig(){
        refreshConfig(new RecipeRefreshListener() {
            @Override
            public void onRecipesRefresh() {
            }

            @Override
            public void onRecipesRefreshFail() {
            }
        });
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     * Plus a listener will be notified of the refresh process.
     */
    public void refreshConfig(final RecipeRefreshListener listener){
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendPath(PROCESS_PATH).build();
        String requestBody = null;
        try {
            requestBody = buildEvaluateBody(globalConfig, null, null, null, null);
        } catch (JSONException e) {
            Log.d(TAG, "Can't build request body");
            return;
        }

        try {
            httpClient.nearPost(mContext, url.toString(), requestBody, new NearJsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Got recipes: " + response.toString());
                    recipes = NearJsonAPIUtils.parseList(morpheus, response, Recipe.class);
                    persistList(recipes);
                    listener.onRecipesRefresh();
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    Log.d(TAG, "Error in downloading recipes: " + statusCode);
                    try {
                        recipes = loadChachedList();
                    } catch (JSONException e) {
                        Log.d(TAG, "Recipe format error");
                    }
                    listener.onRecipesRefreshFail();
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onRecipesRefreshFail();
        }
    }

    private void persistList(List<Recipe> recipes) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(recipes);
        editor.putString(TAG , listStringified);
        editor.apply();
    }

    private List<Recipe> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<Recipe>>(){}.getType();
        return gson.<ArrayList<Recipe>>fromJson(sp.getString(TAG, ""), collectionType);
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
        if (recipes == null) return;
        // Find the recipes that matches the pulse
        for (Recipe recipe : recipes){
            // TODO check for null pulse bundle
             if ( recipe.getPulse_plugin_id().equals(pulse_plugin) &&
                  recipe.getPulse_action().getId().equals(pulse_action) &&
                  recipe.getPulse_bundle().getId().equals(pulse_bundle) ) {
                 matchingRecipes.add(recipe);
             }
        }

        // From all the recipes, filter the ones that are scheduled for now
        List<Recipe> validRecipes = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        for (Recipe matchingRecipe : matchingRecipes) {
            if (matchingRecipe.isScheduledNow(now)){
                validRecipes.add(matchingRecipe);
            }
        }

        mRecipeCooler.filterRecipe(validRecipes);

        if (validRecipes.isEmpty()){
            // if no recipe is found the the online fallback
            onlinePulseEvaluation(pulse_plugin, pulse_action, pulse_bundle);
        } else {
            // take the first recipe and run with it
            Recipe winnerRecipe = validRecipes.get(0);
            if (winnerRecipe.isEvaluatedOnline()){
                evaluateRecipe(winnerRecipe.getId());
            } else {
                gotRecipe(winnerRecipe);
            }
        }
    }

    /**
     * Tries to trigger a recipe. If no reaction plugin can handle the recipe, nothing happens.
     *
     * @param recipe the recipe to trigger.
     */
    public void gotRecipe(Recipe recipe){
        String stringRecipe = recipe.getName();
        Reaction reaction = reactions.get(recipe.getReaction_plugin_id());
        reaction.handleReaction(recipe);
    }

    /**
     * Process a recipe from its id. Typically called for processing a push recipe.
     * @param id push id.
     */
    public void processRecipe(final String id) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(id)
                .appendQueryParameter("filter[core][profile_id]", globalConfig.getProfileId())
                .appendQueryParameter("include", "reaction_bundle")
                .build();
        try {
            httpClient.nearGet(mContext, url.toString(), new NearJsonHttpResponseHandler(){

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    String reactionPluginName = recipe.getReaction_plugin_id();
                    Reaction reaction = reactions.get(reactionPluginName);
                    reaction.handlePushReaction(recipe, id, recipe.getReaction_bundle());
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    Log.d(TAG, "single recipe failed");
                }
            });
        } catch (AuthenticationException e) {
        }
    }

    private void onlinePulseEvaluation(String pulse_plugin, String pulse_action, String pulse_bundle) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = buildEvaluateBody(globalConfig,
                    mRecipeCooler, pulse_plugin, pulse_action, pulse_bundle);
        } catch (JSONException e) {
            Log.d(TAG, "body build error");
            return;
        }

        try {
            httpClient.nearPost(mContext, url.toString(), evaluateBody, new NearJsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    if (recipe != null){
                        gotRecipe(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    Log.d(TAG, "Error in handling on failure: " + statusCode);
                }
            });
        } catch (AuthenticationException e) {
            Log.d(TAG, "Authentication error");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Unsuported encoding");
        } catch (NullPointerException e){
            Log.d(TAG, "Shouldn't be here");
        }
    }

    /**
     * Online evaluation of a recipe.
     * @param recipeId recipe identifier.
     */
    public void evaluateRecipe(String recipeId){
        Log.d(TAG, "Evaluating recipe: " + recipeId);
        if (recipeId == null) return;
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(recipeId)
                .appendPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = buildEvaluateBody(globalConfig,
                                            mRecipeCooler, null, null, null);
        } catch (JSONException e) {
            Log.d(TAG, "body build error");
            return;
        }

        try {
            httpClient.nearPost(mContext, url.toString(), evaluateBody, new NearJsonHttpResponseHandler(){
                @Override
                public void setUsePoolThread(boolean pool) {
                    super.setUsePoolThread(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    // TODO refactor plugin
                    if (recipe != null){
                        gotRecipe(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    Log.d(TAG, "Error in handling on failure: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            Log.d(TAG, "Error");
        }
    }

    /**
     * Sends tracking on a recipe. Lets choose the notified status.
     * @param context the app context.
     * @param recipeId the recipe identifier.
     * @param trackingEvent notified status to send. Can either be NO
     * @throws JSONException
     */
    public static void sendTracking(Context context, String recipeId, String trackingEvent) throws JSONException {
        if (trackingEvent.equals(Recipe.NOTIFIED_STATUS)){
            if (mRecipeCooler != null){
                mRecipeCooler.markRecipeAsShown(recipeId);
            }
        }
        String trackingBody = Recipe.buildTrackingBody(GlobalConfig.getInstance(context), recipeId, trackingEvent);
        Uri url = Uri.parse(TRACKINGS_PATH).buildUpon().build();
        NearNetworkUtil.sendTrack(context, url.toString(), trackingBody);
    }

    public static String buildEvaluateBody(@NonNull GlobalConfig globalConfig,
                                           @Nullable RecipeCooler recipeCooler,
                                           @Nullable String pulse_plugin,
                                           @Nullable String pulse_action,
                                           @Nullable String pulse_bundle) throws JSONException {
        if (globalConfig.getProfileId() == null ||
                globalConfig.getInstallationId() == null ||
                globalConfig.getAppId() == null){
            throw new JSONException("missing data");
        }
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("core", buildCoreObject(globalConfig, recipeCooler));
        if (pulse_plugin != null) attributes.put(PULSE_PLUGIN_ID_KEY, pulse_plugin);
        if (pulse_action != null) attributes.put(PULSE_ACTION_ID_KEY, pulse_action);
        if (pulse_bundle != null) attributes.put(PULSE_BUNDLE_ID_KEY, pulse_bundle);
        return NearJsonAPIUtils.toJsonAPI("evaluation", attributes);
    }

    private static HashMap<String, Object> buildCoreObject(@NonNull GlobalConfig globalConfig,
                                                           @Nullable RecipeCooler recipeCooler) {
        HashMap<String, Object> coreObj = new HashMap<>();
        coreObj.put("profile_id", globalConfig.getProfileId());
        coreObj.put("installation_id", globalConfig.getInstallationId());
        coreObj.put("app_id", globalConfig.getAppId());
        if (recipeCooler != null) {
            coreObj.put("cooldown", buildCooldownBlock(recipeCooler));
        }

        return coreObj;
    }

    private static HashMap<String, Object> buildCooldownBlock(@NonNull RecipeCooler recipeCooler) {
        HashMap<String, Object> block = new HashMap<>();
        block.put("last_notified_at", recipeCooler.getLatestLogEntry());
        block.put("recipes_notified_at", recipeCooler.getRecipeLogMap());
        return block;
    }
}
