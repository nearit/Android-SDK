package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class RecipeCooler {

    private static final String RECIPE_COOLER_PREFS_NAME = "NearRecipeCoolerPrefsName";
    private static final String LOG_MAP = "LOG_MAP";
    static final double NEVER_REPEAT = -1D;
    private static final String LATEST_LOG = "LATEST_LOG";
    static final String GLOBAL_COOLDOWN = "global_cooldown";
    static final String SELF_COOLDOWN = "self_cooldown";

    private final SharedPreferences sharedPreferences;
    private final CurrentTime currentTime;
    private Map<String, Long> mRecipeLogMap;
    private Long mLatestLogEntry;

    public RecipeCooler(@NonNull SharedPreferences sharedPreferences, @NonNull CurrentTime currentTime) {
        this.sharedPreferences = checkNotNull(sharedPreferences);
        this.currentTime = checkNotNull(currentTime);
    }

    /**
     * Filters a recipe list against the log of recipes that have been marked as shown and its cooldown period.
     *
     * @param recipes the recipe list to filter. This object will be modified.
     */
    public void filterRecipe(List<Recipe> recipes) {
        for (Iterator<Recipe> it = recipes.iterator(); it.hasNext(); ) {
            Recipe recipe = it.next();
            if (!canShowRecipe(recipe)) {
                it.remove();
            }
        }
    }

    private boolean canShowRecipe(Recipe recipe) {
        Map<String, Object> cooldown = recipe.getCooldown();
        return cooldown == null ||
                (globalCooldownCheck(cooldown) && selfCooldownCheck(recipe, cooldown));
    }

    private boolean globalCooldownCheck(Map<String, Object> cooldown) {
        if (!cooldown.containsKey(GLOBAL_COOLDOWN) ||
                cooldown.get(GLOBAL_COOLDOWN) == null) return true;

        long expiredSeconds = (currentTime.currentTimestamp() - getLatestLogEntry()) / 1000;
        return expiredSeconds >= ((Double) cooldown.get(GLOBAL_COOLDOWN)).longValue();
    }

    private boolean selfCooldownCheck(Recipe recipe, Map<String, Object> cooldown) {
        if (!cooldown.containsKey(SELF_COOLDOWN) ||
                cooldown.get(SELF_COOLDOWN) == null ||
                !getRecipeLogMap().containsKey(recipe.getId())) return true;

        if ((Double)cooldown.get(SELF_COOLDOWN) == NEVER_REPEAT &&
                getRecipeLogMap().containsKey(recipe.getId())) return false;

        long recipeLatestEntry = getRecipeLogMap().get(recipe.getId());
        long expiredSeconds = (currentTime.currentTimestamp() - recipeLatestEntry) / 1000;
        return expiredSeconds >= ((Double) cooldown.get(SELF_COOLDOWN)).longValue();
    }

    /**
     * Get the latest recipe shown event timestamp.
     *
     * @return the timestamp for the last recipe shown event.
     */
    public Long getLatestLogEntry() {
        if (mLatestLogEntry == null) {
            mLatestLogEntry = loadLatestEntry();
        }
        return mLatestLogEntry;
    }

    /**
     * Get the map of recipe shown event timestamps.
     *
     * @return the map of timestamps.
     */
    public Map<String, Long> getRecipeLogMap() {
        if (mRecipeLogMap == null) {
            mRecipeLogMap = loadMap();
        }
        return mRecipeLogMap;
    }

    /**
     * Register the recipe as shown for future cooldown evaluation.
     *
     * @param recipeId the recipe identifier.
     */
    public void markRecipeAsShown(String recipeId) {
        long timeStamp = currentTime.currentTimestamp();
        getRecipeLogMap().put(recipeId, timeStamp);
        saveMap(mRecipeLogMap);
        saveLatestEntry(currentTime.currentTimestamp());
    }

    private void saveMap(Map<String, Long> inputMap) {
        if (sharedPreferences != null) {
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(LOG_MAP).commit();
            editor.putString(LOG_MAP, jsonString);
            editor.commit();
        }
    }

    private Map<String, Long> loadMap() {
        Map<String, Long> outputMap = new HashMap<String, Long>();
        try {
            if (sharedPreferences != null) {
                String jsonString = sharedPreferences.getString(LOG_MAP, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    Long value = (Long) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return outputMap;
    }

    private Long loadLatestEntry() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(LATEST_LOG, 0L);
        }
        return 0L;
    }

    private void saveLatestEntry(long timestamp) {
        mLatestLogEntry = timestamp;
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(LATEST_LOG).commit();
            editor.putLong(LATEST_LOG, timestamp);
            editor.commit();
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(RECIPE_COOLER_PREFS_NAME, Context.MODE_PRIVATE);
    }
}
