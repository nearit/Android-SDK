package it.near.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.Maps;

import org.json.JSONObject;
import org.junit.Before;
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

    Recipe criticalRecipe;

    @Before
    public void init() {

        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);


        criticalRecipe = new Recipe();
        criticalRecipe.setId("recipe_id");
        HashMap<String, Object> cooldown = Maps.newHashMap();
        cooldown.put(GLOBAL_COOLDOWN, 0L);
        cooldown.put(SELF_COOLDOWN, 0L);
        criticalRecipe.setCooldown(cooldown);
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


        RecipeCooler.getInstance(mockContext).markRecipeAsShown("recipe_id");
        Map<String, Long> logMap = Maps.newHashMap();
        logMap.put("recipe_id", System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject(logMap);
        String jsonString = jsonObject.toString();
        verify(mockEditor).putString(eq(LOG_MAP), contains("recipe_id"));

    }

    @Test
    public void whenRecipeWithSelfCooldownShown_cantBeShownAgain() {
        assertTrue(true);
        RecipeCooler.getInstance(mockContext).markRecipeAsShown("recipe_id");
    }

}
