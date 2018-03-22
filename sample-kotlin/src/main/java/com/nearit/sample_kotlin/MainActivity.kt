package com.nearit.sample_kotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.nearit.ui_bindings.NearITUIBindings
import com.nearit.ui_bindings.permissions.views.PermissionBar
import it.near.sdk.NearItManager
import it.near.sdk.utils.NearUtils

class MainActivity : AppCompatActivity() {

    private val NEAR_PERMISSION_REQUEST: Int = 1000
    private lateinit var bar: PermissionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bar = findViewById(R.id.permission_bar)
        bar.bindToActivity(this, NEAR_PERMISSION_REQUEST)
    }

    override fun onDestroy() {
        super.onDestroy()
        bar.unbindFromActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bar.onActivityResult(requestCode, resultCode)
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

            NearITUIBindings.onNewIntent(this, intent)
        }
    }

    fun triggerCustomAction(view : View) {
        NearItManager.getInstance().processCustomTrigger("MY_CUSTOM_TRIGGER")
    }
}