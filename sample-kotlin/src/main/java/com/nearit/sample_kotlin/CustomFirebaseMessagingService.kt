package com.nearit.sample_kotlin

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.near.sdk.push.NearFcmListenerService

class CustomFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        NearFcmListenerService.processRemoteMessage(remoteMessage)
    }

}