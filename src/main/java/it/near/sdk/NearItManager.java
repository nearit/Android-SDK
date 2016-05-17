package it.near.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import org.altbeacon.beacon.BeaconManager;

import it.near.sdk.Beacons.BeaconForest.ForestManager;
import it.near.sdk.Beacons.BeaconForest.AltBeaconMonitor;
import it.near.sdk.Push.OpenPushEvent;
import it.near.sdk.Push.PushManager;
import it.near.sdk.Reactions.ContentNotification.ContentNotificationReaction;
import it.near.sdk.Reactions.Event;
import it.near.sdk.Reactions.PollNotification.PollEvent;
import it.near.sdk.Reactions.PollNotification.PollNotificationReaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.OnLifecycleEventListener;
import it.near.sdk.Utils.ULog;

/**
 * Central class used to interact with the Near framework. This class should be instantiated in a custom Application class.
 * This class starts all the plugins manually and initialize global values like the apiKey and the push senderId.
 * To be able to use beacon technology, make sure to ask for the proper permission in the manifest or runtime, depending on your targeted API.
 *
 * <pre>
 * {@code
 *
 * // inside the custom Application onCreate method
 * nearItManager = new NearItManager(this, getResources().getString(R.string.api_key));
 * nearItManager.setSenderId(R.string.sender_id);
 *
 * }
 * </pre>
 *
 * @author cattaneostefano
 */
public class NearItManager {

    private static final String TAG = "NearItManager";
    private static final String ENTER = "enter";
    private static final String LEAVE = "leave";
    private static final String REGION_MESSAGE_ACTION = "it.near.sdk.permission.REGION_MESSAGE";
    private static final String PUSH_MESSAGE_ACTION = "it.near.sdk.permission.PUSH_MESSAGE";
    private static String APP_PACKAGE_NAME;
    private ForestManager forest;
    private RecipesManager recipesManager;
    private ContentNotificationReaction contentNotification;
    private PollNotificationReaction pollNotification;
    private PushManager pushManager;

    private AltBeaconMonitor monitor;

    Application application;

    /**
     * Default constructor.
     * @param application the application object
     * @param apiKey the apiKey string
     */
    public NearItManager(Application application, String apiKey) {
        this.application = application;
        initLifecycleMonitor();

        GlobalConfig.getInstance(application).setApiKey(apiKey);
        GlobalConfig.getInstance(application).setAppId(NearUtils.fetchAppIdFrom(apiKey));
        GlobalState.getInstance(application).setNearNotifier(nearNotifier);

        plugInSetup();

    }


    private void plugInSetup() {
        recipesManager = new RecipesManager(application);
        GlobalState.getInstance(application).setRecipesManager(recipesManager);

        monitor = new AltBeaconMonitor(application);
        forest = new ForestManager(application, monitor, recipesManager);

        contentNotification = new ContentNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(contentNotification.getPluginName(), contentNotification);

        pollNotification = new PollNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(pollNotification.getPluginName(), pollNotification);

    }

    /**
     * Set the senderId for the push notifications
     * @param senderId
     */
    public void setPushSenderId(String senderId){
        pushManager = new PushManager(application, senderId);
        GlobalState.getInstance(application).setPushManager(pushManager);
    }

    /**
     * Return the recipes manager
     * @return the recipes manager
     */
    public RecipesManager getRecipesManager() {
        return recipesManager;
    }

    /**
     * Checks the device capacity to detect beacons
     *
     * @return true if the device has bluetooth enabled, false otherwise
     * @throws RuntimeException when the device doesn't have the essential BLE compatibility
     */
    public static boolean verifyBluetooth(Context context) throws RuntimeException{
        return BeaconManager.getInstanceForApplication(context.getApplicationContext()).checkAvailability();
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

    /**
     * Set a notification image. Refer to the Android guidelines to determine the best image for a notification
     * @param imgRes the resource int of the image
     * @see <a href="http://developer.android.com/design/patterns/notifications.html">jsonAPI 1.0 specifications</a>
     */
    public void setNotificationImage(int imgRes){
        GlobalConfig.getInstance(application).setNotificationImage(imgRes);
    }


    public void setThreshold(float threshold) {
        GlobalConfig.getInstance(application).setThreshold(threshold);
    }

    /**
     * Force the refresh of all SDK configurations.
     */
    public void refreshConfigs(){
        recipesManager.refreshConfig();
        forest.refreshConfig();
        contentNotification.refreshConfig();
        pollNotification.refreshConfig();
    }


    private NearNotifier nearNotifier = new NearNotifier() {
        @Override
        public void deliverBackgroundRegionReaction(Parcelable parcelable, Recipe recipe) {
            deliverBeackgroundEvent(parcelable, recipe, REGION_MESSAGE_ACTION, null);
        }

        @Override
        public void deliverBackgroundPushReaction(Parcelable parcelable, Recipe recipe, String push_id) {
            deliverBeackgroundEvent(parcelable, recipe, PUSH_MESSAGE_ACTION, push_id);
        }
    };


    private void deliverBeackgroundEvent(Parcelable parcelable, Recipe recipe, String action, String pushId){
        ULog.d(TAG, "deliver Event: " + parcelable.toString());

        Intent resultIntent = new Intent(action);
        if (action.equals(PUSH_MESSAGE_ACTION)){
            resultIntent.putExtra("push_id", pushId);
        }
        // set recipe id
        resultIntent.putExtra("recipe_id", recipe.getId());
        // set notification text
        resultIntent.putExtra("notif", recipe.getNotification());
        // set contet to show
        resultIntent.putExtra("content", parcelable);
        // set the content type so the app can cast the parcelable to correct content
        resultIntent.putExtra("reaction-plugin", recipe.getReaction_plugin_id());
        resultIntent.putExtra("reaction-action", recipe.getReaction_action().getId());
        // set the pulse info
        resultIntent.putExtra("pulse-plugin", recipe.getPulse_plugin_id());
        resultIntent.putExtra("pulse-action", recipe.getPulse_action().getId());
        resultIntent.putExtra("pulse-bundle", recipe.getPulse_bundle().getId());

        application.sendOrderedBroadcast(resultIntent, null);
    }

    /**
     * Sends an action to the SDK, that might delegate it to other plugins, based on its type.
     * @param event the event to send.
     * @return true if the action was a recognized action, false otherwise.
     */
    public boolean sendEvent(Event event){
        switch (event.getPlugin()){
            case PollEvent.PLUGIN_NAME:
                pollNotification.sendEvent((PollEvent)event);
                return true;
            case OpenPushEvent.PLUGIN_NAME:
                pushManager.sendEvent((OpenPushEvent) event);
                return true;
        }
        return false;
    }
}
