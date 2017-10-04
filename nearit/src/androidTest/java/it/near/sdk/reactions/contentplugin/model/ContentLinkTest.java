package it.near.sdk.reactions.contentplugin.model;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ContentLinkTest {

    @Test
    public void isParcelable() {
        ContentLink contentLink = new ContentLink("a", "b");
        Parcel parcel = Parcel.obtain();
        contentLink.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        ContentLink actual = ContentLink.CREATOR.createFromParcel(parcel);
        assertNotNull(actual);
        assertThat(actual.label, is(contentLink.label));
        assertThat(actual.url, is(contentLink.url));
    }



}