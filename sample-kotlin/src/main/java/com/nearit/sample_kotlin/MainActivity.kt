package com.nearit.sample_kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nearit.ui_bindings.NearITUIBindings
import it.near.sdk.NearItManager
import it.near.sdk.recipes.inbox.model.HistoryItem
import it.near.sdk.recipes.inbox.update.NotificationHistoryUpdateListener
import it.near.sdk.utils.NearUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NotificationHistoryUpdateListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permission_bar.bindToActivity(this, NEAR_PERMISSION_REQUEST)

        NearItManager.getInstance().addNotificationHistoryUpdateListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        permission_bar.unbindFromActivity()
        NearItManager.getInstance().removeNotificationHistoryUpdateListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //  call this method to inform the bar that a result occurred so it can hide itself
        permission_bar.onActivityResult(requestCode, resultCode)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null &&
                intent.extras != null &&
                NearUtils.carriesNearItContent(intent)) {
            // we got a NearIT intent

            NearITUIBindings.onNewIntent(this, intent)
        }
    }

    override fun onNotificationHistoryUpdated(items: MutableList<HistoryItem>?) {
        inboxButton.text = getString(R.string.inbox_button_label, items?.count { it.isNew } ?: 0)
    }

    fun triggerCustomAction(v : View) {
        NearItManager.getInstance().triggerInAppEvent("my_trigger")
    }

    fun openNotificationHistory(v: View) {
        startActivity(
                NearITUIBindings.getInstance(this)
                        .notificationHistoryIntentBuilder()
                        .setTitle("My Inbox")
                        .build()
        )
    }

    fun openCouponHistory(v: View) {
        startActivity(
            NearITUIBindings.getInstance(this)
                .couponListIntentBuilder()
                .jaggedBorders()
                .setTitle("My Coupons")
                .build()
        )
    }

    fun profileMale(v: View) {
        NearItManager.getInstance().setUserData("gender", "male")
    }

    fun profileFemale(v: View) {
        NearItManager.getInstance().setUserData("gender", "female")
    }

    fun resetProfile(v: View) {
        NearItManager.getInstance().setUserData("gender", null as String?)
    }

    fun readTheDocs(v: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.nearit.com/android/installation")))
    }


    companion object {
        private const val NEAR_PERMISSION_REQUEST: Int = 1000
    }

}
