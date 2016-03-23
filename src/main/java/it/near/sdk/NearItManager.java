package it.near.sdk;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import it.near.sdk.Beacons.AltBeaconWrapper;
import it.near.sdk.Beacons.BeaconDynamicRadar;
import it.near.sdk.Beacons.NearRangeNotifier;
import it.near.sdk.Communication.NearItServer;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.OnLifecycleEventListener;
import it.near.sdk.Utils.TraceNotifier;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItManager {

    private static final String TAG = "NearItManager";

    Application application;


    public NearItManager(Application application, String apiKey) {
        this.application = application;
        initLifecycleMonitor();
        NearRangeNotifier nearRangeNotifier = new NearRangeNotifier(application);
        GlobalState.getInstance(application).setNearRangeNotifier(nearRangeNotifier);
        GlobalState.getInstance(application).setApiKey(apiKey);

        NearItServer server = NearItServer.getInstance(application);
        server.downloadNearConfiguration();

    }


    public void startRanging(){
        Intent intent = new Intent(application, AltBeaconWrapper.class);
        application.startService(intent);
        application.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopRanging(){
        if (mBound) {
            application.unbindService(mConnection);
            mBound = false;
        }
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

    private AltBeaconWrapper mService;
    private boolean mBound;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AltBeaconWrapper.LocalBinder binder = (AltBeaconWrapper.LocalBinder) service;
            mService = binder.getService();
            mService.configureScanner(GlobalState.getInstance(application).getConfiguration());
            GlobalState.getInstance(application).setAltBeaconWrapper(mService);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    public void setTraceNotifier(TraceNotifier notifier){
        GlobalState.getInstance(application).setTraceNotifier(notifier);
    }
}
