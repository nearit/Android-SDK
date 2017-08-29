package it.near.sdk.recipes;

import it.near.sdk.recipes.models.TriggerRequest;

public interface RecipeEvaluator {

    void handleTriggerRequest(TriggerRequest triggerRequest);
}
