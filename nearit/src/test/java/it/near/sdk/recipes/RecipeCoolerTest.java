package it.near.sdk.recipes;

import android.content.SharedPreferences;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static com.google.common.collect.Lists.newArrayList;
import static it.near.sdk.recipes.RecipeCooler.*;
import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RecipeCoolerTest {

    @Mock
    SharedPreferences mMockSharedPreferences;
    @Mock
    SharedPreferences.Editor mMockEditor;

    private Recipe mCriticalRecipe;
    private Recipe mNonCriticalRecipe;
    private RecipeCooler mRecipeCooler;

    @Before
    public void setUp() {
        mCriticalRecipe = buildRecipe("critical", buildCooldown(0D, 0D));
        mNonCriticalRecipe = buildRecipe("pedestrian", buildCooldown(24 * 60 * 60D,
                                                                    24 * 60 * 60D));
        when(mMockSharedPreferences.edit()).thenReturn(mMockEditor);
        when(mMockEditor.remove(anyString())).thenReturn(mMockEditor);
        when(mMockEditor.commit()).thenReturn(true);

        CurrentTime currentTime = new CurrentTime();
        mRecipeCooler = new RecipeCooler(mMockSharedPreferences, currentTime);
    }

    @Test
    public void whenLogIsEmpty_enableRecipe() {
        // when we filter recipes with a fresh history
        List<Recipe> recipeList = newArrayList(mCriticalRecipe,
                            mCriticalRecipe,
                            mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then they all pass the filter
        assertThat(recipeList, hasSize(3));
        assertThat(recipeList, hasItems(mCriticalRecipe, mNonCriticalRecipe));
        assertThat(mRecipeCooler.getRecipeLogMap().values(), is(empty()));
    }

    @Test
    public void whenRecipeShown_historyUpdated() {
        // when we mark a recipe as shown
        mRecipeCooler.markRecipeAsShown(mNonCriticalRecipe.getId());
        // then its timestamp is put in history
        assertThat(keySetOf(mRecipeCooler.getRecipeLogMap()), both(hasItem(mNonCriticalRecipe.getId()))
                                                            .and(not(hasItem(mCriticalRecipe.getId()))));
        // when we mark another recipe as shown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        // then its timestamp is added to history
        assertThat(keySetOf(mRecipeCooler.getRecipeLogMap()), both(hasItem(mNonCriticalRecipe.getId()))
                                                                .and(hasItem(mCriticalRecipe.getId())));
    }

    @Test
    public void whenRecipeWithSelfCooldownShown_cantBeShownAgain() {
        // when a non critical recipe is shown
        mRecipeCooler.markRecipeAsShown(mNonCriticalRecipe.getId());
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then it can't be displayed again
        assertThat(recipeList, hasSize(0));
        assertThat(recipeList, org.hamcrest.core.IsNot.not(hasItem(mNonCriticalRecipe)));
    }

    @Test
    public void whenRecipeHasNoCooldown_canBeShownAgain() {
        // when a recipe is shown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        List<Recipe> recipeList = newArrayList(mCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then a critical recipe can still be shown shortly afterwards
        assertThat(recipeList, hasSize(1));
        recipeList.add(mCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        assertThat(recipeList, hasSize(2));
    }

    @Test
    public void whenRecipeIsShown_globalCooldownApplies() {
        // when a recipe is shown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then a non critical recipe won't be shown
        assertThat(recipeList, hasSize(0));
        assertThat(recipeList, org.hamcrest.core.IsNot.not(hasItem(mNonCriticalRecipe)));
        recipeList.add(mCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // but a critical recipe will
        assertThat(recipeList, hasSize(1));
        recipeList.add(mCriticalRecipe);
        recipeList.add(mNonCriticalRecipe);
        recipeList.add(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        assertThat(recipeList, hasSize(2));
        assertThat(recipeList, hasItem(mCriticalRecipe));
        assertThat(recipeList, org.hamcrest.core.IsNot.not(hasItem(mNonCriticalRecipe)));
    }

    @Test
    public void whenRecipeIsShown_updateLastLogEntry() {
        long beforeTimestamp = System.currentTimeMillis();
        // when we mark a recipe as shown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        long afterTimestamp = System.currentTimeMillis();
        long actualTimestamp = mRecipeCooler.getLatestLogEntry();
        // then the latest log entry is updated
        assertTrue(beforeTimestamp <= actualTimestamp);
        assertTrue(actualTimestamp <= afterTimestamp);
        assertThat(keySetOf(mRecipeCooler.getRecipeLogMap()), hasItem(mCriticalRecipe.getId()));
    }

    @Test
    public void whenCooldownMissing_showRecipe() {
        // when there's a recent entry log
        mRecipeCooler.markRecipeAsShown(mNonCriticalRecipe.getId());
        // and a recipe without the cooldown section
        mNonCriticalRecipe.setCooldown(null);
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then it gets treated as a critical recipe
        assertThat(recipeList, hasSize(1));
        assertThat(recipeList, hasItem(mNonCriticalRecipe));
    }

    @Test
    public void whenMissingSelfCooldown_considerItZero() {
        // when a recipe has no selfcooldown
        mNonCriticalRecipe.setCooldown(buildCooldown(0D, null));
        mRecipeCooler.markRecipeAsShown(mNonCriticalRecipe.getId());
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then it gets treated as critical
        assertThat(recipeList, hasSize(1));
        assertThat(recipeList, hasItem(mNonCriticalRecipe));
    }

    @Test
    public void whenMissingGlobalCoolDown_considerItZero() {
        // when a recipe has no globalcooldown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        mNonCriticalRecipe.setCooldown(buildCooldown(null, 0D));
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then its get treated as critical
        assertThat(recipeList, hasSize(1));
        assertThat(recipeList, hasItem(mNonCriticalRecipe));
    }

    @Test
    public void whenRecipeIsNeverToBeShownAgain_itShouldNeverBeShown() {
        CurrentTime mockCurrentTime = mock(CurrentTime.class);
        mRecipeCooler = new RecipeCooler(mMockSharedPreferences, mockCurrentTime);
        Recipe onlyOnceRecipe = buildRecipe("never again", buildCooldown(0D, NEVER_REPEAT));
        // when a one time only recipe is shown
        when(mockCurrentTime.currentTimestamp()).thenReturn(System.currentTimeMillis());
        mRecipeCooler.markRecipeAsShown(onlyOnceRecipe.getId());
        List<Recipe> recipeList = newArrayList(onlyOnceRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // it should never be shown again
        assertThat(recipeList, hasSize(0));
        // even if it is checked in the far future
        DateTime farFuture = new DateTime(2060, 1, 1, 1, 1, 1);
        when(mockCurrentTime.currentTimestamp()).thenReturn(farFuture.getMillis());
        recipeList = newArrayList(onlyOnceRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // it should never be shown again
        assertThat(recipeList, hasSize(0));
    }

    private List<String> keySetOf(Map<String, Long> map){
        return Lists.newArrayList(map.keySet());
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
