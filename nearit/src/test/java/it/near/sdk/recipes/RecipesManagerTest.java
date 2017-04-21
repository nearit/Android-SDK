package it.near.sdk.recipes;

import com.google.common.collect.Maps;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import it.near.sdk.GlobalConfig;

import static it.near.sdk.recipes.RecipesManager.PULSE_ACTION_ID_KEY;
import static it.near.sdk.recipes.RecipesManager.PULSE_BUNDLE_ID_KEY;
import static it.near.sdk.recipes.RecipesManager.PULSE_PLUGIN_ID_KEY;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

/**
 * Created by cattaneostefano on 01/03/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecipesManagerTest {

    @Mock
    GlobalConfig mockGlobalConfig;
    @Mock
    RecipeCooler mockRecipeCooler;

    @Before
    public void setUp() {
        when(mockGlobalConfig.getAppId()).thenReturn("app_id");
        when(mockGlobalConfig.getInstallationId()).thenReturn("installation_id");
        when(mockGlobalConfig.getProfileId()).thenReturn("profile_id");
    }

    @Test
    public void failTest() {
        fail();
    }

    /*@Test
    public void buildCorrectEvaluationBody_noCoolerNoPulse() throws JSONException {
        String actual = RecipesManager.buildEvaluateBody(mockGlobalConfig, null, null, null, null);
        JSONObject actualObj = new JSONObject(actual);
        assertThat(actualObj = actualObj.getJSONObject("data"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("attributes"), is(notNullValue()));
        assertThat(actualObj.has(PULSE_PLUGIN_ID_KEY), is(false));
        assertThat(actualObj.has(PULSE_ACTION_ID_KEY), is(false));
        assertThat(actualObj.has(PULSE_BUNDLE_ID_KEY), is(false));
        assertThat(actualObj = actualObj.getJSONObject("core"), is(notNullValue()));
        assertThat((String) actualObj.get("profile_id"), is("profile_id"));
        assertThat((String) actualObj.get("installation_id"), is("installation_id"));
        assertThat((String) actualObj.get("app_id"), is("app_id"));
        assertThat(actualObj.has("cooldown"), is(false));
    }

    @Test
    public void buildCorrectEvaluationBody_noCooldownNoPulse() throws JSONException {
        String actual = RecipesManager.buildEvaluateBody(mockGlobalConfig, mockRecipeCooler, null, null, null);
        JSONObject actualObj = new JSONObject(actual);
        assertThat(actualObj = actualObj.getJSONObject("data"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("attributes"), is(notNullValue()));
        assertThat(actualObj.has(PULSE_PLUGIN_ID_KEY), is(false));
        assertThat(actualObj.has(PULSE_ACTION_ID_KEY), is(false));
        assertThat(actualObj.has(PULSE_BUNDLE_ID_KEY), is(false));
        assertThat(actualObj = actualObj.getJSONObject("core"), is(notNullValue()));
        assertThat((String) actualObj.get("profile_id"), is("profile_id"));
        assertThat((String) actualObj.get("installation_id"), is("installation_id"));
        assertThat((String) actualObj.get("app_id"), is("app_id"));
        assertThat(actualObj = actualObj.getJSONObject("cooldown"), is(notNullValue()));
        assertThat(Long.valueOf((Integer)actualObj.get("last_notified_at")), is(0L));
        assertThat(actualObj.getJSONObject("recipes_notified_at").length(), is(0));
    }

    @Test
    public void buildCorrectEvaluationBody_withPulse() throws JSONException {
        String actual = RecipesManager.buildEvaluateBody(mockGlobalConfig, mockRecipeCooler, "plugin", "action", "bundle");
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
        when(mockRecipeCooler.getRecipeLogMap()).thenReturn(logMap);
        String actual = RecipesManager.buildEvaluateBody(mockGlobalConfig, mockRecipeCooler, "plugin", "action", "bundle");
        JSONObject actualObj = new JSONObject(actual);
        assertThat(actualObj = actualObj.getJSONObject("data"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("attributes"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("core"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("cooldown"), is(notNullValue()));
        assertThat(actualObj = actualObj.getJSONObject("recipes_notified_at"), is(notNullValue()));
        assertThat(actualObj.length(), is(2));
        assertThat(Long.valueOf((Integer)actualObj.get("recipe_id_1")), is(0L));
        assertThat(Long.valueOf((Integer)actualObj.get("recipe_id_2")), is(1000L));
    }
*/
}
