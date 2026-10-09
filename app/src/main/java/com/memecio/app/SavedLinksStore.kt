package com.memecio.app

import android.content.Context
import android.net.Uri

object SavedLinksStore {
    private const val PREF_NAME = "memecio_saved_links"
    private const val KEY_LIST = "saved_links"
    private const val ITEM_SEP = "\u0001"
    private const val FIELD_SEP = "\u0002"

    @JvmStatic
    fun getAll(context: Context): List<MediaItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_LIST, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        val list = mutableListOf<MediaItem>()
        for (item in raw.split(ITEM_SEP)) {
            val parts = item.split(FIELD_SEP)
            if (parts.size >= 2) {
                val title = parts[0]
                val url = parts[1]
                val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO)
                mi.title = title
                mi.isLocal = false
                list.add(mi)
            }
        }
        return list
    }

    @JvmStatic
    fun tambah(context: Context, title: String, url: String) {
        val list = getAll(context).toMutableList()
        list.removeAll { it.uri.toString() == url }
        val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO)
        mi.title = title
        mi.isLocal = false
        list.add(0, mi)
        saveAll(context, list)
    }

    @JvmStatic
    fun hapus(context: Context, url: String) {
        val list = getAll(context).toMutableList()
        list.removeAll { it.uri.toString() == url }
        saveAll(context, list)
    }

    @JvmStatic
    fun updateTitle(context: Context, url: String, newTitle: String) {
        val list = getAll(context).toMutableList()
        for (item in list) {
            if (item.uri.toString() == url) {
                item.title = newTitle
            }
        }
        saveAll(context, list)
    }

    private fun saveAll(context: Context, list: List<MediaItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (item in list) {
            if (sb.isNotEmpty()) sb.append(ITEM_SEP)
            sb.append(item.title ?: "").append(FIELD_SEP).append(item.uri.toString())
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply()
    }
}
