package com.memecio.app

import android.content.Context
import android.widget.Toast

object DeveloperModeStore {

    private const val PREF_NAME = "memecio_dev"
    private const val KEY_ENABLED = "dev_mode_enabled"

    @JvmStatic
    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    @JvmStatic
    fun toggle(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = prefs.getBoolean(KEY_ENABLED, false)
        val next = !current
        prefs.edit().putBoolean(KEY_ENABLED, next).apply()
        val msg = if (next) {
            "Developer Mode: AKTIF\n\nAkses: tombol tambahan muncul di tab Profil"
        } else {
            "Developer Mode: MATI"
        }
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
