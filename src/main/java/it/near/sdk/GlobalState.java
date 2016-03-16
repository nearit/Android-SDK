package it.near.sdk;

import android.content.Context;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class GlobalState {
    private static final String TAG = "GlobalState";

    private static GlobalState mInstance = null;

    private Context mContext;
    private String apiKey;

    public GlobalState(Context mContext) {
        this.mContext = mContext;
    }

    public static GlobalState getInstance(Context context){
        if(mInstance == null)
        {
            mInstance = new GlobalState(context);
        }
        return mInstance;
    }

    public Context getmContext() {
        return mContext;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
