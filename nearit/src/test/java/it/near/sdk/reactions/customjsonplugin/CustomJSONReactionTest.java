package it.near.sdk.reactions.customjsonplugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import it.near.sdk.reactions.BaseReactionTest;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;

import static it.near.sdk.reactions.customjsonplugin.CustomJSONReaction.JSON_CONTENT_RES;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CustomJSONReactionTest extends BaseReactionTest<CustomJSONReaction> {

    @Before
    public void setUp() throws Exception {
        reaction = mock(CustomJSONReaction.class);
        setUpMockForRealMethods();
        doCallRealMethod().when(reaction).injectRecipeId(any(CustomJSON.class), anyString());
        doCallRealMethod().when(reaction).normalizeElement(any(CustomJSON.class));
    }

    @Test
    public void pluginNameIsReturned() {
        assertThat(reaction.getReactionPluginName(), is(CustomJSONReaction.PLUGIN_NAME));
    }

    @Test
    public void refreshUrlShouldBeReturned() {
        assertThat(reaction.getRefreshUrl(),
                is("plugins/json-sender/json_contents"));
    }

    @Test
    public void singleReactionUrlShouldBeReturned() {
        String bundleId = "ididididi";
        assertThat(reaction.getSingleReactionUrl(bundleId),
                is("plugins/json-sender/json_contents/" + bundleId));
    }

    @Test
    public void defaultShowActionShouldBeReturned() {
        assertThat(reaction.getDefaultShowAction(),
                is(CustomJSONReaction.SHOW_JSON_ACTION));
    }

    @Test
    public void injectRecipeIdDoesNothing() {
        CustomJSON customJson = mock(CustomJSON.class);
        reaction.injectRecipeId(customJson, "recipe_id");
        verifyZeroInteractions(customJson);
    }

    @Test
    public void normalizeElementDoesNothing() {
        CustomJSON customJSON = spy(new CustomJSON());
        reaction.normalizeElement(customJSON);
        verifyZeroInteractions(customJSON);
    }

    @Test
    public void shouldReturnModelMap() {
        HashMap<String, Class> map = reaction.getModelHashMap();
        assertThat(map.get(JSON_CONTENT_RES), is((Object) CustomJSON.class));
    }


}