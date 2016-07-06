package it.near.sdk.Trackings;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import it.near.sdk.Communication.NearInstallation;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano.
 */
public class BluetoothStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothStatusReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
                ULog.d(TAG, "ho spento il bluetooth");
                NearInstallation.registerInstallation(context);
            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                ULog.d(TAG, "ho acceso il blue");
                NearInstallation.registerInstallation(context);
            }
        }
    }
}
