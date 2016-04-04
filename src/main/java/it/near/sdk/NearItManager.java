package it.near.sdk;

import android.app.Application;

import org.altbeacon.beacon.BeaconManager;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Beacons.AltBeaconWrapper;
import it.near.sdk.Beacons.NearMonitorNotifier;
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
    private AltBeaconWrapper altBeaconWrapper;

    private List<NearListener> nearListeners;

    Application application;

    public NearItManager(Application application, String apiKey) {
        this.application = application;
        initLifecycleMonitor();
        nearListeners = new ArrayList<>();


        GlobalState.getInstance(application).setApiKey(apiKey);
        GlobalState.getInstance(application).setMatchingNotifier(matchingNotifier);

        server = NearItServer.getInstance(application);
        refreshNearConfig();

        altBeaconWrapper = new AltBeaconWrapper(application);

    }

    public void refreshNearConfig() {
        server.downloadNearConfiguration();
    }


    public void startRanging(){
        altBeaconWrapper.startRanging();
        altBeaconWrapper.configureScanner(getConfiguration());

        /*Intent intent = new Intent(application, AltBeaconWrapper.class);
        application.startService(intent);
        application.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);*/
    }

    public void stopRanging(){

        altBeaconWrapper.stopRangingAll();

        /*if (mBound) {
            application.unbindService(mConnection);
            mBound = false;
        }
        Intent intent = new Intent(application, AltBeaconWrapper.class);
        application.stopService(intent);*/

    }

    /**
     * Checks the device capabilities to detect beacons
     *
     * @return true if the device has bluetooth enabled, false otherwise
     * @throws RuntimeException when the device doesn't have the essential BLE compatibility
     */
    public boolean verifyBluetooth() throws RuntimeException{
        return BeaconManager.getInstanceForApplication(application).checkAvailability();
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
    /*private ServiceConnection mConnection = new ServiceConnection() {

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
    };*/




    public void setTraceNotifier(TraceNotifier notifier){
        GlobalState.getInstance(application).setTraceNotifier(notifier);
    }

    private Configuration getConfiguration(){
        return GlobalState.getInstance(application).getConfiguration();
    }

    public void addContentListener(NearListener listener){
        nearListeners.add(listener);
    }

    private void deliverContent(Content content, Matching matching){
        for (NearListener listener : nearListeners){
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
