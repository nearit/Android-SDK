package it.near.sdk.Utils;

import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * @author riccardomoro
 */
public class ULog {

    private static final boolean DEBUG = ((ApplicationInfo.FLAG_DEBUGGABLE) != 0);

    public static void i(String TAG, String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String TAG, String msg, Throwable t) {
        if (DEBUG) {
            Log.i(TAG, msg, t);
        }
    }

    public static void w(String TAG, String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String TAG, String msg, Throwable t) {
        if (DEBUG) {
            Log.w(TAG, msg, t);
        }
    }

    public static void e(String TAG, String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String TAG, String msg, Throwable t) {
        if (DEBUG) {
            Log.e(TAG, msg, t);
        }
    }

    public static void v(String TAG, String msg) {
        if (DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void v(String TAG, String msg, Throwable t) {
        if (DEBUG) {
            Log.v(TAG, msg, t);
        }
    }

    public static void d(String TAG, String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String TAG, String msg, Throwable t) {
        if (DEBUG) {
            Log.d(TAG, msg, t);
        }
    }

    public static void wtf(String TAG, String msg) {
        if (DEBUG) {
            Log.wtf(TAG, msg);
        }
    }

    public static void wtf(String TAG, String msg, Throwable t) {
        if (DEBUG) {
            Log.wtf(TAG, msg, t);
        }
    }
}