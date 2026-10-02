package com.memecio.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * UpdateStore — simpan data terkait fitur auto-update.
 */
object UpdateStore {

    private const val PREF_NAME = "memecio_update"
    private const val KEY_SKIP_VERSION = "skip_version"
    private const val KEY_LAST_CHECK = "last_check"
    private const val KEY_APK_LIST = "apk_list"
    private const val KEY_LAST_KNOWN_LATEST = "last_known_latest"
    private const val CHECK_CACHE_MS = 6 * 60 * 60 * 1000L // 6 jam

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ─────────────────────────────────────────────────
    // VERSI SKIP
    // ─────────────────────────────────────────────────

    @JvmStatic
    fun setSkipVersion(ctx: Context, version: String) {
        prefs(ctx).edit().putString(KEY_SKIP_VERSION, version).apply()
    }

    @JvmStatic
    fun getSkipVersion(ctx: Context): String? {
        return prefs(ctx).getString(KEY_SKIP_VERSION, null)
    }

    @JvmStatic
    fun isSkipped(ctx: Context, version: String): Boolean {
        return getSkipVersion(ctx) == version
    }

    @JvmStatic
    fun clearSkip(ctx: Context) {
        prefs(ctx).edit().remove(KEY_SKIP_VERSION).apply()
    }

    // ─────────────────────────────────────────────────
    // CACHE CEK UPDATE
    // ─────────────────────────────────────────────────

    @JvmStatic
    fun shouldCheck(ctx: Context): Boolean {
        val last = prefs(ctx).getLong(KEY_LAST_CHECK, 0)
        if (last == 0L) return true
        return System.currentTimeMillis() - last >= CHECK_CACHE_MS
    }

    @JvmStatic
    fun markChecked(ctx: Context) {
        prefs(ctx).edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    @JvmStatic
    fun clearCheckCache(ctx: Context) {
        prefs(ctx).edit().remove(KEY_LAST_CHECK).apply()
    }

    // ─────────────────────────────────────────────────
    // VERSI TERBARU YANG DIKETAHUI
    // ─────────────────────────────────────────────────

    @JvmStatic
    fun setLastKnownLatest(ctx: Context, version: String) {
        prefs(ctx).edit().putString(KEY_LAST_KNOWN_LATEST, version).apply()
    }

    @JvmStatic
    fun getLastKnownLatest(ctx: Context): String? {
        return prefs(ctx).getString(KEY_LAST_KNOWN_LATEST, null)
    }

    // ─────────────────────────────────────────────────
    // DAFTAR APK YANG SUDAH DIDOWNLOAD
    // ─────────────────────────────────────────────────

    class ApkEntry {
        var version: String = ""
        var path: String = ""
        var sizeBytes: Long = 0L
        var downloadedAt: Long = 0L
    }

    @JvmStatic
    fun addApk(ctx: Context, version: String, path: String, sizeBytes: Long) {
        val list = getAllApk(ctx)
        val filtered = list.filter { it.version != version }.toMutableList()
        val entry = ApkEntry()
        entry.version = version
        entry.path = path
        entry.sizeBytes = sizeBytes
        entry.downloadedAt = System.currentTimeMillis()
        filtered.add(0, entry)
        saveAllApk(ctx, filtered)
    }

    @JvmStatic
    fun getAllApk(ctx: Context): MutableList<ApkEntry> {
        val result = ArrayList<ApkEntry>()
        val json = prefs(ctx).getString(KEY_APK_LIST, "") ?: ""
        if (json.isEmpty()) return result
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val e = ApkEntry()
                e.version = obj.optString("version", "")
                e.path = obj.optString("path", "")
                e.sizeBytes = obj.optLong("size", 0)
                e.downloadedAt = obj.optLong("at", 0)
                result.add(e)
            }
        } catch (ignored: Exception) {}
        return result
    }

    @JvmStatic
    fun removeApk(ctx: Context, version: String) {
        val list = getAllApk(ctx)
        val filtered = list.filter { it.version != version }.toMutableList()
        saveAllApk(ctx, filtered)
    }

    @JvmStatic
    fun clearAllApk(ctx: Context) {
        prefs(ctx).edit().remove(KEY_APK_LIST).apply()
    }

    private fun saveAllApk(ctx: Context, list: List<ApkEntry>) {
        try {
            val arr = JSONArray()
            for (e in list) {
                val obj = JSONObject()
                obj.put("version", e.version)
                obj.put("path", e.path)
                obj.put("size", e.sizeBytes)
                obj.put("at", e.downloadedAt)
                arr.put(obj)
            }
            prefs(ctx).edit().putString(KEY_APK_LIST, arr.toString()).apply()
        } catch (ignored: Exception) {}
    }
}
