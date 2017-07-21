package it.near.sdk.reactions.feedbackplugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import it.near.sdk.reactions.BaseReactionTest;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;

import static it.near.sdk.reactions.feedbackplugin.FeedbackReaction.FEEDBACKS_NOTIFICATION_RESOURCE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackReactionTest extends BaseReactionTest<FeedbackReaction> {

    @Before
    public void setUp() throws Exception {
        reaction = mock(FeedbackReaction.class);
        setUpMockForRealMethods();
        doCallRealMethod().when(reaction).injectRecipeId(any(Feedback.class), anyString());
        doCallRealMethod().when(reaction).normalizeElement(any(Feedback.class));
    }

    @Test
    public void pluginNameIsReturned() {
        assertThat(reaction.getReactionPluginName(), is(FeedbackReaction.PLUGIN_NAME));
    }

    @Test
    public void refreshUrlShouldBeReturned() {
        assertThat(reaction.getRefreshUrl(), is("plugins/feedbacks/feedbacks"));
    }

    @Test
    public void singleReactionUrlShouldBeReturned() {
        String bundleId = "kdfksdfnfdsjf";
        assertThat(reaction.getSingleReactionUrl(bundleId),
                is("plugins/feedbacks/feedbacks/" + bundleId));
    }

    @Test
    public void defaultShowActionShouldBeReturned() {
        assertThat(reaction.getDefaultShowAction(),
                is(FeedbackReaction.ASK_FEEDBACK_ACTION_NAME));
    }

    @Test
    public void injectRecipeShouldInjectRecipeId() {
        Feedback feedback = new Feedback();
        String recipeId = "recipeId";
        reaction.injectRecipeId(feedback, recipeId);
        assertThat(feedback.getRecipeId(), is(recipeId));
    }

    @Test
    public void normalizeElementDoesNothing() {
        Feedback feedback = spy(new Feedback());
        reaction.normalizeElement(feedback);
        verifyZeroInteractions(feedback);
    }

    @Test
    public void shouldReturnModelsMap() {
        HashMap<String, Class> map = reaction.getModelHashMap();
        assertThat(map.get(FEEDBACKS_NOTIFICATION_RESOURCE), is((Object)Feedback.class));
    }
}
