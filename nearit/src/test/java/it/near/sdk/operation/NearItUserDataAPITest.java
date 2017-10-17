package it.near.sdk.operation;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.NearAsyncHttpClient;

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
    }

    @Test
    public void whenProfileIdIsNull_noSendData() {
        when(mockGlobalConfig.getProfileId()).thenReturn(null);
        final HashMap<String, String> userData = Maps.newHashMap();
        userData.put("name", "a");
        dataAPI.sendDataPoints(userData, mockSendListener);
        verifyZeroInteractions(mockHttpClient);
    }
}
