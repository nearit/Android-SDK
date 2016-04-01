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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.near.sdk.Beacons.AltBeaconWrapper;
import it.near.sdk.Beacons.BeaconDynamicRadar;
import it.near.sdk.Beacons.NearRangeNotifier;
import it.near.sdk.Communication.NearItServer;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Rules.MatchingNotifier;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.OnLifecycleEventListener;
import it.near.sdk.Utils.TraceNotifier;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItManager {

    private static final String TAG = "NearItManager";
    private static String APP_PACKAGE_NAME;
    private final NearItServer server;

    private List<ContentListener> contentListeners;

    Application application;

    public NearItManager(Application application, String apiKey) {
        this.application = application;
        initLifecycleMonitor();
        contentListeners = new ArrayList<>();

        GlobalState.getInstance(application).setNearRangeNotifier(new NearRangeNotifier(application));
        GlobalState.getInstance(application).setApiKey(apiKey);
        GlobalState.getInstance(application).setMatchingNotifier(matchingNotifier);

        server = NearItServer.getInstance(application);
        refreshNearConfig();

    }

    public void refreshNearConfig() {
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
            mService.configureScanner(getConfiguration());
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

    private Configuration getConfiguration(){
        return GlobalState.getInstance(application).getConfiguration();
    }

    public void addContentListener(ContentListener listener){
        contentListeners.add(listener);
    }

    private void deliverContent(Content content, Matching matching){
        for (ContentListener listener : contentListeners){
            if (listener != null){
                listener.onContentToDisplay(content, matching);
            }
        }
    }

    private MatchingNotifier matchingNotifier = new MatchingNotifier() {
        @Override
        public void onRuleFullfilled(Matching matching) {
            getConfiguration();
            Content content = getConfiguration().getContentFromId(matching.getContent_id());
            // TODO deliver content
            deliverContent(content, matching);
        }
    };
}
