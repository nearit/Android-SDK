package it.near.sdk.recipes;

import java.util.List;

public interface RecipeEvaluator {

    /**
     * Handle a pulse triple, based on a bundle_id with only local data. Returns true if a matching recipe was found and handled.
     * @return true if the pulse triple was handled by the evaluator.
     */
    boolean handlePulseLocally(String plugin_name, String plugin_action, String plugin_bundle);

    /**
     * Handle a pulse triple, based on a tag list with only local data. Returns true if a matching recipe was found and handled.
     * @return true if the pulse triple was handled by the evaluator.
     */
    boolean handlePulseTags(String plugin_name, String plugin_action, List<String> tags);

    /**
     * Check online for a recipe with a matching triple.
     */
    void handlePulseOnline(String plugin_name, String plugin_action, String plugin_bundle, String tag_action, List<String> tags);

}
