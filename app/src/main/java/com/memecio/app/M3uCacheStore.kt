package com.memecio.app

import android.content.Context
import java.util.ArrayList

object M3uCacheStore {

    private const val PREF_NAME = "memecio_m3u_cache"
    private const val KEY_PREFIX = "m3u_"
    private const val KEY_TIME = "t_"
    private const val EXPIRE_MS = 30 * 60 * 1000L // 30 menit
    private const val SEP_ENTRY = "\u001C"
    private const val SEP_FIELD = "\u001D"

    @JvmStatic
    fun save(context: Context, url: String?, entries: List<M3uParser.MediaEntry>?) {
        if (url == null || entries == null || entries.isEmpty()) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (e in entries) {
            if (sb.isNotEmpty()) sb.append(SEP_ENTRY)
            sb.append(e.url ?: "")
                .append(SEP_FIELD)
                .append(e.title ?: "")
                .append(SEP_FIELD)
                .append(e.thumbUrl ?: "")
        }
        // Batasi ukuran cache 500 KB per entry
        var data = sb.toString()
        if (data.length > 500000) {
            data = data.substring(0, 500000)
        }
        prefs.edit()
            .putString(KEY_PREFIX + url, data)
            .putLong(KEY_TIME + url, System.currentTimeMillis())
            .apply()
    }

    @JvmStatic
    fun load(context: Context, url: String?): List<M3uParser.MediaEntry>? {
        if (url == null) return null
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val time = prefs.getLong(KEY_TIME + url, 0)
        if (time == 0L) return null
        if (System.currentTimeMillis() - time > EXPIRE_MS) return null
        val data = prefs.getString(KEY_PREFIX + url, "") ?: return null
        if (data.isEmpty()) return null
        val result = ArrayList<M3uParser.MediaEntry>()
        val entries = data.split(SEP_ENTRY)
        for (entry in entries) {
            if (entry.trim().isEmpty()) continue
            val parts = entry.split(SEP_FIELD, ignoreCase = false, limit = -1)
            if (parts.size < 1) continue
            val u = parts[0]
            val title = if (parts.size > 1) parts[1] else null
            val thumb = if (parts.size > 2) parts[2] else null
            if (u.isEmpty()) continue
            result.add(
                M3uParser.MediaEntry(
                    u,
                    if (title.isNullOrEmpty()) null else title,
                    if (thumb.isNullOrEmpty()) null else thumb
                )
            )
        }
        return if (result.isEmpty()) null else result
    }

    @JvmStatic
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
