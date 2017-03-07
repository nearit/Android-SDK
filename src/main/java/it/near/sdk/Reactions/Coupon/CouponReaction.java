package it.near.sdk.Reactions.Coupon;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearJsonHttpResponseHandler;
import it.near.sdk.GlobalConfig;
import it.near.sdk.Reactions.Content.Image;
import it.near.sdk.Reactions.ContentFetchListener;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.NearJsonAPIUtils;

/**
 * @author cattaneostefano.
 */
public class CouponReaction extends CoreReaction {

    public static final String PLUGIN_NAME = "coupon-blaster";
    private static final String PREFS_SUFFIX = "NearCoupon";
    private static final String COUPONS_RES = "coupons";
    private static final String CLAIMS_RES = "claims";
    private static final String SHOW_COUPON_ACTION_NAME = "show_coupon";
    private static final String PLUGIN_ROOT_PATH = "coupon-blaster";
    private static final String TAG = "CouponReactiom";

    public CouponReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
    }

    @Override
    public String getPrefSuffix() {
        return PREFS_SUFFIX;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        HashMap<String, Class> map = new HashMap<>();
        map.put(CLAIMS_RES, Claim.class);
        map.put(COUPONS_RES, Coupon.class);
        map.put("imaes", Image.class);
        return map;
    }

    @Override
    protected String getResTypeName() {
        return COUPONS_RES;
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<String>();
        supportedActions.add(SHOW_COUPON_ACTION_NAME);
    }

    @Override
    public void refreshConfig() {
        // TODO download stuff
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, final Recipe recipe) {
        Coupon coupon = (Coupon) reaction_bundle;
        formatLinks(coupon);
        if (recipe.isForegroundRecipe()){
            nearNotifier.deliverForegroundReaction(coupon, recipe);
        } else {
            nearNotifier.deliverBackgroundReaction(coupon, recipe);
        }
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle reaction_bundle) {
        Coupon coupon = (Coupon) reaction_bundle;
        formatLinks(coupon);
        nearNotifier.deliverBackgroundPushReaction(coupon, recipe, push_id);
    }


    public void requestSingleResource(String bundleId, AsyncHttpResponseHandler responseHandler){
        String profileId = GlobalConfig.getInstance(mContext).getProfileId();
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(COUPONS_RES)
                .appendPath(bundleId)
                .appendQueryParameter("filter[claims.profile_id]", profileId)
                .appendQueryParameter("include", "claims,icon").build();
        try {
            httpClient.nearGet(mContext, url.toString(), responseHandler);
        } catch (AuthenticationException e) {
            Log.d(TAG, "Auth error");
        }

    }

    public void getCoupons(Context context, final CouponListener listener) throws UnsupportedEncodingException, MalformedURLException {
        String profile_id = null;
        profile_id = GlobalConfig.getInstance(context).getProfileId();
        if (profile_id == null){
            listener.onCouponDownloadError("Missing profileId");
            return;
        }
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(COUPONS_RES)
                .appendQueryParameter("filter[claims.profile_id]", profile_id)
                .appendQueryParameter("include", "claims,icon").build();
        String output = url.toString();
        Log.d(TAG, output);
        try {
            httpClient.nearGet(context, url.toString(), new NearJsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Copuns downloaded: " + response.toString());
                    List<Coupon> coupons = NearJsonAPIUtils.parseList(morpheus, response, Coupon.class);
                    formatLinks(coupons);
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
        Log.d(TAG, "Not implemented");
    }


    private void formatLinks(List<Coupon> notifications){
        for (Coupon notification : notifications) {
            formatLinks(notification);
        }
    }

    private void formatLinks(Coupon notification){
        Image icon = notification.getIcon();
        if (icon == null) return;
        notification.setIconSet(icon.toImageSet());
    }

}
