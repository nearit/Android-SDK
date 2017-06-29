package it.near.sdk.reaction.content;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import it.near.sdk.reactions.contentplugin.model.Upload;

import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UploadTest {

    @Test
    public void uploadIsParcelable() {
        Upload upload = new Upload();
        upload.setId("upload_id");
        HashMap<String, Object> map = Maps.newHashMap();
        String pdfUrl = "http://jsnfijegfuien.pdf";
        map.put("url", pdfUrl);
        upload.uploadMap = (map);
        Parcel parcel = Parcel.obtain();
        upload.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Upload actual = Upload.CREATOR.createFromParcel(parcel);
        assertEquals(upload.getId(), actual.getId());
        assertEquals(upload.uploadMap, actual.uploadMap);
        assertEquals(pdfUrl, actual.getUrl());
    }
}
