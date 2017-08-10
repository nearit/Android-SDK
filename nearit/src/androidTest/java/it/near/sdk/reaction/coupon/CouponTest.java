package it.near.sdk.reaction.coupon;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.couponplugin.model.Claim;
import it.near.sdk.reactions.couponplugin.model.Coupon;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CouponTest {

    @Test
    public void couponIsParcelable() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon_id");
        coupon.name = "coupon_name";
        coupon.description = "coupon_description";
        coupon.value = "coupon_value";
        coupon.expires_at = "expiring_soon";
        coupon.redeemable_from = "redeemable_from";
        coupon.setIcon_id("coupon_icon_id");
        coupon.claims = Lists.newArrayList(new Claim(), new Claim());
        coupon.setIconSet(new ImageSet());
        coupon.notificationMessage = "fjnef";

        Parcel parcel = Parcel.obtain();
        coupon.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Coupon actual = Coupon.CREATOR.createFromParcel(parcel);

        assertEquals(coupon.getId(), actual.getId());
        assertEquals(coupon.name, actual.name);
        assertEquals(coupon.description, actual.description);
        assertEquals(coupon.value, actual.value);
        assertEquals(coupon.expires_at, actual.expires_at);
        assertEquals(coupon.redeemable_from, actual.redeemable_from);
        assertEquals(coupon.getIcon_id(), actual.getIcon_id());
        assertThat(coupon.claims, containsInAnyOrder(actual.claims.toArray()));
        assertThat(coupon.notificationMessage, is(actual.notificationMessage));
        assertEquals(coupon.getIconSet(), actual.getIconSet());

    }

}
