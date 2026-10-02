package com.memecio.app

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.ArrayList

object ExternalMediaStore {

    private const val PREF_NAME = "memecio_external_media"
    private const val KEY_LIST = "external_list"
    private const val KEY_SOURCE_LABEL = "external_source_label"
    private const val SEPARATOR = "\u0002"
    private const val FIELD_SEP = "\u0003"
    private const val MAX_PREF_LENGTH = 200_000 // 200 KB limit for prefs
    private const val BIG_FILE_NAME = "external_media_big.txt"
    private const val BIG_LABEL_NAME = "external_media_big_label.txt"

    private fun getBigFile(context: Context): File {
        return File(context.filesDir, BIG_FILE_NAME)
    }

    private fun getBigLabelFile(context: Context): File {
        return File(context.filesDir, BIG_LABEL_NAME)
    }

    @JvmStatic
    fun gantiSemua(context: Context, items: List<MediaItem>, sourceLabel: String?) {
        val sb = StringBuilder()
        for (item in items) {
            if (sb.isNotEmpty()) sb.append(SEPARATOR)
            sb.append(item.type).append(FIELD_SEP)
                .append(item.uri.toString()).append(FIELD_SEP)
                .append(item.title ?: "").append(FIELD_SEP)
                .append(item.thumbUrl ?: "")
        }
        val data = sb.toString()

        try {
            if (data.length > MAX_PREF_LENGTH) {
                // Simpan ke file, hapus pref list
                val f = getBigFile(context)
                val out = FileOutputStream(f)
                out.write(data.toByteArray(StandardCharsets.UTF_8))
                out.close()

                val lf = getBigLabelFile(context)
                val lout = FileOutputStream(lf)
                lout.write((sourceLabel ?: "").toByteArray(StandardCharsets.UTF_8))
                lout.close()

                val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .remove(KEY_LIST)
                    .putString(KEY_SOURCE_LABEL, sourceLabel)
                    .apply()
            } else {
                // Hapus file besar kalau ada
                try {
                    val f = getBigFile(context)
                    if (f.exists()) f.delete()
                } catch (ignored: Exception) {
                }
                try {
                    val lf = getBigLabelFile(context)
                    if (lf.exists()) lf.delete()
                } catch (ignored: Exception) {
                }

                val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(KEY_LIST, data)
                    .putString(KEY_SOURCE_LABEL, sourceLabel)
                    .apply()
            }
        } catch (ignored: Exception) {
        }

        // Auto-save ke MultiSourceStore (untuk multi-source support)
        try {
            if (!sourceLabel.isNullOrEmpty()) {
                // FIX 1: Set sourceTitle ke setiap item yang belum punya
                for (m in items) {
                    if (m.sourceTitle.isNullOrEmpty()) {
                        m.sourceTitle = sourceLabel
                    }
                }
                // FIX 2 (sebelumnya): sourceId unique pakai timestamp
                val sid = "src_" + System.currentTimeMillis() + "_" + Math.abs(sourceLabel.hashCode())
                MultiSourceStore.save(context, sid, sourceLabel, "auto", items)
            }
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun tambahSatu(context: Context, uri: String, type: Int, sourceLabel: String?) {
        val list = getAll(context)
        list.add(MediaItem(Uri.parse(uri), type))
        gantiSemua(context, list, sourceLabel)
    }

    @JvmStatic
    fun getAll(context: Context): MutableList<MediaItem> {
        val result = ArrayList<MediaItem>()
        var data: String? = null

        // Cek file besar dulu
        try {
            val f = getBigFile(context)
            if (f.exists() && f.length() > 0) {
                val input = FileInputStream(f)
                val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                data = sb.toString()
            }
        } catch (ignored: Exception) {
        }

        // Kalau tidak ada, ambil dari prefs
        if (data.isNullOrEmpty()) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            data = prefs.getString(KEY_LIST, "")
        }

        if (data.isNullOrEmpty()) return result

        val entries = data.split(SEPARATOR)
        for (entry in entries) {
            if (entry.trim().isEmpty()) continue
            val parts = entry.split(FIELD_SEP, limit = -1)
            if (parts.size < 2) continue
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
            } catch (ignored: Exception) {
            }
        }
        return result
    }

    @JvmStatic
    fun getSourceLabel(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Cek file label besar
        try {
            val lf = getBigLabelFile(context)
            if (lf.exists() && lf.length() > 0) {
                val input = FileInputStream(lf)
                val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                val label = sb.toString().trim()
                if (label.isNotEmpty()) return label
            }
        } catch (ignored: Exception) {
        }

        return prefs.getString(KEY_SOURCE_LABEL, "") ?: ""
    }

    @JvmStatic
    fun kosongkan(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LIST, "")
            .putString(KEY_SOURCE_LABEL, "")
            .apply()

        try {
            val f = getBigFile(context)
            if (f.exists()) f.delete()
        } catch (ignored: Exception) {
        }
        try {
            val lf = getBigLabelFile(context)
            if (lf.exists()) lf.delete()
        } catch (ignored: Exception) {
        }
    }
}
