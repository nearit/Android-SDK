package it.near.sdk.recipes.validation;

import android.support.annotation.NonNull;

import java.util.Map;

import it.near.sdk.recipes.RecipesHistory;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class CooldownValidator extends Validator {

    static final String GLOBAL_COOLDOWN = "global_cooldown";
    static final String SELF_COOLDOWN = "self_cooldown";
    static final double NEVER_REPEAT = -1D;


    private final CurrentTime currentTime;
    private final RecipesHistory recipeHistory;

    public CooldownValidator(@NonNull RecipesHistory recipesHistory, @NonNull CurrentTime currentTime) {
        this.recipeHistory = checkNotNull(recipesHistory);
        this.currentTime = checkNotNull(currentTime);
    }

    @Override
    boolean validate(Recipe recipe) {
        Map<String, Object> cooldown = recipe.getCooldown();
        try {
            return cooldown == null ||
                    (globalCooldownCheck(cooldown) && selfCooldownCheck(recipe, cooldown));
        } catch (ClassCastException exp) {
            return true;
        }
    }

    private boolean globalCooldownCheck(Map<String, Object> cooldown) throws ClassCastException {
        if (!cooldown.containsKey(GLOBAL_COOLDOWN) ||
                cooldown.get(GLOBAL_COOLDOWN) == null) return true;

        long expiredSeconds = (currentTime.currentTimeStampSeconds() - recipeHistory.getLatestLogEntry());
        return expiredSeconds >= ((Double) cooldown.get(GLOBAL_COOLDOWN)).longValue();
    }

    private boolean selfCooldownCheck(Recipe recipe, Map<String, Object> cooldown) throws ClassCastException {
        if (!cooldown.containsKey(SELF_COOLDOWN) ||
                cooldown.get(SELF_COOLDOWN) == null ||
                !recipeHistory.isRecipeInLog(recipe.getId())) return true;

        if ((Double)cooldown.get(SELF_COOLDOWN) == NEVER_REPEAT &&
                recipeHistory.isRecipeInLog(recipe.getId())) return false;

        long recipeLatestEntry = recipeHistory.latestLogEntryFor(recipe.getId());
        long expiredSeconds = (currentTime.currentTimeStampSeconds() - recipeLatestEntry);
        return expiredSeconds >= ((Double) cooldown.get(SELF_COOLDOWN)).longValue();
    }
}
