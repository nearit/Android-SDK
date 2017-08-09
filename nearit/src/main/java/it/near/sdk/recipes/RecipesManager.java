package it.near.sdk.recipes;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.pulse.TriggerRequest;
import it.near.sdk.recipes.validation.RecipeValidationFilter;

/**
 * Menage recipes download, caching and direct calling.
 *
 * @author cattaneostefano
 */
public class RecipesManager implements RecipeEvaluator {

    private static final String TAG = "RecipesManager";

    private final RecipeTrackSender recipeTrackSender;
    private final RecipeValidationFilter recipeValidationFilter;
    private final RecipeRepository recipeRepository;
    private final RecipesApi recipesApi;
    private final RecipeReactionHandler recipeReactionHandler;
    private boolean online_eval = true;

    public RecipesManager(RecipeValidationFilter recipeValidationFilter,
                          RecipeTrackSender recipeTrackSender,
                          RecipeRepository recipeRepository,
                          RecipesApi recipesApi,
                          RecipeReactionHandler recipeReactionHandler) {
        this.recipeValidationFilter = recipeValidationFilter;
        this.recipeTrackSender = recipeTrackSender;
        this.recipeRepository = recipeRepository;
        this.recipesApi = recipesApi;
        this.recipeReactionHandler = recipeReactionHandler;
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
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     * Plus a listener will be notified of the refresh process.
     */
    public void refreshConfig(final RecipeRefreshListener listener) {
        recipeRepository.refreshRecipes(new RecipeRepository.RecipesListener() {
            @Override
            public void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback, boolean dataChanged) {
                listener.onRecipesRefresh();
                RecipesManager.this.online_eval = online_evaluation_fallback;
            }
        });
    }

    /**
     * Sync the recipe configuration, only if the cache is cold.
     */
    public void syncConfig(final RecipeRefreshListener listener) {
        recipeRepository.syncRecipes(new RecipeRepository.RecipesListener() {
            @Override
            public void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback, boolean dataChanged) {
                listener.onRecipesRefresh();
                RecipesManager.this.online_eval = online_evaluation_fallback;
            }
        });
    }

    /**
     * Process a recipe from its id. Typically called for processing a push recipe.
     *
     * @param id push id.
     * @param listener
     */
    public void processRecipe(final String id, RecipesApi.SingleRecipeListener listener) {
        recipesApi.fetchRecipe(id, listener);
    }

    private void onlinePulseEvaluation(TriggerRequest triggerRequest) {
        recipesApi.onlinePulseEvaluation(triggerRequest.plugin_name, triggerRequest.plugin_action, triggerRequest.bundle_id, new RecipesApi.SingleRecipeListener() {
            @Override
            public void onRecipeFetchSuccess(Recipe recipe) {
                recipeRepository.addRecipe(recipe);
                recipeReactionHandler.gotRecipe(recipe);
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
    private void evaluateRecipe(String recipeId) {
        recipesApi.evaluateRecipe(recipeId, new RecipesApi.SingleRecipeListener() {
            @Override
            public void onRecipeFetchSuccess(Recipe recipe) {
                recipeReactionHandler.gotRecipe(recipe);
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
            recipeReactionHandler.gotRecipe(winnerRecipe);
        }
        return true;
    }

    private boolean handlePulseLocally(TriggerRequest triggerRequest) {
        if (triggerRequest == null ||
                triggerRequest.plugin_name == null ||
                triggerRequest.plugin_action == null ||
                triggerRequest.bundle_id == null) return false;

        List<Recipe> recipes = recipeRepository.getLocalRecipes();
        List<Recipe> matchingRecipes = new ArrayList<>();
        if (recipes == null) return false;
        // Find the recipes that matches the pulse
        for (Recipe recipe : recipes) {
            if (recipe.getPulse_plugin_id() == null ||
                    recipe.getPulse_action() == null ||
                    recipe.getPulse_action().getId() == null ||
                    recipe.getPulse_bundle() == null ||
                    recipe.getPulse_bundle().getId() == null)
                continue;
            if (recipe.getPulse_plugin_id().equals(triggerRequest.plugin_name) &&
                    recipe.getPulse_action().getId().equals(triggerRequest.plugin_action) &&
                    recipe.getPulse_bundle().getId().equals(triggerRequest.bundle_id)) {
                matchingRecipes.add(recipe);
            }
        }

        if (matchingRecipes.isEmpty()) return false;

        return filterAndNotify(matchingRecipes);

    }


    private boolean handlePulseTags(TriggerRequest triggerRequest) {
        if (triggerRequest == null ||
                triggerRequest.plugin_name == null ||
                triggerRequest.plugin_tag_action == null ||
                triggerRequest.tags == null ||
                triggerRequest.tags.isEmpty())
            return false;

        List<Recipe> recipes = recipeRepository.getLocalRecipes();
        List<Recipe> matchingRecipes = new ArrayList<>();
        if (recipes == null) return false;
        // Find the recipes that matches the pulse

        for (Recipe recipe : recipes) {
            if (recipe.getPulse_plugin_id() == null ||
                    recipe.getPulse_action() == null ||
                    recipe.getPulse_action().getId() == null ||
                    recipe.tags == null ||
                    recipe.tags.isEmpty())
                continue;
            if (recipe.getPulse_plugin_id().equals(triggerRequest.plugin_name) &&
                    recipe.getPulse_action().getId().equals(triggerRequest.plugin_tag_action) &&
                    triggerRequest.tags.containsAll(recipe.tags)) {
                matchingRecipes.add(recipe);
            }
        }

        if (matchingRecipes.isEmpty()) return false;

        return filterAndNotify(matchingRecipes);
    }

    private void handlePulseOnline(final TriggerRequest triggerRequest) {
        if (online_eval) {
            onlinePulseEvaluation(triggerRequest);
        } else {
            recipeRepository.syncRecipes(new RecipeRepository.RecipesListener() {
                @Override
                public void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback, boolean dataChanged) {
                    if (dataChanged) {
                        boolean found = handlePulseLocally(triggerRequest) ||
                                handlePulseTags(triggerRequest);
                        if (!found && online_evaluation_fallback) {
                            onlinePulseEvaluation(triggerRequest);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void handleTriggerRequest(TriggerRequest triggerRequest) {
        if (!handlePulseLocally(triggerRequest) &&
                !handlePulseTags(triggerRequest)) {
            handlePulseOnline(triggerRequest);
        }
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

}
