package it.near.sdk.trackings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TrackCache {

    private static final String TRACKING_PREFS_NAME = "near_trackings_prefs_name";
    static final String KEY_DISK_CACHE = "near_track_cache";

    private final SharedPreferences sharedPreferences;
    private List<TrackRequest> requestCache;

    public TrackCache(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public List<TrackRequest> getRequests() {
        if (requestCache == null) {
            requestCache = readListFromCache();
        }
        return requestCache;
    }


    public void addToCache(TrackRequest request) {
        getRequests().add(request);
        persistList(getRequests());
    }

    public boolean removeFromCache(TrackRequest request) {
        boolean removed = getRequests().remove(request);
        if (removed) {
            persistList(getRequests());
        }
        return removed;
    }

    public void removeAll() {
        if (requestCache != null) {
            requestCache = new ArrayList<>();
            persistList(requestCache);
        }
    }

    private List<TrackRequest> readListFromCache() {
        List<TrackRequest> items = new ArrayList<>();
        Set<String> set = sharedPreferences.getStringSet(KEY_DISK_CACHE, null);
        if (set != null) {
            for (String string : set) {
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    TrackRequest request = TrackRequest.fromJsonObject(jsonObject);
                    items.add(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return items;
    }

    @SuppressLint("ApplySharedPref")
    private void persistList(List<TrackRequest> listToPersist) {
        Set<String> set = new HashSet<>();
        for (TrackRequest trackRequest : getRequests()) {
            set.add(trackRequest.getJsonObject().toString());
        }

        sharedPreferences.edit()
                .putStringSet(KEY_DISK_CACHE, set)
                .commit();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(TRACKING_PREFS_NAME, Context.MODE_PRIVATE);
    }
}

