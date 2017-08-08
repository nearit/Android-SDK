package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.timestamp.NearTimestampChecker;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    private static final String ONLINE_EV = "online_evaluation";
    private static final String TIMESTAMP = "timestamp";
    private static final String NEAR_RECIPES_REPO_PREFS_NAME = "NearITRecipeSP";
    private List<Recipe> recipes = new ArrayList<>();

    private final NearTimestampChecker nearTimestampChecker;
    private final Cacher<Recipe> cache;
    private final RecipesApi recipesApi;
    private final CurrentTime currentTime;
    private final SharedPreferences sp;

    public RecipeRepository(NearTimestampChecker nearTimestampChecker,
                            Cacher<Recipe> cache,
                            RecipesApi recipesApi,
                            CurrentTime currentTime,
                            SharedPreferences sp) {
        this.nearTimestampChecker = nearTimestampChecker;
        this.cache = cache;
        this.recipesApi = recipesApi;
        this.currentTime = currentTime;
        this.sp = sp;

        try {
            recipes = loadCachedList();
        } catch (Exception e) {
            NearLog.d(TAG, "Recipes format error");
        }
    }

    public List<Recipe> getLocalRecipes() {
        if (recipes != null) return recipes;
        else return Collections.emptyList();
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        cache.persistList(recipes);
    }

    public void syncRecipes(final RecipesListener listener) {
        long timestamp = getCacheTimestamp();
        nearTimestampChecker.checkRecipeTimeStamp(timestamp, new NearTimestampChecker.SyncCheckListener() {
            @Override
            public void syncNeeded() {
                refreshRecipes(listener);
            }

            @Override
            public void syncNotNeeded() {
                listener.onGotRecipes(recipes, getOnlineEv());
            }
        });
    }

    public void refreshRecipes(final RecipesListener listener) {
        recipesApi.processRecipes(new RecipesApi.RecipesListener() {
            @Override
            public void onRecipeProcessSuccess(List<Recipe> remote_recipes, boolean online_evaluation_fallback) {
                recipes = remote_recipes;
                setCacheTimestamp(currentTime.currentTimeStampSeconds());
                cache.persistList(recipes);

                setOnlineEv(online_evaluation_fallback);
                listener.onGotRecipes(recipes, online_evaluation_fallback);
            }

            @Override
            public void onRecipeProcessError() {
                listener.onRecipesError();
            }
        });
    }

    private void setCacheTimestamp(long timestamp) {
        sp.edit().putLong(TIMESTAMP, timestamp).commit();
    }

    private long getCacheTimestamp() {
        return sp.getLong(TIMESTAMP, 0L);
    }

    private void setOnlineEv(boolean online_ev) {
        sp.edit().putBoolean(ONLINE_EV, online_ev).commit();
    }

    private boolean getOnlineEv() {
        return sp.getBoolean(ONLINE_EV, true);
    }

    private List<Recipe> loadCachedList() throws JSONException {
        return cache.loadList(new TypeToken<List<Recipe>>() {}.getType());
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NEAR_RECIPES_REPO_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public interface RecipesListener {
        void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback);
        void onRecipesError();
    }

}
