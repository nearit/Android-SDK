package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.Reaction;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.validation.RecipeValidationFilter;

/**
 * Menage recipes download, caching and direct calling.
 *
 * @author cattaneostefano
 */
public class RecipesManager implements RecipeEvaluator {

    private static final String TAG = "RecipesManager";
    private static final String NEAR_RECIPES_PREFS_NAME = "NearRecipes";

    private HashMap<String, Reaction> reactions = new HashMap<>();
    private final RecipeTrackSender recipeTrackSender;
    private final RecipeValidationFilter recipeValidationFilter;
    private final Cacher<Recipe> listCacher;
    private final RecipeRepository recipeRepository;
    private final RecipesApi recipesApi;

    public RecipesManager(RecipeValidationFilter recipeValidationFilter,
                          RecipeTrackSender recipeTrackSender,
                          Cacher<Recipe> listCacher,
                          RecipeRepository recipeRepository,
                          RecipesApi recipesApi) {
        this.recipeValidationFilter = recipeValidationFilter;
        this.recipeTrackSender = recipeTrackSender;
        this.listCacher = listCacher;
        this.recipeRepository = recipeRepository;
        this.recipesApi = recipesApi;

        syncConfig();
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
        return recipeRepository.getLocalRecipes();
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

    public void syncConfig() {
        syncConfig(new RecipeRefreshListener() {
            @Override
            public void onRecipesRefresh() {

            }

            @Override
            public void onRecipesRefreshFail() {

            }
        });
    }

    /**
     * Sync the recipe configuration, only if the cache is cold.
     */
    public void syncConfig(final RecipeRefreshListener listener) {
        recipeRepository.syncRecipes(new RecipeRepository.RecipesListener() {
            @Override
            public void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback) {
                listener.onRecipesRefresh();
            }

            @Override
            public void onRecipesError() {
                listener.onRecipesRefreshFail();
            }
        });
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     * Plus a listener will be notified of the refresh process.
     */
    public void refreshConfig(final RecipeRefreshListener listener) {
        recipeRepository.refreshRecipes(new RecipeRepository.RecipesListener() {
            @Override
            public void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback) {
                listener.onRecipesRefresh();
            }

            @Override
            public void onRecipesError() {
                listener.onRecipesRefreshFail();
            }
        });
    }

    private List<Recipe> loadChachedList() throws JSONException {
        return listCacher.loadList(new TypeToken<List<Recipe>>() {}.getType());
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
        recipesApi.fetchRecipe(id, new RecipesApi.SingleRecipeListener() {
            @Override
            public void onRecipeFetchSuccess(Recipe recipe) {
                String reactionPluginName = recipe.getReaction_plugin_id();
                Reaction reaction = reactions.get(reactionPluginName);
                reaction.handlePushReaction(recipe, id, recipe.getReaction_bundle());
            }

            @Override
            public void onRecipeFetchError(String error) {
                NearLog.d(TAG, "single recipe failed: " + error);
            }
        });
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
        recipesApi.onlinePulseEvaluation(pulse_plugin, pulse_action, pulse_bundle, new RecipesApi.SingleRecipeListener() {
            @Override
            public void onRecipeFetchSuccess(Recipe recipe) {
                recipeRepository.addRecipe(recipe);
                gotRecipe(recipe);
            }

            @Override
            public void onRecipeFetchError(String error) {
                NearLog.d(TAG, "Recipe eval by pulse error: " + error);
            }
        });
    }

    /**
     * Online evaluation of a recipe.
     *
     * @param recipeId recipe identifier.
     */
    public void evaluateRecipe(String recipeId) {
        recipesApi.evaluateRecipe(recipeId, new RecipesApi.SingleRecipeListener() {
            @Override
            public void onRecipeFetchSuccess(Recipe recipe) {
                gotRecipe(recipe);
            }

            @Override
            public void onRecipeFetchError(String error) {
                NearLog.d(TAG, "Recipe evaluation error: " + error);
            }
        });
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

        List<Recipe> recipes = recipeRepository.getLocalRecipes();
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

        List<Recipe> recipes = recipeRepository.getLocalRecipes();
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
