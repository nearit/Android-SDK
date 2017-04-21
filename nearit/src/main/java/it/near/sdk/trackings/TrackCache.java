package it.near.sdk.trackings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;


public class TrackCache {

    private static final String TRACKING_PREFS_NAME = "trackings_prefs_name";
    private final SharedPreferences sharedPreferences;


    public TrackCache(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public List<TrackRequest> getCachedRequests() {
        // TODO impl
        return null;
    }

    public void addToCache(TrackRequest request) {
        // TODO
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(TRACKING_PREFS_NAME, Context.MODE_PRIVATE);
    }
}
