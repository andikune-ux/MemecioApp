package com.memecio.app;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class DisplayModeStore {
    private static final String PREF_NAME = "memecio_settings";
    private static final String KEY_MODE = "display_mode";

    public static final String MODE_AUTO = "auto";
    public static final String MODE_TV = "tv";
    public static final String MODE_ANDROID = "android";

    public static boolean isTvDevice(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static String getMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MODE, MODE_AUTO);
    }

    public static String getEffectiveMode(Context context) {
        String stored = getMode(context);
        if (MODE_AUTO.equals(stored)) {
            return isTvDevice(context) ? MODE_TV : MODE_ANDROID;
        }
        return stored;
    }

    public static boolean isTvMode(Context context) {
        return MODE_TV.equals(getEffectiveMode(context));
    }

    public static void setMode(Context context, String mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, mode).apply();
    }

    public static String getModeLabel(String mode) {
        if (MODE_TV.equals(mode)) return "Android TV (paksa)";
        if (MODE_ANDROID.equals(mode)) return "Android (paksa)";
        return "Otomatis";
    }

    public static String nextMode(String current) {
        if (MODE_AUTO.equals(current)) return MODE_TV;
        if (MODE_TV.equals(current)) return MODE_ANDROID;
        return MODE_AUTO;
    }
}
