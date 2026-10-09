package com.memecio.app

import android.content.Context

object DisplayModeStore {
    private const val PREF = "memecio_display_mode"
    private const val KEY = "mode"

    @JvmStatic
    fun getEffectiveMode(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "android") ?: "android"
    }

    @JvmStatic
    fun setMode(context: Context, mode: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, mode).apply()
    }

    @JvmStatic
    fun isTvMode(context: Context): Boolean {
        return getEffectiveMode(context) == "tv"
    }
}
