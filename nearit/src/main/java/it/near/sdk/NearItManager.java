package it.near.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;


import org.altbeacon.beacon.BeaconManager;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.communication.NearInstallation;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.logging.NearLog;
import it.near.sdk.operation.NearItUserProfile;
import it.near.sdk.operation.ProfileCreationListener;
import it.near.sdk.reactions.content.ContentReaction;
import it.near.sdk.reactions.coupon.CouponListener;
import it.near.sdk.reactions.coupon.CouponReaction;
import it.near.sdk.reactions.customjson.CustomJSONReaction;
import it.near.sdk.reactions.Event;
import it.near.sdk.reactions.feedback.FeedbackEvent;
import it.near.sdk.reactions.feedback.FeedbackReaction;
import it.near.sdk.reactions.poll.PollEvent;
import it.near.sdk.reactions.poll.PollReaction;
import it.near.sdk.reactions.simplenotification.SimpleNotificationReaction;
import it.near.sdk.recipes.EvaluationBodyBuilder;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.RecipeCooler;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.trackings.TrackCache;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackSender;
import it.near.sdk.utils.ApplicationVisibility;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.NearItIntentConstants;
import it.near.sdk.utils.NearUtils;

/**
 * Central class used to interact with the Near framework. This class should be instantiated in a custom Application class.
 * This class starts all the plugins manually and initialize global values like the apiKey.
 * To be able to use beacon technology, make sure to ask for the proper permission in the manifest or runtime, depending on your targeted API.
 * <p>
 * <pre>
 * {@code
 * // inside the custom Application onCreate method
 * nearItManager = new NearItManager(this, getResources().getString(R.string.api_key));
 * nearItManager.setNotificationImage(R.drawable.beacon_notif_icon);
 * }
 * </pre>
 */
public class NearItManager {

    private static final String TAG = "NearItManager";
    public static final String GEO_MESSAGE_ACTION = "it.near.sdk.permission.GEO_MESSAGE";
    public static final String PUSH_MESSAGE_ACTION = "it.near.sdk.permission.PUSH_MESSAGE";
    private final GlobalConfig globalConfig;
    private GeopolisManager geopolis;
    private RecipesManager recipesManager;
    private ContentReaction contentNotification;
    private SimpleNotificationReaction simpleNotification;
    private PollReaction polls;
    private CouponReaction couponReaction;
    private CustomJSONReaction customJSON;
    private FeedbackReaction feedback;
    private final List<ProximityListener> proximityListenerList = new CopyOnWriteArrayList<>();
    private Application application;

    /**
     * Default constructor.
     *
     * @param context the context
     * @param apiKey  the apiKey string
     */
    public NearItManager(Context context, String apiKey) {
        this.application = (Application) context.getApplicationContext();

        this.globalConfig = GlobalConfig.getInstance(application);

        globalConfig.setApiKey(apiKey);
        globalConfig.setAppId(NearUtils.fetchAppIdFrom(apiKey));

        plugInSetup(application, globalConfig);

        NearItUserProfile.createNewProfile(application, new ProfileCreationListener() {
            @Override
            public void onProfileCreated(boolean created, String profileId) {
                NearLog.d(TAG, created ? "Profile created successfully." : "Profile is present");
                refreshConfigs();
            }

            @Override
            public void onProfileCreationError(String error) {
                NearLog.d(TAG, "Error creating profile. Profile not present");
                // in case of success, the installation is automatically registered
                // so we update/create the installation only on profile failure
                NearInstallation.registerInstallation(application);
            }
        });
    }

    private void plugInSetup(Application application, GlobalConfig globalConfig) {
        RecipeCooler recipeCooler = new RecipeCooler(
                RecipeCooler.getSharedPreferences(application),
                new CurrentTime()
        );
        EvaluationBodyBuilder evaluationBodyBuilder = new EvaluationBodyBuilder(recipeCooler, globalConfig);
        TrackManager trackManager = getTrackManager(application);

        recipesManager = new RecipesManager(
                new NearAsyncHttpClient(application),
                globalConfig,
                recipeCooler,
                evaluationBodyBuilder,
                RecipesManager.getSharedPreferences(application),
                trackManager);
        RecipesManager.setInstance(recipesManager);

        GlobalState.getInstance(application).setRecipesManager(recipesManager);

        geopolis = new GeopolisManager(application, recipesManager, globalConfig, trackManager);

        contentNotification = new ContentReaction(application, nearNotifier);
        recipesManager.addReaction(contentNotification);

        simpleNotification = new SimpleNotificationReaction(application, nearNotifier);
        recipesManager.addReaction(simpleNotification);

        polls = new PollReaction(application, nearNotifier);
        recipesManager.addReaction(polls);

        couponReaction = new CouponReaction(application, nearNotifier, globalConfig);
        recipesManager.addReaction(couponReaction);

        customJSON = new CustomJSONReaction(application, nearNotifier);
        recipesManager.addReaction(customJSON);

        feedback = new FeedbackReaction(application, nearNotifier, globalConfig);
        recipesManager.addReaction(feedback);
    }

    @NonNull
    private TrackManager getTrackManager(Application application) {
        return new TrackManager(
                (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE),
                new TrackSender(new NearAsyncHttpClient(application)),
                new TrackCache(TrackCache.getSharedPreferences(application)),
                new ApplicationVisibility());
    }

    /**
     * Return the recipes manager
     *
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
    public static boolean verifyBluetooth(Context context) throws RuntimeException {
        return BeaconManager.getInstanceForApplication(context.getApplicationContext()).checkAvailability();
    }

    /**
     * Set an icon for proximity notification.
     *
     * @param imgRes the resource int of the image.
     */
    public void setProximityNotificationIcon(int imgRes) {
        globalConfig.setProximityNotificationIcon(imgRes);
    }

    /**
     * Set an icon for push notification.
     *
     * @param imgRes the resource int of the image.
     */
    public void setPushNotificationIcon(int imgRes) {
        globalConfig.setPushNotificationIcon(imgRes);
    }

    /**
     * Force the refresh of all SDK configurations.
     */
    public void refreshConfigs() {
        refreshConfigs(new RecipeRefreshListener() {
            @Override
            public void onRecipesRefresh() {
            }

            @Override
            public void onRecipesRefreshFail() {
            }
        });
    }

    /**
     * Force the refresh of all SDK configurations. The listener will be notified with the recipes refresh outcome.
     */
    public void refreshConfigs(RecipeRefreshListener listener) {
        recipesManager.refreshConfig(listener);
        geopolis.refreshConfig();
        contentNotification.refreshConfig();
        simpleNotification.refreshConfig();
        customJSON.refreshConfig();
        polls.refreshConfig();
        feedback.refreshConfig();
    }

    public void initLifecycleMethods(Application application) {
        geopolis.initLifecycle(application);
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

    private void deliverBeackgroundEvent(Parcelable parcelable, Recipe recipe, String action, String pushId) {
        NearLog.d(TAG, "deliver Event: " + parcelable.toString());
        Intent resultIntent = new Intent(action);
        Recipe.fillIntentExtras(resultIntent, recipe, parcelable);
        if (action.equals(PUSH_MESSAGE_ACTION)) {
            resultIntent.putExtra(NearItIntentConstants.PUSH_ID, pushId);
        }
        application.sendOrderedBroadcast(resultIntent, null);
    }

    public boolean sendEvent(Event event) {
        return sendEvent(event, new NearITEventHandler() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail(int statusCode, String error) {

            }
        });
    }

    /**
     * Sends an action to the SDK, that might delegate it to other plugins, based on its type.
     *
     * @param event the event to send.
     * @return true if the action was a recognized action, false otherwise.
     */
    public boolean sendEvent(Event event, NearITEventHandler handler) {
        switch (event.getPlugin()) {
            case PollEvent.PLUGIN_NAME:
                polls.sendEvent((PollEvent) event, handler);
                return true;
            case FeedbackEvent.PLUGIN_NAME:
                feedback.sendEvent((FeedbackEvent) event, handler);
                return true;
        }
        return false;
    }

    /**
     * Return a list of coupon claimed by the user and that are currently valid.
     *
     * @param listener a listener for success or failure. If there are no coupons available the success method will be called with a null paramaeter.
     */
    public void getCoupons(CouponListener listener) {
        try {
            couponReaction.getCoupons(application, listener);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
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

    public void addProximityListener(ProximityListener proximityListener) {
        proximityListenerList.add(proximityListener);
    }

    public void removeProximityListener(ProximityListener proximityListener) {
        proximityListenerList.remove(proximityListener);
    }

    public void removeAllProximityListener() {
        proximityListenerList.clear();
    }

    public void sendTracking(String recipeId, String event) {
        try {
            recipesManager.sendTracking(recipeId, event);
        } catch (JSONException e) {
            NearLog.d(TAG, "invalid tracking body");
        }
    }

}
