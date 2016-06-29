package it.near.sdk.Reactions.Coupon;

import java.util.List;

/**
 * @author cattaneostefano.
 */
public interface ClaimsListener {
    public abstract void onClaimsDownloaded(List<Claim> claims);
    public abstract void onClaimsDownloadError(String error);
}
