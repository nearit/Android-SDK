package it.near.sdk.recipes;

import it.near.sdk.recipes.models.Recipe;

public interface RecipeReactionHandler {
    void gotRecipe(Recipe recipe);
    void processRecipe(String recipeId);
    void processRecipe(String recipeId, String notificationText, String reactionPluginId, String reactionActionId, String reactionBundleId);
    boolean processReactionBundle(String recipeId, String notificationText, String reactionPluginId, String reactionActionId, String reactionBundleString);
}
