package it.near.sdk;

import android.content.Context;

/**
 * Created by cattaneostefano on 12/04/16.
 */
public class GlobalConfig {

    private static GlobalConfig mInstance = null;
    private Context mContext;
    private String apiKey;
    private int notificationImage = 0;

    public GlobalConfig(Context mContext) {
        this.mContext = mContext;
    }

    public static GlobalConfig getInstance(Context context){
        if(mInstance == null)
        {
            mInstance = new GlobalConfig(context);
        }
        return mInstance;
    }

    public int getNotificationImage() {
        return notificationImage;
    }

    public void setNotificationImage(int notificationImage) {
        this.notificationImage = notificationImage;
    }


    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
