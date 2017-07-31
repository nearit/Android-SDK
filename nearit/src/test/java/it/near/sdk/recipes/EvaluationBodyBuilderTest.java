package it.near.sdk.recipes;

import com.google.common.collect.Maps;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import it.near.sdk.GlobalConfig;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_APP_ID;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_COOLDOWN;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_CORE;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_INSTALLATION_ID;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_LANG;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_LAST_NOTIFIED_AT;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_PROFILE_ID;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_RECIPES_NOTIFIED_AT;
import static it.near.sdk.recipes.EvaluationBodyBuilder.KEY_UTC_OFFSET;
import static it.near.sdk.recipes.EvaluationBodyBuilder.PULSE_ACTION_ID_KEY;
import static it.near.sdk.recipes.EvaluationBodyBuilder.PULSE_BUNDLE_ID_KEY;
import static it.near.sdk.recipes.EvaluationBodyBuilder.PULSE_PLUGIN_ID_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationBodyBuilderTest {

    private static final String MOCKED_APP_ID = "app_id";
    private static final String MOCKED_INSTALLATION_ID = "installation_id";
    private static final String MOCKED_PROFILE_ID = "profile_id";

    @Mock
    GlobalConfig mockGlobalConfig;
    @Mock
    RecipesHistory mockRecipeHistory;
    @Mock
    CurrentTime mockCurrentTime;

    private EvaluationBodyBuilder evaluationBodyBuilder;

    @Before
    public void setUp() {
        when(mockGlobalConfig.getAppId()).thenReturn(MOCKED_APP_ID);
        when(mockGlobalConfig.getInstallationId()).thenReturn(MOCKED_INSTALLATION_ID);
        when(mockGlobalConfig.getProfileId()).thenReturn(MOCKED_PROFILE_ID);
        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30")));
        Locale.setDefault(new Locale("es", "ES"));
        evaluationBodyBuilder = new EvaluationBodyBuilder(mockGlobalConfig, mockRecipeHistory, mockCurrentTime);
    }

    @Test
    public void buildCorrectEvaluationBody_noCooldownNoPulse() throws JSONException {
        String actual = evaluationBodyBuilder.buildEvaluateBody();
        JSONObject actualObj = new JSONObject(actual);
        assertThat(actualObj = actualObj.getJSONObject("data"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("attributes"), is(notNullValue()));
        assertThat(actualObj.has(PULSE_PLUGIN_ID_KEY), is(false));
        assertThat(actualObj.has(PULSE_ACTION_ID_KEY), is(false));
        assertThat(actualObj.has(PULSE_BUNDLE_ID_KEY), is(false));
        assertThat(actualObj = actualObj.getJSONObject(KEY_CORE), is(notNullValue()));
        assertThat((String) actualObj.get(KEY_PROFILE_ID), is(MOCKED_PROFILE_ID));
        assertThat((String) actualObj.get(KEY_INSTALLATION_ID), is(MOCKED_INSTALLATION_ID));
        assertThat((String) actualObj.get(KEY_APP_ID), is(MOCKED_APP_ID));
        assertThat((String) actualObj.get(KEY_UTC_OFFSET), is("+05:30"));
        assertThat((String) actualObj.get(KEY_LANG), is("es-ES"));
        assertThat(actualObj = actualObj.getJSONObject(KEY_COOLDOWN), is(notNullValue()));
        assertThat(Long.valueOf((Integer) actualObj.get(KEY_LAST_NOTIFIED_AT)), is(0L));
        assertThat(actualObj.getJSONObject(KEY_RECIPES_NOTIFIED_AT).length(), is(0));
    }

    @Test
    public void buildCorrectEvaluationBody_withPulse() throws JSONException {
        String actual = evaluationBodyBuilder.buildEvaluateBody("plugin", "action", "bundle");
        JSONObject actualObj = new JSONObject(actual);
        assertThat(actualObj = actualObj.getJSONObject("data"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("attributes"), is(notNullValue()));
        assertThat((String) actualObj.get(PULSE_PLUGIN_ID_KEY), is("plugin"));
        assertThat((String) actualObj.get(PULSE_ACTION_ID_KEY), is("action"));
        assertThat((String) actualObj.get(PULSE_BUNDLE_ID_KEY), is("bundle"));
    }

    @Test
    public void buildCorrectEvaluationBody_withCooldown() throws JSONException {
        Map<String, Long> logMap = Maps.newHashMap();
        logMap.put("recipe_id_1", 0L);
        logMap.put("recipe_id_2", 1000L);
        when(mockRecipeHistory.getRecipeLogMap()).thenReturn(logMap);
        String actual = evaluationBodyBuilder.buildEvaluateBody("plugin", "action", "bundle");
        JSONObject actualObj = new JSONObject(actual);
        assertThat(actualObj = actualObj.getJSONObject("data"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("attributes"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject(KEY_CORE), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject(KEY_COOLDOWN), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject(KEY_RECIPES_NOTIFIED_AT), is(notNullValue()));
        assertThat(actualObj.length(), is(2));
        assertThat(Long.valueOf((Integer) actualObj.get("recipe_id_1")), is(0L));
        assertThat(Long.valueOf((Integer) actualObj.get("recipe_id_2")), is(1000L));
    }

    @Test
    public void whenOperatingInVariuousTimezones_utcOffsetIsCorrectlyBuilt() throws JSONException {
        // TODO test the dst additional offset

        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30")));
        assertThat(
                extractUtcOffsetFrom(evaluationBodyBuilder.buildEvaluateBody()),
                is("+05:30")
        );
        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance(TimeZone.getTimeZone("GMT+14:00")));
        assertThat(
                extractUtcOffsetFrom(evaluationBodyBuilder.buildEvaluateBody()),
                is("+14:00")
        );
        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance(TimeZone.getTimeZone("GMT±00:00")));
        assertThat(
                extractUtcOffsetFrom(evaluationBodyBuilder.buildEvaluateBody()),
                is("+00:00")
        );
        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance(TimeZone.getTimeZone("GMT-3:30")));
        assertThat(
                extractUtcOffsetFrom(evaluationBodyBuilder.buildEvaluateBody()),
                is("-03:30")
        );
        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance(TimeZone.getTimeZone("GMT-12:00")));
        assertThat(
                extractUtcOffsetFrom(evaluationBodyBuilder.buildEvaluateBody()),
                is("-12:00")
        );
    }

    private String extractUtcOffsetFrom(String actual) throws JSONException {
        JSONObject actualObj = new JSONObject(actual);
        actualObj = actualObj.getJSONObject("data");
        actualObj = actualObj.getJSONObject("attributes");
        actualObj = actualObj.getJSONObject(KEY_CORE);
        return (String) actualObj.get(KEY_UTC_OFFSET);
    }

    @Test(expected = JSONException.class)
    public void whenMissingGlobalData_shouldThrow() throws JSONException {
        when(mockGlobalConfig.getAppId()).thenReturn(null);
        evaluationBodyBuilder.buildEvaluateBody("plugin", "action", "bundle");
    }
}
