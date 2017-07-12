package it.near.sdk;

import android.content.Context;
import android.content.SharedPreferences;


import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.logging.NearLog;

/**
 * Class containing global configuration. It saves all configuration strings on disk.
 *
 * @author cattaneostefano
 */
public class GlobalConfig {

    public static final int DEFAULT_EMPTY_NOTIFICATION = 0;
    static final String KEY_PROXIMITY_ICON = "proximity_icon_key";
    static final String KEY_PUSH_ICON = "push_icon_key";

    // ---------- Value string and string keys ----------
    static final String APIKEY = "apikey";
    private String apiKey;
    static final String APPID = "appid";
    private String appId;
    static final String DEVICETOKEN = "devicetoken";
    private String deviceToken;
    static final String INSTALLATIONID = "installationid";
    private String installationId;
    static final String PROFILE_ID = "profileId";
    private String profileId;
    private int proximityNotificationIconRes = DEFAULT_EMPTY_NOTIFICATION;
    private int pushNotificationIconRes = DEFAULT_EMPTY_NOTIFICATION;
    // ---------- suffix for sharedpreferences ----------
    private static final String PREFS_NAME = "NearConfig";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public GlobalConfig(SharedPreferences sp) {
        this.sp = sp;
        this.editor = sp.edit();
    }

    static SharedPreferences buildSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getProximityNotificationIcon() {
        if (proximityNotificationIconRes == DEFAULT_EMPTY_NOTIFICATION) {
            proximityNotificationIconRes = sp.getInt(KEY_PROXIMITY_ICON, DEFAULT_EMPTY_NOTIFICATION);
        }
        return proximityNotificationIconRes;
    }

    public void setProximityNotificationIcon(int imgRes) {
        proximityNotificationIconRes = imgRes;
        editor.putInt(KEY_PROXIMITY_ICON, imgRes).apply();
    }

    public int getPushNotificationIcon() {
        if (pushNotificationIconRes == DEFAULT_EMPTY_NOTIFICATION) {
            pushNotificationIconRes = sp.getInt(KEY_PUSH_ICON, DEFAULT_EMPTY_NOTIFICATION);
        }
        return pushNotificationIconRes;
    }

    public void setPushNotificationIcon(int imgRes) {
        pushNotificationIconRes = imgRes;
        editor.putInt(KEY_PUSH_ICON, imgRes).apply();
    }

    public String getApiKey() throws AuthenticationException {
        if (apiKey == null) {
            apiKey = getLocalString(APIKEY);
            if (apiKey == null) {
                throw new AuthenticationException();
            }
        }
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        setLocalString(APIKEY, apiKey);
    }

    public String getAppId() {
        if (appId == null) {
            appId = getLocalString(APPID);
        }
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
        setLocalString(APPID, appId);
    }

    public String getDeviceToken() {
        if (deviceToken == null) {
            deviceToken = getLocalString(DEVICETOKEN);
        }
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        NearLog.d("GlobalConfig", "Set deviceToken to : " + deviceToken);
        this.deviceToken = deviceToken;
        setLocalString(DEVICETOKEN, deviceToken);
    }

    public String getInstallationId() {
        if (installationId == null) {
            installationId = getLocalString(INSTALLATIONID);
        }
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
        setLocalString(INSTALLATIONID, installationId);
    }

    public String getProfileId() {
        if (profileId == null) {
            profileId = getLocalString(PROFILE_ID);
        }
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
        setLocalString(PROFILE_ID, profileId);
    }

    private void setLocalString(String name, String value) {
        editor.putString(name, value).apply();
    }

    private String getLocalString(String name) {
        return sp.getString(name, null);
    }


}
