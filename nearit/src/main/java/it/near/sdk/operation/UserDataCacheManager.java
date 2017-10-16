package it.near.sdk.operation;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by Federico Boschini on 16/10/17.
 */

public class UserDataCacheManager {

    private static final String SP_MAP_KEY = "NearItUserDataMap";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public UserDataCacheManager(SharedPreferences sharedPreferences, Gson gson) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
    }

    boolean hasQueue() {
        return !loadUserDataFromCache().isEmpty();
    }

    HashMap<String, Object> loadUserDataFromCache() {
        String stringMap = sharedPreferences.getString(SP_MAP_KEY, null);
        Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
        HashMap<String, Object> map = gson.fromJson(stringMap, type);
        return map;
    }

    @SuppressLint("ApplySharedPref")
    void saveUserDataToCache(String key, String value) {
        HashMap<String, Object> cachedData = loadUserDataFromCache();
        if(cachedData.get(key) != value) {
            cachedData.put(key, value);
            String stringMap = gson.toJson(cachedData);
            sharedPreferences.edit().putString(SP_MAP_KEY, stringMap).commit();
        }
    }

    @SuppressLint("ApplySharedPref")
    void removeSentData(HashMap<String, Object> sentData) {
        HashMap<String, Object> cachedData = loadUserDataFromCache();
        for (String key: sentData.keySet()) {
            if (cachedData.get(key) == sentData.get(key)) {
                cachedData.remove(key);
            }
        }
        String stringMap = gson.toJson(cachedData);
        sharedPreferences.edit().putString(SP_MAP_KEY, stringMap).commit();
    }

    @SuppressLint("ApplySharedPref")
    void removeAllDataFromCache() {
        sharedPreferences.edit().remove(SP_MAP_KEY).commit();
    }

}
