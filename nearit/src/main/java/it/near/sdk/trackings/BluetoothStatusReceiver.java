package it.near.sdk.trackings;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import it.near.sdk.NearItManager;
import it.near.sdk.logging.NearLog;

public class BluetoothStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothStatusReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        NearItManager nearItManager = NearItManager.getInstance(context);
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                NearLog.d(TAG, "BT turned off");
                nearItManager.updateInstallation();
            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                NearLog.d(TAG, "BT turned on");
                nearItManager.updateInstallation();
            }
        }
    }
}
