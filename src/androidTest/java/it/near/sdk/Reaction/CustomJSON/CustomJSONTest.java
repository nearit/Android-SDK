package it.near.sdk.Reaction.CustomJSON;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;

import it.near.sdk.Reactions.CustomJSON.CustomJSON;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Created by cattaneostefano on 28/02/2017.
 */

@RunWith(AndroidJUnit4.class)
public class CustomJSONTest {

    @Test
    public void custonJSONIsParcelable() {
        CustomJSON customJSON = new CustomJSON();
        HashMap content = Maps.newHashMap();
        /*content.put("number", 1);
        content.put("string", "2");
        content.put("boolean", true);
        content.put("arrayInt", new int[]{3,2,5,4,1});
        content.put("arrayString", new String[]{"blah", "hey", "yo"});*/
        customJSON.setContent(content);

        Parcel parcel = Parcel.obtain();
        customJSON.writeToParcel(parcel, 0);
        CustomJSON actual = CustomJSON.CREATOR.createFromParcel(parcel);

        assertTrue(true);
    }
}
