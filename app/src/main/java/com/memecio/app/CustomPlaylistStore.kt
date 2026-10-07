package com.memecio.app

import android.content.Context
import android.net.Uri
import java.util.ArrayList

object CustomPlaylistStore {

    private const val PREF_NAME = "memecio_custom_playlists"
    private const val KEY_DATA = "playlists_data"
    private const val SEP_PLAYLIST = "\u0004"
    private const val SEP_LINE = "\u0005"
    private const val SEP_FIELD = "\u0006"

    class Playlist {
        var name: String? = null
        var items: MutableList<MediaItem> = ArrayList()
    }

    @JvmStatic
    fun getAll(context: Context): MutableList<Playlist> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_DATA, "") ?: ""
        val result = ArrayList<Playlist>()
        if (data.isEmpty()) return result

        val blocks = data.split(SEP_PLAYLIST)
        for (block in blocks) {
            if (block.trim().isEmpty()) continue
            val lines = block.split(SEP_LINE)
            if (lines.isEmpty()) continue

            val pl = Playlist()
            pl.name = lines[0]
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.trim().isEmpty()) continue
                val parts = line.split(SEP_FIELD, limit = -1)
                if (parts.size < 2) continue
                try {
                    val type = parts[0].toInt()
                    val uriStr = parts[1]
                    val title = if (parts.size > 2) parts[2] else ""
                    val item = MediaItem(Uri.parse(uriStr), type)
                    item.isLocal = false
                    item.title = if (title.isEmpty()) null else title
                    pl.items.add(item)
                } catch (ignored: Exception) {
                }
            }
            result.add(pl)
        }
        return result
    }

    @JvmStatic
    fun saveAll(context: Context, playlists: List<Playlist>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sb = StringBuilder()
        for (i in playlists.indices) {
            val pl = playlists[i]
            if (i > 0) sb.append(SEP_PLAYLIST)
            sb.append(pl.name)
            for (item in pl.items) {
                sb.append(SEP_LINE)
                    .append(item.type).append(SEP_FIELD)
                    .append(item.uri.toString()).append(SEP_FIELD)
                    .append(item.title ?: "")
            }
        }
        prefs.edit().putString(KEY_DATA, sb.toString()).apply()
    }

    @JvmStatic
    fun tambahPlaylist(context: Context, nama: String?) {
        val pls = getAll(context)
        val pl = Playlist()
        pl.name = nama
        pls.add(pl)
        saveAll(context, pls)
    }

    @JvmStatic
    fun hapusPlaylist(context: Context, index: Int) {
        val pls = getAll(context)
        if (index >= 0 && index < pls.size) {
            pls.removeAt(index)
            saveAll(context, pls)
        }
    }

    @JvmStatic
    fun tambahItem(context: Context, index: Int, item: MediaItem?) {
        if (item == null) return
        val pls = getAll(context)
        if (index >= 0 && index < pls.size) {
            pls[index].items.add(item)
            saveAll(context, pls)
        }
    }
}
