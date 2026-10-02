package com.memecio.app

import android.content.Context

/**
 * GridColumnsStore — simpan preferensi jumlah kolom GridView (1-8).
 * Nilai -1 artinya "Auto" (pakai nilai default dari XML: portrait 5, landscape 7).
 */
object GridColumnsStore {

    private const val PREF = "memecio_settings"
    private const val KEY = "grid_columns"

    const val AUTO = -1

    @JvmStatic
    fun get(ctx: Context): Int {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return p.getInt(KEY, AUTO)
    }

    @JvmStatic
    fun set(ctx: Context, value: Int) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (value < 1 || value > 8) {
            p.edit().remove(KEY).apply()
        } else {
            p.edit().putInt(KEY, value).apply()
        }
    }

    @JvmStatic
    fun label(ctx: Context): String {
        val v = get(ctx)
        return if (v < 1) "Auto" else "$v Kolom"
    }
}
