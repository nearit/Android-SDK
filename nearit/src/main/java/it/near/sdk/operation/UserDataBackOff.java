package it.near.sdk.operation;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import it.near.sdk.GlobalConfig;

class UserDataBackOff {

    private static final String TAG = "UserDataBackOff";

    private final UserDataCacheManager userDataCacheManager;
    private final NearItUserDataAPI userDataAPI;
    private final UserDataTimer timer;
    private final GlobalConfig globalConfig;

    private ProfileDataUpdateListener profileDataUpdateListener;

    private HashMap<String, String> userData;
    private boolean isBusy = false;

    UserDataBackOff(UserDataCacheManager userDataCacheManager, NearItUserDataAPI userDataAPI, UserDataTimer timer, GlobalConfig globalConfig) {
        this.userDataCacheManager = userDataCacheManager;
        this.userDataAPI = userDataAPI;
        this.timer = timer;
        this.globalConfig = globalConfig;
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
}
