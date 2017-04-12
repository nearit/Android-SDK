package it.near.sdk.reactions.coupon;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.morpheusnear.Resource;

/**
 * @author cattaneostefano.
 */
public class Claim extends Resource implements Parcelable {
    @SerializedName("serial_number")
    public String serial_number;
    @SerializedName("claimed_at")
    public String claimed_at;
    @SerializedName("redeemed_at")
    public String redeemed_at;
    @Relationship("coupon")
    public Coupon coupon;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(serial_number);
        dest.writeString(claimed_at);
        dest.writeString(redeemed_at);
    }

    public static final Creator<Claim> CREATOR = new Creator<Claim>() {
        @Override
        public Claim createFromParcel(Parcel in) {
            return new Claim(in);
        }

        @Override
        public Claim[] newArray(int size) {
            return new Claim[size];
        }
    };

    protected Claim(Parcel in) {
        setId(in.readString());
        serial_number = in.readString();
        claimed_at = in.readString();
        redeemed_at = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim claim = (Claim) o;

        if (serial_number != null ? !serial_number.equals(claim.serial_number) : claim.serial_number != null)
            return false;
        if (claimed_at != null ? !claimed_at.equals(claim.claimed_at) : claim.claimed_at != null)
            return false;
        return redeemed_at != null ? redeemed_at.equals(claim.redeemed_at) : claim.redeemed_at == null;

    }

    @Override
    public int hashCode() {
        int result = serial_number != null ? serial_number.hashCode() : 0;
        result = 31 * result + (claimed_at != null ? claimed_at.hashCode() : 0);
        result = 31 * result + (redeemed_at != null ? redeemed_at.hashCode() : 0);
        return result;
    }
}
