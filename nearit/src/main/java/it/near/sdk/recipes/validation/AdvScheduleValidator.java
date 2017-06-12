package it.near.sdk.recipes.validation;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

public class AdvScheduleValidator extends Validator {

    private final CurrentTime currentTime;

    public AdvScheduleValidator(CurrentTime currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    boolean validate(Recipe recipe) {
        return false;
    }
}
