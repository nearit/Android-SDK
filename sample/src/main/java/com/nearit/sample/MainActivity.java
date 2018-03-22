package com.nearit.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.nearit.ui_bindings.NearITUIBindings;
import com.nearit.ui_bindings.permissions.views.PermissionBar;

import it.near.sdk.NearItManager;
import it.near.sdk.utils.NearUtils;

public class MainActivity extends AppCompatActivity {

    private PermissionBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bar = findViewById(R.id.permission_bar);
        bar.bindToActivity(this, 6);
    }

    public void triggerCustomAction(View view) {
        NearItManager.getInstance().processCustomTrigger("MY_TRIGGER");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bar.unbindFromActivity();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null &&
                intent.getExtras() != null &&
                NearUtils.carriesNearItContent(intent)) {
            // we got a NearIT intent

            NearITUIBindings.onNewIntent(this, intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  call this method to inform the bar that a result occured so it can hide itself
        bar.onActivityResult(requestCode, resultCode);
    }
}
