package com.nearit.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nearit.ui_bindings.NearITUIBindings;
import com.nearit.ui_bindings.permissions.views.PermissionBar;

import it.near.sdk.NearItManager;
import it.near.sdk.operation.values.NearMultipleChoiceDataPoint;
import it.near.sdk.recipes.inbox.model.HistoryItem;
import it.near.sdk.recipes.inbox.update.NotificationHistoryUpdateListener;
import it.near.sdk.utils.NearUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NotificationHistoryUpdateListener {

    private PermissionBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bar = findViewById(R.id.permission_bar);
        bar.bindToActivity(this, 6);

        NearItManager.getInstance().addNotificationHistoryUpdateListener(this);
    }

    public void triggerCustomAction(View view) {
        NearItManager.getInstance().triggerInAppEvent("MY_TRIGGER");
    }

    public void onInbox(View view) {
        Intent intent = NearITUIBindings.getInstance(this).notificationHistoryIntentBuilder().build();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bar.unbindFromActivity();

        NearItManager.getInstance().removeNotificationHistoryUpdateListener(this);
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

    public void sendMultiDataPoint(View view) {
        NearMultipleChoiceDataPoint multi = new NearMultipleChoiceDataPoint();
        multi.put("food", true);
        multi.put("drink", true);
        multi.put("exercise", false);
        NearItManager.getInstance().setUserData("interests", multi);
    }

    @Override
    public void onNotificationHistoryUpdated(List<HistoryItem> items) {
        int unreadCount = 0;
        for (HistoryItem item : items) {
            if (item.isNew) unreadCount++;
        }
    }
}
