package com.nearit.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import it.near.sdk.NearItManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

public class MainActivity extends AppCompatActivity implements ProximityListener, CoreContentsListener {

    private static final String TAG = "MainActivity";
    private static final int NEAR_PERMISSION_REQUEST = 1000;
    /**
     * This field key must be mapped on the CMS
     */
    private static final String KEY_FOR_AGE_FIELD = "age";

    Button requestPermissionButton, setAgeButtom, refreshRecipesButton;
    EditText ageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissionButton = (Button) findViewById(R.id.permission_button);
        requestPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(PermissionsActivity.createIntent(MainActivity.this), NEAR_PERMISSION_REQUEST);
            }
        });
        refreshRecipesButton = (Button) findViewById(R.id.refresh_button);
        refreshRecipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshNearRecipes();
            }
        });

        ageEditText = (EditText) findViewById(R.id.ageEditText);
        setAgeButtom = (Button) findViewById(R.id.ageSetButton);
        setAgeButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileMyUser();
            }
        });

        NearItManager.getInstance().addProximityListener(this);

    }

    private void refreshNearRecipes() {
        NearItManager.getInstance().refreshConfigs(new RecipeRefreshListener() {
            @Override
            public void onRecipesRefresh() {
                Toast.makeText(MainActivity.this, "Recipes refreshed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecipesRefreshFail() {
                Toast.makeText(MainActivity.this, "Could not refresh recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEAR_PERMISSION_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            NearItManager.getInstance().startRadar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NearItManager.getInstance().removeProximityListener(this);
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

            NearUtils.parseCoreContents(intent, this);
        }
    }

    private void profileMyUser() {
        NearItManager.getInstance().setUserData(KEY_FOR_AGE_FIELD, (ageEditText.getText().toString()), new UserDataNotifier() {
            @Override
            public void onDataCreated() {
                Toast.makeText(MainActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataNotSetError(String error) {

            }
        });
    }

    @Override
    public void foregroundEvent(Parcelable content, TrackingInfo trackingInfo) {
        NearUtils.parseCoreContents(content, trackingInfo, this);
    }

    @Override
    public void gotContentNotification(Content notification, TrackingInfo trackingInfo) {
        Toast.makeText(this, "You received a notification with content", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotCouponNotification(Coupon notification, TrackingInfo trackingInfo) {
        Toast.makeText(this, "You received a coupon", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(notification.getSerial()).create().show();
    }

    @Override
    public void gotCustomJSONNotification(CustomJSON notification, TrackingInfo trackingInfo) {
        Toast.makeText(this, "You received a custom json", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotSimpleNotification(SimpleNotification s_notif, TrackingInfo trackingInfo) {
        Toast.makeText(this, "You received a simple notification: " + s_notif.getNotificationMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotFeedbackNotification(Feedback s_notif, TrackingInfo trackingInfo) {
        Toast.makeText(this, "You received a feedback request", Toast.LENGTH_SHORT).show();
    }

}
