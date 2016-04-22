package it.near.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import it.near.sdk.Communication.Constants;

/**
 * Created by cattaneostefano on 12/04/16.
 */
public class GlobalConfig {

    private static GlobalConfig mInstance = null;
    private Context mContext;
    private final String APIKEY = "apikey";
    private String apiKey;
    private final String APPID = "appid";
    private String appId;
    private final String SENDERID = "senderid";
    private String senderId;
    private final String NOTIFICATIONIMAGE = "notification_image";
    private int notificationImage = 0;
    private SharedPreferences sp;
    private String prefsNameSuffix = "NearConfig";
    private SharedPreferences.Editor editor;

    public GlobalConfig(Context mContext) {
        this.mContext = mContext;
        setUpSharedPreferences();
    }

    private void setUpSharedPreferences() {
        String PACK_NAME = mContext.getApplicationContext().getPackageName();
        String PREFS_NAME = PACK_NAME + prefsNameSuffix;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
        editor = sp.edit();
    }

    public static GlobalConfig getInstance(Context context){
        if(mInstance == null)
        {
            mInstance = new GlobalConfig(context);
        }
        return mInstance;
    }

    public int getNotificationImage() {
        if (notificationImage == 0){
            notificationImage = sp.getInt(NOTIFICATIONIMAGE, 0);
        }
        return notificationImage;
    }

    public void setNotificationImage(int notificationImage) {
        this.notificationImage = notificationImage;
        editor.putInt(NOTIFICATIONIMAGE, notificationImage).apply();
    }


    public String getApiKey() {
        if (apiKey == null){
            apiKey = sp.getString(APIKEY, null);
        }
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        editor.putString(APIKEY, apiKey).apply();
    }

    public String getAppId() {
        if (appId == null){
            appId = sp.getString(APPID, null);
        }
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
        editor.putString(APPID, appId);
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
        editor.putString(SENDERID, senderId).apply();
    }

    public String getSenderId() {
        if (senderId == null){
            senderId = sp.getString(SENDERID, null);
        }
        return senderId;
    }
}
