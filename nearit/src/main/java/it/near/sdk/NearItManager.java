package it.near.sdk;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.altbeacon.beacon.BeaconManager;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearInstallation;
import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.geopolis.geofences.GeoFenceSystemEventsReceiver;
import it.near.sdk.logging.NearLog;
import it.near.sdk.operation.NearItUserProfile;
import it.near.sdk.operation.ProfileCreationListener;
import it.near.sdk.operation.ProfileUpdateListener;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.Event;
import it.near.sdk.reactions.Reaction;
import it.near.sdk.reactions.contentplugin.ContentReaction;
import it.near.sdk.reactions.couponplugin.CouponApi;
import it.near.sdk.reactions.couponplugin.CouponListener;
import it.near.sdk.reactions.couponplugin.CouponReaction;
import it.near.sdk.reactions.customjsonplugin.CustomJSONReaction;
import it.near.sdk.reactions.feedbackplugin.FeedbackEvent;
import it.near.sdk.reactions.feedbackplugin.FeedbackReaction;
import it.near.sdk.reactions.simplenotificationplugin.SimpleNotificationReaction;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.RecipeReactionHandler;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.recipes.RecipeRepository;
import it.near.sdk.recipes.RecipeTrackSender;
import it.near.sdk.recipes.RecipesApi;
import it.near.sdk.recipes.RecipesHistory;
import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.recipes.background.NearBackgroundJobIntentService;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.validation.AdvScheduleValidator;
import it.near.sdk.recipes.validation.CooldownValidator;
import it.near.sdk.recipes.validation.RecipeValidationFilter;
import it.near.sdk.recipes.validation.Validator;
import it.near.sdk.trackings.BluetoothStatusReceiver;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.ApiKeyConfig;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.NearUtils;
import it.near.sdk.utils.timestamp.NearItTimeStampApi;
import it.near.sdk.utils.timestamp.NearTimestampChecker;

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
public class NearItManager implements ProfileUpdateListener, RecipeReactionHandler {

    private static final int NEAR_JOB_SERVICE_ID = 888;
    @Nullable
    private volatile static NearItManager sInstance = null;

    /**
     * Private lock object for singleton initialization protecting against denial-of-service attack.
     */
    private static final Object SINGLETON_LOCK = new Object();

    private static final String TAG = "NearItManager";
    public static final String GEO_MESSAGE_ACTION = "it.near.sdk.permission.GEO_MESSAGE";
    public static final String PUSH_MESSAGE_ACTION = "it.near.sdk.permission.PUSH_MESSAGE";
    public final GlobalConfig globalConfig;
    private GeopolisManager geopolis;
    private RecipesManager recipesManager;
    private ContentReaction contentNotification;
    private SimpleNotificationReaction simpleNotification;
    private CouponReaction couponReaction;
    private CustomJSONReaction customJSON;
    private FeedbackReaction feedback;
    private final List<ProximityListener> proximityListenerList = new CopyOnWriteArrayList<>();
    private NearInstallation nearInstallation;
    private NearItUserProfile nearItUserProfile;
    private HashMap<String, Reaction> reactions = new HashMap<>();
    private static Context context;

    /**
     * Setup method for the library, this should absolutely be called inside the onCreate callback of the app Application class.
     */
    @NonNull
    public static NearItManager init(@NonNull Application application, @NonNull String apiKey) {
        // store api key
        context = application;
        ApiKeyConfig.saveApiKey(application, apiKey);

        // build instance
        NearItManager nearItManager = getInstance();
        // init lifecycle method
        nearItManager.initLifecycleMethods(application);
        // first run: this is executed only after the setup.
        // usually to inject the nearit manager instance in object that needs it, it couldn't been done inside the nearItManager constructor.
        nearItManager.firstRun();

        registerReceivers();

        return nearItManager;
    }

    /**
     * Double check pattern for getter.
     */
    @NonNull
    public static NearItManager getInstance() {
        if (sInstance == null) {
            synchronized (SINGLETON_LOCK) {
                if (sInstance == null) {
                    sInstance = new NearItManager(context);
                }
            }
        }
        return sInstance;
    }

    protected NearItManager(Context context) {
        if (context == null) {
            NearLog.e(TAG, "The NearIT library could not be instantiated. Is the api key included in the manifest?");
        }
        String apiKey = ApiKeyConfig.readApiKey(context);
        this.context = context.getApplicationContext();

        this.globalConfig = new GlobalConfig(
                GlobalConfig.buildSharedPreferences(context));

        globalConfig.setApiKey(apiKey);
        globalConfig.setAppId(NearUtils.fetchAppIdFrom(apiKey));

        nearInstallation = new NearInstallation(context, new NearAsyncHttpClient(context), globalConfig);
        nearItUserProfile = new NearItUserProfile(globalConfig, new NearAsyncHttpClient(context));

        plugInSetup(context, globalConfig);
    }

    private void firstRun() {
        nearItUserProfile.setProfileUpdateListener(this);
        nearItUserProfile.createNewProfile(context, new ProfileCreationListener() {
            @Override
            public void onProfileCreated(boolean created, String profileId) {
                NearLog.d(TAG, created ? "Profile created successfully." : "Profile is present");
                if (created) {
                    recipesManager.refreshConfig();
                } else {
                    recipesManager.syncConfig();
                }
            }

            @Override
            public void onProfileCreationError(String error) {
                NearLog.d(TAG, "Error creating profile. Profile not present");
                // in case of success, the installation is automatically registered
                // so we update/create the installation only on profile failure
                updateInstallation();
                recipesManager.syncConfig();
            }
        });
    }

    private static void registerReceivers() {
        context.registerReceiver(new BluetoothStatusReceiver(), new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        context.registerReceiver(new GeoFenceSystemEventsReceiver(), new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    private void plugInSetup(Context context, GlobalConfig globalConfig) {
        RecipesHistory recipesHistory = new RecipesHistory(
                RecipesHistory.getSharedPreferences(context),
                new CurrentTime()
        );
        TrackManager trackManager = TrackManager.obtain(context);
        List<Validator> validators = new ArrayList<>();
        validators.add(new CooldownValidator(recipesHistory, new CurrentTime()));
        validators.add(new AdvScheduleValidator(new CurrentTime()));
        RecipeValidationFilter recipeValidationFilter = new RecipeValidationFilter(validators);

        RecipesApi recipesApi = RecipesApi.obtain(context, recipesHistory, globalConfig);
        NearItTimeStampApi nearItTimeStampApi = new NearItTimeStampApi(
                new NearAsyncHttpClient(this.context),
                NearItTimeStampApi.buildMorpheus(),
                globalConfig);
        NearTimestampChecker nearTimestampChecker = new NearTimestampChecker(nearItTimeStampApi);
        RecipeRepository recipeRepository = new RecipeRepository(
                nearTimestampChecker,
                new Cacher<Recipe>(RecipeRepository.getSharedPreferences(this.context)),
                recipesApi,
                new CurrentTime(),
                RecipeRepository.getSharedPreferences(this.context)
        );
        RecipeTrackSender recipeTrackSender = new RecipeTrackSender(globalConfig, recipesHistory, trackManager, new CurrentTime());
        recipesManager = new RecipesManager(
                recipeValidationFilter,
                recipeTrackSender,
                recipeRepository,
                recipesApi,
                this);

        geopolis = new GeopolisManager(context, recipesManager, globalConfig, trackManager);

        contentNotification = ContentReaction.obtain(context, nearNotifier);
        addReaction(contentNotification);

        simpleNotification = new SimpleNotificationReaction(nearNotifier);
        addReaction(simpleNotification);

        couponReaction = CouponReaction.obtain(context, nearNotifier, globalConfig,
                CouponApi.obtain(context, globalConfig));
        addReaction(couponReaction);

        customJSON = CustomJSONReaction.obtain(context, nearNotifier);
        addReaction(customJSON);

        feedback = FeedbackReaction.obtain(context, nearNotifier, globalConfig);
        addReaction(feedback);

    }

    private void initLifecycleMethods(Application application) {
        geopolis.initLifecycle(application);
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

    public void setProfileId(String profileId) {
        nearItUserProfile.setProfileId(profileId);
        updateInstallation();
    }

    public void resetProfileId() {
        setProfileId(null);
    }

    @Nullable
    public String getProfileId() {
        return nearItUserProfile.getProfileId();
    }

    public void setUserData(String key, String value, UserDataNotifier listener) {
        nearItUserProfile.setUserData(context, key, value, listener);
    }

    public void setBatchUserData(Map<String, String> valuesMap, UserDataNotifier listener) {
        nearItUserProfile.setBatchUserData(context, valuesMap, listener);
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
        recipesManager.syncConfig(listener);
        geopolis.refreshConfig();
        contentNotification.refreshConfig();
        simpleNotification.refreshConfig();
        customJSON.refreshConfig();
        feedback.refreshConfig();
    }

    private NearNotifier nearNotifier = new NearNotifier() {
        @Override
        public void deliverBackgroundReaction(ReactionBundle reactionBundle, TrackingInfo trackingInfo) {
            deliverBackgroundEvent(reactionBundle, GEO_MESSAGE_ACTION, trackingInfo);
        }

        @Override
        public void deliverBackgroundPushReaction(ReactionBundle reactionBundle, TrackingInfo trackingInfo) {
            deliverBackgroundEvent(reactionBundle, PUSH_MESSAGE_ACTION, trackingInfo);
        }

        @Override
        public void deliverForegroundReaction(final ReactionBundle reactionBundle, final Recipe recipe, final TrackingInfo trackingInfo) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (ProximityListener proximityListener : proximityListenerList) {
                        proximityListener.foregroundEvent(reactionBundle, trackingInfo);
                    }
                }
            });
        }
    };

    private void deliverBackgroundEvent(
            Parcelable parcelable, String action, TrackingInfo trackingInfo) {
        NearLog.d(TAG, "deliver Event: " + parcelable.toString());

        Intent intent = new Intent();
        Recipe.fillIntentExtras(intent, parcelable, trackingInfo, action);

        NearBackgroundJobIntentService.enqueueWork(context, intent);
        // NearItIntentService.sendSimpleNotification(context, resultIntent);

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
            couponReaction.getCoupons(listener);
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

    public void sendTracking(TrackingInfo trackingInfo, String event) {
        try {
            recipesManager.sendTracking(trackingInfo, event);
        } catch (JSONException e) {
            NearLog.d(TAG, "invalid tracking body");
        }
    }

    public void updateInstallation() {
        nearInstallation.refreshInstallation();
    }

    @Override
    public void onProfileUpdated() {
        nearInstallation.refreshInstallation();
        refreshConfigs();
    }

    private void addReaction(Reaction reaction) {
        reactions.put(reaction.getReactionPluginName(), reaction);
    }

    @Override
    public void gotRecipe(Recipe recipe, TrackingInfo trackingInfo) {
        Reaction reaction = reactions.get(recipe.getReaction_plugin_id());
        if (reaction != null) {
            reaction.handleReaction(recipe, trackingInfo);
        }
    }

    @Override
    public void processRecipe(String recipeId) {
        recipesManager.processRecipe(recipeId, new RecipesApi.SingleRecipeListener() {
            @Override
            public void onRecipeFetchSuccess(Recipe recipe) {
                String reactionPluginName = recipe.getReaction_plugin_id();
                Reaction reaction = reactions.get(reactionPluginName);
                reaction.handlePushReaction(recipe, recipe.getReaction_bundle());
            }

            @Override
            public void onRecipeFetchError(String error) {

            }
        });
    }

    @Override
    public void processRecipe(String recipeId, String notificationText, String reactionPluginId, String reactionActionId, String reactionBundleId) {
        Reaction reaction = reactions.get(reactionPluginId);
        if (reaction == null) return;
        reaction.handlePushReaction(recipeId, notificationText, reactionActionId, reactionBundleId);
    }

    @Override
    public boolean processReactionBundle(String recipeId, String notificationText, String reactionPluginId, String reactionActionId, String reactionBundleString) {
        Reaction reaction = reactions.get(reactionPluginId);
        if (reaction == null) return false;
        return reaction.handlePushBundledReaction(recipeId, notificationText, reactionActionId, reactionBundleString);
    }

    public RecipeReactionHandler getRecipesReactionHandler() {
        return this;
    }
}
