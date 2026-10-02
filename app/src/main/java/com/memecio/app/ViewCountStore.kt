package com.memecio.app

import android.content.Context
import android.content.SharedPreferences

/**
 * ViewCountStore — hitung berapa kali setiap video ditonton.
 * Dipakai untuk kategori "Populer" di Beranda.
 */
object ViewCountStore {

    private const val PREF_NAME = "memecio_view_count"

    @JvmStatic
    fun tambah(ctx: Context, uri: String) {
        try {
            val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val key = "v_" + uri.hashCode()
            val current = prefs.getInt(key, 0)
            prefs.edit().putInt(key, current + 1).apply()
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun getCount(ctx: Context, uri: String): Int {
        return try {
            val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val key = "v_" + uri.hashCode()
            prefs.getInt(key, 0)
        } catch (ignored: Exception) {
            0
        }
    }

    @JvmStatic
    fun reset(ctx: Context, uri: String) {
        try {
            val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val key = "v_" + uri.hashCode()
            prefs.edit().remove(key).apply()
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun clearAll(ctx: Context) {
        try {
            val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (ignored: Exception) {
        }
    }
}
