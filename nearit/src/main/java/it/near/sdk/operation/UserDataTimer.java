package it.near.sdk.operation;

import android.os.Handler;

public class UserDataTimer {

    private final static int TIMEOUT = 2000;
    private Handler handler;

    public UserDataTimer() {
        handler = new Handler();
    }

    void start(final TimerListener listener) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(listener!=null){
                    listener.sendNow();
                }
            }
        }, TIMEOUT);
    }

    public interface TimerListener {
        void sendNow();
    }

}
