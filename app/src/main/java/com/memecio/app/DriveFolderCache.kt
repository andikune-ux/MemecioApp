package com.memecio.app

import android.content.Context
import android.net.Uri
import java.util.ArrayList

object DriveFolderCache {

    private const val PREF_NAME = "memecio_drive_cache"
    private const val KEY_PREFIX = "folder_"
    private const val KEY_TIME = "time_"
    private const val EXPIRE_MS = 30 * 60 * 1000L // 30 menit
    private const val SEP_ITEM = "\u001A"
    private const val SEP_FIELD = "\u001B"

    @JvmStatic
    fun save(context: Context, folderId: String?, items: List<MediaItem>?) {
        if (folderId == null || items == null) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (m in items) {
            if (sb.isNotEmpty()) sb.append(SEP_ITEM)
            sb.append(m.type).append(SEP_FIELD)
                .append(m.uri.toString()).append(SEP_FIELD)
                .append(m.title ?: "").append(SEP_FIELD)
                .append(m.thumbUrl ?: "")
        }
        prefs.edit()
            .putString(KEY_PREFIX + folderId, sb.toString())
            .putLong(KEY_TIME + folderId, System.currentTimeMillis())
            .apply()
    }

    @JvmStatic
    fun load(context: Context, folderId: String?): List<MediaItem>? {
        if (folderId == null) return null
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val time = prefs.getLong(KEY_TIME + folderId, 0)
        if (time == 0L) return null
        if (System.currentTimeMillis() - time > EXPIRE_MS) return null
        val data = prefs.getString(KEY_PREFIX + folderId, "") ?: return null
        if (data.isEmpty()) return null
        val result = ArrayList<MediaItem>()
        val entries = data.split(SEP_ITEM)
        for (entry in entries) {
            if (entry.trim().isEmpty()) continue
            val parts = entry.split(SEP_FIELD, ignoreCase = false, limit = -1)
            if (parts.size < 3) continue
            try {
                val type = parts[0].toInt()
                val uriStr = parts[1]
                val title = parts[2]
                val thumb = if (parts.size > 3) parts[3] else ""
                val mi = MediaItem(Uri.parse(uriStr), type)
                mi.isLocal = false
                mi.title = if (title.isEmpty()) null else title
                mi.thumbUrl = if (thumb.isEmpty()) null else thumb
                result.add(mi)
            } catch (ignored: Exception) {
            }
        }
        return if (result.isEmpty()) null else result
    }

    @JvmStatic
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
