package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * MediaScanCache — cache timestamp scan terakhir.
 * Kalau scan < 5 menit, skip scan ulang (biar cepat).
 */
public class MediaScanCache {
    private static final String PREF = "memecio_scan";
    private static final String KEY_LAST_SCAN = "last_scan_ms";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 menit

    public static void markScanned(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            p.edit().putLong(KEY_LAST_SCAN, System.currentTimeMillis()).apply();
        } catch (Exception ignored) {}
    }

    public static boolean isFresh(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            long last = p.getLong(KEY_LAST_SCAN, 0);
            if (last == 0) return false;
            return (System.currentTimeMillis() - last) < CACHE_TTL_MS;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void invalidate(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            p.edit().remove(KEY_LAST_SCAN).apply();
        } catch (Exception ignored) {}
    }
}
