package it.near.sdk.recipes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.List;

import it.near.sdk.reactions.Reaction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.validation.RecipeValidationFilter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipesManagerTest {

    private RecipesManager recipesManager;

    @Mock
    private RecipeTrackSender recipeTrackSender;
    @Mock
    private RecipeValidationFilter recipeValidationFilter;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private RecipesApi recipesApi;
    @Mock
    private List<Recipe> dummyRecipes;

    @Mock
    private RecipeRefreshListener recipeRefreshListener;

    @Before
    public void setUp() throws Exception {
        recipesManager = new RecipesManager(
                recipeValidationFilter,
                recipeTrackSender,
                recipeRepository,
                recipesApi
        );
    }

    @Test
    public void getLocalRecipes_onlyReturnsLocalRecipes() {
        when(recipeRepository.getLocalRecipes()).thenReturn(dummyRecipes);
        assertThat(recipesManager.getRecipes(), is(dummyRecipes));
    }

    @Test
    public void refreshAndSyncRequestWithoutListener_getsTunneled() {
        RecipesManager spy = Mockito.spy(recipesManager);
        spy.refreshConfig();
        verify(spy, atLeastOnce()).refreshConfig(any(RecipeRefreshListener.class));
        spy.syncConfig();
        verify(spy, atLeastOnce()).syncConfig(any(RecipeRefreshListener.class));
    }

    @Test
    public void refreshRequests_isFulfilled() {
        mockRefreshRequest(dummyRecipes, true, true);
        recipesManager.refreshConfig(recipeRefreshListener);
        verify(recipeRefreshListener, atLeastOnce()).onRecipesRefresh();
    }

    @Test
    public void syncRequest_isFulfilled() {
        mockSyncRequest(dummyRecipes, true, true);
        recipesManager.syncConfig(recipeRefreshListener);
        verify(recipeRefreshListener, atLeastOnce()).onRecipesRefresh();
    }

    @Test
    public void getRecipe_triggersRightReactionPlugin() {
        String plugin_name = "My_plugin";
        Reaction rightReaction = addMockedReaction(plugin_name);

        Reaction wrongReaction = addMockedReaction("not_my_plugin");

        Recipe recipe = new Recipe();
        recipe.setReaction_plugin_id("something_different");
        recipesManager.gotRecipe(recipe);
        verify(rightReaction, never()).handleReaction(recipe);
        verify(wrongReaction, never()).handleReaction(recipe);

        recipe.setReaction_plugin_id(plugin_name);
        recipesManager.gotRecipe(recipe);
        verify(rightReaction, atLeastOnce()).handleReaction(recipe);
        verify(wrongReaction, never()).handleReaction(recipe);
    }

    @Test
    public void singleRecipeProcessRequest_isDealtOnSuccess() {
        String plugin_name = "plugin_name";
        Reaction rightReaction = addMockedReaction(plugin_name);

        final Recipe recipe = new Recipe();
        String recipeId = "id";
        recipe.setId(recipeId);
        recipe.setReaction_plugin_id(plugin_name);
        ReactionBundle reactionBundle = mock(ReactionBundle.class);
        recipe.setReaction_bundle(reactionBundle);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener)invocation.getArguments()[1]).onRecipeFetchSuccess(recipe);
                return null;
            }
        }).when(recipesApi).fetchRecipe(anyString(), any(RecipesApi.SingleRecipeListener.class));
        recipesManager.processRecipe(recipeId);
        verify(rightReaction, atLeastOnce()).handlePushReaction(recipe, reactionBundle);
    }

    @Test
    public void singleRecipeProcessRequest_onAPIError() {
        Reaction reaction = addMockedReaction("my_plugin");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener)invocation.getArguments()[1]).onRecipeFetchError("error");
                return null;
            }
        }).when(recipesApi).fetchRecipe(anyString(), any(RecipesApi.SingleRecipeListener.class));
        recipesManager.processRecipe("id");
        verify(reaction, never()).handlePushReaction(any(Recipe.class), any(ReactionBundle.class));
    }

    private Reaction addMockedReaction(String plugin_name) {
        Reaction reaction = mock(Reaction.class);
        when(reaction.getReactionPluginName()).thenReturn(plugin_name);
        recipesManager.addReaction(reaction);
        return reaction;
    }

    private void mockRefreshRequest(final List<Recipe> recipes, final boolean online_ev_fallback, final boolean data_changed) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipeRepository.RecipesListener) invocation.getArguments()[0]).onGotRecipes(recipes, online_ev_fallback, data_changed);
                return null;
            }
        }).when(recipeRepository).refreshRecipes(any(RecipeRepository.RecipesListener.class));
    }

    private void mockSyncRequest(final List<Recipe> recipes, final boolean online_ev_fallback, final boolean data_changed) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipeRepository.RecipesListener) invocation.getArguments()[0]).onGotRecipes(recipes, online_ev_fallback, data_changed);
                return null;
            }
        }).when(recipeRepository).syncRecipes(any(RecipeRepository.RecipesListener.class));
    }

}
