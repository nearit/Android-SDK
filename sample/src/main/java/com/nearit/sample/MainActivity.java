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
import it.near.sdk.operation.NearItUserProfile;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearItIntentConstants;
import it.near.sdk.utils.NearUtils;

public class MainActivity extends AppCompatActivity implements ProximityListener, CoreContentsListener {

    private static final String TAG = "MainActivity";
    private static final int NEAR_PERMISSION_REQUEST = 1000;
    /**
     * This field key must be mapped on the CMS
     */
    private static final String KEY_FOR_AGE_FIELD = "age";

    Button requestPermissionButton, setAgeButtom;
    EditText ageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissionButton = (Button) findViewById(R.id.button);
        requestPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(PermissionsActivity.createIntent(MainActivity.this), NEAR_PERMISSION_REQUEST);
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

        NearItManager.getInstance(this).addProximityListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEAR_PERMISSION_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            NearItManager.getInstance(this).startRadar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NearItManager.getInstance(this).removeProximityListener(this);
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
                intent.hasExtra(NearItIntentConstants.RECIPE_ID)) {
            // we got a NearIT intent

            // track it as engaged, since we tapped on it
            NearItManager.getInstance(this).sendTracking(
                    intent.getStringExtra(NearItIntentConstants.RECIPE_ID),
                    Recipe.ENGAGED_STATUS
            );

            NearUtils.parseCoreContents(intent, this);
        }
    }

    private void profileMyUser() {
        NearItManager.getInstance(this).setUserData(KEY_FOR_AGE_FIELD, (ageEditText.getText().toString()), new UserDataNotifier() {
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
    public void foregroundEvent(Parcelable content, Recipe recipe) {
        NearUtils.parseCoreContents(content, recipe, this);
    }

    @Override
    public void gotContentNotification(@Nullable Intent intent, Content notification, String recipeId, String notificationMessage) {
        Toast.makeText(this, "You received a notification with content", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotCouponNotification(@Nullable Intent intent, Coupon notification, String recipeId, String notificationMessage) {
        Toast.makeText(this, "You received a coupon", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(notification.getSerial()).create().show();
    }

    @Override
    public void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, String recipeId, String notificationMessage) {
        Toast.makeText(this, "You received a custom json", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, String recipeId, String notificationMessage) {
        Toast.makeText(this, "You received a simple notification", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotFeedbackNotification(@Nullable Intent intent, Feedback s_notif, String recipeId, String notificationMessage) {
        Toast.makeText(this, "You received a feedback request", Toast.LENGTH_SHORT).show();
    }

}
