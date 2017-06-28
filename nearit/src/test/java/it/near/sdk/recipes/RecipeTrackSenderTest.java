package it.near.sdk.recipes;

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
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackRequest;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.recipes.RecipeTrackSender.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipeTrackSenderTest {

    private static final String DUMMY_PROFILE_ID = "dummy_profile_id";
    private static final String DUMMY_INSTALLATION_ID = "dummy_installation_id";
    private static final String DUMMY_APP_ID = "dummy_app_id";
    public static final long DUMMY_LONG_TIMESTAMP = 100L;

    private RecipeTrackSender recipeTrackSender;

    @Mock
    private GlobalConfig mockGlobalConfig;
    @Mock
    private RecipesHistory mockRecipeHistory;
    @Mock
    private TrackManager mockTrackManager;
    @Mock
    private CurrentTime mockCurrentTime;

    @Before
    public void setUp() throws Exception {
        when(mockCurrentTime.currentTimestamp()).thenReturn(DUMMY_LONG_TIMESTAMP);
        when(mockGlobalConfig.getProfileId()).thenReturn(DUMMY_PROFILE_ID);
        when(mockGlobalConfig.getInstallationId()).thenReturn(DUMMY_INSTALLATION_ID);
        when(mockGlobalConfig.getAppId()).thenReturn(DUMMY_APP_ID);
        recipeTrackSender = new RecipeTrackSender(mockGlobalConfig, mockRecipeHistory, mockTrackManager, mockCurrentTime);
    }

    @Test
    public void whenNotifiedTrackingIsSent_recipeIsMarkedAsShownAndTrackingIsSent() throws JSONException {
        String dummyRecipeId = "dummy_recipe_id";
        recipeTrackSender.sendTracking(dummyRecipeId, Recipe.NOTIFIED_STATUS);

        verify(mockRecipeHistory, atLeastOnce()).markRecipeAsShown(dummyRecipeId);

        ArgumentCaptor<TrackRequest> argumentCaptor = ArgumentCaptor.forClass(TrackRequest.class);
        verify(mockTrackManager, atLeastOnce()).sendTracking(argumentCaptor.capture());
        TrackRequest sentRequest = argumentCaptor.getValue();
        String sentBody = sentRequest.getBody();
        JSONObject jsonBody = new JSONObject(sentBody).getJSONObject("data");
        assertThat((String)jsonBody.get("type"), is(TRACKINGS_TYPE));
        assertThat(jsonBody = jsonBody.getJSONObject("attributes"), is(notNullValue()));
        assertThat((String) jsonBody.get(TRACKING_PROFILE_ID), is(DUMMY_PROFILE_ID));
        assertThat((String) jsonBody.get(TRACKING_INSTALLATION_ID), is(DUMMY_INSTALLATION_ID));
        assertThat((String) jsonBody.get(TRACKING_APP_ID), is(DUMMY_APP_ID));
        assertThat((String) jsonBody.get(TRACKING_RECIPE_ID), is(dummyRecipeId));
        assertThat((String) jsonBody.get(TRACKING_EVENT), is(Recipe.NOTIFIED_STATUS));
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        Date now = new Date(DUMMY_LONG_TIMESTAMP);
        String expectedFormattedDate = sdf.format(now);
        assertThat((String) jsonBody.get(TRACKING_TRACKED_AT), is(expectedFormattedDate));
        assertThat(sentRequest.getUrl(), is(TRACKINGS_PATH));
    }

    @Test
    public void whenEngagedTrackingIsSent_recipeIsNotMarkedButIsSent() throws JSONException {
        String dummyRecipeId = "dummy_recipe_id";
        recipeTrackSender.sendTracking(dummyRecipeId, Recipe.ENGAGED_STATUS);

        verify(mockRecipeHistory, never()).markRecipeAsShown(dummyRecipeId);

        ArgumentCaptor<TrackRequest> argumentCaptor = ArgumentCaptor.forClass(TrackRequest.class);
        verify(mockTrackManager, atLeastOnce()).sendTracking(argumentCaptor.capture());
        TrackRequest sentRequest = argumentCaptor.getValue();
        String sentBody = sentRequest.getBody();
        JSONObject jsonBody = new JSONObject(sentBody).getJSONObject("data");
        assertThat((String)jsonBody.get("type"), is(TRACKINGS_TYPE));
        assertThat(jsonBody = jsonBody.getJSONObject("attributes"), is(notNullValue()));
        assertThat((String) jsonBody.get(TRACKING_PROFILE_ID), is(DUMMY_PROFILE_ID));
        assertThat((String) jsonBody.get(TRACKING_INSTALLATION_ID), is(DUMMY_INSTALLATION_ID));
        assertThat((String) jsonBody.get(TRACKING_APP_ID), is(DUMMY_APP_ID));
        assertThat((String) jsonBody.get(TRACKING_RECIPE_ID), is(dummyRecipeId));
        assertThat((String) jsonBody.get(TRACKING_EVENT), is(Recipe.ENGAGED_STATUS));
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        Date now = new Date(DUMMY_LONG_TIMESTAMP);
        String expectedFormattedDate = sdf.format(now);
        assertThat((String) jsonBody.get(TRACKING_TRACKED_AT), is(expectedFormattedDate));
        assertThat(sentRequest.getUrl(), is(TRACKINGS_PATH));
    }

    @Test
    public void whenTrackingIsMissingRecipeOrStatus_nothingHappens() throws JSONException {
        recipeTrackSender.sendTracking("r", null);
        verify(mockRecipeHistory, never()).markRecipeAsShown(anyString());
        verify(mockTrackManager, never()).sendTracking(any(TrackRequest.class));

        recipeTrackSender.sendTracking(null, "r");
        verify(mockRecipeHistory, never()).markRecipeAsShown(anyString());
        verify(mockTrackManager, never()).sendTracking(any(TrackRequest.class));

        recipeTrackSender.sendTracking(null, null);
        verify(mockRecipeHistory, never()).markRecipeAsShown(anyString());
        verify(mockTrackManager, never()).sendTracking(any(TrackRequest.class));
    }

    @Test(expected = JSONException.class)
    public void whenProfileIdIsMissing_throws() throws JSONException {
        when(mockGlobalConfig.getProfileId()).thenReturn(null);
        recipeTrackSender.sendTracking("a", "b");
    }

    @Test(expected = JSONException.class)
    public void whenInstallationIdIsMissing_throws() throws JSONException {
        when(mockGlobalConfig.getInstallationId()).thenReturn(null);
        recipeTrackSender.sendTracking("a", "b");
    }

    @Test(expected = JSONException.class)
    public void whenAppIdIsMissing_throws() throws JSONException {
        when(mockGlobalConfig.getAppId()).thenReturn(null);
        recipeTrackSender.sendTracking("a", "b");
    }
}