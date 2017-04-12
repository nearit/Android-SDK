package it.near.sdk.morpheusnear;


import it.near.sdk.logging.NearLog;

/**
 * Logger you can turn on and off.
 */
public class Logger {
  private static final String TAG = "Morpheus";
  private static boolean debug = false;

  public static void debug(String message) {
    if (debug) {
      NearLog.d(TAG, message);
    }
  }

  public static void setDebug(boolean debug) {
    Logger.debug = debug;
  }
}
