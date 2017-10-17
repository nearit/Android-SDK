package it.near.sdk.operation;

import java.util.HashMap;

public class NearItUserProfiler implements NearItUserDataAPI.UserDataSendListener {

    private static final String TAG = "NearItUserProfiler";

    private final UserDataCacheManager userDataCacheManager;
    private final NearItUserDataAPI userDataAPI;
    private final UserDataTimer timer;

    private HashMap<String, String> userData;
    private boolean hasTimer = false;
    private boolean isBusy = false;

    NearItUserProfiler(UserDataCacheManager userDataCacheManager, NearItUserDataAPI userDataAPI, UserDataTimer timer) {
        this.userDataCacheManager = userDataCacheManager;
        this.userDataAPI = userDataAPI;
        this.timer = timer;
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
        userDataCacheManager.setUserData(key, value);
    }

    public void sendDataPoints() {
        userData = userDataCacheManager.getUserData();
        isBusy = true;
        userDataAPI.sendDataPoints(userData, this);
    }

    public void clearUserData() {
        userDataCacheManager.removeAllData();
    }

    private void startSend() {
        shouldSendDataPoints();
        hasTimer = false;
    }

    private void shouldSendDataPoints() {
        if (userDataCacheManager.hasData()) {
            userData = userDataCacheManager.getUserData();
            isBusy = true;
            userDataAPI.sendDataPoints(userData, this);
        }
    }

    @Override
    public void onSendingSuccess(HashMap<String, String> sentData) {
        isBusy = false;
        userDataCacheManager.removeSentData(sentData);
    }

    @Override
    public void onSendingFailure() {
        isBusy = false;
    }
}
