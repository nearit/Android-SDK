package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.Collections;
import java.util.List;

import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.timestamp.NearTimestampChecker;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    static final String ONLINE_EV = "online_evaluation";
    static final String TIMESTAMP = "timestamp";
    static final String NEAR_RECIPES_REPO_PREFS_NAME = "NearITRecipeSP";
    static final long TIMESTAMP_DEF_VALUE = 0L;
    static final boolean ONLINE_EV_DEFAULT = true;
    private List<Recipe> recipes;

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

    List<Recipe> getLocalRecipes() {
        if (recipes != null) return recipes;
        else return Collections.emptyList();
    }

    void addRecipe(Recipe recipe) {
        List<Recipe> recipes = getLocalRecipes();
        recipes.add(recipe);
        cache.persistList(recipes);
        this.recipes = recipes;
    }

    void syncRecipes(final RecipesListener listener) {
        long timestamp = getCacheTimestamp();
        if (timestamp == TIMESTAMP_DEF_VALUE) {
            refreshRecipes(listener);
            return;
        }
        nearTimestampChecker.checkRecipeTimeStamp(timestamp, new NearTimestampChecker.SyncCheckListener() {
            @Override
            public void syncNeeded() {
                refreshRecipes(listener);
            }

            @Override
            public void syncNotNeeded() {
                NearLog.i(TAG, "Local recipes were up to date, we avoided a process request");
                listener.onGotRecipes(getLocalRecipes(), getOnlineEv(), false);
            }
        });
    }

    void refreshRecipes(final RecipesListener listener) {
        recipesApi.processRecipes(new RecipesApi.RecipesListener() {
            @Override
            public void onRecipeProcessSuccess(List<Recipe> remote_recipes, boolean online_evaluation_fallback) {
                recipes = remote_recipes;
                setCacheTimestamp(currentTime.currentTimeStampSeconds());
                cache.persistList(recipes);

                setOnlineEv(online_evaluation_fallback);
                listener.onGotRecipes(recipes, online_evaluation_fallback, true);
            }

            @Override
            public void onRecipeProcessError() {
                listener.onGotRecipes(getLocalRecipes(), getOnlineEv(), false);
            }
        });
    }

    private void setCacheTimestamp(long timestamp) {
        sp.edit().putLong(TIMESTAMP, timestamp).commit();
    }

    private long getCacheTimestamp() {
        return sp.getLong(TIMESTAMP, TIMESTAMP_DEF_VALUE);
    }

    private void setOnlineEv(boolean online_ev) {
        sp.edit().putBoolean(ONLINE_EV, online_ev).commit();
    }

    private boolean getOnlineEv() {
        return sp.getBoolean(ONLINE_EV, ONLINE_EV_DEFAULT);
    }

    private List<Recipe> loadCachedList() throws JSONException {
        return cache.loadList(new TypeToken<List<Recipe>>() {}.getType());
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NEAR_RECIPES_REPO_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean shouldEvaluateOnline() {
        return getOnlineEv();
    }

    interface RecipesListener {
        void onGotRecipes(List<Recipe> recipes, boolean online_evaluation_fallback, boolean dataChanged);
    }

}
