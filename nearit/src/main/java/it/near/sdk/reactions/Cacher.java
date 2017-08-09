package it.near.sdk.reactions;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.List;

public class Cacher<T> {

    private static final String KEY_LIST = "list";
    private final SharedPreferences sp;
    private Gson gson;

    public Cacher(SharedPreferences sp) {
        this.sp = sp;
        gson = new GsonBuilder().serializeNulls().create();
    }

    public void persistList(List<T> list) {
        String persistedString = gson.toJson(list);
        sp.edit().putString(KEY_LIST, persistedString)
                .apply();
    }

    public List<T> loadList(Type type) throws JSONException {
        String cachedString = loadCachedString();
        try {
            return gson.fromJson(cachedString, type);
        } catch (JsonSyntaxException e) {
            throw new JSONException("old format");
        }
    }

    private String loadCachedString() {
        return sp.getString(Cacher.KEY_LIST, "");
    }

}
