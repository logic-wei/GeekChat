package com.logic.geekchat;

import android.util.Log;

public class LogUtil {
    private static boolean mDebug = true;

    public static void i(String tag, String msg) {
        if (mDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (mDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (mDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (mDebug)
            Log.v(tag, msg);
    }
}
