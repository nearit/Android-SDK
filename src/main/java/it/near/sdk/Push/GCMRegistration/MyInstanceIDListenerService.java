package it.near.sdk.Push.GCMRegistration;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.near.sdk.Communication.NearInstallation;
import it.near.sdk.GlobalConfig;
import it.near.sdk.Utils.ULog;

/**
 * Handles token refreshes. When a new device token is obtained it triggers a remote registration.
 * It either creates or update an installation.
 *
 * @author cattaneostefano
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDListenerService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        ULog.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist registration to NearIt servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        GlobalConfig.getInstance(this.getApplicationContext()).setDeviceToken(token);
        NearInstallation.registerInstallation(this.getApplicationContext());
    }
}
