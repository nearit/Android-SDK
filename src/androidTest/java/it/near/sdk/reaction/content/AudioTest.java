package it.near.sdk.reaction.content;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import it.near.sdk.reactions.content.Audio;

import static junit.framework.Assert.*;

/**
 * Created by cattaneostefano on 02/03/2017.
 */

@RunWith(AndroidJUnit4.class)
public class AudioTest {

    @Test
    public void audioIsParcelable() {
        Audio audio = new Audio();
        HashMap<String, Object> map = Maps.newHashMap();
        String audioUrl = "http://jsnfijegfuien.mp3";
        map.put("url", audioUrl);
        audio.setAudio(map);
        audio.setId("audio_id");
        Parcel parcel = Parcel.obtain();
        audio.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Audio actual = Audio.CREATOR.createFromParcel(parcel);
        assertEquals(audio.getAudio(), actual.getAudio());
        assertEquals(audioUrl, actual.getUrl());
        assertEquals(audio.getId(), actual.getId());
    }

}
