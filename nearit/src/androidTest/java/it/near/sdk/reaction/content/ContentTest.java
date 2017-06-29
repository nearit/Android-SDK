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
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.contentplugin.model.Upload;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
        Audio audio = new Audio();
        HashMap<String, Object> audioMap = Maps.newHashMap();
        audioMap.put("url", "a.mp3");
        audio.audioMap = audioMap;
        content.audio = audio;
        Upload upload = new Upload();
        HashMap<String, Object> uploadMap = Maps.newHashMap();
        uploadMap.put("url", "a.pdf");
        upload.setUpload(uploadMap);
        content.upload = upload;
        Parcel parcel = Parcel.obtain();
        content.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Content actual = Content.CREATOR.createFromParcel(parcel);
        assertEquals(content.contentString, actual.contentString);
        assertThat(content.getImages_links(), containsInAnyOrder(actual.getImages_links().toArray()));
        assertEquals(content.video_link, actual.video_link);
        assertEquals(content.getId(), actual.getId());
        assertEquals(content.updated_at, actual.updated_at);
        assertEquals(content.audio.audioMap, actual.audio.audioMap);
        assertEquals(content.upload.getUpload(), actual.upload.getUpload());
    }

}
