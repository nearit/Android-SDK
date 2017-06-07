package it.near.sdk.recipes.validation;

import it.near.sdk.recipes.models.Recipe;

abstract class Validator {

    abstract boolean validate(Recipe recipe);

    public Validator() {
    }
}
