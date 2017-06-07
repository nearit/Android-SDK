package it.near.sdk.recipes.validation;

import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import it.near.sdk.recipes.RecipesHistory;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.recipes.validation.CooldownValidator.GLOBAL_COOLDOWN;
import static it.near.sdk.recipes.validation.CooldownValidator.NEVER_REPEAT;
import static it.near.sdk.recipes.validation.CooldownValidator.SELF_COOLDOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CooldownValidatorTest {

    @Mock
    private RecipesHistory mockRecipesHistory;
    @Mock
    private CurrentTime mockCurrentTime;

    private CooldownValidator cooldownValidator;
    private Recipe mCriticalRecipe;
    private Recipe mNonCriticalRecipe;

    @Before
    public void setUp() {
        mCriticalRecipe = buildRecipe("critical", buildCooldown(0D, 0D));
        mNonCriticalRecipe = buildRecipe("pedestrian", buildCooldown(24 * 60 * 60D,
                24 * 60 * 60D));
        when(mockRecipesHistory.getLatestLogEntry()).thenReturn(0L);
        when(mockRecipesHistory.isRecipeInLog(anyString())).thenReturn(false);
        when(mockCurrentTime.currentTimeStampSeconds())
                .thenReturn(secondsFor(now()));
        cooldownValidator = new CooldownValidator(mockRecipesHistory, mockCurrentTime);
    }

    @Test
    public void whenLogIsEmpty_enableRecipe() {
        // when history is empty
        assertThat(cooldownValidator.validate(mCriticalRecipe), is(true));
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(true));
    }

    @Test
    public void whenRecipeWithSelfCooldownShown_cantBeShownAgain() {
        when(mockRecipesHistory.isRecipeInLog(mNonCriticalRecipe.getId()))
                .thenReturn(true);
        when(mockRecipesHistory.latestLogEntryFor(mNonCriticalRecipe.getId()))
                .thenReturn(secondsFor(new DateTime().minusHours(1)));
        assertThat(cooldownValidator.validate(mCriticalRecipe), is(true));
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(false));
    }

    @Test
    public void whenRecipeHasNoCooldown_canBeShownAgain() {
        // when a recipe is shown
        when(mockRecipesHistory.isRecipeInLog(mCriticalRecipe.getId()))
                .thenReturn(true);
        when(mockRecipesHistory.latestLogEntryFor(mCriticalRecipe.getId()))
                .thenReturn(secondsFor(new DateTime().minusSeconds(1)));
        assertThat(cooldownValidator.validate(mCriticalRecipe), is(true));
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(true));
    }

    @Test
    public void whenRecipeIsShown_globalCooldownApplies() {
        // when a recipe was recently shown
        when(mockRecipesHistory.getLatestLogEntry())
                .thenReturn(secondsFor(now().minusHours(1)));
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(false));
        assertThat(cooldownValidator.validate(mCriticalRecipe), is(true));
    }

    @Test
    public void whenCooldownMissing_showRecipe() {
        // when there's a recent entry log
        long nowSeconds = secondsFor(now());
        when(mockRecipesHistory.getLatestLogEntry())
                .thenReturn(nowSeconds);
        when(mockRecipesHistory.isRecipeInLog(mNonCriticalRecipe.getId()))
                .thenReturn(true);
        when(mockRecipesHistory.latestLogEntryFor(mNonCriticalRecipe.getId()))
                .thenReturn(nowSeconds);
        // and a recipe without the cooldown section
        mNonCriticalRecipe.setCooldown(null);
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(true));
    }

    @Test
    public void whenMissingSelfCooldown_considerItZero() {
        // there's recent history for a recipe
        when(mockRecipesHistory.isRecipeInLog(mNonCriticalRecipe.getId()))
                .thenReturn(true);
        when(mockRecipesHistory.latestLogEntryFor(mNonCriticalRecipe.getId()))
                .thenReturn(secondsFor(now()));
        // and the recipe has no self-cooldown
        mNonCriticalRecipe.setCooldown(buildCooldown(0D, null));
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(true));
    }

    @Test
    public void whenMissingGlobalCoolDown_considerItZero() {
        // there's recent history for some recipe
        when(mockRecipesHistory.getLatestLogEntry())
                .thenReturn(secondsFor(now()));
        // and a recipe has no global-cooldown
        mNonCriticalRecipe.setCooldown(buildCooldown(null, 0D));
        assertThat(cooldownValidator.validate(mNonCriticalRecipe), is(true));
    }

    @Test
    public void whenRecipeIsNeverToBeShownAgain_itShouldNeverBeShown() {
        // when a one time only recipe is shown
        Recipe onlyOnceRecipe = buildRecipe("never again", buildCooldown(0D, NEVER_REPEAT));
        when(mockCurrentTime.currentTimeStampSeconds())
                .thenReturn(secondsFor(now()));
        when(mockRecipesHistory.isRecipeInLog(onlyOnceRecipe.getId()))
                .thenReturn(true);
        when(mockRecipesHistory.latestLogEntryFor(onlyOnceRecipe.getId()))
                .thenReturn(secondsFor(now().minusHours(1)));
        // it is not shown again
        assertThat(cooldownValidator.validate(onlyOnceRecipe), is(false));
        // not even in the far far future
        when(mockCurrentTime.currentTimeStampSeconds())
                .thenReturn(secondsFor(now().plusYears(60)));
        assertThat(cooldownValidator.validate(onlyOnceRecipe), is(false));
    }

    private DateTime now() {
        return new DateTime();
    }

    private long secondsFor(DateTime dateTime) {
        return dateTime.getMillis() / 1000;
    }

    private Recipe buildRecipe(String id, HashMap<String, Object> cooldown) {
        Recipe criticalRecipe = new Recipe();
        criticalRecipe.setId(id);
        criticalRecipe.setCooldown(cooldown);
        return criticalRecipe;
    }

    private HashMap<String, Object> buildCooldown(Double globalCD, Double selfCD) {
        HashMap<String, Object> cooldown = Maps.newHashMap();
        cooldown.put(GLOBAL_COOLDOWN, globalCD);
        cooldown.put(SELF_COOLDOWN, selfCD);
        return cooldown;
    }
}