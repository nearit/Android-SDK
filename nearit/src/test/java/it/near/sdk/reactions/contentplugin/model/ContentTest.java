package it.near.sdk.reactions.contentplugin.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class ContentTest {

    private Content content;

    @Before
    public void setUp() {
        content = new Content();
    }

    @Test
    public void shouldNotHaveContentToInclude() {
        assertThat(content.hasContentToInclude(), is(false));
        // also if the images array is empty
        content.images = new ArrayList<>();
        assertThat(content.hasContentToInclude(), is(false));
    }

    @Test
    public void contentWithImages_shouldHaveContentToInclude() {
        content.images = new ArrayList<Image>();
        content.images.add(new Image());
        assertThat(content.hasContentToInclude(), is(true));
    }

    @Test
    public void contentWithAudio_shouldHaveContentToInclude() {
        content.audio = new Audio();
        assertThat(content.hasContentToInclude(), is(true));
    }

    @Test
    public void contentWithUpload_shouldHaveContentToInclude() {
        content.upload = new Upload();
        assertThat(content.hasContentToInclude(), is(true));
    }

}
