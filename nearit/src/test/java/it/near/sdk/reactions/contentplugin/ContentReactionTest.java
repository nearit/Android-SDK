package it.near.sdk.reactions.contentplugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.reactions.BaseReactionTest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ContentReactionTest extends BaseReactionTest<ContentReaction> {

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        reaction = mock(ContentReaction.class);
        setUpMockForRealMethods();
    }

    @Test
    public void pluginNameIsReturned() {
        assertThat(reaction.getReactionPluginName(), is(ContentReaction.PLUGIN_NAME));
    }

    @Test
    public void refreshUrlShouldBeReturned() {
        assertThat(reaction.getRefreshUrl(),
                is("plugins/content-notification/contents?include=images,audio,upload".replace(",", "%2C")));
    }

    @Test
    public void singleReactionUrlShouldBeReturned() {
        String bundleId = "ididididi";
        assertThat(reaction.getSingleReactionUrl(bundleId),
                is(("plugins/content-notification/contents/" + bundleId + "?include=images,audio,upload").replace(",", "%2C")));
    }

    @Test
    public void defaultShowActionShouldBeReturned() {
        assertThat(reaction.getDefaultShowAction(),
                is(ContentReaction.SHOW_CONTENT_ACTION));
    }

}