package it.near.sdk.operation;


import android.os.Handler;

/**
 * Created by Federico Boschini on 16/10/17.
 */

public class UserDataTimer {

    private Handler handler;

    public UserDataTimer() {
        handler = new Handler();
    }

    void start(final TimerListener listener) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.sendNow();
            }
        }, 2000);
    }

    public interface TimerListener {
        void sendNow();
    }

}
