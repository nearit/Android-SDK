package it.near.sdk.reaction.customjson;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CustomJSONTest {

    @Test
    public void customJSONIsParcelable() {
        CustomJSON customJSON = new CustomJSON();
        HashMap content = Maps.newHashMap();
        content.put("number", 1);
        content.put("string", "2");
        content.put("boolean", true);
        content.put("arrayInt", new int[]{3,2,5,4,1});
        content.put("arrayString", new String[]{"blah", "hey", "yo"});
        customJSON.content = content;
        String customJSONId = "custom_json_id";
        customJSON.setId(customJSONId);

        Parcel parcel = Parcel.obtain();
        customJSON.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        CustomJSON actual = CustomJSON.CREATOR.createFromParcel(parcel);

        assertThat((Integer)actual.content.get("number"), is(1));
        assertThat((String)actual.content.get("string"), is("2"));
        assertThat((Boolean)actual.content.get("boolean"), is(true));
        assertThat((int[])actual.content.get("arrayInt"), is(new int[]{3,2,5,4,1}));
        assertThat((String[])actual.content.get("arrayString"), is(new String[]{"blah", "hey", "yo"}));

        assertThat(actual.getId(), is(customJSONId));
    }
}
