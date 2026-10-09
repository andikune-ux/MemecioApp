package com.memecio.app

import android.content.Context
import android.net.Uri
import java.util.ArrayList
import java.util.LinkedHashSet

object MultiSourceStore {
    private const val PREF_NAME = "memecio_multi_source"
    private const val KEY_SOURCES = "sources_list"
    private const val KEY_PREFIX = "src_"
    private const val SEP_FIELD = "\u0003"
    private const val SEP_ITEM = "\u0002"

    fun save(ctx: Context, sourceId: String?, sourceLabel: String?, sourceType: String?, items: List<MediaItem>?) {
        if (sourceId.isNullOrEmpty() || items == null) return

        val sb = StringBuilder()
        sb.append(sourceLabel ?: "").append(SEP_FIELD)
        sb.append(sourceType ?: "")
        for (m in items) {
            sb.append(SEP_ITEM)
            sb.append(m.type).append(SEP_FIELD)
                .append(m.uri.toString()).append(SEP_FIELD)
                .append(m.title ?: "").append(SEP_FIELD)
                .append(m.thumbUrl ?: "").append(SEP_FIELD)
                .append(m.sourceTitle ?: "")
        }

        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = LinkedHashSet(prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: emptySet())
        sources.add(sourceId)
        prefs.edit()
            .putStringSet(KEY_SOURCES, sources)
            .putString(KEY_PREFIX + sourceId, sb.toString())
            .apply()
    }

    fun getAllMerged(ctx: Context): List<MediaItem> {
        val result = ArrayList<MediaItem>()
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: emptySet()
        for (sourceId in sources) {
            val data = prefs.getString(KEY_PREFIX + sourceId, "") ?: ""
            if (data.isNotEmpty()) {
                result.addAll(parseData(data))
            }
        }
        return result
    }

    fun getBySource(ctx: Context, sourceId: String): List<MediaItem> {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_PREFIX + sourceId, "") ?: ""
        if (data.isEmpty()) return emptyList()
        return parseData(data)
    }

    fun listSourceIds(ctx: Context): List<String> {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: emptySet()
        return ArrayList(sources)
    }

    private fun parseData(data: String): List<MediaItem> {
        val list = ArrayList<MediaItem>()
        val itemTokens = data.split(SEP_ITEM.toRegex())
        for (i in 1 until itemTokens.size) {
            val itemStr = itemTokens[i]
            if (itemStr.isEmpty()) continue
            val parts = itemStr.split(SEP_FIELD.toRegex())
            if (parts.size >= 2) {
                val type = parts[0].toIntOrNull() ?: MediaItem.TYPE_VIDEO
                val uri = Uri.parse(parts[1])
                val title = if (parts.size > 2) parts[2] else ""
                val thumb = if (parts.size > 3) parts[3] else ""
                val src = if (parts.size > 4) parts[4] else ""
                val item = MediaItem(uri, type).apply {
                    this.title = title
                    this.thumbUrl = thumb
                    this.sourceTitle = src
                    this.isLocal = false
                }
                list.add(item)
            }
        }
        return list
    }
}
