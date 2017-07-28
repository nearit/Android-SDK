package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.Reaction;
import it.near.sdk.recipes.models.OperationAction;
import it.near.sdk.recipes.models.PulseAction;
import it.near.sdk.recipes.models.PulseBundle;
import it.near.sdk.recipes.models.ReactionAction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.validation.RecipeValidationFilter;
import it.near.sdk.utils.NearJsonAPIUtils;

/**
 * Menage recipes download, caching and direct calling.
 *
 * @author cattaneostefano
 */
public class RecipesManager implements RecipeEvaluator {

    private static final String TAG = "RecipesManager";
    private static final String NEAR_RECIPES_PREFS_NAME = "NearRecipes";
    private static final String PROCESS_PATH = "process";
    private static final String EVALUATE = "evaluate";

    private Morpheus morpheus;
    private List<Recipe> recipes = new ArrayList<>();
    private HashMap<String, Reaction> reactions = new HashMap<>();
    private final NearAsyncHttpClient httpClient;
    private final GlobalConfig globalConfig;
    private final EvaluationBodyBuilder evaluationBodyBuilder;
    private final RecipeTrackSender recipeTrackSender;
    private final RecipeValidationFilter recipeValidationFilter;
    private final Cacher<Recipe> listCacher;

    public RecipesManager(NearAsyncHttpClient httpClient,
                          GlobalConfig globalConfig,
                          RecipeValidationFilter recipeValidationFilter,
                          EvaluationBodyBuilder evaluationBodyBuilder,
                          RecipeTrackSender recipeTrackSender,
                          Cacher<Recipe> listCacher) {
        this.httpClient = httpClient;
        this.globalConfig = globalConfig;
        this.evaluationBodyBuilder = evaluationBodyBuilder;
        this.recipeValidationFilter = recipeValidationFilter;
        this.recipeTrackSender = recipeTrackSender;
        this.listCacher = listCacher;

        try {
            recipes = listCacher.loadList();
        } catch (Exception e) {
            NearLog.d(TAG, "Recipes format error");
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

    public void addReaction(Reaction reaction) {
        reactions.put(reaction.getReactionPluginName(), reaction);
    }

    /**
     * return the list of recipes
     *
     * @return the list of recipes
     */
    public List<Recipe> getRecipes() {
        return recipes != null ? recipes : new ArrayList<Recipe>();
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     */
    public void refreshConfig() {
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
    public void refreshConfig(final RecipeRefreshListener listener) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendPath(PROCESS_PATH).build();
        String requestBody = null;
        try {
            requestBody = evaluationBodyBuilder.buildEvaluateBody();
        } catch (JSONException e) {
            NearLog.d(TAG, "Can't build request body");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), requestBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "Got recipes: " + response.toString());
                    recipes = NearJsonAPIUtils.parseList(morpheus, response, Recipe.class);
                    listCacher.persistList(recipes);
                    listener.onRecipesRefresh();
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in downloading recipes: " + statusCode);
                    try {
                        recipes = listCacher.loadList();

                    } catch (Exception e) {
                        NearLog.d(TAG, "Recipe format error");
                    }
                    listener.onRecipesRefreshFail();
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onRecipesRefreshFail();
        }
    }
    
    /**
     * Tries to trigger a recipe. If no reaction plugin can handle the recipe, nothing happens.
     *
     * @param recipe the recipe to trigger.
     */
    public void gotRecipe(Recipe recipe) {
        Reaction reaction = reactions.get(recipe.getReaction_plugin_id());
        reaction.handleReaction(recipe);
    }

    /**
     * Process a recipe from its id. Typically called for processing a push recipe.
     *
     * @param id push id.
     */
    public void processRecipe(final String id) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(id)
                .appendQueryParameter("filter[core][profile_id]", globalConfig.getProfileId())
                .appendQueryParameter("include", "reaction_bundle")
                .build();
        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    String reactionPluginName = recipe.getReaction_plugin_id();
                    Reaction reaction = reactions.get(reactionPluginName);
                    reaction.handlePushReaction(recipe, id, recipe.getReaction_bundle());
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "single recipe failed");
                }
            });
        } catch (AuthenticationException e) {
        }
    }

    /**
     * Process a recipe from the reaction triple. Used for getting a content from a push

     */
    public void processRecipe(String recipeId, String notificationText, String reactionPlugin, String reactionAction, String reactionBundleId) {
        Reaction reaction = reactions.get(reactionPlugin);
        if (reaction == null) return;
        reaction.handlePushReaction(recipeId, notificationText, reactionAction, reactionBundleId);
    }

    public boolean processReactionBundle(String recipeId, String notificationText, String reactionPlugin, String reactionAction, String reactionBundleString) {
        Reaction reaction = reactions.get(reactionPlugin);
        if (reaction == null) return false;
        return reaction.handlePushBundledReaction(recipeId, notificationText, reactionAction, reactionBundleString);
    }

    private void onlinePulseEvaluation(String pulse_plugin, String pulse_action, String pulse_bundle) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = evaluationBodyBuilder.buildEvaluateBody(pulse_plugin, pulse_action, pulse_bundle);
        } catch (JSONException e) {
            NearLog.d(TAG, "body build error");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), evaluateBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    if (recipe != null) {
                        // add recipe to cache
                        recipes.add(recipe);
                        listCacher.persistList(recipes);
                        gotRecipe(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in handling on failure: " + statusCode);
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Authentication error");
        } catch (UnsupportedEncodingException e) {
            NearLog.d(TAG, "Unsuported encoding");
        } catch (NullPointerException e) {
            NearLog.d(TAG, "Shouldn't be here");
        }
    }

    /**
     * Online evaluation of a recipe.
     *
     * @param recipeId recipe identifier.
     */
    public void evaluateRecipe(String recipeId) {
        NearLog.d(TAG, "Evaluating recipe: " + recipeId);
        if (recipeId == null) return;
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(recipeId)
                .appendPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = evaluationBodyBuilder.buildEvaluateBody();
        } catch (JSONException e) {
            NearLog.d(TAG, "body build error");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), evaluateBody, new NearJsonHttpResponseHandler() {
                @Override
                public void setUsePoolThread(boolean pool) {
                    super.setUsePoolThread(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    // TODO refactor plugin
                    if (recipe != null) {
                        gotRecipe(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in handling on failure: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            NearLog.d(TAG, "Error");
        }
    }

    private boolean filterAndNotify(List<Recipe> matchingRecipes) {
        matchingRecipes = recipeValidationFilter.filterRecipes(matchingRecipes);
        if (matchingRecipes.isEmpty()) return false;
        Recipe winnerRecipe = matchingRecipes.get(0);
        if (winnerRecipe.isEvaluatedOnline()) {
            evaluateRecipe(winnerRecipe.getId());
        } else {
            gotRecipe(winnerRecipe);
        }
        return true;
    }

    @Override
    public boolean handlePulseLocally(String plugin_name, String plugin_action, String plugin_bundle) {
        if (plugin_name == null || plugin_action == null || plugin_bundle == null) return false;

        List<Recipe> matchingRecipes = new ArrayList<>();
        if (recipes == null) return false;
        // Find the recipes that matches the pulse
        try {
            for (Recipe recipe : recipes) {
                if (recipe.getPulse_plugin_id() == null ||
                        recipe.getPulse_action() == null ||
                        recipe.getPulse_action().getId() == null ||
                        recipe.getPulse_bundle() == null ||
                        recipe.getPulse_bundle().getId() == null)
                    continue;
                if (recipe.getPulse_plugin_id().equals(plugin_name) &&
                        recipe.getPulse_action().getId().equals(plugin_action) &&
                        recipe.getPulse_bundle().getId().equals(plugin_bundle)) {
                    matchingRecipes.add(recipe);
                }
            }
        } catch (ClassCastException exception) {
            recipes = null;
            return false;
        }

        if (matchingRecipes.isEmpty()) return false;

        return filterAndNotify(matchingRecipes);
    }


    @Override
    public boolean handlePulseTags(String plugin_name, String plugin_action, List<String> plugin_tags) {
        if (plugin_name == null || plugin_action == null || plugin_tags == null || plugin_tags.isEmpty())
            return false;

        List<Recipe> matchingRecipes = new ArrayList<>();
        if (recipes == null) return false;
        // Find the recipes that matches the pulse
        try {
            for (Recipe recipe : recipes) {
                if (recipe.getPulse_plugin_id() == null ||
                        recipe.getPulse_action() == null ||
                        recipe.getPulse_action().getId() == null ||
                        recipe.tags == null ||
                        recipe.tags.isEmpty())
                    continue;
                if (recipe.getPulse_plugin_id().equals(plugin_name) &&
                        recipe.getPulse_action().getId().equals(plugin_action) &&
                        plugin_tags.containsAll(recipe.tags)) {
                    matchingRecipes.add(recipe);
                }
            }
        } catch (ClassCastException exception) {
            recipes = null;
            return false;
        }

        if (matchingRecipes.isEmpty()) return false;

        return filterAndNotify(matchingRecipes);
    }

    @Override
    public void handlePulseOnline(String plugin_name, String plugin_action, String plugin_bundle) {
        onlinePulseEvaluation(plugin_name, plugin_action, plugin_bundle);
    }

    /**
     * Sends tracking on a recipe.
     * Those two statuses are natively supported:
     * {@value Recipe#NOTIFIED_STATUS} and {@value Recipe#ENGAGED_STATUS}
     * If you wish to use custom tracking, send your string as a tracking event.
     *
     * @param recipeId      the recipe identifier.
     * @param trackingEvent notified status to send.
     * @throws JSONException
     */
    public void sendTracking(String recipeId, String trackingEvent) throws JSONException {
        recipeTrackSender.sendTracking(recipeId, trackingEvent);
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NEAR_RECIPES_PREFS_NAME, Context.MODE_PRIVATE);
    }
}
