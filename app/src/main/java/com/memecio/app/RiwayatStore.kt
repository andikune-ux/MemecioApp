package com.memecio.app

import android.content.Context
import java.util.LinkedHashSet

object RiwayatStore {
    private const val KEY_LIST = "riwayat_list"
    private const val PREF_NAME = "memecio_riwayat"
    private const val SEPARATOR = "\u0001"

    @JvmStatic
    fun tambah(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LIST, "") ?: ""
        val set = LinkedHashSet<String>()
        set.add(url)
        if (existing.isNotEmpty()) {
            set.addAll(existing.split(SEPARATOR))
        }
        val sb = StringBuilder()
        for (item in set) {
            if (sb.isNotEmpty()) {
                sb.append(SEPARATOR)
            }
            sb.append(item)
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply()
    }

    @JvmStatic
    fun getAll(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LIST, "") ?: ""
        val result = mutableListOf<String>()
        if (existing.isNotEmpty()) {
            result.addAll(existing.split(SEPARATOR))
        }
        return result
    }

    @JvmStatic
    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LIST).apply()
    }
}
