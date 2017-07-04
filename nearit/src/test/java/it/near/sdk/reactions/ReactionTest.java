package it.near.sdk.reactions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.recipes.models.ReactionAction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReactionTest {

    private static final String TEST_PLUGIN_NAME = "test_plugin";

    private Reaction reactionUT;

    @Before
    public void setUp() throws Exception {
        reactionUT = mock(Reaction.class);

        doCallRealMethod().when(reactionUT).handleReaction(any(Recipe.class));
        when(reactionUT.getReactionPluginName()).thenReturn(TEST_PLUGIN_NAME);

    }

    @Test
    public void shouldCheckPluginName() throws Exception {
        ReactionAction dummyReactionAction = new ReactionAction();
        String dummyReactionActionId = "action_id";
        dummyReactionAction.setId(dummyReactionActionId);
        ReactionBundle dummyReactionBundle = new ReactionBundle();
        Recipe recipe = new Recipe();
        recipe.reaction_plugin_id = "wrong_plugin_name";
        recipe.reaction_action = dummyReactionAction;
        recipe.reaction_bundle = dummyReactionBundle;
        reactionUT.handleReaction(recipe);
        verify(reactionUT, never()).handleReaction(anyString(), any(ReactionBundle.class), eq(recipe));

        recipe.reaction_plugin_id = TEST_PLUGIN_NAME;
        reactionUT.handleReaction(recipe);

        verify(reactionUT, atLeastOnce()).handleReaction(eq(dummyReactionActionId), any(ReactionBundle.class), eq(recipe));

    }
}