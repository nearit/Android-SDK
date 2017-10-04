package it.near.sdk.reaction.content;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import it.near.sdk.reactions.contentplugin.model.Audio;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.contentplugin.model.ContentLink;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.contentplugin.model.Upload;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by cattaneostefano on 28/02/2017.
 */

@RunWith(AndroidJUnit4.class)
public class ContentTest {

    @Test
    public void contentIsParcelable() {
        Content content = new Content();
        content.contentString = "content";
        content.setImages_links(Lists.newArrayList(new ImageSet(), new ImageSet()));
        content.video_link = "video_link";
        content.setId("content_id");
        content.updated_at = "updated@whatever";
        content.title = "title";
        Audio audio = new Audio();
        HashMap<String, Object> audioMap = Maps.newHashMap();
        audioMap.put("url", "a.mp3");
        audio.audioMap = audioMap;
        content.audio = audio;
        Upload upload = new Upload();
        HashMap<String, Object> uploadMap = Maps.newHashMap();
        uploadMap.put("url", "a.pdf");
        upload.uploadMap = uploadMap;
        content.upload = upload;
        content.notificationMessage = "fejrf";
        ContentLink contentLink = new ContentLink("a", "b");
        content.setCta(contentLink);

        Parcel parcel = Parcel.obtain();
        content.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Content actual = Content.CREATOR.createFromParcel(parcel);
        assertThat(content.contentString, is(actual.contentString));
        assertThat(content.getImages_links(), containsInAnyOrder(actual.getImages_links().toArray()));
        assertThat(content.video_link, is(actual.video_link));
        assertThat(content.getId(), is(actual.getId()));
        assertThat(content.updated_at, is(actual.updated_at));
        assertThat(content.audio.audioMap, is(actual.audio.audioMap));
        assertThat(content.upload.uploadMap, is(actual.upload.uploadMap));
        assertThat(content.notificationMessage, is(actual.notificationMessage));
        assertThat(content.title, is(actual.title));
        assertThat(content.getCta(), is(actual.getCta()));
        assertThat(content.getImageLink(), is(actual.getImageLink()));
    }

}
