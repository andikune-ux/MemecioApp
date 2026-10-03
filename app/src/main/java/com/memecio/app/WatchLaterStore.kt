package com.memecio.app

import android.content.Context
import android.net.Uri
import java.util.ArrayList

object WatchLaterStore {

    private const val PREF_NAME = "memecio_watch_later"
    private const val KEY_LIST = "watch_later_list"
    private const val SEP_LINE = "\u0007"
    private const val SEP_FIELD = "\u0008"

    @JvmStatic
    fun add(context: Context, uri: String, title: String?) {
        val items = getAll(context).toMutableList()
        for (item in items) {
            if (item.uri.toString() == uri) return
        }
        val item = MediaItem(Uri.parse(uri), MediaItem.TYPE_VIDEO).apply {
            isLocal = false
            this.title = title
        }
        items.add(item)
        save(context, items)
    }

    @JvmStatic
    fun remove(context: Context, uri: String) {
        val items = getAll(context)
        val result = ArrayList<MediaItem>()
        for (item in items) {
            if (item.uri.toString() != uri) result.add(item)
        }
        save(context, result)
    }

    @JvmStatic
    fun getAll(context: Context): List<MediaItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_LIST, "") ?: ""
        val result = ArrayList<MediaItem>()
        if (data.isEmpty()) return result
        val lines = data.split(SEP_LINE)
        for (line in lines) {
            if (line.isEmpty()) continue
            val parts = line.split(SEP_FIELD)
            if (parts.size < 2) continue
            try {
                val uriStr = parts[0]
                val title = if (parts.size > 1) parts[1] else ""
                val item = MediaItem(Uri.parse(uriStr), MediaItem.TYPE_VIDEO).apply {
                    isLocal = false
                    this.title = if (title.isEmpty()) null else title
                }
                result.add(item)
            } catch (_: Exception) {}
        }
        return result
    }

    private fun save(context: Context, items: List<MediaItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (item in items) {
            if (sb.isNotEmpty()) sb.append(SEP_LINE)
            sb.append(item.uri.toString()).append(SEP_FIELD)
                .append(item.title ?: "")
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply()
    }
}
