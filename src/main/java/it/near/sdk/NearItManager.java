package it.near.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import org.altbeacon.beacon.BeaconManager;

import it.near.sdk.Beacons.BeaconForest.ForestManager;
import it.near.sdk.Beacons.BeaconForest.AltBeaconMonitor;
import it.near.sdk.Push.PushManager;
import it.near.sdk.Reactions.Action;
import it.near.sdk.Reactions.ContentNotification.ContentNotificationReaction;
import it.near.sdk.Reactions.PollNotification.PollAction;
import it.near.sdk.Reactions.PollNotification.PollNotificationReaction;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotificationReaction;
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
    private static String APP_PACKAGE_NAME;
    private ForestManager forest;
    private RecipesManager recipesManager;
    private SimpleNotificationReaction simpleNotification;
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
     * Set the senderId for the push notifications
     * @param senderId
     */
    public void setPushSenderId(String senderId){
        pushManager = new PushManager(application, senderId);
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
        simpleNotification.refreshConfig();
        contentNotification.refreshConfig();
        pollNotification.refreshConfig();
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
        // set recipe id
        resultIntent.putExtra("recipe_id", recipe.getId());
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

    /**
     * Sends an action to the SDK, that might delegate it to other plugins, based on its type.
     * @param action
     * @return true if the action was a recognized action, false otherwise.
     */
    public boolean sendAction(Action action){
        switch (action.getIngredient()){
            case PollAction.INGREDIENT_NAME:
                pollNotification.sendAction((PollAction)action);
                return true;
        }
        return false;
    }
}
