package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import it.near.sdk.TestUtils;
import it.near.sdk.logging.NearLog;
import it.near.sdk.reactions.Cacher;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;
import it.near.sdk.utils.timestamp.NearTimestampChecker;

import static it.near.sdk.recipes.RecipeRepository.NEAR_RECIPES_REPO_PREFS_NAME;
import static it.near.sdk.recipes.RecipeRepository.ONLINE_EV;
import static it.near.sdk.recipes.RecipeRepository.ONLINE_EV_DEFAULT;
import static it.near.sdk.recipes.RecipeRepository.RecipesListener;
import static it.near.sdk.recipes.RecipeRepository.TIMESTAMP;
import static it.near.sdk.recipes.RecipeRepository.TIMESTAMP_DEF_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@RunWith(MockitoJUnitRunner.class)
public class RecipeRepositoryTest {

    private RecipeRepository recipeRepository;

    @Mock
    private NearTimestampChecker nearTimestampChecker;
    @Mock
    private Cacher<Recipe> cache;
    @Mock
    private RecipesApi recipesApi;
    @Mock
    private CurrentTime currentTime;
    @Mock
    private SharedPreferences sp;
    @Mock
    private SharedPreferences.Editor editor;
    @Mock
    private List<Recipe> dummyRecipes;

    private long FAKE_TIMESTAMP = 1234L;

    @BeforeClass
    public static void init() {
        NearLog.setLogger(TestUtils.emptyLogger());
    }

    @Before
    public void setUp() throws Exception {
        when(sp.edit()).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(currentTime.currentTimeStampSeconds()).thenReturn(FAKE_TIMESTAMP);

        recipeRepository = new RecipeRepository(
                nearTimestampChecker,
                cache,
                recipesApi,
                currentTime,
                sp);
    }

    @Test
    public void whenRepoIsEmpty_ItHasNoRecipesAndAsksTheServer() {
        when(sp.getLong(TIMESTAMP, TIMESTAMP_DEF_VALUE)).thenReturn(TIMESTAMP_DEF_VALUE);
        when(sp.getBoolean(ONLINE_EV, ONLINE_EV_DEFAULT)).thenReturn(ONLINE_EV_DEFAULT);
        boolean dummyOnlineEvResponse = false;
        mockRecipeSuccessResponse(dummyRecipes, dummyOnlineEvResponse);

        assertThat(recipeRepository.getLocalRecipes(), empty());
        assertThat(recipeRepository.shouldEvaluateOnline(), is(ONLINE_EV_DEFAULT));
        RecipesListener listener = mock(RecipesListener.class);
        recipeRepository.syncRecipes(listener);
        // since we are in a default state, we don't even check for the timestamp remotely
        verifyZeroInteractions(nearTimestampChecker);
        // we just do the request
        verify(recipesApi, atLeastOnce()).processRecipes(any(RecipesApi.RecipesListener.class));
        // values are returned in the listener
        // and persisted
        assertThat(recipeRepository.getLocalRecipes(), is(dummyRecipes));
        verify(editor, atLeastOnce()).putLong(TIMESTAMP, FAKE_TIMESTAMP);
        verify(cache, atLeastOnce()).persistList(dummyRecipes);
        verify(editor, atLeastOnce()).putBoolean(ONLINE_EV, dummyOnlineEvResponse);
        verify(listener, atLeastOnce()).onGotRecipes(dummyRecipes, dummyOnlineEvResponse, true);
    }

    @Test
    public void whenRepoIsUpToDate_MinimumInteractionsAreDone() throws JSONException {
        mockSyncNotNeeded();
        long upToDateTimestamp = 1234L;
        boolean online_ev = false;
        when(sp.getLong(TIMESTAMP, TIMESTAMP_DEF_VALUE)).thenReturn(upToDateTimestamp);
        when(sp.getBoolean(ONLINE_EV, ONLINE_EV_DEFAULT)).thenReturn(online_ev);
        when(cache.loadList(any(Type.class))).thenReturn(dummyRecipes);
        // we instantiate again, so that the cache loading results are actually loaded in the constructor
        recipeRepository = new RecipeRepository(
                nearTimestampChecker,
                cache,
                recipesApi,
                currentTime,
                sp);
        RecipesListener listener = mock(RecipesListener.class);
        recipeRepository.syncRecipes(listener);
        verifyZeroInteractions(recipesApi);
        verify(nearTimestampChecker, atLeastOnce()).checkRecipeTimeStamp(eq(upToDateTimestamp), any(NearTimestampChecker.SyncCheckListener.class));
        verify(listener, atLeastOnce()).onGotRecipes(dummyRecipes, online_ev, false);
    }

    @Test
    public void whenRepoHasOldData_itRefreshesData() throws JSONException {
        mockSyncNeeded();
        boolean newOnlineDev = true;
        mockRecipeSuccessResponse(dummyRecipes, newOnlineDev);
        long notUpToDateTimestamp = 1234L;
        boolean online_ev = false;
        when(sp.getLong(TIMESTAMP, TIMESTAMP_DEF_VALUE)).thenReturn(notUpToDateTimestamp);
        when(sp.getBoolean(ONLINE_EV, ONLINE_EV_DEFAULT)).thenReturn(newOnlineDev);
        when(cache.loadList(any(Type.class))).thenReturn(Collections.<Recipe>emptyList());
        long rightNowTimeStamp = 555555L;
        when(currentTime.currentTimeStampSeconds()).thenReturn(rightNowTimeStamp);
        // we instantiate again, so that the cache loading results are actually loaded in the constructor
        recipeRepository = new RecipeRepository(
                nearTimestampChecker,
                cache,
                recipesApi,
                currentTime,
                sp);
        RecipesListener listener = mock(RecipesListener.class);
        recipeRepository.syncRecipes(listener);
        verify(nearTimestampChecker,atLeastOnce()).checkRecipeTimeStamp(eq(notUpToDateTimestamp), any(NearTimestampChecker.SyncCheckListener.class));
        verify(recipesApi, atLeastOnce()).processRecipes(any(RecipesApi.RecipesListener.class));
        assertThat(recipeRepository.getLocalRecipes(), is(dummyRecipes));
        assertThat(recipeRepository.shouldEvaluateOnline(), is(newOnlineDev));
        verify(editor, atLeastOnce()).putLong(TIMESTAMP, rightNowTimeStamp);
        verify(cache, atLeastOnce()).persistList(dummyRecipes);
        verify(editor, atLeastOnce()).putBoolean(ONLINE_EV, newOnlineDev);
        verify(listener, atLeastOnce()).onGotRecipes(dummyRecipes, newOnlineDev, true);
    }

    @Test
    public void whenRefreshFails_oldDataIsReturned() throws JSONException {
        mockSyncNeeded();
        mockRecipeFailResponse();
        when(sp.getLong(TIMESTAMP, TIMESTAMP_DEF_VALUE)).thenReturn(9876L);
        boolean online_ev = true;
        when(sp.getBoolean(ONLINE_EV, ONLINE_EV_DEFAULT)).thenReturn(online_ev);
        RecipesListener listener = mock(RecipesListener.class);
        when(cache.loadList(any(Type.class))).thenReturn(dummyRecipes);
        // we instantiate again, so that the cache loading results are actually loaded in the constructor
        recipeRepository = new RecipeRepository(
                nearTimestampChecker,
                cache,
                recipesApi,
                currentTime,
                sp);
        recipeRepository.syncRecipes(listener);
        verify(listener, atLeastOnce()).onGotRecipes(dummyRecipes, online_ev, false);
    }

    @Test
    public void whenRecipeIsAdded_isPersistedAndReturned() {
        Recipe recipe = mock(Recipe.class);
        recipeRepository.addRecipe(recipe);
        verify(cache, atLeastOnce()).persistList((List<Recipe>) argThat(hasItem(recipe)));
        assertThat(recipeRepository.getLocalRecipes(), hasItem(recipe));
        assertThat(recipeRepository.getLocalRecipes(), hasSize(1));
        Recipe recipe2 = mock(Recipe.class);
        recipeRepository.addRecipe(recipe2);
        verify(cache, atLeastOnce()).persistList((List<Recipe>) argThat(hasItems(recipe, recipe2)));
        assertThat(recipeRepository.getLocalRecipes(), hasSize(2));
        assertThat(recipeRepository.getLocalRecipes(), hasItems(recipe, recipe2));
    }

    @Test
    public void whenSharedPreferenceIsRequested_isProperlyReturned() {
        Context context = mock(Context.class);
        RecipeRepository.getSharedPreferences(context);
        verify(context, atLeastOnce()).getSharedPreferences(
                NEAR_RECIPES_REPO_PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void mockSyncNeeded() {
        mockTimestampCheckSync(true);
    }

    private void mockSyncNotNeeded() {
        mockTimestampCheckSync(false);
    }

    private void mockTimestampCheckSync(final boolean syncNeeded) {
        doAnswer(new Answer() {
                     @Override
                     public Object answer(InvocationOnMock invocation) throws Throwable {
                         NearTimestampChecker.SyncCheckListener listener =
                                 ((NearTimestampChecker.SyncCheckListener)invocation.getArguments()[1]);
                         if (syncNeeded) listener.syncNeeded();
                         else listener.syncNotNeeded();
                         return null;
                     }
                 }
        ).when(nearTimestampChecker).checkRecipeTimeStamp(anyLong(), any(NearTimestampChecker.SyncCheckListener.class));
    }

    private void mockRecipeSuccessResponse(final List<Recipe> recipes, final boolean online_ev) {
        doAnswer(new Answer() {
                     @Override
                     public Object answer(InvocationOnMock invocation) throws Throwable {
                         ((RecipesApi.RecipesListener) invocation.getArguments()[0]).onRecipeProcessSuccess(recipes, online_ev);
                         return null;
                     }
                 }
        ).when(recipesApi).processRecipes(any(RecipesApi.RecipesListener.class));
    }

    private void mockRecipeFailResponse() {
        doAnswer(new Answer() {
                     @Override
                     public Object answer(InvocationOnMock invocation) throws Throwable {
                         ((RecipesApi.RecipesListener)invocation.getArguments()[0]).onRecipeProcessError();
                         return null;
                     }
                 }
        ).when(recipesApi).processRecipes(any(RecipesApi.RecipesListener.class));
    }


}
