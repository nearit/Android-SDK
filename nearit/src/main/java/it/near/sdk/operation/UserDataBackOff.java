package it.near.sdk.operation;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import it.near.sdk.GlobalConfig;
import it.near.sdk.utils.AppVisibilityDetector;
import it.near.sdk.utils.ApplicationVisibility;
import it.near.sdk.utils.device.ConnectionChecker;

public class UserDataBackOff implements AppVisibilityDetector.AppVisibilityCallback {

    private static final String TAG = "UserDataBackOff";

    private final UserDataCacheManager userDataCacheManager;
    private final NearItUserDataAPI userDataAPI;
    private final UserDataTimer timer;
    private final GlobalConfig globalConfig;
    private final ConnectionChecker connectionChecker;
    private final ApplicationVisibility applicationVisibility;

    private ProfileDataUpdateListener profileDataUpdateListener;

    private HashMap<String, String> userData;
    private boolean isBusy = false;

    public UserDataBackOff(UserDataCacheManager userDataCacheManager, NearItUserDataAPI userDataAPI, UserDataTimer timer, GlobalConfig globalConfig, ConnectionChecker connectionChecker, ApplicationVisibility applicationVisibility) {
        this.userDataCacheManager = userDataCacheManager;
        this.userDataAPI = userDataAPI;
        this.timer = timer;
        this.globalConfig = globalConfig;
        this.connectionChecker = connectionChecker;
        this.applicationVisibility = applicationVisibility;
        this.applicationVisibility.setCallback(this);
    }

    void setProfileDataUpdateListener(ProfileDataUpdateListener profileDataUpdateListener) {
        this.profileDataUpdateListener = profileDataUpdateListener;
    }

    void setUserData(@NonNull String key, String value) {
        if (value == null) value = "";
        if (key.equals("")) return;
        if (globalConfig.getProfileId() != null) {
            timer.start(new UserDataTimer.TimerListener() {
                @Override
                public void sendNow() {
                    shouldSendDataPoints();
                }
            });
        }
        userDataCacheManager.setUserData(key, value);
    }

    void setBatchUserData(Map<String, String> valuesMap) {
        if (globalConfig.getProfileId() != null) {
            for (Map.Entry<String, String> entry : valuesMap.entrySet()) {
                setUserData(entry.getKey(), entry.getValue());
            }
        }
    }

    void sendDataPoints() {
        if (!isBusy) {
            userData = userDataCacheManager.getUserData();
            if (userData.isEmpty()) {
                return;
            }
            isBusy = true;
            userDataAPI.sendDataPoints(userData, new NearItUserDataAPI.UserDataSendListener() {
                @Override
                public void onSendingSuccess(HashMap<String, String> sentData) {
                    isBusy = false;
                    profileDataUpdateListener.onProfileDataUpdated();
                    userDataCacheManager.removeSentData(sentData);
                }

                @Override
                public void onSendingFailure() {
                    isBusy = false;
                }
            });
        }
    }

    void clearUserData() {
        userDataCacheManager.removeAllData();
    }

    private void shouldSendDataPoints() {
        if (userDataCacheManager.hasData()) {
            if (!isBusy) {
                userData = userDataCacheManager.getUserData();
                isBusy = true;
                userDataAPI.sendDataPoints(userData, new NearItUserDataAPI.UserDataSendListener() {
                    @Override
                    public void onSendingSuccess(HashMap<String, String> sentData) {
                        isBusy = false;
                        profileDataUpdateListener.onProfileDataUpdated();
                        userDataCacheManager.removeSentData(sentData);
                    }

                    @Override
                    public void onSendingFailure() {
                        isBusy = false;
                    }
                });
            }
        }
    }

    public static UserDataBackOff obtain(UserDataCacheManager userDataCacheManager, GlobalConfig globalConfig, Context context) {
        return new UserDataBackOff(userDataCacheManager, NearItUserDataAPI.obtain(globalConfig, context), new UserDataTimer(), globalConfig, new ConnectionChecker(context), new ApplicationVisibility());
    }

    @Override
    public void onAppGotoForeground() {
        if (connectionChecker.isDeviceOnline()) sendDataPoints();
    }

    @Override
    public void onAppGotoBackground() {

    }

    public void onOptOut() {
        userDataCacheManager.onOptOut();
    }
}
