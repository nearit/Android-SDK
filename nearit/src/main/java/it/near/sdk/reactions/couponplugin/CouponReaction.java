package it.near.sdk.reactions.couponplugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.couponplugin.model.Claim;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;


public class CouponReaction extends CoreReaction<Coupon> {

    public static final String PLUGIN_NAME = "coupon-blaster";
    private static final String COUPONS_RES = "coupons";
    private static final String CLAIMS_RES = "claims";
    private static final String SHOW_COUPON_ACTION_NAME = "show_coupon";
    private static final String PLUGIN_ROOT_PATH = "coupon-blaster";
    private static final String TAG = "CouponReaction";

    private final GlobalConfig globalConfig;

    public CouponReaction(SharedPreferences sp, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, GlobalConfig globalConfig) {
        super(sp, httpClient, nearNotifier, Coupon.class);
        this.globalConfig = globalConfig;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(CLAIMS_RES, Claim.class);
        map.put(COUPONS_RES, Coupon.class);
        map.put("images", Image.class);
        return map;
    }

    @Override
    protected String getResTypeName() {
        return COUPONS_RES;
    }

    @Override
    public List<String> buildActions() {
        List<String> supportedActions = new ArrayList<String>();
        supportedActions.add(SHOW_COUPON_ACTION_NAME);
        return supportedActions;
    }

    @Override
    public void refreshConfig() {
        // TODO download stuff
    }

    @Override
    protected void normalizeElement(Coupon element) {
        Image icon = element.icon;
        if (icon == null) return;
        element.setIconSet(icon.toImageSet());
    }


    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, final Recipe recipe) {
        Coupon coupon = (Coupon) reaction_bundle;
        if (coupon.hasContentToInclude()) {
            requestSingleReaction(reaction_bundle.getId(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Coupon coupon = NearJsonAPIUtils.parseElement(morpheus, response, Coupon.class);
                    normalizeElement(coupon);
                    notifyCoupon(coupon, recipe);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "couldn't fetch content for push recipe");
                }
            });
        } else {
            normalizeElement(coupon);
            notifyCoupon(coupon, recipe);
        }

    }

    private void notifyCoupon(Coupon coupon, Recipe recipe) {
        if (recipe.isForegroundRecipe()) {
            nearNotifier.deliverForegroundReaction(coupon, recipe);
        } else {
            nearNotifier.deliverBackgroundReaction(coupon, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reaction_bundle) {
        Coupon coupon = (Coupon) reaction_bundle;
        if (coupon.hasContentToInclude()) {
            requestSingleReaction(reaction_bundle.getId(), new NearJsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Coupon coupon = NearJsonAPIUtils.parseElement(morpheus, response, Coupon.class);
                            normalizeElement(coupon);
                            nearNotifier.deliverBackgroundPushReaction(coupon, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
                        }

                        @Override
                        public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                            NearLog.d(TAG, "couldn't fetch content for push recipe");
                        }
                    },
                    new Random().nextInt(1000));
        } else {
            normalizeElement(coupon);
            nearNotifier.deliverBackgroundPushReaction(coupon, recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
        }
    }

    @Override
    public void handlePushReaction(final String recipeId, final String notificationText, String reactionAction, String reactionBundleId) {
        requestSingleReaction(reactionBundleId, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Coupon coupon = NearJsonAPIUtils.parseElement(morpheus, response, Coupon.class);
                        normalizeElement(coupon);
                        nearNotifier.deliverBackgroundPushReaction(coupon, recipeId, notificationText, getReactionPluginName());
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        NearLog.d(TAG, "couldn't fetch content for push recipe");
                    }
                },
                new Random().nextInt(1000));
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        try {
            JSONObject toParse = new JSONObject(reactionBundleString);
            Coupon coupon = NearJsonAPIUtils.parseElement(morpheus, toParse, Coupon.class);
            if (coupon == null || coupon.claims == null || !coupon.anyClaim()) return false;
            normalizeElement(coupon);
            nearNotifier.deliverBackgroundPushReaction(coupon, recipeId, notificationText, getReactionPluginName());
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    protected void requestSingleReaction(String bundleId, AsyncHttpResponseHandler responseHandler) {
        String profileId = globalConfig.getProfileId();
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(COUPONS_RES)
                .appendPath(bundleId)
                .appendQueryParameter("filter[claims.profile_id]", profileId)
                .appendQueryParameter("include", "claims,icon").build();
        try {
            httpClient.nearGet(url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Auth error");
        }

    }

    public void getCoupons(Context context, final CouponListener listener) throws UnsupportedEncodingException, MalformedURLException {
        String profile_id = globalConfig.getProfileId();
        if (profile_id == null) {
            listener.onCouponDownloadError("Missing profileId");
            return;
        }
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(COUPONS_RES)
                .appendQueryParameter("filter[claims.profile_id]", profile_id)
                .appendQueryParameter("include", "claims,icon").build();
        String output = url.toString();
        NearLog.d(TAG, output);
        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "Copuns downloaded: " + response.toString());
                    List<Coupon> coupons = NearJsonAPIUtils.parseList(morpheus, response, Coupon.class);
                    normalizeList(coupons);
                    listener.onCouponsDownloaded(coupons);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    listener.onCouponDownloadError("Download error");
                }
            });
        } catch (AuthenticationException e) {
            listener.onCouponDownloadError("Download error");
        }
    }

    @Override
    protected void getContent(String reaction_bundle, Recipe recipe, ContentFetchListener listener) {
        NearLog.d(TAG, "Not implemented");
    }

    @Override
    protected String getRefreshUrl() {
        return null;
    }


    public static CouponReaction obtain(Context context, NearNotifier nearNotifier, GlobalConfig globalConfig) {
        return new CouponReaction(
                context.getSharedPreferences("never_used", Context.MODE_PRIVATE),
                new NearAsyncHttpClient(context),
                nearNotifier,
                globalConfig);
    }
}
