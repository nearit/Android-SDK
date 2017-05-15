package it.near.sdk.reactions.coupon;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.reactions.content.Image;
import it.near.sdk.reactions.content.ImageSet;
import it.near.sdk.recipes.models.ReactionBundle;

/**
 * @author cattaneostefano.
 */
public class Coupon extends ReactionBundle implements Parcelable {
    private static final String COUPON_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("value")
    public String value;
    @SerializedName("expires_at")
    public String expires_at;
    @SerializedName("redeemable_from")
    public String redeemable_from;
    @SerializedName("icon_id")
    public String icon_id;
    @Relationship("claims")
    public List<Claim> claims;
    @Relationship("icon")
    public Image icon;

    private ImageSet iconSet;

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

    public String getExpiresAt() {
        return expires_at;
    }

    @Nullable
    public Date getExpiresAtDate() {
        return toDate(expires_at);
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public String getRedeemableFrom() {
        return redeemable_from;
    }

    @Nullable
    public Date getRedeemableFromDate() {
        return toDate(redeemable_from);
    }

    public void setRedeemable_from(String redeemable_from) {
        this.redeemable_from = redeemable_from;
    }

    public String getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(String icon_id) {
        this.icon_id = icon_id;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public List<Claim> getClaims() {
        return claims;
    }

    public void setClaims(List<Claim> claims) {
        this.claims = claims;
    }

    public String getSerial() {
        return getClaims().get(0).getSerial_number();
    }

    public String getClaimedAt() {
        return getClaims().get(0).getClaimed_at();
    }

    public Date getClaimedAtDate() {
        return toDate(getClaimedAt());
    }

    public String getRedeemedAt() {
        return getClaims().get(0).getRedeemed_at();
    }

    public Date getRedeemedAtDate() {
        return toDate(getRedeemedAt());
    }

    public ImageSet getIconSet() {
        return iconSet;
    }

    public void setIconSet(ImageSet iconSet) {
        this.iconSet = iconSet;
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
        dest.writeString(redeemable_from);
        dest.writeString(icon_id);
        dest.writeList(claims);
        dest.writeParcelable(iconSet, flags);
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
        redeemable_from = in.readString();
        icon_id = in.readString();
        claims = new ArrayList<Claim>();
        in.readList(claims, Claim.class.getClassLoader());
        iconSet = in.readParcelable(ImageSet.class.getClassLoader());
    }

    @Nullable
    public Date toDate(String toParse) {
        if (toParse == null) return null;
        DateFormat df1 = new SimpleDateFormat(COUPON_DATE_PATTERN, Locale.US);
        try {
            return df1.parse(toParse);
        } catch (ParseException e) {
            return null;
        }
    }
}
