package it.near.sdk.operation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NearItUserProfilerTest {

    private static final String SP_MAP_KEY = "NearItUserDataMap";

    private NearItUserProfiler nearItUserProfiler;

    @Mock
    private UserDataCacheManager mockUserDataCacheManager;
    @Mock
    private NearItUserDataAPI mockUserDataAPI;
    @Mock
    private UserDataTimer mockUserDataTimer;

    @Before
    public void setUP() {
        nearItUserProfiler = new NearItUserProfiler(mockUserDataCacheManager, mockUserDataAPI, mockUserDataTimer);

    }

    @Test
    public void testWithNoProfile() {
        String key = "a";
        String value = "b";
        verify(mockUserDataCacheManager, atLeastOnce()).saveUserDataToCache(key, value);
    }

    @Test
    public void testSingleDataPoint() {

    }

    @Test
    public void testSingleDataPointFailure() {

    }

    @Test
    public void testShouldSendDataPoints() {

    }

}
