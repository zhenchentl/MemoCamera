package me.markchen.util;

/**
 * Created by 振 on 2014/11/23.
 */
public class Log {
    private static final String LOG_TAG = "MemoCamera";

    public static void v(String message) {
        android.util.Log.v(LOG_TAG, message);
    }

    public static void e(String message) {
        android.util.Log.e(LOG_TAG, message);
    }

    public static void i(String message) {
        android.util.Log.i(LOG_TAG, message);
    }

    public static void i(String message, Throwable tr) {
        android.util.Log.i(LOG_TAG, message, tr);
    }

    public static void e(String message, Throwable tr) {
        android.util.Log.e(LOG_TAG, message, tr);
    }

    public static void w(String message) {
        android.util.Log.w(LOG_TAG, message);
    }

    public static void w(String message, Throwable tr) {
        android.util.Log.w(LOG_TAG, message, tr);
    }
}
