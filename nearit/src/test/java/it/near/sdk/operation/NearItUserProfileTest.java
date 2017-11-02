package it.near.sdk.operation;

import android.content.Context;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.NearAsyncHttpClient;

import static it.near.sdk.operation.NearItUserProfile.OPTED_OUT_PROFILE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


/**
 * @author federico.boschini
 */

@RunWith(MockitoJUnitRunner.class)
public class NearItUserProfileTest {

    public static final String dummyProfileId = "dummyProfile123";
    private NearItUserProfile nearItUserProfile;
    @Mock
    private GlobalConfig mockGlobalConfig;
    @Mock
    private NearItUserProfileAPI mockUserProfileAPI;
    @Mock
    private UserDataBackOff mockUserDataBackOff;
    @Mock
    private Context mockContext;

    @Mock
    private ProfileCreationListener mockProfileCreationListener;
    @Mock
    private ProfileIdUpdateListener mockProfileIdUpdateListener;
    @Mock
    private ProfileDataUpdateListener mockProfileDataUpdateListener;
    @Mock
    private NearItUserProfile.ProfileFetchListener mockProfileFetchListener;

    @Before
    public void setUp() {
        nearItUserProfile = new NearItUserProfile(mockGlobalConfig, mockUserDataBackOff, mockUserProfileAPI);
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
        mockSetDataUpdateListener();

        nearItUserProfile.setProfileDataUpdateListener(mockProfileDataUpdateListener);
        nearItUserProfile.setBatchUserData(batch);
        verify(mockUserDataBackOff, times(1)).setBatchUserData(eq(batch));
        verify(mockUserDataBackOff, times(1)).setProfileDataUpdateListener(eq(mockProfileDataUpdateListener));
        verify(mockProfileDataUpdateListener, times(1)).onProfileDataUpdated();
    }

    @Test
    public void testGetProfileId_deprecated() {
        nearItUserProfile.getProfileId();
        verify(mockGlobalConfig, times(1)).getProfileId();
    }

    @Test
    public void testGetProfileId_withListener() {
        nearItUserProfile.getProfileId(mockContext, mockProfileFetchListener);
        verify(mockUserProfileAPI, times(1)).getProfileId(eq(mockProfileFetchListener));
    }

    @Test
    public void testSetProfileId() {
        nearItUserProfile.setProfileIdUpdateListener(mockProfileIdUpdateListener);
        nearItUserProfile.setProfileId(dummyProfileId);
        verify(mockGlobalConfig, times(1)).setProfileId(eq(dummyProfileId));
        verify(mockProfileIdUpdateListener, times(1)).onProfileIdUpdated();
    }

    @Test
    public void testCreateNewProfile() {
        nearItUserProfile.createNewProfile(ArgumentMatchers.<Context>any(), mockProfileCreationListener);
        verify(mockUserProfileAPI, times(1)).createNewProfile(eq(mockProfileCreationListener));
    }

    @Test
    public void testIfOptedOut_shouldNotSetUserData() {
        mockOptOut();
        nearItUserProfile.setUserData("dummy", "dummy");
        verifyZeroInteractions(mockUserDataBackOff);
    }

    @Test
    public void testIfOptedOut_shouldReturnDummyProfileID() {
        mockOptOut();
        nearItUserProfile.getProfileId(mockContext, mockProfileFetchListener);
        verify(mockProfileFetchListener, times(1)).onProfileId(eq(OPTED_OUT_PROFILE_ID));
    }

    private void mockSetDataUpdateListener() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ProfileDataUpdateListener) invocation.getArguments()[0]).onProfileDataUpdated();
                return null;
            }
        }).when(mockUserDataBackOff).setProfileDataUpdateListener(ArgumentMatchers.<ProfileDataUpdateListener>any());
    }

    private void mockOptOut() {
        when(mockGlobalConfig.getOptOut()).thenReturn(true);
    }

}