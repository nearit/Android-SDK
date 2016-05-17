package it.near.sdk.Push.GCMRegistration;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import it.near.sdk.GlobalConfig;
import it.near.sdk.Push.GCMRegistration.RegistrationIntentService;
import it.near.sdk.Utils.ULog;

/**
 * Handles token refreshes. When a new device token is obtained it triggers a remote registration.
 * It either creates or update an installation.
 *
 * @author cattaneostefano
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDListenerService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        ULog.d(TAG , "onToken Refresh");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra(RegistrationIntentService.SENDER_ID, GlobalConfig.getInstance(this).getSenderId());
        startService(intent);
    }
}
