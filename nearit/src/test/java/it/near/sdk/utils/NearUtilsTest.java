package it.near.sdk.utils;

import android.content.Intent;
import android.os.Parcelable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.reactions.contentplugin.ContentReaction;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.CouponReaction;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.CustomJSONReaction;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.FeedbackReaction;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.SimpleNotificationReaction;
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

    public static final String DUMMY_NOTIF_BODY = "notif_body";
    @Mock
    private Intent mockIntent;

    @Mock
    CoreContentsListener mockListener;

    @Mock
    TrackingInfo trackingInfo;

    @Test
    public void parseContent_ofTypeContent(){
        Content content = new Content();
        configMockFor(ContentReaction.PLUGIN_NAME, content);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotContentNotification(mockIntent, content, trackingInfo, DUMMY_NOTIF_BODY);
    }

    @Test
    public void parseContent_ofTypeSimpleContent() {
        SimpleNotification simpleNotification = new SimpleNotification("","");
        configMockFor(SimpleNotificationReaction.PLUGIN_NAME, simpleNotification);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotSimpleNotification(mockIntent, simpleNotification, trackingInfo, DUMMY_NOTIF_BODY);
    }

    @Test
    public void parseContent_ofTypeCoupon() {
        Coupon coupon = new Coupon();
        configMockFor(CouponReaction.PLUGIN_NAME, coupon);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotCouponNotification(mockIntent, coupon, trackingInfo, DUMMY_NOTIF_BODY);
    }

    @Test
    public void parseContent_ofTypeCustomJSON() {
        CustomJSON customJson = new CustomJSON();
        configMockFor(CustomJSONReaction.PLUGIN_NAME, customJson);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotCustomJSONNotification(mockIntent, customJson, trackingInfo, DUMMY_NOTIF_BODY);
    }

    @Test
    public void parseContent_ofTypeFeedback() {
        Feedback feedback = new Feedback();
        configMockFor(FeedbackReaction.PLUGIN_NAME, feedback);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verify(mockListener).gotFeedbackNotification(mockIntent, feedback, trackingInfo, DUMMY_NOTIF_BODY);
    }

    @Test
    public void parseContent_ofUnsupportedType() {
        Parcelable p = mock(Parcelable.class);
        configMockFor("Unsupported type", p);
        NearUtils.parseCoreContents(mockIntent, mockListener);
        verifyZeroInteractions(mockListener);

    }

    private void configMockFor(String plugin_name, Parcelable reaction) {
        when(mockIntent.getStringExtra(NearItIntentConstants.REACTION_PLUGIN))
                .thenReturn(plugin_name);
        when(mockIntent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO))
                .thenReturn(trackingInfo);
        when(mockIntent.getStringExtra(NearItIntentConstants.NOTIF_BODY))
                .thenReturn(DUMMY_NOTIF_BODY);
        when(mockIntent.hasExtra(NearItIntentConstants.CONTENT)).thenReturn(true);
        when(mockIntent.getParcelableExtra(NearItIntentConstants.CONTENT))
                .thenReturn(reaction);
    }

}
