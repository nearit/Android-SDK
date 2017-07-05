package it.near.sdk.geopolis.trackings;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.geopolis.GeopolisManager;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackRequest;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_APP_ID;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_EVENT;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_IDENTIFIER;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_INSTALLATION_ID;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_PROFILE_ID;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_TRACKED_AT;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.KEY_TRACKINGS;
import static it.near.sdk.geopolis.trackings.GeopolisTrackingsManager.TRACKING_RES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeopolisTrackingsManagerTest {

    private static final Long DUMMY_LONG_TIMESTAMP = 100L;
    private static final String DUMMY_PROFILE_ID = "profile_id";
    private static final String DUMMY_APP_ID = "app_id";
    private static final String DUMMY_INSTALLATION_ID = "installation_id";

    private GeopolisTrackingsManager geopolisTrackingsManager;

    @Mock
    TrackManager mockTrackManager;
    @Mock
    GlobalConfig mockGlobalConfig;
    @Mock
    CurrentTime mockCurrentTime;

    @Before
    public void setUp() throws Exception {
        when(mockCurrentTime.currentTimestamp()).thenReturn(DUMMY_LONG_TIMESTAMP);
        when(mockGlobalConfig.getProfileId()).thenReturn(DUMMY_PROFILE_ID);
        when(mockGlobalConfig.getAppId()).thenReturn(DUMMY_APP_ID);
        when(mockGlobalConfig.getInstallationId()).thenReturn(DUMMY_INSTALLATION_ID);
        geopolisTrackingsManager = new GeopolisTrackingsManager(mockTrackManager, mockGlobalConfig, mockCurrentTime);
    }

    @Test
    public void whenTrackingIsSentAndAllDataIsAvailable_itGetsActuallySent() throws Exception {
        String dummyIdentifier = "identifier";
        String dummyEvent = "event";
        geopolisTrackingsManager.trackEvent(dummyIdentifier, dummyEvent);

        ArgumentCaptor<TrackRequest> argumentCaptor = ArgumentCaptor.forClass(TrackRequest.class);
        verify(mockTrackManager, atLeastOnce()).sendTracking(argumentCaptor.capture());
        TrackRequest sentRequest = argumentCaptor.getValue();
        String sentBody = sentRequest.getBody();
        JSONObject jsonBody = new JSONObject(sentBody).getJSONObject("data");
        assertThat((String)jsonBody.get("type"), is(KEY_TRACKINGS));
        assertThat(jsonBody = jsonBody.getJSONObject("attributes"), is(notNullValue()));
        assertThat((String) jsonBody.get(KEY_IDENTIFIER), is(dummyIdentifier));
        assertThat((String) jsonBody.get(KEY_EVENT), is(dummyEvent));
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        Date now = new Date(DUMMY_LONG_TIMESTAMP);
        String expectedFormattedDate = sdf.format(now);
        assertThat((String) jsonBody.get(KEY_TRACKED_AT), is(expectedFormattedDate));
        assertThat((String) jsonBody.get(KEY_PROFILE_ID), is(DUMMY_PROFILE_ID));
        assertThat((String) jsonBody.get(KEY_INSTALLATION_ID), is(DUMMY_INSTALLATION_ID));
        assertThat((String) jsonBody.get(KEY_APP_ID), is(DUMMY_APP_ID));

        assertThat(sentRequest.getUrl(), is(Constants.API.PLUGINS_ROOT + "/" +
                GeopolisManager.PLUGIN_NAME + "/" +
                TRACKING_RES));
    }

    @Test
    public void whenTrackingIsMissingIdOrEvent_nothingHappens() throws JSONException {
        geopolisTrackingsManager.trackEvent(null, "d");
        verify(mockTrackManager, never()).sendTracking(any(TrackRequest.class));

        geopolisTrackingsManager.trackEvent("d", null);
        verify(mockTrackManager, never()).sendTracking(any(TrackRequest.class));

        geopolisTrackingsManager.trackEvent(null, null);
        verify(mockTrackManager, never()).sendTracking(any(TrackRequest.class));
    }

    @Test(expected = JSONException.class)
    public void whenProfileIdIsMissing_throws() throws JSONException {
        when(mockGlobalConfig.getProfileId()).thenReturn(null);
        geopolisTrackingsManager.trackEvent("a", "b");
    }

    @Test(expected = JSONException.class)
    public void whenInstallationIdIsMissing_throws() throws JSONException {
        when(mockGlobalConfig.getInstallationId()).thenReturn(null);
        geopolisTrackingsManager.trackEvent("a", "b");
    }

    @Test(expected = JSONException.class)
    public void whenAppIdIsMissing_throws() throws JSONException {
        when(mockGlobalConfig.getAppId()).thenReturn(null);
        geopolisTrackingsManager.trackEvent("a", "b");
    }
}