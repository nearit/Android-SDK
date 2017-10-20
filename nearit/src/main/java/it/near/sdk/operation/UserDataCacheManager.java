package it.near.sdk.operation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class UserDataCacheManager {

    static final String SP_MAP_KEY = "NearItUserDataMap";

    private final SharedPreferences sharedPreferences;
    private HashMap<String, String> userData;

    public UserDataCacheManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        userData = new HashMap<>();
    }

    boolean hasData() {
        return !userData.isEmpty();
    }

    void setUserData(String key, String value) {
        if (!userData.containsKey(key)) {
            userData.put(key, value);
        } else if (!userData.get(key).equals(value)) {
            userData.put(key, value);
        }
        saveUserDataToSP(key, value);
    }

    @SuppressLint("ApplySharedPref")
    private void saveUserDataToSP(String key, String value) {
        HashMap<String, String> storedData = loadUserDataFromSP();
        if (!storedData.containsKey(key)) {
            storedData.put(key, value);
        } else if (!storedData.get(key).equals(value)) {
            storedData.put(key, value);
        }
        String stringMap = serialize(storedData);
        sharedPreferences.edit().putString(SP_MAP_KEY, stringMap).commit();
    }

    HashMap<String, String> getUserData() {
        if (userData.isEmpty()) {
            userData = loadUserDataFromSP();
        }
        return userData;
    }

    private HashMap<String, String> loadUserDataFromSP() {
        String stringMap = sharedPreferences.getString(SP_MAP_KEY, null);
        return deserialize(stringMap);
    }

    @SuppressLint("ApplySharedPref")
    boolean removeSentData(HashMap<String, String> sentData) {
        boolean removed = false;
        HashMap<String, String> storedData = loadUserDataFromSP();
        for (String key : sentData.keySet()) {
            if (userData.containsKey(key) && userData.get(key).equals(sentData.get(key))) {
                userData.remove(key);
                if (storedData.containsKey(key) && storedData.get(key).equals(sentData.get(key))) {
                    storedData.remove(key);
                }
                removed = true;
            }
        }
        if (removed) {
            String stringMap = serialize(storedData);
            sharedPreferences.edit().putString(SP_MAP_KEY, stringMap).commit();
        }
        return removed;
    }

    @SuppressLint("ApplySharedPref")
    void removeAllData() {
        userData.clear();
        sharedPreferences.edit().clear().commit();

    }

    private String serialize(HashMap<String, String> data) {
        JSONObject jsonObject = new JSONObject(data);
        return jsonObject.toString();
    }

    private HashMap<String, String> deserialize(@Nullable String serializedData) {
        HashMap<String, String> map = new HashMap<>();

        if (serializedData != null) {
            try {
                JSONObject jsonObject = new JSONObject(serializedData);
                Iterator<?> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = jsonObject.getString(key);
                    map.put(key, value);
                }
            } catch (JSONException ignored) {

            }
        }
        return map;
    }

    public static UserDataCacheManager obtain(Context context) {
        return new UserDataCacheManager(context.getSharedPreferences(SP_MAP_KEY, Context.MODE_PRIVATE));
    }
}
