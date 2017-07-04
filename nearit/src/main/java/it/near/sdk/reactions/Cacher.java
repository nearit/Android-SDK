package it.near.sdk.reactions;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.Collection;
import java.util.List;

public class Cacher<T> {

    private static final String KEY_LIST = "list";
    private final SharedPreferences sp;
    private Gson gson;

    public Cacher(SharedPreferences sp) {
        this.sp = sp;
        gson = new Gson();
    }

    public void persistList(List<T> list) {
        String persistedString = gson.toJson(list);
        sp.edit().putString(KEY_LIST, persistedString)
                .apply();
    }

    public List<T> loadList() throws JSONException {
        String cachedString = loadCachedString(KEY_LIST);
        return gson.fromJson(cachedString, new TypeToken<Collection<T>>() {
        }.getType());
    }


    private String loadCachedString(String key) {
        return sp.getString(key, "");
    }

}
