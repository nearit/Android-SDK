package it.near.sdk.recipes.validation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.recipes.models.Recipe;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class RecipeValidationFilter {

    private final List<Validator> validators;

    public RecipeValidationFilter(@NonNull List<Validator> validators) {
        this.validators = checkNotNull(validators);
    }

    public List<Recipe> filterRecipes(@NonNull List<Recipe> recipes) {
        List<Recipe> retain = new ArrayList<>(recipes.size());
        for (Recipe recipe : recipes) {
            if (checkValidityOf(recipe)) {
                retain.add(recipe);
            }
        }
        recipes.clear();
        recipes.addAll(retain);
        return recipes;
    }

    private boolean checkValidityOf(Recipe recipe) {
        boolean validity = true;
        for (Validator validator : validators) {
            validity &= validator.validate(recipe);
        }
        return validity;
    }
}
