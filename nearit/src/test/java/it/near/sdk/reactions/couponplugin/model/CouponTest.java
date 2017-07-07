package it.near.sdk.reactions.couponplugin.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.near.sdk.reactions.contentplugin.model.Image;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class CouponTest {

    private Coupon coupon;

    @Before
    public void setUp() {
        coupon = new Coupon();
    }

    @Test
    public void shouldNotHaveContentToInclude() {
        assertThat(coupon.hasContentToInclude(), is(false));
    }

    @Test
    public void couponWithIcon_shouldHaveContentToInclude() {
        coupon.icon = new Image();
        assertThat(coupon.hasContentToInclude(), is(true));
    }
}