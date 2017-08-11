package it.near.sdk.trackings;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TrackingInfoTest {

    @Test
    public void trackingInfoIsParcelable() {
        TrackingInfo trackingInfo = new TrackingInfo();
        trackingInfo.recipeId = "recipeId";
        HashMap<String, Object> metadata = Maps.newHashMap();
        metadata.put("String", "I'm a string");
        metadata.put("List of strings", Lists.newArrayList("one", "two"));
        metadata.put("Number", 4049L);
        trackingInfo.metadata = metadata;
        Parcel parcel = Parcel.obtain();
        trackingInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        TrackingInfo actual = TrackingInfo.CREATOR.createFromParcel(parcel);
        assertThat(actual.recipeId, is(trackingInfo.recipeId));
        assertThat(actual.metadata, is(trackingInfo.metadata));
    }

}
