package it.near.sdk.reactions.couponplugin;

import android.content.Context;
import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.couponplugin.model.Claim;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearJsonAPIUtils;


public class CouponReaction extends CoreReaction<Coupon> {

    public static final String PLUGIN_NAME = "coupon-blaster";
    static final String COUPONS_RES = "coupons";
    static final String CLAIMS_RES = "claims";
    static final String IMAGES_RES = "images";
    static final String PLUGIN_ROOT_PATH = "coupon-blaster";
    private static final String TAG = "CouponReaction";

    private final GlobalConfig globalConfig;
    private final CouponApi couponApi;

    public CouponReaction(Cacher<Coupon> cacher, NearAsyncHttpClient httpClient, NearNotifier nearNotifier, CouponApi couponApi, GlobalConfig globalConfig, Type cacheType) {
        super(cacher, httpClient, nearNotifier, Coupon.class, cacheType);
        this.globalConfig = globalConfig;
        this.couponApi = couponApi;
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected String getRefreshUrl() {
        return null;
    }

    @Override
    protected String getSingleReactionUrl(String bundleId) {
        String profileId = globalConfig.getProfileId();
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(COUPONS_RES)
                .appendPath(bundleId)
                .appendQueryParameter("filter[claims.profile_id]", profileId)
                .appendQueryParameter("include", "claims,icon").build();
        return url.toString();
    }

    @Override
    protected String getDefaultShowAction() {
        // This should not be called
        return null;
    }

    @Override
    protected void injectRecipeId(Coupon element, String recipeId) {
        // left intentionally blank
    }

    @Override
    protected void normalizeElement(Coupon element) {
        Image icon = element.icon;
        if (icon == null) return;
        try {
            element.setIconSet(icon.toImageSet());
        } catch (Image.MissingImageException ignored) {

        }
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, final Recipe recipe, final TrackingInfo trackingInfo) {
        final Coupon coupon = (Coupon) reaction_bundle;
        if (coupon.hasContentToInclude()) {
            downloadSingleReaction(reaction_bundle.getId(), new ContentFetchListener<Coupon>() {
                @Override
                public void onContentFetched(Coupon content, boolean cached) {
                    notifyCoupon(coupon, recipe, trackingInfo);
                }

                @Override
                public void onContentFetchError(String error) {
                    NearLog.d(TAG, "Error: " + error);
                }
            });
        } else {
            normalizeElement(coupon);
            notifyCoupon(coupon, recipe, trackingInfo);
        }

    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(CLAIMS_RES, Claim.class);
        map.put(COUPONS_RES, Coupon.class);
        map.put(IMAGES_RES, Image.class);
        return map;
    }

    @Override
    public void refreshConfig() {
        // intentionally left blank
    }

    private void notifyCoupon(Coupon coupon, Recipe recipe, TrackingInfo trackingInfo) {
        coupon.notificationMessage = recipe.getNotificationBody();
        if (recipe.isForegroundRecipe()) {
            nearNotifier.deliverForegroundReaction(coupon, recipe, trackingInfo);
        } else {
            nearNotifier.deliverBackgroundReaction(coupon, trackingInfo);
        }
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        try {
            JSONObject toParse = new JSONObject(reactionBundleString);
            Coupon coupon = NearJsonAPIUtils.parseElement(morpheus, toParse, Coupon.class);
            if (coupon == null || coupon.claims == null || !coupon.anyClaim()) return false;
            normalizeElement(coupon);
            coupon.notificationMessage = notificationText;
            nearNotifier.deliverBackgroundPushReaction(coupon, TrackingInfo.fromRecipeId(recipeId));
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void getCoupons(final CouponListener listener) throws UnsupportedEncodingException, MalformedURLException {
        couponApi.getCoupons(new CouponListener() {
            @Override
            public void onCouponsDownloaded(List<Coupon> coupons) {
                normalizeList(coupons);
                listener.onCouponsDownloaded(coupons);
            }

            @Override
            public void onCouponDownloadError(String error) {
                listener.onCouponDownloadError(error);
            }
        });
    }

    @Override
    protected void getContent(String reaction_bundle, Recipe recipe, ContentFetchListener listener) {
        NearLog.d(TAG, "Not implemented");
    }


    public static CouponReaction obtain(Context context, NearNotifier nearNotifier, GlobalConfig globalConfig, CouponApi couponApi) {
        return new CouponReaction(
                new Cacher<Coupon>(
                        context.getSharedPreferences("never_used", Context.MODE_PRIVATE)),
                new NearAsyncHttpClient(context),
                nearNotifier,
                couponApi,
                globalConfig,
                new TypeToken<List<Coupon>>() {
                }.getType());
    }
}
