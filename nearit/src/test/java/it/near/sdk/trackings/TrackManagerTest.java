package it.near.sdk.trackings;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import it.near.sdk.utils.ApplicationVisibility;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TrackManagerTest {

    private static final int MADE_UP_FAIL_STATUS_CODE = 6;
    private static final boolean SUCCESS = true;
    private static final boolean FAIL = false;

    @Mock
    ConnectivityManager mockConnectivityManager;
    @Mock
    NetworkInfo mockNetworkInfo;
    @Mock
    TrackSender mockTrackSender;
    @Mock
    TrackCache mockTrackCache;
    @Mock
    ApplicationVisibility mockApplicationVisibility;

    TrackManager trackManager;

    @Before
    public void setUp() {
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        trackManager = new TrackManager(mockConnectivityManager, mockTrackSender, mockTrackCache, mockApplicationVisibility);
    }

    @Test
    public void whenTrackingIsAddedWhileOnline_itShouldBeSentAndNotCached() {
        mockOnlineStatus();
        TrackRequest dummy = new TrackRequest("a", "b");
        when(mockTrackCache.getRequests()).thenReturn(Lists.newArrayList(dummy));
        mockAnyTrackSentResult(SUCCESS);
        trackManager.sendTracking(dummy);
        verify(mockTrackCache, times(1)).addToCache(dummy);
        verify(mockTrackSender, times(1)).sendTrack(eq(dummy), any(TrackSender.RequestListener.class));
        verify(mockTrackCache, times(1)).removeFromCache(dummy);
        assertThat(dummy.sending, is(false));
        trackManager.sendTracking(dummy);
        verify(mockTrackCache, times(2)).addToCache(dummy);
        verify(mockTrackSender, times(2)).sendTrack(eq(dummy), any(TrackSender.RequestListener.class));
        verify(mockTrackCache, times(2)).removeFromCache(dummy);
        assertThat(dummy.sending, is(false));
    }

    @Test
    public void whenTrackingIsAddedWhileOffline_itShouldBeCachedAndNotSent() {
        mockOfflineStatus();
        TrackRequest dummy = new TrackRequest("a", "b");
        trackManager.sendTracking(dummy);
        verify(mockTrackCache, times(1)).addToCache(dummy);
        verify(mockTrackSender, never()).sendTrack(eq(dummy), any(TrackSender.RequestListener.class));
        verify(mockTrackCache, never()).removeFromCache(dummy);
        assertThat(dummy.sending, is(false));
        trackManager.sendTracking(dummy);
        verify(mockTrackCache, times(2)).addToCache(dummy);
        verify(mockTrackSender, never()).sendTrack(eq(dummy), any(TrackSender.RequestListener.class));
        verify(mockTrackCache, never()).removeFromCache(dummy);
        assertThat(dummy.sending, is(false));
    }

    @Test
    public void whenTrackingFailsWhileOnline_itShouldRemainInCache() {
        mockOnlineStatus();
        TrackRequest dummy = new TrackRequest("a", "b");
        mockTrackSentResult(dummy, FAIL);
        when(mockTrackCache.getRequests()).thenReturn(Lists.newArrayList(dummy));
        trackManager.sendTracking(dummy);
        verify(mockTrackCache, times(1)).addToCache(dummy);
        verify(mockTrackSender, times(1)).sendTrack(eq(dummy), any(TrackSender.RequestListener.class));
        verify(mockTrackCache, never()).removeFromCache(dummy);
        assertThat(dummy.sending, is(false));
    }

    @Test
    public void whenSomeTrackingFailsWhileOnline_failingTracksShouldRemainInCache() {
        mockOnlineStatus();
        TrackRequest dummy = new TrackRequest("a", "b");
        TrackRequest dummy2 = new TrackRequest("c", "d");
        mockTrackSentResult(dummy, SUCCESS);
        mockTrackSentResult(dummy2, FAIL);
        when(mockTrackCache.getRequests()).thenReturn(Lists.newArrayList(dummy, dummy2));
        trackManager.sendTracking(dummy);
        trackManager.sendTracking(dummy2);
        verify(mockTrackCache, atLeastOnce()).removeFromCache(dummy);
        verify(mockTrackCache, never()).removeFromCache(dummy2);
        assertThat(dummy2.sending, is(false));
    }

    @Test
    public void whenAppGoesToForeground_tryToSendCachedResults() {
        mockOnlineStatus();
        TrackRequest dummy = new TrackRequest("a", "b");
        when(mockTrackCache.getRequests()).thenReturn(Lists.newArrayList(dummy));
        trackManager.onAppGotoForeground();
        verify(mockTrackSender, atLeastOnce()).sendTrack(eq(dummy), any(TrackSender.RequestListener.class));
    }

    private void mockTrackSentResult(TrackRequest trackRequest, final boolean result) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (result == SUCCESS) {
                    ((TrackSender.RequestListener) invocation.getArguments()[1]).onSuccess();
                } else {
                    ((TrackSender.RequestListener) invocation.getArguments()[1]).onFailure(MADE_UP_FAIL_STATUS_CODE);
                }
                return null;
            }
        }).when(mockTrackSender).sendTrack(eq(trackRequest), any(TrackSender.RequestListener.class));
    }

    private void mockAnyTrackSentResult(final boolean result) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (result == SUCCESS) {
                    ((TrackSender.RequestListener) invocation.getArguments()[1]).onSuccess();
                } else {
                    ((TrackSender.RequestListener) invocation.getArguments()[1]).onFailure(MADE_UP_FAIL_STATUS_CODE);
                }
                return null;
            }
        }).when(mockTrackSender).sendTrack(any(TrackRequest.class), any(TrackSender.RequestListener.class));
    }

    private void mockOnlineStatus() {
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(true);
    }

    private void mockOfflineStatus() {
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(false);
    }
}