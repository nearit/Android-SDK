package it.near.sdk.Reactions.Coupon;

import java.util.List;

/**
 * @author cattaneostefano.
 */
public interface CouponListener {
    public abstract void onCouponsDownloaded(List<Coupon> claims);
    public abstract void onCouponDownloadError(String error);
}
