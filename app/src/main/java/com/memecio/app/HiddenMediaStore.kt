package com.memecio.app

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.util.ArrayList
import java.util.Locale

object HiddenMediaStore {

    @JvmStatic
    fun getFolder(): File {
        val dir = File(Environment.getExternalStorageDirectory(), "Termux/MediaTersembunyi")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    @JvmStatic
    fun ensureNoMedia(context: Context) {
        try {
            val folder = getFolder()
            val nomedia = File(folder, ".nomedia")
            if (!nomedia.exists()) {
                nomedia.createNewFile()
            }
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun scanHidden(context: Context): MutableList<MediaItem> {
        val result = ArrayList<MediaItem>()
        try {
            val folder = getFolder()
            val files = folder.listFiles() ?: return result
            for (f in files) {
                if (f.isDirectory) continue
                val name = f.name.lowercase(Locale.getDefault())
                if (name.startsWith(".")) continue

                var type = -1
                if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png") || name.endsWith(".webp") ||
                    name.endsWith(".gif")
                ) {
                    type = MediaItem.TYPE_IMAGE
                } else if (name.endsWith(".mp4") || name.endsWith(".mkv") ||
                    name.endsWith(".avi") || name.endsWith(".mov") ||
                    name.endsWith(".webm")
                ) {
                    type = MediaItem.TYPE_VIDEO
                } else if (name.endsWith(".mp3") || name.endsWith(".wav") ||
                    name.endsWith(".m4a")
                ) {
                    type = MediaItem.TYPE_AUDIO
                }

                if (type == -1) continue

                val item = MediaItem(Uri.fromFile(f), type)
                item.isLocal = true
                item.title = f.name
                result.add(item)
            }
        } catch (ignored: Exception) {
        }
        return result
    }

    @JvmStatic
    fun getCount(): Int {
        val folder = getFolder()
        val files = folder.listFiles() ?: return 0
        var count = 0
        for (f in files) {
            if (f.isFile && !f.name.startsWith(".")) count++
        }
        return count
    }
}
