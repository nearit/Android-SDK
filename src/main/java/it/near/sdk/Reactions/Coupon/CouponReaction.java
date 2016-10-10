package it.near.sdk.Reactions.Coupon;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.GlobalConfig;
import it.near.sdk.Reactions.Content.Image;
import it.near.sdk.Reactions.CoreReaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano.
 */
public class CouponReaction extends CoreReaction {

    private static final String PREFS_SUFFIX = "NearCoupon";
    public static final String COUPONS_RES = "coupons";
    public static final String CLAIMS_RES = "claims";
    private static final String PLUGIN_NAME = "coupon-blaster";
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
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        // TODO this will likely never get called because coupon recipes are online evaluated or push recipes.
    }

    @Override
    public void handlePushReaction(final Recipe recipe, final String push_id, ReactionBundle bundle) {
        requestSingleResource(bundle.getId(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ULog.d(TAG, response.toString());
                Coupon coupon = NearUtils.parseElement(morpheus, response, Coupon.class);
                formatLinks(coupon);
                nearNotifier.deliverBackgroundPushReaction(coupon, recipe, push_id);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ULog.d(TAG, "Error in downloading push content: " + statusCode);
            }
        });
    }

    @Override
    public void handleEvaluatedReaction(final Recipe recipe, String bundle_id) {
        requestSingleResource(bundle_id, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ULog.d(TAG, response.toString());
                Coupon coupon = NearUtils.parseElement(morpheus, response, Coupon.class);
                formatLinks(coupon);
                nearNotifier.deliverBackgroundReaction(coupon, recipe);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ULog.d(TAG, "Error in downloading content: " + statusCode);
            }
        });
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
            e.printStackTrace();
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
        ULog.d(TAG, output);
        try {
            httpClient.nearGet(context, url.toString(), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, "Copuns downloaded: " + response.toString());
                    List<Coupon> coupons = NearUtils.parseList(morpheus, response, Coupon.class);
                    formatLinks(coupons);
                    listener.onCouponsDownloaded(coupons);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    listener.onCouponDownloadError("Download error");
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
            listener.onCouponDownloadError("Download error");
        }
/*
        GlobalState.getInstance(context).getRequestQueue().add(
                new CustomJsonRequest(context, url.toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        List<Claim> claims = NearUtils.parseList(morpheus, response, Claim.class);
                        listener.onCouponsDownloaded(claims);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onCouponDownloadError("Download error");
                    }
                })
        );
*/
    }

    @Override
    protected Parcelable getContent(String reaction_bundle, Recipe recipe) {
        return null;
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
