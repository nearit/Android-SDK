package it.near.sdk.Recipes;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * Created by cattaneostefano on 13/02/2017.
 */

public class RecipeCooler {

    private static RecipeCooler INSTANCE;
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public static final String NEAR_COOLDOWN_HISTORY = "NearCooldownHistory";
    public static final String LOG_MAP = "LOG_MAP";
    public static final String LATEST_LOG = "LATEST_LOG";

    public static final String GLOBAL_COOLDOWN = "global_cooldown";
    public static final String SELF_COOLDOWN = "self_cooldown";

    private Map<String, Long> mRecipeLogMap;
    private Long mLatestLogEntry;

    private RecipeCooler(Context context) {
        mContext = context;
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
        long timeStamp = System.currentTimeMillis();
        getMap().put(recipeId, new Long(timeStamp));
        saveMap(mRecipeLogMap);
        saveLatestEntry();

    }

    private boolean canShowRecipe(Recipe recipe){
        Map <String, Object> cooldown = recipe.getCooldown();
        return cooldown != null &&
                globalCooldownCheck(cooldown) &&
                selfCooldownCheck(recipe, cooldown);
    }

    private boolean globalCooldownCheck(Map<String, Object> cooldown) {
        // TODO cosa fare se non c'è?
        if (!cooldown.containsKey(GLOBAL_COOLDOWN)) return true;
        long expiredSeconds = (System.currentTimeMillis() - getLatestLogEntry()) / 1000;
        return expiredSeconds >= (Long)cooldown.get(GLOBAL_COOLDOWN);
    }

    private boolean selfCooldownCheck(Recipe recipe, Map<String, Object> cooldown){
        // TODO cosa fare se non c'è?
        if (!cooldown.containsKey(SELF_COOLDOWN)) return true;
        if (!getMap().containsKey(recipe.getId())) return true;
        long recipeLatestEntry = getMap().get(recipe.getId());
        long expiredSeconds = (System.currentTimeMillis() - recipeLatestEntry) / 1000;
        return expiredSeconds >= (Long)cooldown.get(SELF_COOLDOWN);
    }

    public void filterRecipe(List<Recipe> recipes){
        for (Iterator<Recipe> it = recipes.iterator(); it.hasNext();) {
            Recipe recipe = it.next();
            if (!canShowRecipe(recipe)){
                it.remove();
            }
        }
    }

    private Map<String, Long> getMap() {
        if (mRecipeLogMap == null){
            mRecipeLogMap = loadMap();
        }
        return mRecipeLogMap;
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
            // e.printStackTrace();
        }
        return outputMap;
    }

    private Long getLatestLogEntry(){
        if (mLatestLogEntry == null) {
            mLatestLogEntry = loadLatestEntry();
        }
        return mLatestLogEntry;
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
