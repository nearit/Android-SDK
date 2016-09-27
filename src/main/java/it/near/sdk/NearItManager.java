package it.near.sdk;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import it.near.sdk.Geopolis.GeopolisManager;
import it.near.sdk.Geopolis.BeaconForest.AltBeaconMonitor;
import it.near.sdk.Communication.NearInstallation;
import it.near.sdk.Push.OpenPushEvent;
import it.near.sdk.Push.PushManager;
import it.near.sdk.Reactions.Content.ContentReaction;
import it.near.sdk.Reactions.Coupon.CouponListener;
import it.near.sdk.Reactions.Coupon.CouponReaction;
import it.near.sdk.Reactions.CustomJSON.CustomJSONReaction;
import it.near.sdk.Reactions.Event;
import it.near.sdk.Reactions.Poll.PollEvent;
import it.near.sdk.Reactions.Poll.PollReaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.RecipeRefreshListener;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.AppLifecycleMonitor;
import it.near.sdk.Utils.IntentConstants;
import it.near.sdk.Utils.NearSimpleLogger;
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
 * // inside the custom Application onCreate method
 * nearItManager = new NearItManager(this, getResources().getString(R.string.api_key));
 * nearItManager.setSenderId(R.string.sender_id);
 * nearItManager.setNotificationImage(R.drawable.beacon_notif_icon);
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
    private GeopolisManager forest;
    private RecipesManager recipesManager;
    private ContentReaction contentNotification;
    private PollReaction pollNotification;
    private CouponReaction couponReaction;
    private CustomJSONReaction customJSONReaction;
    private PushManager pushManager;
    private NearSimpleLogger logger;

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

        NearInstallation.registerInstallation(application);

        registerLogReceiver();

        pushManager = new PushManager(application);
        GlobalState.getInstance(application).setPushManager(pushManager);

    }

    /**
     * Set logger for beacon distance information.
     * @param logger logs beacon data.
     */
    public void setLogger(NearSimpleLogger logger) {
        this.logger = logger;
    }

    /**
     * Remove the beacon logger.
     */
    public void removeLogger() {
        this.logger = null;
    }


    private void plugInSetup() {
        recipesManager = new RecipesManager(application);
        GlobalState.getInstance(application).setRecipesManager(recipesManager);

        forest = new GeopolisManager(application, recipesManager);

        contentNotification = new ContentReaction(application, nearNotifier);
        recipesManager.addReaction(contentNotification.getPluginName(), contentNotification);

        pollNotification = new PollReaction(application, nearNotifier);
        recipesManager.addReaction(pollNotification.getPluginName(), pollNotification);

        couponReaction = new CouponReaction(application, nearNotifier);
        recipesManager.addReaction(couponReaction.getPluginName(), couponReaction);

        customJSONReaction = new CustomJSONReaction(application, nearNotifier);
        recipesManager.addReaction(customJSONReaction.getPluginName(), customJSONReaction);

    }


    private void registerLogReceiver() {
        String filter = application.getPackageName() + "log";
        IntentFilter intentFilter = new IntentFilter(filter);
        application.registerReceiver(logReceiver, intentFilter);
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
    public void refreshConfigs() {
        refreshConfigs(new RecipeRefreshListener() {
            @Override
            public void onRecipesRefresh() {
                Log.d(TAG, "empty listener called: success");
            }

            @Override
            public void onRecipesRefreshFail(int statusCode) {
                Log.d(TAG, "empty listener called: fail with code " + statusCode);
            }
        });
    }

    /**
     * Force the refresh of all SDK configurations. The listener will be notified with the recipes refresh outcome.
     */
    public void refreshConfigs(RecipeRefreshListener listener){
        recipesManager.refreshConfig(listener);
        forest.refreshConfig();
        contentNotification.refreshConfig();
        pollNotification.refreshConfig();
    }


    private NearNotifier nearNotifier = new NearNotifier() {
        @Override
        public void deliverBackgroundReaction(Parcelable parcelable, Recipe recipe) {
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
            resultIntent.putExtra(IntentConstants.PUSH_ID, pushId);
        }
        // set recipe id
        resultIntent.putExtra(IntentConstants.RECIPE_ID, recipe.getId());
        // set notification text
        resultIntent.putExtra(IntentConstants.NOTIF_TITLE, recipe.getNotificationTitle());
        resultIntent.putExtra(IntentConstants.NOTIF_BODY, recipe.getNotificationBody());
        // set contet to show
        resultIntent.putExtra(IntentConstants.CONTENT, parcelable);
        // set the content type so the app can cast the parcelable to correct content
        resultIntent.putExtra(IntentConstants.REACTION_PLUGIN, recipe.getReaction_plugin_id());
        resultIntent.putExtra(IntentConstants.REACTION_ACTION, recipe.getReaction_action().getId());
        // set the pulse info
        resultIntent.putExtra(IntentConstants.PULSE_PLUGIN, recipe.getPulse_plugin_id());
        resultIntent.putExtra(IntentConstants.PULSE_ACTION, recipe.getPulse_action().getId());
        resultIntent.putExtra(IntentConstants.PULSE_BUNDLE, recipe.getPulse_bundle().getId());

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

    private BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String log = intent.getStringExtra("log");
            if (logger != null){
                logger.log(log);
            }
        }
    };

    /**
     * Return a list of coupon claimed by the user and that are currently valid.
     * @param listener a listener for success or failure. If there are no coupons available the success method will be called with a null paramaeter.
     */
    public void getCoupons(CouponListener listener) {
        try {
            couponReaction.getCoupons(application, listener);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
            listener.onCouponDownloadError("Error");
        }
    }
}
