package it.near.sdk.utils.timestamp;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.auth.AuthenticationException;

import static it.near.sdk.utils.timestamp.NearTimestampChecker.GEOPOLIS;
import static it.near.sdk.utils.timestamp.NearTimestampChecker.RECIPE;
import static it.near.sdk.utils.timestamp.NearTimestampChecker.SyncCheckListener;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NearTimestampCheckerTest {

    private NearTimestampChecker nearTimestampChecker;

    @Mock
    private NearItTimeStampApi mockApi;
    @Mock
    private SyncCheckListener mockSyncCheckListener;

    @Before
    public void setUp() throws Exception {
        nearTimestampChecker = new NearTimestampChecker(mockApi);
    }

    @Test
    public void whenApiFail_cacheIsCold() throws Exception {
        mockNetworkRequestFail();
        nearTimestampChecker.checkRecipeTimeStamp(0L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(1)).syncNeeded();
        nearTimestampChecker.checkGeopolisTimeStamp(0L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(2)).syncNeeded();

        verify(mockSyncCheckListener, never()).syncNotNeeded();
    }

    @Test
    public void whenRemoteDataIsOlderThanCache_cacheIsHot() {
        mockNetworkRequestSuccess(
                buildTimestampsFor(0L,0L)
        );
        nearTimestampChecker.checkRecipeTimeStamp(1L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(1)).syncNotNeeded();
        nearTimestampChecker.checkRecipeTimeStamp(100000L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(2)).syncNotNeeded();

        nearTimestampChecker.checkGeopolisTimeStamp(1L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(3)).syncNotNeeded();
        nearTimestampChecker.checkGeopolisTimeStamp(10000L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(4)).syncNotNeeded();

        verify(mockSyncCheckListener, never()).syncNeeded();
    }

    @Test
    public void whenRemoteDataTimestampIsUnavailable_cacheIsCold() {
        CacheTimestamp geopolisCTS = new CacheTimestamp();
        geopolisCTS.what = GEOPOLIS;
        geopolisCTS.time = 0L;
        // no recipe timestamp info
        mockNetworkRequestSuccess(Lists.newArrayList(geopolisCTS));
        nearTimestampChecker.checkRecipeTimeStamp(0L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(1)).syncNeeded();
        nearTimestampChecker.checkRecipeTimeStamp(Long.MAX_VALUE, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(2)).syncNeeded();

        verify(mockSyncCheckListener, never()).syncNotNeeded();
    }

    @Test
    public void whenRemoteDataTimestampIsEmpty_cacheIsCold() {
        mockNetworkRequestSuccess(Collections.<CacheTimestamp>emptyList());

        nearTimestampChecker.checkRecipeTimeStamp(0L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(1)).syncNeeded();
        nearTimestampChecker.checkRecipeTimeStamp(Long.MAX_VALUE, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(2)).syncNeeded();
        nearTimestampChecker.checkGeopolisTimeStamp(0L, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(3)).syncNeeded();
        nearTimestampChecker.checkGeopolisTimeStamp(Long.MAX_VALUE, mockSyncCheckListener);
        verify(mockSyncCheckListener, times(4)).syncNeeded();

        verify(mockSyncCheckListener, never()).syncNotNeeded();
    }

    private List<CacheTimestamp> buildTimestampsFor(long recipeTimestamp, long geopolisTimestamp) {
        CacheTimestamp recipeCTS = new CacheTimestamp();
        recipeCTS.what = RECIPE;
        recipeCTS.time = recipeTimestamp;
        CacheTimestamp geopolisCTS = new CacheTimestamp();
        geopolisCTS.what = GEOPOLIS;
        geopolisCTS.time = geopolisTimestamp;
        return Lists.newArrayList(recipeCTS, geopolisCTS);
    }


    private void mockNetworkRequestFail() throws AuthenticationException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((NearItTimeStampApi.TimeStampListener) invocation.getArguments()[0]).onError("error");
                return null;
            }
        }).when(mockApi).fetchTimeStamps(any(NearItTimeStampApi.TimeStampListener.class));
    }

    private void mockNetworkRequestSuccess(final List<CacheTimestamp> timestamps) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((NearItTimeStampApi.TimeStampListener)invocation.getArguments()[0]).onSuccess(timestamps);
                return null;
            }
        }).when(mockApi).fetchTimeStamps(any(NearItTimeStampApi.TimeStampListener.class));
    }
}