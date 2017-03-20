package it.near.sdk.reactions.coupon;

import java.util.List;

/**
 * @author cattaneostefano.
 */
public interface CouponListener {
    void onCouponsDownloaded(List<Coupon> claims);
    void onCouponDownloadError(String error);
}
