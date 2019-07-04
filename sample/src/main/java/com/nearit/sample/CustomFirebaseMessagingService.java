package com.nearit.sample;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import it.near.sdk.push.NearFcmListenerService;


public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        NearFcmListenerService.processRemoteMessage(remoteMessage);
    }

    @Override
    public void onNewToken(String refreshedToken) {
        NearFcmListenerService.sendRegistrationToServer(refreshedToken);
    }
}
