package it.near.sdk.reactions.couponplugin;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.MorphUtil;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.couponplugin.model.Claim;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.reactions.couponplugin.CouponReaction.CLAIMS_RES;
import static it.near.sdk.reactions.couponplugin.CouponReaction.COUPONS_RES;
import static it.near.sdk.reactions.couponplugin.CouponReaction.IMAGES_RES;
import static it.near.sdk.reactions.couponplugin.CouponReaction.PLUGIN_ROOT_PATH;

public class CouponApi {

    private static final String TAG = "CouponApi";
    private final NearAsyncHttpClient httpClient;
    private final GlobalConfig globalConfig;
    private final Morpheus morpheus;

    public CouponApi(NearAsyncHttpClient httpClient, GlobalConfig globalConfig, Morpheus morpheus) {
        this.httpClient = httpClient;
        this.globalConfig = globalConfig;
        this.morpheus = morpheus;
    }

    void getCoupons(final CouponListener listener) throws UnsupportedEncodingException, MalformedURLException {
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
        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "Copuns downloaded: " + response.toString());
                    List<Coupon> coupons = NearJsonAPIUtils.parseList(morpheus, response, Coupon.class);
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

    public static CouponApi obtain(Context context, GlobalConfig globalConfig) {
        HashMap<String, Class> map = new HashMap<>();
        map.put(CLAIMS_RES, Claim.class);
        map.put(COUPONS_RES, Coupon.class);
        map.put(IMAGES_RES, Image.class);
        Morpheus morpheus = MorphUtil.buildMorpheusFrom(map);
        return new CouponApi(
                new NearAsyncHttpClient(context, globalConfig),
                globalConfig,
                morpheus);
    }
}
