package com.android.apparatus.utils;

import android.util.Log;

/**
 * 打印log的工具
 */

public class LoggerUtil {
    private static final boolean debug = true;

    public static void println(Object obj, String msg) {
        String tagName = obj.getClass().getCanonicalName();
        Log.i(tagName, msg);
    }

    public static void println(Class clazz, String msg) {
        String tagName = clazz.getCanonicalName();
        Log.i(tagName, msg);
    }

    public static void println(String tagName, String msg) {
        Log.i(tagName, msg);
    }

    public static void println(String msg) {
        if (debug) {
            Log.i("loggerUtil_debug打印", msg);
        }
    }
}
