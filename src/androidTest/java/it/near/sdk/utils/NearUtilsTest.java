package it.near.sdk.utils;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import static junit.framework.Assert.*;

/**
 * Created by cattaneostefano on 01/03/2017.
 */

@RunWith(AndroidJUnit4.class)
public class NearUtilsTest {

    @Test
    public void testFetchAppIdFrom() {
        String encoded = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aGlzaXNub3RhdGFzZWNyZXRJc3dlYXJvbm15bGlmZSIsImlhdCI6MTQ4MDYxMTA2NSwiZXhwIjoxNjA2ODY3MTk5LCJkYXRhIjp7ImFjY291bnQiOnsiaWQiOiI2YTU2MTg0Yy0zZTliLTQ2MWQtODU4OS04ZDUxNDZmYWUyMzgiLCJyb2xlX2tleSI6ImFwcCJ9fX0.Ne4wDy0kwVe4jkNQ_PR9QskFo4kOgXfqlOsRQTLTJ1o";
        String expectedAppId = "6a56184c-3e9b-461d-8589-8d5146fae238";
        String actualAppId = NearUtils.fetchAppIdFrom(encoded);
        assertEquals(expectedAppId, actualAppId);
    }
}
