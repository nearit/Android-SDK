package it.near.sdk.Recipes;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * Created by cattaneostefano on 13/02/2017.
 */

public class RecipeCooler {

    private static RecipeCooler INSTANCE;
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private static final String NEAR_COOLDOWN_HISTORY = "NearCooldownHistory";
    private static final String LOG_MAP = "LOG_MAP";
    private static final String LATEST_LOG = "LATEST_LOG";
    private Map<String, Long> mRecipeLogMap;
    private Long latestLogEntry;

    private RecipeCooler(Context context) {
        mContext = context;
        mRecipeLogMap = loadMap();
        latestLogEntry = loadLatestEntry();
    }

    private SharedPreferences getSharedPreferences(){
        if (mSharedPreferences == null){
            mSharedPreferences = mContext.getSharedPreferences(NEAR_COOLDOWN_HISTORY, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    public static RecipeCooler getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = new RecipeCooler(context);
        }
        return INSTANCE;
    }

    public void markRecipeAsShown(String recipeId){
        if (mRecipeLogMap == null) mRecipeLogMap = loadMap();
        long timeStamp = System.currentTimeMillis();
        mRecipeLogMap.put(recipeId, new Long(timeStamp));
        saveMap(mRecipeLogMap);

    }

    public boolean canShowRecipe(Recipe recipe){
        long timestamp = System.currentTimeMillis();
        long latestNotificationTimestamp = loadLatestEntry();
        return false;
    }

    public void filterRecipe(List<Recipe> recipes){
        for (Iterator<Recipe> it = recipes.iterator(); it.hasNext();) {
            Recipe recipe = it.next();
            if (!canShowRecipe(recipe)){
                it.remove();
            }
        }
    }

    private void saveMap(Map<String, Long> inputMap){
        SharedPreferences pSharedPref = mContext.getSharedPreferences(NEAR_COOLDOWN_HISTORY, Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(LOG_MAP).commit();
            editor.putString(LOG_MAP, jsonString);
            editor.commit();
        }
    }

    private Map<String,Long> loadMap(){
        Map<String,Long> outputMap = new HashMap<String,Long>();
        SharedPreferences pSharedPref = getSharedPreferences();
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(LOG_MAP, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    Long value = (Long) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }

    private Long loadLatestEntry() {
        SharedPreferences pSharedPref = getSharedPreferences();
        if (pSharedPref != null) {
            return pSharedPref.getLong(LATEST_LOG, 0L);
        }
        return 0L;
    }

    private void saveLatestEntry() {
        SharedPreferences pSharedPref = mContext.getSharedPreferences(NEAR_COOLDOWN_HISTORY, Context.MODE_PRIVATE);
        if (pSharedPref!=null){
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(LATEST_LOG).commit();
            editor.putLong(LATEST_LOG, System.currentTimeMillis());
            editor.commit();
        }
    }
}
