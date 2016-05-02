package it.near.sdk.Push;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import it.near.sdk.GlobalConfig;
import it.near.sdk.Utils.ULog;

/**
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
