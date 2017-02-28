package it.near.sdk.Reaction.SimpleNotification;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.loopj.android.http.PreemptiveAuthorizationHttpRequestInterceptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Created by cattaneostefano on 28/02/2017.
 */

@RunWith(AndroidJUnit4.class)
public class SimpleNotificationTest {

    @Test
    public void simpleNotificationIsParcelable() {
        SimpleNotification simpleNotification = new SimpleNotification("message", "title");
        Parcel parcel = Parcel.obtain();
        simpleNotification.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        SimpleNotification actual = SimpleNotification.CREATOR.createFromParcel(parcel);
        assertEquals(simpleNotification.getNotificationMessage(), actual.getNotificationMessage());
        assertEquals(simpleNotification.getNotificationTitle(), actual.getNotificationTitle());


        SimpleNotification simpleNotificationNoTitle = new SimpleNotification("message", null);
        parcel = Parcel.obtain();
        simpleNotificationNoTitle.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        actual = SimpleNotification.CREATOR.createFromParcel(parcel);
        assertEquals(simpleNotification.getNotificationMessage(), actual.getNotificationMessage());
        assertNull(actual.getNotificationTitle());
    }
}
