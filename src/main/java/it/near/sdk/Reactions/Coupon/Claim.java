package it.near.sdk.Reactions.Coupon;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano.
 */
public class Claim extends Resource implements Parcelable {
    @SerializedName("serial_number")
    String serial_number;
    @SerializedName("claimed_at")
    String claimed_at;
    @SerializedName("redeemed_at")
    String redeemed_at;

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
}
