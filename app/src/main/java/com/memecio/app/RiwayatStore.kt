package com.memecio.app

import android.content.Context
import android.content.SharedPreferences
import java.util.LinkedHashSet

object RiwayatStore {

    private const val PREF_NAME = "memecio_riwayat"
    private const val KEY_LIST = "riwayat_list"
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
        saveSet(prefs, set)
    }

    @JvmStatic
    fun hapus(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LIST, "") ?: ""
        if (existing.isEmpty()) return
        val list = ArrayList(existing.split(SEPARATOR))
        list.remove(url)
        saveSet(prefs, LinkedHashSet(list))
    }

    @JvmStatic
    fun saveAll(context: Context, items: List<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        saveSet(prefs, LinkedHashSet(items))
    }

    @JvmStatic
    fun kosongkan(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LIST, "").apply()
    }

    @JvmStatic
    fun getAll(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LIST, "") ?: ""
        val result = ArrayList<String>()
        if (existing.isNotEmpty()) {
            result.addAll(existing.split(SEPARATOR))
        }
        return result
    }

    private fun saveSet(prefs: SharedPreferences, set: LinkedHashSet<String>) {
        val sb = StringBuilder()
        for (item in set) {
            if (sb.isNotEmpty()) sb.append(SEPARATOR)
            sb.append(item)
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply()
    }
}
