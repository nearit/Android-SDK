package it.near.sdk.recipes.validation;

import java.util.List;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

public class AdvScheduleValidator extends Validator {

    private final CurrentTime currentTime;

    public AdvScheduleValidator(CurrentTime currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    boolean validate(Recipe recipe) {
        return recipe.scheduling == null ||
                checkScheduling(recipe.scheduling);
    }

    private boolean checkScheduling(List<Object> scheduling) {
        return false;
    }
}
