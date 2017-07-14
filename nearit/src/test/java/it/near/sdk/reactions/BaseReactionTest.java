package it.near.sdk.reactions;

import org.hamcrest.Matchers;

import it.near.sdk.recipes.models.ReactionBundle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyVararg;
import static org.mockito.Mockito.when;

public class BaseReactionTest<T extends CoreReaction<? extends ReactionBundle>> {

    protected T reaction;

    protected void setUpMockForRealMethods() {
        when(reaction.getReactionPluginName()).thenCallRealMethod();
        when(reaction.getRefreshUrl()).thenCallRealMethod();
        when(reaction.getSingleReactionUrl(anyString())).thenCallRealMethod();
        when(reaction.getDefaultShowAction()).thenCallRealMethod();
        when(reaction.injectRecipeId(any(ReactionBundle.class), anyString());
    }
}
