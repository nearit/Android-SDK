package it.near.sdk.reactions.contentplugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;

import it.near.sdk.reactions.BaseReactionTest;
import it.near.sdk.reactions.contentplugin.model.Audio;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.contentplugin.model.Upload;

import static it.near.sdk.reactions.contentplugin.ContentReaction.RES_AUDIOS;
import static it.near.sdk.reactions.contentplugin.ContentReaction.RES_CONTENTS;
import static it.near.sdk.reactions.contentplugin.ContentReaction.RES_IMAGES;
import static it.near.sdk.reactions.contentplugin.ContentReaction.RES_UPLOADS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentReactionTest extends BaseReactionTest<ContentReaction> {

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        reaction = mock(ContentReaction.class);
        setUpMockForRealMethods();
        doCallRealMethod().when(reaction).injectRecipeId(any(Content.class), anyString());
        doCallRealMethod().when(reaction).normalizeElement(any(Content.class));
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

    @Test
    public void injectRecipeIdDoesNothing() {
        Content content = mock(Content.class);
        reaction.injectRecipeId(content, "recipe_id");
        verifyZeroInteractions(content);
    }

    @Test
    public void normalizeElementShouldNormalizeImages() {
        Content content = new Content();
        content.images = new ArrayList<>();

        Image image1 = mock(Image.class);
        ImageSet imageSet1 = new ImageSet();
        when(image1.toImageSet()).thenReturn(imageSet1);

        Image image2 = mock(Image.class);
        ImageSet imageSet2 = new ImageSet();
        when(image2.toImageSet()).thenReturn(imageSet2);

        content.images.add(image1);
        content.images.add(image2);

        reaction.normalizeElement(content);

        assertThat(content.getImages_links(), hasItems(imageSet1, imageSet2));
    }

    @Test
    public void shouldReturnModelMap() {
        HashMap<String, Class> modelMap = reaction.getModelHashMap();
        assertThat(modelMap.get(RES_CONTENTS), is((Object) Content.class));
        assertThat(modelMap.get(RES_IMAGES), is((Object) Image.class));
        assertThat(modelMap.get(RES_AUDIOS), is((Object) Audio.class));
        assertThat(modelMap.get(RES_UPLOADS), is((Object) Upload.class));
    }

}