package it.near.sdk.operation;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;

public class
NearItUserProfiler {

    private static final String TAG = "NearItUserProfiler";

    private final UserDataCacheManager userDataCacheManager;
    private final NearItUserDataAPI userDataAPI;
    private final UserDataTimer timer;
    private final GlobalConfig globalConfig;

    private HashMap<String, String> userData;
    private boolean isBusy = false;

    NearItUserProfiler(UserDataCacheManager userDataCacheManager, NearItUserDataAPI userDataAPI, UserDataTimer timer, GlobalConfig globalConfig) {
        this.userDataCacheManager = userDataCacheManager;
        this.userDataAPI = userDataAPI;
        this.timer = timer;
        this.globalConfig = globalConfig;
    }

    public void setUserData(String key, String value) {
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

    public void sendDataPoints() {
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
                    userDataCacheManager.removeSentData(sentData);
                }

                @Override
                public void onSendingFailure() {
                    isBusy = false;
                }
            });
        }
    }

    public void clearUserData() {
        userDataCacheManager.removeAllData();
    }

//    private void startSend() {
//        shouldSendDataPoints();
//    }

    private void shouldSendDataPoints() {
        if (userDataCacheManager.hasData()) {
            if (!isBusy) {
                userData = userDataCacheManager.getUserData();
                isBusy = true;
                userDataAPI.sendDataPoints(userData, new NearItUserDataAPI.UserDataSendListener() {
                    @Override
                    public void onSendingSuccess(HashMap<String, String> sentData) {
                        isBusy = false;
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
