package com.memecio.app

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration

object DisplayModeStore {

    private const val PREF_NAME = "memecio_settings"
    private const val KEY_MODE = "display_mode"

    const val MODE_AUTO = "auto"
    const val MODE_TV = "tv"
    const val MODE_ANDROID = "android"

    @JvmStatic
    fun isTvDevice(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return uiModeManager != null &&
                uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    @JvmStatic
    fun getMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MODE, MODE_AUTO) ?: MODE_AUTO
    }

    @JvmStatic
    fun getEffectiveMode(context: Context): String {
        val stored = getMode(context)
        if (MODE_AUTO == stored) {
            return if (isTvDevice(context)) MODE_TV else MODE_ANDROID
        }
        return stored
    }

    @JvmStatic
    fun isTvMode(context: Context): Boolean {
        return MODE_TV == getEffectiveMode(context)
    }

    @JvmStatic
    fun setMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODE, mode).apply()
    }

    @JvmStatic
    fun getModeLabel(mode: String): String {
        if (MODE_TV == mode) return "Android TV (paksa)"
        if (MODE_ANDROID == mode) return "Android (paksa)"
        return "Otomatis"
    }

    @JvmStatic
    fun nextMode(current: String): String {
        if (MODE_AUTO == current) return MODE_TV
        if (MODE_TV == current) return MODE_ANDROID
        return MODE_AUTO
    }
}
