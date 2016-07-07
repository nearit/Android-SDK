package it.near.sdk.Utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

/**
 * Created by claudiosuardi on 16/10/14.
 *
 *
 *      Requires those permissions:
 *      {@code
 *      <uses-permission android:name="android.permission.BLUETOOTH" />
 *      <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 *      }
 *
 *      Can be use to check for BLE availability.
 *
 *      Can detect BT state change with {@link OnBluetoothEventsListener}
 *      In questo caso occorrerà richiamare anche le funzioni init(l) e destroy()
 *
 */
public class BTDetector {

    private static final String TAG = "BTDetector";
    private static final String filterName = "android.bluetooth.adapter.action.STATE_CHANGED";
    private Context context;
    private IntentFilter filter;
    private static boolean oldState = false;
    private OnBluetoothEventsListener listener;


    public BTDetector(Context _context) {
        context = _context;
    }

    public boolean gotConnection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter==null)
            return false; //bt non supportato
        else
            return bluetoothAdapter.isEnabled();
    }

    public boolean gotBle() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void monitor(OnBluetoothEventsListener listener) {
        this.listener = listener;

        //registro ricevitore per stato bluetooth
        filter = new IntentFilter(filterName);
        context.registerReceiver(detector, filter);

        oldState = gotConnection();
    }

    public void destroy() {
        context.unregisterReceiver(detector);
    }






    private BroadcastReceiver detector = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(filterName)) {

                boolean newState = gotConnection();

                // ON --> OFF
                if (oldState && !newState)
                    listener.onBluetoothOff();

                // OFF --> ON
                else if (!oldState && newState)
                    listener.onBluetoothOn();

                oldState = newState;
            }
        }
    };

}
