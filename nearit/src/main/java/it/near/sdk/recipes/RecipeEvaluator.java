package it.near.sdk.recipes;

import it.near.sdk.recipes.pulse.TriggerRequest;

public interface RecipeEvaluator {

    void handleTriggerRequest(TriggerRequest triggerRequest);
}
