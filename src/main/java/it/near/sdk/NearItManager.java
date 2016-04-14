package it.near.sdk;

import android.app.Application;
import android.content.Intent;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Beacons.BeaconForest.ForestManager;
import it.near.sdk.Beacons.Monitoring.AltBeaconMonitor;
import it.near.sdk.Beacons.Monitoring.NearRegionLogger;
import it.near.sdk.Communication.NearItServer;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Reactions.ContentNotification.ContentNotificationReaction;
import it.near.sdk.Reactions.PollNotification.PollNotificationReaction;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotificationReaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.OnLifecycleEventListener;
import it.near.sdk.Utils.TraceNotifier;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItManager {

    private static final String TAG = "NearItManager";
    private static final String ENTER = "enter";
    private static final String LEAVE = "leave";
    private static final String REGION_MESSAGE_ACTION = "it.near.sdk.permission.REGION_MESSAGE";
    private static String APP_PACKAGE_NAME;
    private final NearItServer server;
    private ForestManager forest;
    private RecipesManager recipesManager;
    private SimpleNotificationReaction simpleNotification;
    private ContentNotificationReaction contentNotification;
    private PollNotificationReaction pollNotification;

    private AltBeaconMonitor monitor;

    private List<NearListener> nearListeners;

    Application application;

    public NearItManager(Application application, String apiKey) {
        ULog.d(TAG, "NearItManager constructor");
        this.application = application;
        initLifecycleMonitor();
        nearListeners = new ArrayList<>();

        GlobalConfig.getInstance(application).setApiKey(apiKey);
        GlobalState.getInstance(application).setNearNotifier(nearNotifier);

        server = NearItServer.getInstance(application);
        refreshNearConfig();

        plugInSetup();

    }

    private void plugInSetup() {
        recipesManager = new RecipesManager(application);

        monitor = new AltBeaconMonitor(application);
        forest = new ForestManager(application, monitor, recipesManager);

        simpleNotification = new SimpleNotificationReaction(application);
        recipesManager.addReaction(simpleNotification.getIngredientName(), simpleNotification);
        contentNotification = new ContentNotificationReaction(application);
        recipesManager.addReaction(contentNotification.getIngredientName(), contentNotification);
        pollNotification = new PollNotificationReaction(application);
        recipesManager.addReaction(pollNotification.getIngredientName(), pollNotification);

    }

    public void setLogger(NearRegionLogger logger){
        // monitor.setLogger(logger);
    }

    public void refreshNearConfig() {
        // server.downloadNearConfiguration();
    }


    public void startRanging(){
        // altBeaconWrapper.startRanging();
        // altBeaconWrapper.configureScanner(getConfiguration());
    }

    public void stopRanging(){
        // altBeaconWrapper.stopRangingAll();
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



    public void setTraceNotifier(TraceNotifier notifier){
        GlobalState.getInstance(application).setTraceNotifier(notifier);
    }

    private Configuration getConfiguration(){
        return GlobalState.getInstance(application).getConfiguration();
    }

    public void addContentListener(NearListener listener){
        ULog.d(TAG , "AddListener");
        nearListeners.add(listener);
    }

    private void deliverContent(Content content, Matching matching){
        for (NearListener listener : nearListeners){
            if (listener != null){
                listener.onContentToDisplay(content, matching);
            }
        }
    }

    private NearNotifier nearNotifier = new NearNotifier() {
        @Override
        public void onRuleFullfilled(Matching matching) {
            getConfiguration();
            Content content = getConfiguration().getContentFromId(matching.getContent_id());
            deliverContent(content, matching);
        }

        @Override
        public void onEnterRegion(Region region) {
            // todo find correct content
            ULog.d(TAG, "Entered in region: " + region.toString());
            //Content content = getConfiguration().getContentList().get(0);
            deliverRegionEvent(ENTER, region, null);
        }

        @Override
        public void onExitRegion(Region region) {
            ULog.d(TAG, "Exited in region: " + region.toString());
            //Content content = getConfiguration().getContentList().get(0);
            deliverRegionEvent(LEAVE, region, null);
        }
    };

    private void deliverRegionEvent(String event, Region region, Content content) {

        ULog.d(TAG , "deliverEvent to " + nearListeners.size() + " listeners" );

        // also send the intent

        Intent resultIntent = new Intent(REGION_MESSAGE_ACTION);
        resultIntent.putExtra("event", event);
        application.sendOrderedBroadcast(resultIntent, null);
    }
}
