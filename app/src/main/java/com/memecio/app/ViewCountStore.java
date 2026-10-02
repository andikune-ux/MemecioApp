package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;

public class ViewCountStore {

    private static final String PREF_NAME = "memecio_view_count";

    public static void tambah(Context context, String uri) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            int current = prefs.getInt(uri, 0);
            prefs.edit().putInt(uri, current + 1).apply();
        } catch (Exception ignored) {}
    }

    public static int getCount(Context context, String uri) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getInt(uri, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void clear(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        } catch (Exception ignored) {}
    }
}
