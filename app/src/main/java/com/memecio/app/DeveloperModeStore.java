package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class DeveloperModeStore {
    private static final String PREF_NAME = "memecio_dev";
    private static final String KEY_ENABLED = "dev_mode_enabled";

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public static void toggle(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean current = prefs.getBoolean(KEY_ENABLED, false);
        boolean next = !current;
        prefs.edit().putBoolean(KEY_ENABLED, next).apply();

        String msg = next
            ? "Developer Mode: AKTIF\n\nAkses: tombol tambahan muncul di tab Profil"
            : "Developer Mode: MATI";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
