package com.nearit.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nearit.ui_bindings.NearITUIBindings;
import com.nearit.ui_bindings.permissions.views.PermissionBar;

import it.near.sdk.NearItManager;
import it.near.sdk.recipes.inbox.model.HistoryItem;
import it.near.sdk.recipes.inbox.update.NotificationHistoryUpdateListener;
import it.near.sdk.utils.NearUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NotificationHistoryUpdateListener {

    private static final int NEAR_PERMISSION_REQUEST = 1000;
    private PermissionBar bar;
    private Button inboxButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bar = findViewById(R.id.permission_bar);
        inboxButton = findViewById(R.id.inbox_button);
        bar.bindToActivity(this, NEAR_PERMISSION_REQUEST);

        NearItManager.getInstance().addNotificationHistoryUpdateListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bar.unbindFromActivity();

        NearItManager.getInstance().removeNotificationHistoryUpdateListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  call this method to inform the bar that a result occurred so it can hide itself
        bar.onActivityResult(requestCode, resultCode);
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
    public void onNotificationHistoryUpdated(List<HistoryItem> items) {
        int unreadCount = 0;
        for (HistoryItem item : items) {
            if (item.isNew) unreadCount++;
        }
        inboxButton.setText(getString(R.string.inbox_button_label, unreadCount));
    }

    public void triggerCustomAction(View view) {
        NearItManager.getInstance().triggerInAppEvent("my_trigger");
    }

    public void openNotificationHistory(View view) {
        startActivity(
                NearITUIBindings.getInstance(this)
                        .notificationHistoryIntentBuilder()
                        .setTitle("My Inbox")
                        .build()
        );
    }

    public void openCouponHistory(View view) {
        startActivity(
                NearITUIBindings.getInstance(this)
                .couponListIntentBuilder()
                .jaggedBorders()
                .setTitle("My Coupons")
                .build()
        );
    }

    public void profileMale(View view) {
        NearItManager.getInstance().setUserData("gender", "male");
    }

    public void profileFemale(View view) {
        NearItManager.getInstance().setUserData("gender", "female");
    }

    public void resetProfile(View view) {
        NearItManager.getInstance().setUserData("gender", (String) null);
    }

    public void readTheDocs(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.nearit.com/android/installation")));
    }
}
