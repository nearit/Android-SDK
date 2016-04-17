package it.near.sdk;

import android.app.Application;
import android.content.Intent;
import android.os.Parcelable;

import org.altbeacon.beacon.BeaconManager;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Beacons.BeaconForest.ForestManager;
import it.near.sdk.Beacons.Monitoring.AltBeaconMonitor;
import it.near.sdk.Reactions.ContentNotification.ContentNotificationReaction;
import it.near.sdk.Reactions.PollNotification.PollNotificationReaction;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotificationReaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
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


        plugInSetup();

    }

    private void plugInSetup() {
        recipesManager = new RecipesManager(application);

        monitor = new AltBeaconMonitor(application);
        forest = new ForestManager(application, monitor, recipesManager);

        simpleNotification = new SimpleNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(simpleNotification.getIngredientName(), simpleNotification);

        contentNotification = new ContentNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(contentNotification.getIngredientName(), contentNotification);

        pollNotification = new PollNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(pollNotification.getIngredientName(), pollNotification);

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
            }

            @Override
            public void onBackground() {
                ULog.d(TAG, "onBackground");
            }
        });
    }

    public void setTraceNotifier(TraceNotifier notifier){
        GlobalState.getInstance(application).setTraceNotifier(notifier);
    }


    public void addContentListener(NearListener listener){
        ULog.d(TAG , "AddListener");
        nearListeners.add(listener);
    }


    private NearNotifier nearNotifier = new NearNotifier() {

        @Override
        public void deliverReaction(Parcelable parcelable, Recipe recipe) {
            deliverRegionEvent(parcelable, recipe);
        }
    };


    private void deliverRegionEvent(Parcelable parcelable, Recipe recipe){
        ULog.d(TAG, "deliver Event: " + parcelable.toString());

        Intent resultIntent = new Intent(REGION_MESSAGE_ACTION);
        // set contet to show
        resultIntent.putExtra("content", parcelable);
        // set the content type so the app can cast the parcelable to correct content
        resultIntent.putExtra("content-source", recipe.getReaction_ingredient_id());
        resultIntent.putExtra("content-type", recipe.getReaction_flavor().getId());
        // set the trigger info
        resultIntent.putExtra("trigger-source", recipe.getPulse_ingredient_id());
        resultIntent.putExtra("trigger-type", recipe.getPulse_flavor().getId());
        resultIntent.putExtra("trigger-item", recipe.getPulse_slice_id());

        application.sendOrderedBroadcast(resultIntent, null);
    }
}
