package it.near.sdk.recipes;

import it.near.sdk.GlobalConfig;

public class RecipeTrackBuilder {

    private final GlobalConfig globalConfig;
    private final RecipeCooler recipeCooler;

    public RecipeTrackBuilder(GlobalConfig globalConfig, RecipeCooler recipeCooler) {
        this.globalConfig = globalConfig;
        this.recipeCooler = recipeCooler;
    }


}
