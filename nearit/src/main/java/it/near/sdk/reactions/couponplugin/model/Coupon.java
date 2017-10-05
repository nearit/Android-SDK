package it.near.sdk.reactions.couponplugin.model;

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
import java.util.TimeZone;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.recipes.models.ReactionBundle;

/**
 * @author cattaneostefano.
 */
public class Coupon extends ReactionBundle implements Parcelable {
    private static final String COUPON_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * @deprecated use {@link #getTitle()}
     */
    @Deprecated
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("value")
    public String value;
    @Nullable
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

    @Nullable
    public String getTitle() {
        return name;
    }

    @Nullable
    public Date getExpiresAtDate() {
        return toDate(expires_at);
    }

    @Nullable
    public Date getRedeemableFromDate() {
        return toDate(redeemable_from);
    }

    @Nullable
    public String getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(String icon_id) {
        this.icon_id = icon_id;
    }

    @Nullable
    public String getSerial() {
        if (anyClaim()) {
            return claims.get(0).serial_number;
        }
        return null;
    }

    public boolean anyClaim() {
        return claims.size() > 0;
    }

    @Nullable
    public String getClaimedAt() {
        if (anyClaim()) {
            return claims.get(0).claimed_at;
        }
        return null;
    }

    @Nullable
    public Date getClaimedAtDate() {
        if (anyClaim()) {
            return toDate(getClaimedAt());
        }
        return null;
    }

    @Nullable
    public String getRedeemedAt() {
        if (anyClaim()) {
            return claims.get(0).redeemed_at;
        }
        return null;
    }

    @Nullable
    public Date getRedeemedAtDate() {
        if (anyClaim()) {
            return toDate(getRedeemedAt());
        }
        return null;
    }

    public String getRecipeId() {
        return claims.get(0).recipe_id;
    }

    @Nullable
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
        super.writeToParcel(dest, flags);
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
        super(in);
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
        df1.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df1.parse(toParse);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public boolean hasContentToInclude() {
        return icon != null;
    }
}
