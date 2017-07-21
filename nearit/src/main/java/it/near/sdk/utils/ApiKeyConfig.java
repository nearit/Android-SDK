package it.near.sdk.utils;

import android.content.Context;

import it.near.sdk.logging.NearLog;

public class ApiKeyConfig {

    private static final String TAG = "ApiKeyConfig";
    private static final String NEAR_IT_API_KEY_HOLDER = "nearit_key_holder";
    private static final String NEARIT_API_KEY = "nearit_key";

    public static void saveApiKey(Context context, String apiKey) {
        context.getSharedPreferences(NEAR_IT_API_KEY_HOLDER, Context.MODE_PRIVATE).edit().putString(NEARIT_API_KEY, apiKey).apply();
    }

    public static String readApiKey(Context context) {
        String apiKey = context.getSharedPreferences(NEAR_IT_API_KEY_HOLDER, Context.MODE_PRIVATE).getString(NEARIT_API_KEY, null);
        if (apiKey == null) {
            NearLog.e(TAG, "The NearIT SDK was not instantiated correctly");
        }
        return apiKey;
    }
}
