package com.memecio.app

import android.content.Context
import android.net.Uri

object SavedLinksStore {

    private const val PREF_NAME = "memecio_saved_links"
    private const val KEY_LIST = "saved_links_list"
    private const val SEPARATOR = "\u0002"
    private const val FIELD_SEP = "\u0003"

    @JvmStatic
    fun tambah(context: Context, title: String?, url: String) {
        val list = getAll(context)
        val item = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO)
        item.isLocal = false
        item.title = title
        list.add(0, item)
        simpanSemua(context, list)
    }

    @JvmStatic
    fun hapus(context: Context, url: String) {
        val list = getAll(context)
        val hasil = ArrayList<MediaItem>()
        for (m in list) {
            if (m.uri.toString() != url) hasil.add(m)
        }
        simpanSemua(context, hasil)
    }

    @JvmStatic
    fun updateTitle(context: Context, url: String, newTitle: String?) {
        val list = getAll(context)
        for (m in list) {
            if (m.uri.toString() == url) {
                m.title = newTitle
                break
            }
        }
        simpanSemua(context, list)
    }

    @JvmStatic
    fun simpanSemua(context: Context, items: List<MediaItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (item in items) {
            if (sb.isNotEmpty()) sb.append(SEPARATOR)
            sb.append(item.type).append(FIELD_SEP)
                .append(item.uri.toString()).append(FIELD_SEP)
                .append(item.title ?: "")
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply()
    }

    @JvmStatic
    fun getAll(context: Context): MutableList<MediaItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LIST, "") ?: ""
        val result = ArrayList<MediaItem>()
        if (existing.isEmpty()) return result
        val entries = existing.split(SEPARATOR)
        for (entry in entries) {
            val parts = entry.split(FIELD_SEP, ignoreCase = false, limit = -1)
            if (parts.size < 2) continue
            try {
                val type = parts[0].toInt()
                val uriStr = parts[1]
                val title = if (parts.size > 2) parts[2] else ""
                val item = MediaItem(Uri.parse(uriStr), type)
                item.isLocal = false
                item.title = if (title.isEmpty()) null else title
                result.add(item)
            } catch (ignored: Exception) {
            }
        }
        return result
    }
}
