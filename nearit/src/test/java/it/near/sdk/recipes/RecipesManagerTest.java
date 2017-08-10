package it.near.sdk.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.json.JSONException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.List;

import it.near.sdk.TestUtils;
import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.models.PulseAction;
import it.near.sdk.recipes.models.PulseBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.pulse.TriggerRequest;
import it.near.sdk.recipes.validation.RecipeValidationFilter;
import it.near.sdk.trackings.TrackingInfo;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

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
    private RecipeReactionHandler recipeReactionHandler;

    @Mock
    private RecipeRefreshListener recipeRefreshListener;

    private TriggerRequest triggerRequest;
    private Recipe matchingBundleRecipe, matchingTagRecipe;

    @BeforeClass
    public static void init() {
        NearLog.setLogger(TestUtils.emptyLogger());
    }

    @Before
    public void setUp() throws Exception {
        initTriggerRequest();
        buildMatchingRecipes();
        recipesManager = new RecipesManager(
                recipeValidationFilter,
                recipeTrackSender,
                recipeRepository,
                recipesApi,
                recipeReactionHandler);
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
    public void processRecipeRequest_sendsNetworkRequestWithPassListener() {
        RecipesApi.SingleRecipeListener singleRecipeListener = mock(RecipesApi.SingleRecipeListener.class);
        String recipeId = "recid";
        recipesManager.processRecipe(recipeId, singleRecipeListener);
        verify(recipesApi).fetchRecipe(recipeId, singleRecipeListener);
    }

    @Test
    public void triggerRequestForLocalRecipeOnBundle_shouldTriggerWithoutNetworkCalls() {
        // prepare a matching recipe in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(
                Lists.newArrayList(matchingBundleRecipe)
        );

        // the recipe is validated
        when(recipeValidationFilter.filterRecipes(any(List.class))).then(returnsFirstArg());

        recipesManager.handleTriggerRequest(triggerRequest);
        verifyZeroInteractions(recipesApi);
        verify(recipeReactionHandler).gotRecipe(matchingBundleRecipe, triggerRequest.trackingInfo);
    }

    @Test
    public void triggerRequestForLocalRecipeOnTags_shouldTriggerWithoutNetworkCalls() {
        // prepare matching recipe
        when(recipeRepository.getLocalRecipes()).thenReturn(
                Lists.newArrayList(matchingTagRecipe)
        );

        // the recipe is validated
        when(recipeValidationFilter.filterRecipes(any(List.class))).then(returnsFirstArg());

        recipesManager.handleTriggerRequest(triggerRequest);
        verifyZeroInteractions(recipesApi);
        verify(recipeReactionHandler).gotRecipe(matchingTagRecipe, triggerRequest.trackingInfo);
    }

    @Test
    public void triggerRequestForLocalRecipeThatMatchesABundleRecipeAndATagRecipe_shouldOnlyTriggerTheBundleRecipe() {
        // prepare matching recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(
                Lists.newArrayList(matchingBundleRecipe, matchingTagRecipe)
        );

        // the recipes are validated
        when(recipeValidationFilter.filterRecipes(any(List.class))).then(returnsFirstArg());

        recipesManager.handleTriggerRequest(triggerRequest);
        verifyZeroInteractions(recipesApi);
        verify(recipeReactionHandler).gotRecipe(matchingBundleRecipe, triggerRequest.trackingInfo);
        verify(recipeReactionHandler, never()).gotRecipe(matchingTagRecipe, triggerRequest.trackingInfo);
    }

    @Test
    public void whenMatchingLocalBundleRecipeIsFiltered_localTagRecipeIsTriggered() {
        // prepare matching recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(
                Lists.newArrayList(matchingBundleRecipe, matchingTagRecipe)
        );

        // only validate tag recipe
        when(recipeValidationFilter.filterRecipes((List<Recipe>) argThat(hasItem(matchingBundleRecipe)))).thenReturn(Lists.<Recipe>newArrayList());
        when(recipeValidationFilter.filterRecipes((List<Recipe>) argThat(hasItem(matchingTagRecipe)))).then(returnsFirstArg());

        recipesManager.handleTriggerRequest(triggerRequest);
        verify(recipeReactionHandler).gotRecipe(matchingTagRecipe, triggerRequest.trackingInfo);
        verify(recipeReactionHandler, never()).gotRecipe(matchingBundleRecipe, triggerRequest.trackingInfo);
    }

    @Test
    public void whenLocalMatchingRecipeIsToBeEvaluated_itGetsEvaluated() {
        matchingBundleRecipe.labels = Maps.newHashMap(
                ImmutableMap.<String, Object>builder().
                put(Recipe.ONLINE, true).
                build());
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.newArrayList(matchingBundleRecipe));

        when(recipeValidationFilter.filterRecipes(any(List.class))).then(returnsFirstArg());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener)invocation.getArguments()[1]).onRecipeFetchSuccess(matchingBundleRecipe);
                return null;
            }
        }).when(recipesApi).evaluateRecipe(eq(matchingBundleRecipe.getId()), any(RecipesApi.SingleRecipeListener.class));

        recipesManager.handleTriggerRequest(triggerRequest);
        verify(recipesApi).evaluateRecipe(eq(matchingBundleRecipe.getId()), any(RecipesApi.SingleRecipeListener.class));
        verify(recipeReactionHandler).gotRecipe(matchingBundleRecipe, triggerRequest.trackingInfo);
    }

    @Test
    public void whenLocalMatchingRecipeIsToBeEvaluatedButFails_nothingHappens() {
        matchingBundleRecipe.labels = Maps.newHashMap(
                ImmutableMap.<String, Object>builder().
                        put(Recipe.ONLINE, true).
                        build());
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.newArrayList(matchingBundleRecipe));

        when(recipeValidationFilter.filterRecipes(any(List.class))).then(returnsFirstArg());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener)invocation.getArguments()[1]).onRecipeFetchError("invalid");
                return null;
            }
        }).when(recipesApi).evaluateRecipe(eq(matchingBundleRecipe.getId()), any(RecipesApi.SingleRecipeListener.class));

        recipesManager.handleTriggerRequest(triggerRequest);
        verify(recipesApi).evaluateRecipe(eq(matchingBundleRecipe.getId()), any(RecipesApi.SingleRecipeListener.class));
        verifyZeroInteractions(recipeReactionHandler);

    }

    @Test
    public void whenNoLocalRecipesAreFoundAndOnlineEvaluationIsEnabled_matchingOnlineRecipeIsTriggeredAndAddedToRepo() {
        // no recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.<Recipe>newArrayList());
        // online evaluation by pulse in enabled
        when(recipeRepository.shouldEvaluateOnline()).thenReturn(true);

        final Recipe recipe = new Recipe();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener) invocation.getArguments()[3]).onRecipeFetchSuccess(recipe);
                return null;
            }
        }).when(recipesApi).onlinePulseEvaluation(anyString(), anyString(), anyString(), any(RecipesApi.SingleRecipeListener.class));

        recipesManager.handleTriggerRequest(triggerRequest);
        verify(recipeReactionHandler).gotRecipe(recipe, triggerRequest.trackingInfo);
        verify(recipeRepository).addRecipe(recipe);
    }

    @Test
    public void whenNoLocalNorRemoteRecipesAreFoundAndOnlineEvaluationIsEnabled_nothingHappens() {
        // no recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.<Recipe>newArrayList());
        // online evaluation by pulse in enabled
        when(recipeRepository.shouldEvaluateOnline()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener) invocation.getArguments()[3]).onRecipeFetchError("error");
                return null;
            }
        }).when(recipesApi).onlinePulseEvaluation(anyString(), anyString(), anyString(), any(RecipesApi.SingleRecipeListener.class));

        recipesManager.handleTriggerRequest(triggerRequest);
        verifyZeroInteractions(recipeReactionHandler);
    }

    @Test
    public void whenNoLocalRecipesMatchAndSyncDoesNotRefreshData_nothingHappens() {
        // no recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.<Recipe>newArrayList());
        // online evaluation by pulse in disabled
        when(recipeRepository.shouldEvaluateOnline()).thenReturn(false);

        doAnswer(new Answer() {
                     @Override
                     public Object answer(InvocationOnMock invocation) throws Throwable {
                         ((RecipeRepository.RecipesListener) invocation.getArguments()[0]).onGotRecipes(
                                 Lists.<Recipe>newArrayList(),
                                 true,
                                 false
                         );
                         return null;
                     }
                 }
        ).when(recipeRepository).syncRecipes(any(RecipeRepository.RecipesListener.class));

        recipesManager.handleTriggerRequest(triggerRequest);
        verifyZeroInteractions(recipeReactionHandler);
    }

    @Test
    public void whenNoLocalRecipesMatchAndSyncedDataHasMatchingRecipe_thatRecipeTriggers() {
        // no recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.<Recipe>newArrayList());
        // online evaluation by pulse in disabled
        when(recipeRepository.shouldEvaluateOnline()).thenReturn(false);

        final Recipe matchingRecipe = new Recipe();
        matchingRecipe.setPulse_plugin_id(triggerRequest.plugin_name);
        PulseAction action = new PulseAction();
        action.setId(triggerRequest.plugin_action);
        matchingRecipe.setPulse_action(action);
        PulseBundle pulseBundle = new PulseBundle();
        pulseBundle.setId(triggerRequest.bundle_id);
        matchingRecipe.setPulse_bundle(pulseBundle);
        matchingRecipe.labels = Maps.newHashMap();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                when(recipeRepository.getLocalRecipes()).thenReturn(Lists.newArrayList(matchingRecipe));
                ((RecipeRepository.RecipesListener) invocation.getArguments()[0]).onGotRecipes(
                        Lists.newArrayList(matchingRecipe),
                        false,
                        true
                );
                return null;
            }
        }).when(recipeRepository).syncRecipes(any(RecipeRepository.RecipesListener.class));

        // the recipes are validated
        when(recipeValidationFilter.filterRecipes(any(List.class))).then(returnsFirstArg());

        recipesManager.handleTriggerRequest(triggerRequest);
        verify(recipeReactionHandler).gotRecipe(matchingRecipe, triggerRequest.trackingInfo);
        verifyZeroInteractions(recipesApi);
    }

    @Test
    public void whenEvOnlineSettingChangesAfterTheSync_ItGetsRespected() {
        // no recipes in the repository
        when(recipeRepository.getLocalRecipes()).thenReturn(Lists.<Recipe>newArrayList());
        // online evaluation by pulse in disabled
        when(recipeRepository.shouldEvaluateOnline()).thenReturn(false);

        final Recipe matchingRecipe = new Recipe();
        matchingRecipe.setPulse_plugin_id(triggerRequest.plugin_name);
        PulseAction action = new PulseAction();
        action.setId(triggerRequest.plugin_action);
        matchingRecipe.setPulse_action(action);
        PulseBundle pulseBundle = new PulseBundle();
        pulseBundle.setId(triggerRequest.bundle_id);
        matchingRecipe.setPulse_bundle(pulseBundle);
        matchingRecipe.labels = Maps.newHashMap();

        // mock online ev setting change
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipeRepository.RecipesListener) invocation.getArguments()[0]).onGotRecipes(
                        Lists.<Recipe>newArrayList(),
                        true,
                        true
                );
                return null;
            }
        }).when(recipeRepository).syncRecipes(any(RecipeRepository.RecipesListener.class));

        // mock evaluation by pulse
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((RecipesApi.SingleRecipeListener) invocation.getArguments()[3]).onRecipeFetchSuccess(matchingRecipe);
                return null;
            }
        }).when(recipesApi).onlinePulseEvaluation(anyString(), anyString(), anyString(), any(RecipesApi.SingleRecipeListener.class));

        recipesManager.handleTriggerRequest(triggerRequest);
        verify(recipeReactionHandler).gotRecipe(matchingRecipe, triggerRequest.trackingInfo);
        verify(recipeRepository).addRecipe(matchingRecipe);
    }

    @Test
    public void sendTracking_shouldTunnelRequest() throws JSONException {
        String recipeId = "id";
        String trackingEvent = "tr";
        recipesManager.sendTracking(triggerRequest.trackingInfo, trackingEvent);
        verify(recipeTrackSender, atLeastOnce()).sendTracking(triggerRequest.trackingInfo, trackingEvent);
    }

    private void initTriggerRequest() {
        triggerRequest = new TriggerRequest();
        triggerRequest.plugin_name = "myplugin";
        triggerRequest.plugin_action = "myAction";
        triggerRequest.bundle_id = "mybundle";
        triggerRequest.plugin_tag_action = "tag_action";
        triggerRequest.tags = Lists.newArrayList("parappa", "the rapper");
        TrackingInfo trackingInfo = new TrackingInfo();
        trackingInfo.recipeId = "reicpeId";
        HashMap<String, Object> metadata = Maps.newHashMap();
        metadata.put("chop", "chop");
        metadata.put("master", "onion");
        trackingInfo.metadata = metadata;
        triggerRequest.trackingInfo = trackingInfo;
    }

    private void buildMatchingRecipes() {
        matchingBundleRecipe = new Recipe();
        matchingBundleRecipe.setId("id1");
        matchingTagRecipe = new Recipe();
        matchingTagRecipe.setId("id2");
        matchingBundleRecipe.setPulse_plugin_id(triggerRequest.plugin_name);
        matchingTagRecipe.setPulse_plugin_id(triggerRequest.plugin_name);
        PulseAction action = new PulseAction();
        action.setId(triggerRequest.plugin_action);
        matchingBundleRecipe.setPulse_action(action);
        PulseAction tagAction = new PulseAction();
        tagAction.setId(triggerRequest.plugin_tag_action);
        matchingTagRecipe.setPulse_action(tagAction);
        PulseBundle pulseBundle = new PulseBundle();
        pulseBundle.setId(triggerRequest.bundle_id);
        matchingBundleRecipe.setPulse_bundle(pulseBundle);
        matchingTagRecipe.tags = triggerRequest.tags;
        matchingBundleRecipe.labels = Maps.newHashMap();
        matchingTagRecipe.labels = Maps.newHashMap();
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
