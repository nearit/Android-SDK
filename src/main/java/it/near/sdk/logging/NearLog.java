package it.near.sdk.logging;

public class NearLog {

    private static NearLogger sLogger = new DefaultLogger();

    public static void setLogger(NearLogger logger) {
        if (logger == null) {
            throw new NullPointerException("Logger may not be null.");
        }
        sLogger = logger;
    }

    public static NearLogger getLogger() {
        return sLogger;
    }

    public static void v(String tag, String msg) {
        sLogger.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        sLogger.v(tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        sLogger.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        sLogger.d(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        sLogger.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        sLogger.i(tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        sLogger.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        sLogger.w(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        sLogger.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        sLogger.e(tag, msg, tr);
    }

    private NearLog() {
        // no instances
    }
}
