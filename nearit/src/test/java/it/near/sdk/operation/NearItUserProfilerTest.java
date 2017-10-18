package it.near.sdk.operation;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NearItUserProfilerTest {

    private static final String SP_MAP_KEY = "NearItUserDataMap";

    @Mock
    private UserDataCacheManager mockUserDataCacheManager;
    @Mock
    private NearItUserDataAPI mockUserDataAPI;
    @Mock
    private UserDataTimer mockUserDataTimer;
    @Mock
    private NearItUserDataAPI.UserDataSendListener mockSendListener;

    private NearItUserProfiler nearItUserProfiler;

    @Before
    public void setUP() {
        nearItUserProfiler = new NearItUserProfiler(mockUserDataCacheManager, mockUserDataAPI, mockUserDataTimer);
    }

    @Test
    public void testShouldSendData() {
        nearItUserProfiler.setUserData("dummy", "dummy");
        verify(mockUserDataTimer, times(1)).start(any(UserDataTimer.TimerListener.class));
        verify(mockUserDataCacheManager, times(1)).setUserData("dummy", "dummy");

        nearItUserProfiler.setUserData("dummy2", "dummy2");
        ArgumentCaptor<UserDataTimer.TimerListener> captor = ArgumentCaptor.forClass(UserDataTimer.TimerListener.class);
        verify(mockUserDataTimer, times(2)).start(captor.capture());
        verify(mockUserDataCacheManager, times(1)).setUserData("dummy2", "dummy2");
        verify(mockUserDataCacheManager, times(2)).setUserData(anyString(), anyString());

        captor.getValue().sendNow();

//        verify(mockUserDataAPI, times(1)).sendDataPoints(ArgumentMatchers.<HashMap<String, String>>any(), any(NearItUserDataAPI.UserDataSendListener.class));

       /* HashMap<String, String> cachedData = Maps.newHashMap();
        cachedData.put("dummy", "dummy");
        verify(mockUserDataCacheManager, times(1)).setUserData(eq("dummy"), eq("dummy"));
        when(mockUserDataCacheManager.getUserData()).thenReturn(cachedData);
        when(mockUserDataCacheManager.hasData()).thenReturn(true);
        verify(mockUserDataTimer, times(1)).start(nearItUserProfiler);
        assertTrue(mockUserDataCacheManager.hasData());
        verify(mockUserDataAPI, times(1)).sendDataPoints(eq(cachedData), any(NearItUserDataAPI.UserDataSendListener.class));
    */
    }

    @Test
    public void testSendData() {
        HashMap<String, String> cachedData = Maps.newHashMap();
        cachedData.put("dummy", "dummy");
        when(mockUserDataCacheManager.getUserData()).thenReturn(cachedData);
        nearItUserProfiler.sendDataPoints();
        verify(mockUserDataCacheManager, times(1)).getUserData();
        verify(mockUserDataAPI, times(1)).sendDataPoints(eq(cachedData), any(NearItUserDataAPI.UserDataSendListener.class));
    }

    @Test
    public void testSendFailure_shouldNotRemoveFromCache() {
        when(mockUserDataCacheManager.hasData()).thenReturn(true);
        HashMap<String, String> toBeSent = Maps.newHashMap();
        toBeSent.put("dummy", "dummy");
        when(mockUserDataCacheManager.getUserData()).thenReturn(toBeSent);
        mockSendFailure();

        nearItUserProfiler.sendDataPoints();

        verify(mockUserDataCacheManager, times(1)).getUserData();
        verify(mockUserDataAPI, times(1)).sendDataPoints(eq(toBeSent), any(NearItUserDataAPI.UserDataSendListener.class));
        verify(mockUserDataCacheManager, never()).removeSentData(ArgumentMatchers.<HashMap<String, String>>any());
    }

    @Test
    public void testSendSuccess_shouldRemoveFromCache() {
        when(mockUserDataCacheManager.hasData()).thenReturn(true);
        HashMap<String, String> toBeSent = Maps.newHashMap();
        toBeSent.put("dummy", "dummy");
        when(mockUserDataCacheManager.getUserData()).thenReturn(toBeSent);
        mockSendSuccess();

        nearItUserProfiler.sendDataPoints();

        verify(mockUserDataCacheManager, times(1)).getUserData();
        verify(mockUserDataAPI).sendDataPoints(eq(toBeSent), any(NearItUserDataAPI.UserDataSendListener.class));
        verify(mockUserDataCacheManager, times(1)).removeSentData(eq(toBeSent));
    }

    @Test
    public void testIfBusy_DoNotSend() {
        when(mockUserDataCacheManager.hasData()).thenReturn(true);
        HashMap<String, String> toBeSent = Maps.newHashMap();
        toBeSent.put("dummy", "dummy");
        when(mockUserDataCacheManager.getUserData()).thenReturn(toBeSent);

        nearItUserProfiler.setUserData("ehi", "ehi");
        verify(mockUserDataTimer).start(any(UserDataTimer.TimerListener.class));
        verify(mockUserDataCacheManager, times(1)).setUserData(eq("ehi"), eq("ehi"));


    }



    @Test
    public void testClearData() {
        nearItUserProfiler.clearUserData();
        verify(mockUserDataCacheManager, times(1)).removeAllData();
    }

    private void mockSendFailure() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((NearItUserDataAPI.UserDataSendListener) invocation.getArguments()[1]).onSendingFailure();
                return null;
            }
        }).when(mockUserDataAPI).sendDataPoints(ArgumentMatchers.<HashMap<String, String>>any(), any(NearItUserDataAPI.UserDataSendListener.class));
    }

    private void mockSendSuccess() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((NearItUserDataAPI.UserDataSendListener) invocation.getArguments()[1]).onSendingSuccess(
                        (HashMap<String, String>) invocation.getArguments()[0]
                );
                return null;
            }
        }).when(mockUserDataAPI).sendDataPoints(ArgumentMatchers.<HashMap<String, String>>any(), any(NearItUserDataAPI.UserDataSendListener.class));
    }

}
