package it.near.sdk.operation;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    @Mock
    private UserDataTimer.TimerListener mockTimeListener;
    @Mock
    private NearItUserDataAPI.UserDataSendListener mockSendListener;


    @Before
    public void setUP() {
        nearItUserProfiler = new NearItUserProfiler(mockUserDataCacheManager, mockUserDataAPI, mockUserDataTimer);

    }


}
