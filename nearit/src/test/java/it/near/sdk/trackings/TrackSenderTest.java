package it.near.sdk.trackings;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class TrackSenderTest {

    @Mock
    NearAsyncHttpClient httpClient;

    private TrackSender trackSender;

    @Before
    public void setUp() throws Exception {
        trackSender = new TrackSender(httpClient);
    }

    @Test
    public void whenRequestIsSuccessful_itNotifiesTheListener() throws Exception {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((NearJsonHttpResponseHandler) invocation.getArguments()[2]).onSuccess(200, null, new JSONObject());
                return null;
            }
        }).when(httpClient.nearPost(anyString(), anyString(), any(NearJsonHttpResponseHandler.class)));

        trackSender.sendTrack(any(TrackRequest.class), any(N));
    }
}