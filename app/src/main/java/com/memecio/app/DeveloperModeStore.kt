package com.memecio.app

import android.content.Context
import android.widget.Toast

object DeveloperModeStore {
    private const val PREF_NAME = "memecio_dev_prefs"
    private const val KEY_DEV_MODE = "developer_mode_enabled"

    fun isEnabled(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_DEV_MODE, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_DEV_MODE, enabled).apply()
    }

    fun toggle(context: Context): Boolean {
        val newVal = !isEnabled(context)
        setEnabled(context, newVal)
        Toast.makeText(
            context,
            if (newVal) "Developer Mode AKTIF" else "Developer Mode NONAKTIF",
            Toast.LENGTH_SHORT
        ).show()
        return newVal
    }
}
