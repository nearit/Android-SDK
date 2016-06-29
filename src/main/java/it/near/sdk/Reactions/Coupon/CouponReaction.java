package it.near.sdk.Reactions.Coupon;

import android.content.Context;
import android.net.Uri;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.Reactions.CoreReaction;
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
    private static final String PLUGIN_NAME = "";
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
    protected void handleReaction(String reaction_action, String reaction_bundle, Recipe recipe) {

    }

    @Override
    public void handlePushReaction(Recipe recipe, String push_id, String bundle_id) {

    }

    public void getClaims(Context context, final ClaimsListener listener) throws UnsupportedEncodingException, MalformedURLException {
        String profile_id = null;
        profile_id = GlobalConfig.getInstance(context).getProfileId();
        if (profile_id == null){
            listener.onClaimsDownloadError("Missing profileId");
            return;
        }
        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_ROOT_PATH)
                .appendPath(CLAIMS_RES)
                .appendQueryParameter("filter[profile_id]", profile_id)
                .appendQueryParameter("include", "coupon").build();
        String output = url.toString();
        ULog.d(TAG, output);
        // TODO not tested
        try {
            httpClient.nearGet(context, url.toString(), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, response.toString());
                    List<Claim> claims = NearUtils.parseList(morpheus, response, Claim.class);
                    listener.onClaimsDownloaded(claims);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    listener.onClaimsDownloadError("Download error");
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
            listener.onClaimsDownloadError("Download error");
        }
/*
        GlobalState.getInstance(context).getRequestQueue().add(
                new CustomJsonRequest(context, url.toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, response.toString());
                        List<Claim> claims = NearUtils.parseList(morpheus, response, Claim.class);
                        listener.onClaimsDownloaded(claims);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onClaimsDownloadError("Download error");
                    }
                })
        );
*/
    }
}
