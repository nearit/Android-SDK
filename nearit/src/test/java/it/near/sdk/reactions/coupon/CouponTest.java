package it.near.sdk.reactions.coupon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class CouponTest {

    private Coupon coupon;

    @Before
    public void setUp() {
        coupon = new Coupon();
    }

    @Test
    public void testDateParser() {
        String dateString = "2017-02-15T23:59:59.999Z";
        Date actual = coupon.toDate(dateString);
        long expected = 1487203199999L;
        assert actual != null;
        assertThat(actual.getTime(), is(expected));
    }

    @Test
    public void testDateParserWithWrongFormatString() {
        String incorrectFormat = "2017-02-15T23:59:59Z";
        Date actual = coupon.toDate(incorrectFormat);
        assertNull(actual);
    }

    @Test
    public void testDateParserWithNullValue() {
        Date actual = coupon.toDate(null);
        assertNull(actual);
    }

}