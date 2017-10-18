package com.nearit.sample_kotlin

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import it.near.sdk.push.fcmregistration.NearInstanceIDListenerService

class CustomFirebaseInstanceIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val refreshedToken: String? = FirebaseInstanceId.getInstance().token
        NearInstanceIDListenerService.sendRegistrationToServer(refreshedToken)
    }

}