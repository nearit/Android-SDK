package it.near.sdk.Recipes;

import android.content.SharedPreferences;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.RecipeCooler;

import static com.google.common.collect.Lists.newArrayList;
import static it.near.sdk.Recipes.RecipeCooler.*;
import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by cattaneostefano on 14/02/2017.
 */

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
        mCriticalRecipe = buildRecipe("critical", buildCooldown(0L, 0L));
        mNonCriticalRecipe = buildRecipe("pedestrian", buildCooldown(24 * 60 * 60L,
                                                                    24 * 60 * 60L));

        when(mMockSharedPreferences.edit()).thenReturn(mMockEditor);
        when(mMockEditor.remove(anyString())).thenReturn(mMockEditor);
        when(mMockEditor.commit()).thenReturn(true);

        mRecipeCooler = new RecipeCooler(mMockSharedPreferences);
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
    }

    @Test
    public void whenRecipeShown_historyUpdated() {
        // when we mark a recipe as shown
        mRecipeCooler.markRecipeAsShown(mNonCriticalRecipe.getId());
        // then its timestamp is put in history
        verify(mMockEditor).putString(eq(LOG_MAP), and(contains(mNonCriticalRecipe.getId()),
                                                        not(contains(mCriticalRecipe.getId()))));
        // when we mark another recipe as shown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        // then its timestamp is added to history
        verify(mMockEditor, atLeastOnce())
                .putString(eq(LOG_MAP), and(contains(mNonCriticalRecipe.getId()),
                                            contains(mCriticalRecipe.getId())));
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
        mNonCriticalRecipe.setCooldown(buildCooldown(0L, null));
        mRecipeCooler.markRecipeAsShown(mNonCriticalRecipe.getId());
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then it gets treaded as critical
        assertThat(recipeList, hasSize(1));
        assertThat(recipeList, hasItem(mNonCriticalRecipe));
    }

    @Test
    public void whenMissingGlobalCoolDown_considerItZero() {
        // when a recipe has no globalcooldown
        mRecipeCooler.markRecipeAsShown(mCriticalRecipe.getId());
        mNonCriticalRecipe.setCooldown(buildCooldown(null, 0L));
        List<Recipe> recipeList = newArrayList(mNonCriticalRecipe);
        mRecipeCooler.filterRecipe(recipeList);
        // then its get treaded as critical
        assertThat(recipeList, hasSize(1));
        assertThat(recipeList, hasItem(mNonCriticalRecipe));
    }

    private Recipe buildRecipe(String id, HashMap<String, Object> cooldown) {
        Recipe criticalRecipe = new Recipe();
        criticalRecipe.setId(id);
        criticalRecipe.setCooldown(cooldown);
        return criticalRecipe;
    }

    private HashMap<String, Object> buildCooldown(Long globalCD, Long selfCD) {
        HashMap<String, Object> cooldown = Maps.newHashMap();
        cooldown.put(GLOBAL_COOLDOWN, globalCD);
        cooldown.put(SELF_COOLDOWN, selfCD);
        return cooldown;
    }

}
