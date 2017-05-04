package it.near.sdk.push.fcmregistration;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.near.sdk.communication.NearInstallation;
import it.near.sdk.GlobalConfig;
import it.near.sdk.logging.NearLog;

/**
 * Handles token refreshes. When a new device token is obtained it triggers a remote registration.
 * It either creates or update an installation.
 *
 * @author cattaneostefano
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "NearInstIDListenerServ";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        NearLog.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist registration to NearIt servers.
     * <p>
     * Modify this method to associate the user's Fcm registration token with any server-side account
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
