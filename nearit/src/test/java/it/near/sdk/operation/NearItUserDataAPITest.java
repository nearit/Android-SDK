package it.near.sdk.operation;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.TestUtils;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.logging.NearLog;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class NearItUserDataAPITest {

    private NearItUserDataAPI dataAPI;

    @Mock
    private GlobalConfig mockGlobalConfig;
    @Mock
    private NearAsyncHttpClient mockHttpClient;
    @Mock
    private NearItUserDataAPI.UserDataSendListener mockSendListener;

    @Before
    public void setUp() {
        dataAPI = new NearItUserDataAPI(mockGlobalConfig, mockHttpClient);
        NearLog.setLogger(TestUtils.emptyLogger());
    }

    @Test
    public void whenUserOptedOut_noSend() {
        when(mockGlobalConfig.getOptOut()).thenReturn(true);
        final HashMap<String, String> userData = Maps.newHashMap();
        userData.put("name", "a");
        dataAPI.sendDataPoints(userData, mockSendListener);
        verifyZeroInteractions(mockHttpClient);
        verify(mockSendListener, times(1)).onSendingFailure();
    }

    @Test
    public void whenEmptyData_noSend() {
        final HashMap<String, String> userData = Maps.newHashMap();
        dataAPI.sendDataPoints(userData, mockSendListener);
        verifyZeroInteractions(mockHttpClient);
        verify(mockSendListener, times(1)).onSendingFailure();
    }

    @Test
    public void whenProfileIdIsNull_noSend() {
        when(mockGlobalConfig.getProfileId()).thenReturn(null);
        final HashMap<String, String> userData = Maps.newHashMap();
        userData.put("name", "a");
        dataAPI.sendDataPoints(userData, mockSendListener);
        verifyZeroInteractions(mockHttpClient);
        verify(mockSendListener, times(1)).onSendingFailure();
    }

}
