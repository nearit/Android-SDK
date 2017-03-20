package it.near.sdk.Reaction.Content;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import it.near.sdk.reactions.content.Upload;

import static junit.framework.Assert.*;

/**
 * Created by cattaneostefano on 02/03/2017.
 */

@RunWith(AndroidJUnit4.class)
public class UploadTest {

    @Test
    public void uploadIsParcelable() {
        Upload upload = new Upload();
        upload.setId("upload_id");
        HashMap<String, Object> map = Maps.newHashMap();
        String pdfUrl = "http://jsnfijegfuien.pdf";
        map.put("url", pdfUrl);
        upload.setUpload(map);
        Parcel parcel = Parcel.obtain();
        upload.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Upload actual = Upload.CREATOR.createFromParcel(parcel);
        assertEquals(upload.getId(), actual.getId());
        assertEquals(upload.getUpload(), actual.getUpload());
        assertEquals(pdfUrl, actual.getUrl());
    }
}
