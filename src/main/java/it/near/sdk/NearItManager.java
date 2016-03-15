package it.near.sdk;

import android.app.Application;
import android.content.Intent;

import it.near.sdk.Beacons.AltBeaconWrapper;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.OnLifecycleEventListener;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItManager {

    private static final String TAG = "NearItManager";

    Application application;

    public NearItManager(Application application) {
        this.application = application;
        initLifecycleMonitor();
    }


    public void startRanging(){
        Intent intent = new Intent(application, AltBeaconWrapper.class);
        application.startService(intent);
    }

    public void stopRanging(){
        Intent intent = new Intent(application, AltBeaconWrapper.class);
        application.stopService(intent);
    }

    private void initLifecycleMonitor() {
        new AppLifecycleMonitor(application, new OnLifecycleEventListener() {
            @Override
            public void onForeground() {
                ULog.d(TAG, "onForeground" );
                startRanging();
            }

            @Override
            public void onBackground() {
                ULog.d(TAG, "onBackground");
                stopRanging();
            }
        });
    }
}
