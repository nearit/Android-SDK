package it.near.sdk.reactions.simplenotificationplugin.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.recipes.models.Recipe;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleNotificationTest {

    @Test
    public void shouldCreateSimpleNotification_fromRecipe() throws Exception {
        Recipe recipe = mock(Recipe.class);
        when(recipe.getNotificationBody()).thenReturn("body");
        when(recipe.getNotificationTitle()).thenReturn("title");

        SimpleNotification simpleNotification = SimpleNotification.fromRecipe(recipe);

        assertThat(simpleNotification.getNotificationMessage(), is("body"));
        assertThat(simpleNotification.getNotificationTitle(), is("title"));

        when(recipe.getNotificationTitle()).thenReturn(null);

        simpleNotification = SimpleNotification.fromRecipe(recipe);

        assertThat(simpleNotification.getNotificationMessage(), is("body"));
        assertNull(simpleNotification.getNotificationTitle());

    }

    @Test
    public void shouldCreateSimpleNotification_fromNotificationText() throws Exception {
        String notification = "notification";

        SimpleNotification simpleNotification = SimpleNotification.fromNotificationText(notification);

        assertThat(simpleNotification.getNotificationMessage(), is(notification));
        assertNull(simpleNotification.getNotificationTitle());

    }
}