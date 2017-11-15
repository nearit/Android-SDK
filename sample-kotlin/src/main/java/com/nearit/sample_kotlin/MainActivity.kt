package com.nearit.sample_kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import it.near.sdk.NearItManager
import it.near.sdk.geopolis.beacons.ranging.ProximityListener
import it.near.sdk.operation.NearItUserProfile
import it.near.sdk.operation.UserDataNotifier
import it.near.sdk.reactions.contentplugin.model.Content
import it.near.sdk.reactions.couponplugin.model.Coupon
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON
import it.near.sdk.reactions.feedbackplugin.model.Feedback
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification
import it.near.sdk.recipes.RecipeRefreshListener
import it.near.sdk.trackings.TrackingInfo
import it.near.sdk.utils.CoreContentsListener
import it.near.sdk.utils.NearUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ProximityListener, CoreContentsListener {

    private val NEAR_PERMISSION_REQUEST: Int = 1000
    /**
     * This field key must be mapped on the CMS
     */
    private val KEY_FOR_AGE_FIELD = "age"

    private var isUserLoggedIn: Boolean = false
    private var fakeCrm: FakeCrm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permission_button.setOnClickListener { requestPermissions() }
        refresh_button.setOnClickListener { refreshNearRecipes() }
        ageSetButton.setOnClickListener { profileMyUser() }
        loginButton.setOnClickListener { userLogInAndOut() }

        fakeCrm = FakeCrm()

        setLoginButtonText(isUserLoggedIn)
        NearItManager.getInstance().addProximityListener(this@MainActivity)
    }

    private fun requestPermissions() {
        startActivityForResult(PermissionsActivity.createIntent(this@MainActivity), NEAR_PERMISSION_REQUEST)
    }

    private fun refreshNearRecipes() {
        NearItManager.getInstance().refreshConfigs(object : RecipeRefreshListener {
            override fun onRecipesRefresh() {
                Toast.makeText(this@MainActivity, "Recipes refreshed", Toast.LENGTH_SHORT).show()
            }

            override fun onRecipesRefreshFail() {
                Toast.makeText(this@MainActivity, "Could not refresh recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NEAR_PERMISSION_REQUEST && resultCode == Activity.RESULT_OK) {
            NearItManager.getInstance().startRadar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NearItManager.getInstance().removeProximityListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null &&
                intent.extras != null &&
                NearUtils.carriesNearItContent(intent)) {
            // we got a NearIT intent

            NearUtils.parseCoreContents(intent, this)
        }
    }

    private fun profileMyUser() {
        NearItManager.getInstance().setUserData(KEY_FOR_AGE_FIELD, ageEditText.text.toString())
    }

    private fun userLogInAndOut() {
        // this is an example crm integration
        if (isUserLoggedIn) {
            logout()
        } else {
            login()
        }
    }

    private fun logout() {
        fakeCrm?.logout()
        NearItManager.getInstance().resetProfileId(object : NearItUserProfile.ProfileFetchListener {
            override fun onProfileId(profileId: String) {
                // a new empty profile was generated
            }

            override fun onError(error: String) {
                // your local profile was wiped, but no new profile was created
            }
        })
        isUserLoggedIn = false
        setLoginButtonText(isUserLoggedIn)
    }

    private fun login() {
        fakeCrm?.login("username", "password", object : FakeCrm.LoginListener {

            override fun onLogin(userData: FakeCrm.UserData) {
                isUserLoggedIn = true
                setLoginButtonText(isUserLoggedIn)
                val nearProfileFromServer = userData.nearProfileId
                if (nearProfileFromServer == null) {
                    // user needs a near profile
                    NearItManager.getInstance().getProfileId(object : NearItUserProfile.ProfileFetchListener {

                        override fun onProfileId(profileId: String) {
                            fakeCrm?.saveProfileId(profileId)
                        }

                        override fun onError(error: String) {}
                    })
                } else {
                    // user already has a near profile
                    NearItManager.getInstance().profileId = nearProfileFromServer
                }
            }
        })
    }

    private fun setLoginButtonText(isUserLoggedIn: Boolean) {
        loginButton.text = if (isUserLoggedIn) "logout" else "login"
    }

    override fun foregroundEvent(content: Parcelable?, trackingInfo: TrackingInfo?) {
        NearUtils.parseCoreContents(content, trackingInfo, this@MainActivity)
    }

    override fun gotContentNotification(notification: Content, trackingInfo: TrackingInfo) {
        Toast.makeText(this, "You received a notification with content", Toast.LENGTH_SHORT).show()
    }

    override fun gotCouponNotification(notification: Coupon, trackingInfo: TrackingInfo) {
        Toast.makeText(this, "You received a coupon", Toast.LENGTH_SHORT).show()
        val builder = AlertDialog.Builder(this)
        builder.setMessage(notification.serial).create().show()
    }

    override fun gotCustomJSONNotification(notification: CustomJSON, trackingInfo: TrackingInfo) {
        Toast.makeText(this, "You received a custom json", Toast.LENGTH_SHORT).show()
    }

    override fun gotSimpleNotification(s_notif: SimpleNotification, trackingInfo: TrackingInfo) {
        Toast.makeText(this, "You received a simple notification: " + s_notif.getNotificationMessage(), Toast.LENGTH_SHORT).show()
    }

    override fun gotFeedbackNotification(s_notif: Feedback, trackingInfo: TrackingInfo) {
        Toast.makeText(this, "You received a feedback request", Toast.LENGTH_SHORT).show()
    }


}