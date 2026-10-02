package com.memecio.app.js;

import android.util.Log;

/**
 * console.log/error/warn — untuk JS plugin.
 */
public class JsConsole {

    private static final String TAG = "JsPlugin";

    public JsConsole() {}

    public void log(String msg) {
        Log.d(TAG, msg == null ? "null" : msg);
    }

    public void error(String msg) {
        Log.e(TAG, msg == null ? "null" : msg);
    }

    public void warn(String msg) {
        Log.w(TAG, msg == null ? "null" : msg);
    }

    public void info(String msg) {
        Log.i(TAG, msg == null ? "null" : msg);
    }
}
