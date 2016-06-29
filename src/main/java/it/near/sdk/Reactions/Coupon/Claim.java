package it.near.sdk.Reactions.Coupon;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano.
 */
public class Claim extends Resource {
    @SerializedName("serial_number")
    String serial_number;
    @SerializedName("claimed_at")
    String claimed_at;
    @SerializedName("redeemed_at")
    String redeemed_at;
    @Relationship("coupon")
    Coupon coupon;

    public Claim() {
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getClaimed_at() {
        return claimed_at;
    }

    public void setClaimed_at(String claimed_at) {
        this.claimed_at = claimed_at;
    }

    public String getRedeemed_at() {
        return redeemed_at;
    }

    public void setRedeemed_at(String redeemed_at) {
        this.redeemed_at = redeemed_at;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}
