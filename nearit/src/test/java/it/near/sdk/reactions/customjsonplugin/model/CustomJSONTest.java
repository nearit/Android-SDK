package it.near.sdk.reactions.customjsonplugin.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class CustomJSONTest {

    private CustomJSON customJSON;

    @Before
    public void setUp() throws Exception {
        customJSON = new CustomJSON();
    }

    @Test
    public void shouldNotHaveContentToInclude() {
        assertThat(customJSON.hasContentToInclude(), is(false));
    }
}
