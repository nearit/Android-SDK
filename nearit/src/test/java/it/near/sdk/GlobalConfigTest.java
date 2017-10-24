package it.near.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.logging.NearLog;

import static it.near.sdk.GlobalConfig.APIKEY;
import static it.near.sdk.GlobalConfig.APPID;
import static it.near.sdk.GlobalConfig.DEFAULT_EMPTY_NOTIFICATION;
import static it.near.sdk.GlobalConfig.DEFAULT_OPT_OUT;
import static it.near.sdk.GlobalConfig.DEVICETOKEN;
import static it.near.sdk.GlobalConfig.INSTALLATIONID;
import static it.near.sdk.GlobalConfig.KEY_PROXIMITY_ICON;
import static it.near.sdk.GlobalConfig.KEY_PUSH_ICON;
import static it.near.sdk.GlobalConfig.OPT_OUT;
import static it.near.sdk.GlobalConfig.PREFS_NAME;
import static it.near.sdk.GlobalConfig.PROFILE_ID;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalConfigTest {

    private GlobalConfig globalConfig;

    @Mock
    SharedPreferences mockSharedPreferences;
    @Mock
    SharedPreferences.Editor mockEditor;
    @Mock
    GlobalConfig.OptOutListener mockOptOutListener;

    @Before
    public void setUp() throws Exception {
        NearLog.setLogger(TestUtils.emptyLogger());
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        globalConfig = new GlobalConfig(mockSharedPreferences);
        globalConfig.setOptOutListener(mockOptOutListener);
    }

    @Test
    public void returnsDefaultValue() {
        when(mockSharedPreferences.getInt(KEY_PROXIMITY_ICON, DEFAULT_EMPTY_NOTIFICATION)).thenReturn(DEFAULT_EMPTY_NOTIFICATION);
        when(mockSharedPreferences.getInt(KEY_PUSH_ICON, DEFAULT_EMPTY_NOTIFICATION)).thenReturn(DEFAULT_EMPTY_NOTIFICATION);
        assertThat(globalConfig.getOptOut(), is(DEFAULT_OPT_OUT));
        assertThat(globalConfig.getProximityNotificationIcon(), is(DEFAULT_EMPTY_NOTIFICATION));
        assertThat(globalConfig.getPushNotificationIcon(), is(DEFAULT_EMPTY_NOTIFICATION));
        assertNull(globalConfig.getAppId());
        assertNull(globalConfig.getProfileId());
        assertNull(globalConfig.getInstallationId());
        assertNull(globalConfig.getDeviceToken());
    }

    @Test(expected = AuthenticationException.class)
    public void missingApiToken_throws() throws AuthenticationException {
        globalConfig.getApiKey();
    }

    @Test
    public void returnPersistedValues() throws AuthenticationException {
        when(mockSharedPreferences.getBoolean(eq(OPT_OUT), anyBoolean())).thenReturn(DEFAULT_OPT_OUT);
        assertThat(globalConfig.getOptOut(), is(DEFAULT_OPT_OUT));

        int proxIconRes = 6;
        when(mockSharedPreferences.getInt(eq(KEY_PROXIMITY_ICON), anyInt())).thenReturn(proxIconRes);
        assertThat(globalConfig.getProximityNotificationIcon(), is(proxIconRes));

        int pushIconRes = 7;
        when(mockSharedPreferences.getInt(eq(KEY_PUSH_ICON), anyInt())).thenReturn(pushIconRes);
        assertThat(globalConfig.getPushNotificationIcon(), is(pushIconRes));

        String appId = "appId";
        when(mockSharedPreferences.getString(eq(APPID), nullable(String.class))).thenReturn(appId);
        assertThat(globalConfig.getAppId(), is(appId));

        String deviceToken = "deviceToken";
        when(mockSharedPreferences.getString(eq(DEVICETOKEN), nullable(String.class))).thenReturn(deviceToken);
        assertThat(globalConfig.getDeviceToken(), is(deviceToken));

        String installationId = "installationId";
        when(mockSharedPreferences.getString(eq(INSTALLATIONID), nullable(String.class))).thenReturn(installationId);
        assertThat(globalConfig.getInstallationId(), is(installationId));

        String apiKey = "apiKey";
        when(mockSharedPreferences.getString(eq(APIKEY), nullable(String.class))).thenReturn(apiKey);
        assertThat(globalConfig.getApiKey(), is(apiKey));

        String profileId = "profileId";
        when(mockSharedPreferences.getString(eq(PROFILE_ID), nullable(String.class))).thenReturn(profileId);
        assertThat(globalConfig.getProfileId(), is(profileId));
    }

    @Test
    public void testPersistence() {
        globalConfig.setOptOut();
        verify(mockEditor, atLeastOnce()).putBoolean(OPT_OUT, true);

        String appId = "appId";
        globalConfig.setAppId(appId);
        verify(mockEditor, atLeastOnce()).putString(APPID, appId);

        String deviceToken = "device_token";
        globalConfig.setDeviceToken(deviceToken);
        verify(mockEditor, atLeastOnce()).putString(DEVICETOKEN, deviceToken);

        String installationId = "installationId";
        globalConfig.setInstallationId(installationId);
        verify(mockEditor, atLeastOnce()).putString(INSTALLATIONID, installationId);

        String apiKey = "apiKey";
        globalConfig.setApiKey(apiKey);
        verify(mockEditor, atLeastOnce()).putString(APIKEY, apiKey);

        String profileId = "profileId";
        globalConfig.setProfileId(profileId);
        verify(mockEditor, atLeastOnce()).putString(PROFILE_ID, profileId);

        int proximityNotificationRes = 6;
        globalConfig.setProximityNotificationIcon(proximityNotificationRes);
        verify(mockEditor, atLeastOnce()).putInt(KEY_PROXIMITY_ICON, proximityNotificationRes);

        int pushNotificationIcon = 7;
        globalConfig.setPushNotificationIcon(pushNotificationIcon);
        verify(mockEditor, atLeastOnce()).putInt(KEY_PUSH_ICON, pushNotificationIcon);
    }

    @Test
    public void testOptOut() {
        when(mockSharedPreferences.getBoolean(eq(OPT_OUT), anyBoolean())).thenReturn(false);
        globalConfig.setOptOut();
        verify(mockOptOutListener, atLeastOnce()).onOptOut();
    }

    @Test
    public void testOptOutIdempotent() {
        when(mockSharedPreferences.getBoolean(eq(OPT_OUT), anyBoolean())).thenReturn(true);
        globalConfig.setOptOut();
        verifyZeroInteractions(mockOptOutListener);
    }

    @Test
    public void prefBuilderReturnsPrefs() {
        Context context = mock(Context.class);
        GlobalConfig.buildSharedPreferences(context);
        verify(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

}