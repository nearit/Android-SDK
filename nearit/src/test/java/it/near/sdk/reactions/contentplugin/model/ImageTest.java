package it.near.sdk.reactions.contentplugin.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class ImageTest {

    private Image image;

    @Before
    public void setUp() throws Exception {
        image = new Image();
    }

    @Test
    public void whenAllDataAvailable_shouldConvert() throws Image.MissingImageException {
        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("url", "my-url");
        HashMap<String, Object> squareMap = new HashMap<>();
        squareMap.put("url", "square-url");
        imageMap.put("square_300", squareMap);
        image.imageMap = imageMap;
        ImageSet imageSet = image.toImageSet();
        assertNotNull(imageSet);
        assertThat(imageSet.getFullSize(), is("my-url"));
        assertThat(imageSet.getSmallSize(), is("square-url"));
    }

    @Test
    public void whenMissingSquareImage_shouldStillConvert() throws Image.MissingImageException {
        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("url", "my-url");
        image.imageMap = imageMap;
        ImageSet imageSet = image.toImageSet();
        assertNotNull(imageSet);
        assertThat(imageSet.getFullSize(), is("my-url"));
        assertNull(imageSet.getSmallSize());
    }

    @Test(expected = Image.MissingImageException.class)
    public void whenMissingUrl_shouldThrow() throws Image.MissingImageException {
        HashMap<String, Object> imageMap = new HashMap<>();
        image.imageMap = imageMap;
        image.toImageSet();
    }

    @Test(expected = Image.MissingImageException.class)
    public void whenMissingMap_shouldThrow() throws Image.MissingImageException {
        image.toImageSet();
    }


}