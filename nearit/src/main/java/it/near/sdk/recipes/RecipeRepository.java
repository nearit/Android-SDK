package it.near.sdk.recipes;

import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.timestamp.NearTimestampChecker;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    private static final String ONLINE_EV = "online_evaluation";
    private static final String TIMESTAMP = "timestamp";
    private List<Recipe> recipes = new ArrayList<>();

    private final NearTimestampChecker nearTimestampChecker;
    private final Cacher<Recipe> cache;
    private final EvaluationBodyBuilder evaluationBodyBuilder;
    private final RecipesApi recipesApi;
    private final SharedPreferences sp;

    public RecipeRepository(NearTimestampChecker nearTimestampChecker,
                            Cacher<Recipe> cache,
                            Morpheus morpheus,
                            EvaluationBodyBuilder evaluationBodyBuilder,
                            RecipesApi recipesApi,
                            SharedPreferences sp) {
        this.nearTimestampChecker = nearTimestampChecker;
        this.cache = cache;
        this.evaluationBodyBuilder = evaluationBodyBuilder;
        this.recipesApi = recipesApi;
        this.sp = sp;

        try {
            recipes = loadCachedList();
        } catch (Exception e) {
            NearLog.d(TAG, "Recipes format error");
        }
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
                listener.onGotRecipes(recipes);
            }
        });
    }

    public void refreshRecipes(final RecipesListener listener) {
        recipesApi.processRecipes(new RecipesApi.RecipesListener() {
            @Override
            public void onRecipeProcessSuccess(List<Recipe> remote_recipes, boolean online_evaluation_fallback) {
                setOnlineEvaluationPreference(online_evaluation_fallback);
                recipes = remote_recipes;
                cache.persistList(recipes);
                listener.onGotRecipes(recipes);
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

    private void setOnlineEvaluationPreference(boolean enabled) {
        sp.edit().putBoolean(ONLINE_EV, enabled).commit();
    }

    private boolean getOnlineEvaluationPref() {
        return sp.getBoolean(ONLINE_EV, true);
    }

    private List<Recipe> loadCachedList() throws JSONException {
        return cache.loadList(new TypeToken<List<Recipe>>() {}.getType());
    }

    public interface RecipesListener {
        void onGotRecipes(List<Recipe> recipes);
        void onRecipesError();
    }

}
