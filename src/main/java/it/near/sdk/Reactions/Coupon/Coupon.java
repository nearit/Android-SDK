package it.near.sdk.Reactions.Coupon;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano.
 */
public class Coupon extends Resource implements Parcelable{
    @SerializedName("name")
    String name;
    @SerializedName("description")
    String description;
    @SerializedName("value")
    String value;
    @SerializedName("expires_at")
    String expires_at;
    @SerializedName("icon_id")
    String icon_id;
    @Relationship("claims")
    List<Claim> claims;

    public Coupon() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public String getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(String icon_id) {
        this.icon_id = icon_id;
    }

    public List<Claim> getClaims() {
        return claims;
    }

    public void setClaims(List<Claim> claims) {
        this.claims = claims;
    }

    public String getSerial(){
        return getClaims().get(0).getSerial_number();
    }

    public String getClaimedAt(){
        return getClaims().get(0).getClaimed_at();
    }

    public String getRedeemedAt(){
        return getClaims().get(0).getRedeemed_at();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(value);
        dest.writeString(expires_at);
        dest.writeString(icon_id);
        dest.writeList(claims);
    }

    public static final Creator<Coupon> CREATOR = new Creator<Coupon>() {
        @Override
        public Coupon createFromParcel(Parcel in) {
            return new Coupon(in);
        }

        @Override
        public Coupon[] newArray(int size) {
            return new Coupon[size];
        }
    };

    protected Coupon(Parcel in) {
        setId(in.readString());
        name = in.readString();
        description = in.readString();
        value = in.readString();
        expires_at = in.readString();
        icon_id = in.readString();
        claims = new ArrayList<Claim>();
        in.readList(claims, Claim.class.getClassLoader());
    }
}
