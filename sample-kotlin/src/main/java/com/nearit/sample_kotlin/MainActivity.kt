package com.nearit.sample_kotlin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import it.near.sdk.NearItManager
import it.near.sdk.operation.NearItUserProfile
import it.near.sdk.reactions.contentplugin.model.Content
import it.near.sdk.reactions.couponplugin.model.Coupon
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON
import it.near.sdk.reactions.feedbackplugin.model.Feedback
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification
import it.near.sdk.recipes.RecipeRefreshListener
import it.near.sdk.trackings.TrackingInfo
import it.near.sdk.utils.ContentsListener
import it.near.sdk.utils.NearUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ContentsListener {

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
        fakeCrm = FakeCrm()

        setLoginButtonText(isUserLoggedIn)
    }

    fun requestPermissions(view : View) {
        startActivityForResult(PermissionsActivity.createIntent(this@MainActivity), NEAR_PERMISSION_REQUEST)
    }


    fun refreshNearRecipes(view : View) {
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
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            NearItManager.getInstance().startRadar()
        }
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

            NearUtils.parseContents(intent, this)
        }
    }

    fun profileMyUser(view : View) {
        NearItManager.getInstance().setUserData(KEY_FOR_AGE_FIELD, ageEditText.text.toString())
    }

    fun triggerCustomAction(view : View) {
        NearItManager.getInstance().processCustomTrigger("MY_CUSTOM_TRIGGER")
    }

    fun userLogInAndOut(view : View) {
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