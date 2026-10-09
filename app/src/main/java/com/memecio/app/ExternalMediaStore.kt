package com.memecio.app

import android.content.Context
import android.net.Uri

object ExternalMediaStore {
    private const val FIELD_SEP = "\u0003"
    private const val KEY_LIST = "external_list"
    private const val KEY_SOURCE_LABEL = "external_source_label"
    private const val PREF_NAME = "memecio_external_media"
    private const val SEPARATOR = "\u0002"

    @JvmStatic
    fun gantiSemua(context: Context, items: List<MediaItem>, sourceLabel: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (item in items) {
            if (sb.isNotEmpty()) {
                sb.append(SEPARATOR)
            }
            sb.append(item.type)
                .append(FIELD_SEP)
                .append(item.uri.toString())
                .append(FIELD_SEP)
                .append(item.title ?: "")
                .append(FIELD_SEP)
                .append(item.thumbUrl ?: "")
        }
        prefs.edit()
            .putString(KEY_LIST, sb.toString())
            .putString(KEY_SOURCE_LABEL, sourceLabel)
            .apply()
    }

    @JvmStatic
    fun tambahSatu(context: Context, uri: String, type: Int, sourceLabel: String) {
        val list = mutableListOf<MediaItem>()
        val mi = MediaItem(Uri.parse(uri), type)
        mi.isLocal = false
        list.add(mi)
        gantiSemua(context, list, sourceLabel)
    }

    @JvmStatic
    fun getAll(context: Context): List<MediaItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LIST, "") ?: ""
        val result = mutableListOf<MediaItem>()
        if (existing.isEmpty()) {
            return result
        }
        val entries = existing.split(SEPARATOR)
        for (entry in entries) {
            val parts = entry.split(FIELD_SEP)
            if (parts.size >= 2) {
                try {
                    val type = parts[0].toInt()
                    val uriStr = parts[1]
                    val title = if (parts.size > 2) parts[2] else ""
                    val thumb = if (parts.size > 3) parts[3] else ""
                    val item = MediaItem(Uri.parse(uriStr), type)
                    item.isLocal = false
                    item.title = if (title.isEmpty()) null else title
                    item.thumbUrl = if (thumb.isEmpty()) null else thumb
                    result.add(item)
                } catch (ignored: Exception) {}
            }
        }
        return result
    }

    @JvmStatic
    fun getSourceLabel(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SOURCE_LABEL, "") ?: ""
    }

    @JvmStatic
    fun kosongkan(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LIST, "")
            .putString(KEY_SOURCE_LABEL, "")
            .apply()
    }
}
