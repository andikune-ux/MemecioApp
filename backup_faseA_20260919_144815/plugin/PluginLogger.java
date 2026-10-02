package com.memecio.app.plugin;

import android.util.Log;

/**
 * Logger khusus plugin — biar log gampang difilter.
 * Tag format: MEMECIO_PLUGIN_<namaPlugin>
 */
public class PluginLogger {

    private static final String TAG_PREFIX = "MEMECIO_PLUGIN_";

    public static void log(String pluginName, String msg) {
        Log.d(TAG_PREFIX + safe(pluginName), msg);
    }

    public static void error(String pluginName, String msg, Throwable t) {
        Log.e(TAG_PREFIX + safe(pluginName), msg, t);
    }

    private static String safe(String s) {
        if (s == null) return "UNKNOWN";
        return s.replaceAll("[^A-Za-z0-9_]", "_").toUpperCase();
    }
}
