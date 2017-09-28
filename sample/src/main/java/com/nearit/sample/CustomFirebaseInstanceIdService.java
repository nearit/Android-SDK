package com.nearit.sample;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.near.sdk.push.fcmregistration.NearInstanceIDListenerService;


public class CustomFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        NearInstanceIDListenerService.sendRegistrationToServer(refreshedToken);
    }
}
