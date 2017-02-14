package it.near.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.Maps;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.RecipeCooler;

import static it.near.sdk.Recipes.RecipeCooler.*;
import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by cattaneostefano on 14/02/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class RecipeCoolerTest {

    @Mock
    Context mockContext;
    @Mock
    SharedPreferences mockSharedPreferences;
    @Mock
    SharedPreferences.Editor mockEditor;

    private Recipe criticalRecipe;
    private Recipe nonCriticalRecipe;

    @Before
    public void initRecipes() {
        criticalRecipe = buildRecipe("critical", 0L, 0L);
        nonCriticalRecipe = buildRecipe("pedestrian", 24 * 60 * 60L, 24 * 60 * 60L);
    }

    @Before
    public void initMocks() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);

    }

    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = RecipeCooler.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void whenLogIsEmpty_enableRecipe() {
        when(mockSharedPreferences.getString(eq(LOG_MAP), anyString())).thenReturn("{}");

        List<Recipe> recipeList = new ArrayList(Arrays.asList(criticalRecipe));
        assertEquals(1, recipeList.size());
        RecipeCooler.getInstance(mockContext).filterRecipe(recipeList);
        assertEquals(1, recipeList.size());

    }

    @Test
    public void whenRecipeShown_historyUpdated() {
        RecipeCooler.getInstance(mockContext).markRecipeAsShown(criticalRecipe.getId());
        verify(mockEditor).putString(eq(LOG_MAP), contains(criticalRecipe.getId()));
    }

    @Test
    public void whenRecipeWithSelfCooldownShown_cantBeShownAgain() {
        RecipeCooler recipeCooler = RecipeCooler.getInstance(mockContext);
        recipeCooler.markRecipeAsShown(nonCriticalRecipe.getId());
        List<Recipe> recipeList = new ArrayList(Arrays.asList(nonCriticalRecipe));
        recipeCooler.filterRecipe(recipeList);
        assertEquals(0, recipeList.size());
    }

    @Test
    public void whenRecipeHasNoCooldown_canBeShownAgain() {
        RecipeCooler recipeCooler = RecipeCooler.getInstance(mockContext);
        recipeCooler.markRecipeAsShown(criticalRecipe.getId());
        List<Recipe> recipeList = new ArrayList(Arrays.asList(criticalRecipe));
        recipeCooler.filterRecipe(recipeList);
        assertEquals(1, recipeList.size());
        recipeList.add(criticalRecipe);
        recipeCooler.filterRecipe(recipeList);
        assertEquals(2, recipeList.size());
        recipeCooler.markRecipeAsShown(criticalRecipe.getId());
    }

    @Test
    public void whenRecipeIsShown_globalCooldownApplies() {
        RecipeCooler recipeCooler = RecipeCooler.getInstance(mockContext);
        recipeCooler.markRecipeAsShown(criticalRecipe.getId());
        List<Recipe> recipeList = new ArrayList(Arrays.asList(nonCriticalRecipe));
        recipeCooler.filterRecipe(recipeList);
        assertEquals(0, recipeList.size());
        recipeList.add(criticalRecipe);
        recipeCooler.filterRecipe(recipeList);
        assertEquals(1, recipeList.size());
        recipeList.add(criticalRecipe);
        recipeList.add(nonCriticalRecipe);
        recipeList.add(nonCriticalRecipe);
        recipeCooler.filterRecipe(recipeList);
        assertEquals(2, recipeList.size());
    }

    @Test
    public void whenRecipeIsShown_updateLastLogEntry() {
        long beforeTimestamp = System.currentTimeMillis();
        RecipeCooler recipeCooler = RecipeCooler.getInstance(mockContext);
        recipeCooler.markRecipeAsShown(criticalRecipe.getId());
        long afterTimestamp = System.currentTimeMillis();
        long actualTimestamp = recipeCooler.getLatestLogEntry();
        assertTrue(beforeTimestamp <= actualTimestamp);
        assertTrue(actualTimestamp <= afterTimestamp);
    }

    private Recipe buildRecipe(String id, long globalCD, long selfCD) {
        Recipe criticalRecipe = new Recipe();
        criticalRecipe.setId(id);
        HashMap<String, Object> cooldown = Maps.newHashMap();
        cooldown.put(GLOBAL_COOLDOWN, globalCD);
        cooldown.put(SELF_COOLDOWN, selfCD);
        criticalRecipe.setCooldown(cooldown);
        return criticalRecipe;
    }

}
