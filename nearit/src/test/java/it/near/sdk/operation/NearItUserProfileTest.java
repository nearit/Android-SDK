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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author federico.boschini
 */

@RunWith(MockitoJUnitRunner.class)
public class NearItUserProfileTest {

    private NearItUserProfile nearItUserProfile;
    @Mock
    private GlobalConfig mockGlobalConfig;
    @Mock
    private NearAsyncHttpClient mockHttpClient;
    @Mock
    private UserDataBackOff mockUserDataBackOff;

    @Before
    public void setUp() {
        nearItUserProfile = new NearItUserProfile(mockGlobalConfig, mockHttpClient, mockUserDataBackOff);
    }

    @Test
    public void testSetUserData() {
        nearItUserProfile.setUserData("dummy", "dummy");
        verify(mockUserDataBackOff, times(1)).setUserData(eq("dummy"), eq("dummy"));
    }

    @Test
    public void testSetBatchUserData() {
        HashMap<String, String> batch = Maps.newHashMap();
        batch.put("dummy", "dummy");
        batch.put("dummy2", "dummy2");

        nearItUserProfile.setBatchUserData(batch);
        verify(mockUserDataBackOff, times(1)).setBatchUserData(eq(batch));
    }

}