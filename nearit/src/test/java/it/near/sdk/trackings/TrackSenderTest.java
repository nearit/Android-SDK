package it.near.sdk.trackings;

import com.loopj.android.http.ResponseHandlerInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.NearAsyncHttpClient;

import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TrackSenderTest {

    @Mock
    NearAsyncHttpClient mockNearAsyncHttpClient;

    TrackSender trackSender;

    @Before
    public void setUp() {
        trackSender = new TrackSender(mockNearAsyncHttpClient);
    }

    @Test
    public void correctTracking_shouldBeSent() throws AuthenticationException {
        
        TrackSender.RequestListener requestListener = mock(TrackSender.RequestListener.class);
        TrackRequest dummy = new TrackRequest("dummy", "dummy2");
        trackSender.sendTrack(dummy, requestListener);
        verify(requestListener, times(1)).onSuccess();
    }

}