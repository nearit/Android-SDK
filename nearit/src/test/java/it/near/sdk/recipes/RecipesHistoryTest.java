package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipesHistoryTest {

    private static final String TEST_RECIPE_ID = "test_recipe_id";

    @Mock
    SharedPreferences mMockSharedPreferences;
    @Mock
    CurrentTime mMockCurrentTime;
    @Mock
    SharedPreferences.Editor mMockEditor;

    private Recipe testRecipe;
    private RecipesHistory recipesHistory;

    @Before
    public void setUp() throws Exception {
        testRecipe = new Recipe();
        testRecipe.setId(TEST_RECIPE_ID);

        when(mMockSharedPreferences.edit()).thenReturn(mMockEditor);
        when(mMockSharedPreferences.getString(anyString(), anyString()))
                .then(returnsSecondArg());
        when(mMockEditor.remove(anyString())).thenReturn(mMockEditor);
        when(mMockEditor.commit()).thenReturn(true);

        recipesHistory = new RecipesHistory(mMockSharedPreferences, mMockCurrentTime);
    }

    @Test
    public void whenNoHistory_HistoryHasDefaultValues() {
        assertThat(recipesHistory.getLatestLogEntry(), is(0L));
        assertThat(recipesHistory.isRecipeInLog(TEST_RECIPE_ID), is(false));
    }

    @Test
    public void whenRecipeShown_historyUpdated() {
        String anotherRecipeID = "another_recipe_id";
        Recipe anotherRecipe = new Recipe();
        anotherRecipe.setId(anotherRecipeID);
        // when we mark a recipe as shown
        recipesHistory.markRecipeAsShown(testRecipe.getId());
        // then its timestamp is put in history
        assertThat(recipesHistory.isRecipeInLog(TEST_RECIPE_ID),is(true));
        assertThat(recipesHistory.isRecipeInLog(anotherRecipeID), is(false));
        // when we mark another recipe as shown
        recipesHistory.markRecipeAsShown(anotherRecipe.getId());
        // then its timestamp is added to history
        assertThat(recipesHistory.isRecipeInLog(TEST_RECIPE_ID),is(true));
        assertThat(recipesHistory.isRecipeInLog(anotherRecipeID), is(true));
    }

    @Test
    public void whenRecipeIsShown_updateLastLogEntry() {
        Long expected = 100L;
        when(mMockCurrentTime.currentTimeStampSeconds()).thenReturn(expected);
        // when we mark a recipe as shown
        recipesHistory.markRecipeAsShown(testRecipe.getId());
        long actualTimestamp = recipesHistory.getLatestLogEntry();
        // then the latest log entry is updated
        assertThat(actualTimestamp, is(expected));
        assertThat(recipesHistory.isRecipeInLog(TEST_RECIPE_ID), is(true));
        assertThat(recipesHistory.latestLogEntryFor(TEST_RECIPE_ID), is(expected));
    }

    @Test
    public void whenHistoryIsMade_isActuallyPersisted() {
        String logMapStringMock = "{\n" +
                "    \"test_recipe_id\" : 1496769570\n" +
                "}";
        when(mMockSharedPreferences.getString(eq(RecipesHistory.LOG_MAP), anyString()))
                .thenReturn(logMapStringMock);
        Long latestLogMock = 1496769570L;
        when(mMockSharedPreferences.getLong(eq(RecipesHistory.LATEST_LOG), anyLong()))
                .thenReturn(latestLogMock);

        assertThat(recipesHistory.getLatestLogEntry(), is(latestLogMock));
        assertThat(recipesHistory.isRecipeInLog(TEST_RECIPE_ID), is(true));
        assertThat(recipesHistory.latestLogEntryFor(TEST_RECIPE_ID), is(latestLogMock));
    }

    @Test
    public void whenAskingForSharedPref_provideOne() {
        Context mockContext = mock(Context.class);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mMockSharedPreferences);
        assertThat(RecipesHistory.getSharedPreferences(mockContext), is(mMockSharedPreferences));
    }
}