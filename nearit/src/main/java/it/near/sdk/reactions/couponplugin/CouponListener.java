package it.near.sdk.reactions.couponplugin;

import java.util.List;

import it.near.sdk.reactions.couponplugin.model.Coupon;

/**
 * @author cattaneostefano.
 */
public interface CouponListener {
    void onCouponsDownloaded(List<Coupon> claims);

    void onCouponDownloadError(String error);
}
