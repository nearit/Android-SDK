package it.near.sdk.reaction.coupon;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.near.sdk.reactions.content.ImageSet;
import it.near.sdk.reactions.coupon.Claim;
import it.near.sdk.reactions.coupon.Coupon;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Created by cattaneostefano on 28/02/2017.
 */

@RunWith(AndroidJUnit4.class)
public class CouponTest {

    @Test
    public void couponIsParcelable() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon_id");
        coupon.setName("coupon_name");
        coupon.setDescription("coupon_description");
        coupon.setValue("coupon_value");
        coupon.setExpires_at("expiring_soon");
        coupon.setIcon_id("coupon_icon_id");
        coupon.setClaims(Lists.newArrayList(new Claim(), new Claim()));
        coupon.setIconSet(new ImageSet());

        Parcel parcel = Parcel.obtain();
        coupon.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Coupon actual = Coupon.CREATOR.createFromParcel(parcel);

        assertEquals(coupon.getId(), actual.getId());
        assertEquals(coupon.getName(), actual.getName());
        assertEquals(coupon.getDescription(), actual.getDescription());
        assertEquals(coupon.getValue(), actual.getValue());
        assertEquals(coupon.getExpires_at(), actual.getExpires_at());
        assertEquals(coupon.getIcon_id(), actual.getIcon_id());
        assertThat(coupon.getClaims(), containsInAnyOrder(actual.getClaims().toArray()));
        assertEquals(coupon.getIconSet(), actual.getIconSet());

    }

}
