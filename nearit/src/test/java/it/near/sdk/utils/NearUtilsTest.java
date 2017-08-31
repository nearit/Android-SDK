package it.near.sdk.utils;

import android.content.Intent;
import android.os.Parcelable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.trackings.TrackingInfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


/**
 * Created by cattaneostefano on 28/02/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class NearUtilsTest {

    @Mock
    private Intent mockIntent;

    @Mock
    CoreContentsListener mockListener;

    @Mock
    TrackingInfo trackingInfo;

    @Test
    public void parseContent_ofTypeContent(){
        Content content = new Content();
        configMockFor(content);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotContentNotification(content, trackingInfo);
    }

    @Test
    public void parseContent_ofTypeSimpleContent() {
        SimpleNotification simpleNotification = new SimpleNotification("","");
        configMockFor(simpleNotification);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotSimpleNotification(simpleNotification, trackingInfo);
    }

    @Test
    public void parseContent_ofTypeCoupon() {
        Coupon coupon = new Coupon();
        configMockFor(coupon);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotCouponNotification(coupon, trackingInfo);
    }

    @Test
    public void parseContent_ofTypeCustomJSON() {
        CustomJSON customJson = new CustomJSON();
        configMockFor(customJson);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotCustomJSONNotification(customJson, trackingInfo);
    }

    @Test
    public void parseContent_ofTypeFeedback() {
        Feedback feedback = new Feedback();
        configMockFor(feedback);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotFeedbackNotification(feedback, trackingInfo);
    }

    @Test
    public void parseContent_ofUnsupportedType() {
        Parcelable p = mock(Parcelable.class);
        configMockFor(p);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verifyZeroInteractions(mockListener);

    }

    private void configMockFor(Parcelable reaction) {
        when(mockIntent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO))
                .thenReturn(trackingInfo);
        when(mockIntent.hasExtra(NearItIntentConstants.CONTENT)).thenReturn(true);
        when(mockIntent.getParcelableExtra(NearItIntentConstants.CONTENT))
                .thenReturn(reaction);
    }
}
