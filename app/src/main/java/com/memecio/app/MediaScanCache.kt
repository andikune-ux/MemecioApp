package com.memecio.app

import android.content.Context

/**
 * MediaScanCache — cache timestamp scan terakhir.
 * Kalau scan < 5 menit, skip scan ulang (biar cepat).
 */
object MediaScanCache {

    private const val PREF = "memecio_scan"
    private const val KEY_LAST_SCAN = "last_scan_ms"
    private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 menit

    @JvmStatic
    fun markScanned(ctx: Context) {
        try {
            val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            p.edit().putLong(KEY_LAST_SCAN, System.currentTimeMillis()).apply()
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun isFresh(ctx: Context): Boolean {
        return try {
            val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            val last = p.getLong(KEY_LAST_SCAN, 0)
            if (last == 0L) return false
            System.currentTimeMillis() - last < CACHE_TTL_MS
        } catch (ignored: Exception) {
            false
        }
    }

    @JvmStatic
    fun invalidate(ctx: Context) {
        try {
            val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            p.edit().remove(KEY_LAST_SCAN).apply()
        } catch (ignored: Exception) {
        }
    }
}
