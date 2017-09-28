package it.near.sdk.push.fcmregistration;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.near.sdk.NearItManager;
import it.near.sdk.logging.NearLog;

/**
 * Handles token refreshes. When a new device token is obtained it triggers a remote registration.
 * It either creates or update an installation.
 *
 * @author cattaneostefano
 */
public class NearInstanceIDListenerService extends FirebaseInstanceIdService {

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
    public static void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        NearItManager nearItManager = NearItManager.getInstance();
        nearItManager.globalConfig.setDeviceToken(token);
        nearItManager.updateInstallation();
    }
}
