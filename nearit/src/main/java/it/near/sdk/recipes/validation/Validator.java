package it.near.sdk.recipes.validation;

import it.near.sdk.recipes.models.Recipe;

public abstract class Validator {

    abstract boolean validate(Recipe recipe);

    public Validator() {
    }
}
