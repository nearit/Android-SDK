package it.near.sdk.recipes.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReactionBundleTest {

    private ReactionBundle reactionBundle;

    @Before
    public void setUp() {
        reactionBundle = mock(ReactionBundle.class);
        when(reactionBundle.hasContentToInclude()).thenCallRealMethod();
    }

    @Test
    public void defaultHasContentToIncludeBehavior_shouldBeFalse() {
        assertThat(reactionBundle.hasContentToInclude(), is(false));
    }

}