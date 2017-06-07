package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class RecipesHistory {

    static final String LOG_MAP = "LOG_MAP";
    static final String LATEST_LOG = "LATEST_LOG";
    private static final String RECIPE_HISTORY_PREFS_NAME = "NearRecipeHistoryPrefsName";

    private Map<String, Long> mRecipeLogMap;
    private Long mLatestLogEntry;

    private final SharedPreferences sharedPreferences;
    private final CurrentTime currentTime;

    public RecipesHistory(@NonNull SharedPreferences sharedPreferences,
                          @NonNull CurrentTime currentTime) {
        this.sharedPreferences = checkNotNull(sharedPreferences);
        this.currentTime = checkNotNull(currentTime);
    }

    public void markRecipeAsShown(String recipeId) {
        long timeStamp = currentTime.currentTimeStampSeconds();
        getRecipeLogMap().put(recipeId, timeStamp);
        saveMap(mRecipeLogMap);
        saveLatestEntry(currentTime.currentTimeStampSeconds());
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
     * @param recipeId the recipe identifier
     * @return whether the recipe was ever shown according to the local DB
     */
    public boolean isRecipeInLog(String recipeId) {
        return getRecipeLogMap().containsKey(recipeId);
    }

    /**
     * @param recipeId the recipe identifier
     * @return the latest epoch timestamp for the latest recipe occurrence of a certain recipe
     */
    public Long latestLogEntryFor(String recipeId) {
        return getRecipeLogMap().get(recipeId);
    }

    public Map<String, Long> getRecipeLogMap() {
        if (mRecipeLogMap == null) {
            mRecipeLogMap = loadMap();
        }
        return mRecipeLogMap;
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
                String jsonString = sharedPreferences.getString(LOG_MAP, (new JSONObject("{}")).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    Long value = ((Integer) jsonObject.get(key)).longValue();
                    outputMap.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    private Long loadLatestEntry() {
        return sharedPreferences.getLong(LATEST_LOG, 0L);
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
        return context.getSharedPreferences(RECIPE_HISTORY_PREFS_NAME, Context.MODE_PRIVATE);
    }
}
