package com.memecio.app

import java.util.ArrayList

object SmartPlaylistHelper {
    const val MODE_RESET = ""
    const val MODE_FOLDER = "folder"
    const val MODE_DATE = "date"
    const val MODE_SIZE = "size"

    fun getFolders(input: List<MediaItem>): List<String> {
        val list = ArrayList<String>()
        for (m in input) {
            val f = m.folderPath ?: "(tanpa folder)"
            if (!list.contains(f)) list.add(f)
        }
        return list
    }

    fun filterByFolder(input: List<MediaItem>, folder: String): List<MediaItem> {
        val out = ArrayList<MediaItem>()
        for (m in input) {
            val f = m.folderPath ?: "(tanpa folder)"
            if (f == folder) out.add(m)
        }
        return out
    }
}
