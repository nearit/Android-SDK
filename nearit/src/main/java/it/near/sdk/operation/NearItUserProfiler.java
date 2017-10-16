package it.near.sdk.operation;

import java.util.HashMap;

/**
 * Created by Federico Boschini on 16/10/17.
 */

public class NearItUserProfiler implements NearItUserDataAPI.UserDataSendListener {

    private static final String TAG = "NearItUserProfiler";

    private final UserDataCacheManager userDataCacheManager;
    private final NearItUserDataAPI userDataAPI;
    private final UserDataTimer timer;

    private HashMap<String, Object> userData;
    private boolean hasTimer = false;


    public NearItUserProfiler(UserDataCacheManager userDataCacheManager, NearItUserDataAPI userDataAPI, UserDataTimer timer) {
        this.userDataCacheManager = userDataCacheManager;
        this.userDataAPI = userDataAPI;
        this.timer = timer;
    }

    public void clearUserData() {
        userDataCacheManager.removeAllDataFromCache();
    }

    public void setUserData(String key, String value) {
        if (!hasTimer) {
            hasTimer = true;
            timer.start(new UserDataTimer.TimerListener() {
                @Override
                public void sendNow() {
                    startSend();
                }
            });
        }
        userDataCacheManager.saveUserDataToCache(key, value);
    }

    public void sendDataPoints() {
        userData = userDataCacheManager.loadUserDataFromCache();
        userDataAPI.sendDataPoints(userData, this);
    }

    private void startSend() {
        shouldSendDataPoints();
        hasTimer = false;
    }

    private void shouldSendDataPoints() {
        if (userDataCacheManager.hasQueue()) {
            userData = userDataCacheManager.loadUserDataFromCache();
            userDataAPI.sendDataPoints(userData, this);
        }
    }

    @Override
    public void onSendingSuccess(HashMap<String, Object> sentData) {
        userDataCacheManager.removeSentData(sentData);
    }

    @Override
    public void onSendingFailure() {

    }
}
