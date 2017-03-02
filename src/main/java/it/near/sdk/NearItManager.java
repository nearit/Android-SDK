package it.near.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Geopolis.GeopolisManager;
import it.near.sdk.Geopolis.Beacons.AltBeaconMonitor;
import it.near.sdk.Communication.NearInstallation;
import it.near.sdk.Geopolis.Beacons.Ranging.ProximityListener;
import it.near.sdk.Operation.NearItUserProfile;
import it.near.sdk.Operation.ProfileCreationListener;
import it.near.sdk.Reactions.Content.ContentReaction;
import it.near.sdk.Reactions.Coupon.CouponListener;
import it.near.sdk.Reactions.Coupon.CouponReaction;
import it.near.sdk.Reactions.CustomJSON.CustomJSONReaction;
import it.near.sdk.Reactions.Event;
import it.near.sdk.Reactions.Feedback.FeedbackEvent;
import it.near.sdk.Reactions.Feedback.FeedbackReaction;
import it.near.sdk.Reactions.Poll.PollEvent;
import it.near.sdk.Reactions.Poll.PollReaction;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotificationReaction;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.RecipeRefreshListener;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearItIntentConstants;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * Central class used to interact with the Near framework. This class should be instantiated in a custom Application class.
 * This class starts all the plugins manually and initialize global values like the apiKey.
 * To be able to use beacon technology, make sure to ask for the proper permission in the manifest or runtime, depending on your targeted API.
 *
 * <pre>
 * {@code
 * // inside the custom Application onCreate method
 * nearItManager = new NearItManager(this, getResources().getString(R.string.api_key));
 * nearItManager.setNotificationImage(R.drawable.beacon_notif_icon);
 * }
 * </pre>
 *
 * @author cattaneostefano
 */
public class NearItManager {

    private static final String TAG = "NearItManager";
    public static final String GEO_MESSAGE_ACTION = "it.near.sdk.permission.GEO_MESSAGE";
    public static final String PUSH_MESSAGE_ACTION = "it.near.sdk.permission.PUSH_MESSAGE";
    private GeopolisManager geopolis;
    private RecipesManager recipesManager;
    private ContentReaction contentNotification;
    private SimpleNotificationReaction simpleNotification;
    private PollReaction pollNotification;
    private CouponReaction couponReaction;
    private CustomJSONReaction customJSONReaction;
    private FeedbackReaction feedbackReaction;
    private List<ProximityListener> proximityListenerList = new ArrayList<>();
    Application application;

    /**
     * Default constructor.
     * @param application the application object
     * @param apiKey the apiKey string
     */
    public NearItManager(final Application application, String apiKey) {
        this.application = application;

        GlobalConfig.getInstance(application).setApiKey(apiKey);
        GlobalConfig.getInstance(application).setAppId(NearUtils.fetchAppIdFrom(apiKey));

        plugInSetup();

        NearItUserProfile.createNewProfile(application, new ProfileCreationListener() {
            @Override
            public void onProfileCreated(boolean created, String profileId) {
                ULog.d(TAG, created ? "Profile created successfully." : "Profile is present");
            }

            @Override
            public void onProfileCreationError(String error) {
                ULog.wtf(TAG, "Error creating profile. Profile not present");
                // in case of success, the installation is automatically registered
                // so we update/create the installation only on profile failure
                NearInstallation.registerInstallation(application);
            }
        });
    }

    private void plugInSetup() {
        recipesManager = new RecipesManager(application);
        GlobalState.getInstance(application).setRecipesManager(recipesManager);

        geopolis = new GeopolisManager(application, recipesManager);

        contentNotification = new ContentReaction(application, nearNotifier);
        recipesManager.addReaction(contentNotification);

        simpleNotification = new SimpleNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(simpleNotification);

        pollNotification = new PollReaction(application, nearNotifier);
        recipesManager.addReaction(pollNotification);

        couponReaction = new CouponReaction(application, nearNotifier);
        recipesManager.addReaction(couponReaction);

        customJSONReaction = new CustomJSONReaction(application, nearNotifier);
        recipesManager.addReaction(customJSONReaction);

        feedbackReaction = new FeedbackReaction(application, nearNotifier);
        recipesManager.addReaction(feedbackReaction);

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

    /**
     * Set a notification image. Refer to the Android guidelines to determine the best image for a notification
     * @param imgRes the resource int of the image
     * @see <a href="http://developer.android.com/design/patterns/notifications.html">jsonAPI 1.0 specifications</a>
     */
    public void setNotificationImage(int imgRes){
        GlobalConfig.getInstance(application).setNotificationImage(imgRes);
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
        geopolis.refreshConfig();
        contentNotification.refreshConfig();
        pollNotification.refreshConfig();
    }

    private NearNotifier nearNotifier = new NearNotifier() {
        @Override
        public void deliverBackgroundReaction(Parcelable parcelable, Recipe recipe) {
            deliverBeackgroundEvent(parcelable, recipe, GEO_MESSAGE_ACTION, null);
        }

        @Override
        public void deliverBackgroundPushReaction(Parcelable parcelable, Recipe recipe, String push_id) {
            deliverBeackgroundEvent(parcelable, recipe, PUSH_MESSAGE_ACTION, push_id);
        }

        @Override
        public void deliverForegroundReaction(Parcelable content, Recipe recipe) {
            for (ProximityListener proximityListener : proximityListenerList) {
                proximityListener.foregroundEvent(content, recipe);
            }
        }
    };

    private void deliverBeackgroundEvent(Parcelable parcelable, Recipe recipe, String action, String pushId){
        ULog.d(TAG, "deliver Event: " + parcelable.toString());
        Intent resultIntent = new Intent(action);
        Recipe.fillIntentExtras(resultIntent, recipe, parcelable);
        if (action.equals(PUSH_MESSAGE_ACTION)){
            resultIntent.putExtra(NearItIntentConstants.PUSH_ID, pushId);
        }
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
            case FeedbackEvent.PLUGIN_NAME:
                feedbackReaction.sendEvent((FeedbackEvent) event);
                return true;
        }
        return false;
    }

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

    /**
     * Start the monitoring radar that works in the background.
     * This should be stopped only if you don't want to be notified anymore (even in background) and don't want to track the user location.
     */
    public void startRadar() {
        geopolis.startRadar();
    }

    public void stopRadar() {
        geopolis.stopRadar();
    }

    public void addProximityListener(ProximityListener proximityListener){
        synchronized (proximityListenerList) {
            proximityListenerList.add(proximityListener);
        }
    }

    public void removeProximityListener(ProximityListener proximityListener) {
        synchronized (proximityListenerList){
            proximityListenerList.remove(proximityListener);
        }
    }

    public void removeAllProximityListener(){
        synchronized (proximityListenerList) {
            proximityListenerList.clear();
        }
    }

}
