package com.memecio.app

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.regex.Pattern

object M3uParser {

    class MediaEntry {
        var url: String
        var title: String?
        var thumbUrl: String?
        var groupTitle: String? = null

        constructor(url: String, title: String?, thumbUrl: String?) {
            this.url = url
            this.title = title
            this.thumbUrl = thumbUrl
        }
    }

    interface Callback {
        fun onSuccess(entries: List<MediaEntry>?)
        fun onError(message: String?)
    }

    private val LOGO_PATTERN: Pattern = Pattern.compile("tvg-logo=\"([^\"]*)\"")
    private val GROUP_PATTERN: Pattern = Pattern.compile("group-title=\"([^\"]*)\"")

    @JvmStatic
    fun parseAsync(context: Context, source: String?, callback: Callback) {
        if (source == null) {
            callback.onError("Sumber kosong")
            return
        }

        // Cek cache dulu (hanya untuk HTTP URL)
        if (source.startsWith("http")) {
            val cached = M3uCacheStore.load(context, source)
            if (cached != null && cached.isNotEmpty()) {
                postResult(context, callback, cached, null)
                return
            }
        }

        Thread {
            var conn: HttpURLConnection? = null
            try {
                val inputStream: InputStream
                // Kalau sumber bukan http, langsung buka dari content resolver (file lokal)
                if (!source.startsWith("http://") && !source.startsWith("https://")) {
                    if (source.startsWith("/")) {
                        val f = File(source)
                        if (!f.exists()) {
                            postResult(context, callback, null, "File tidak ditemukan")
                            return@Thread
                        }
                        inputStream = FileInputStream(f)
                    } else {
                        val uri = Uri.parse(source)
                        inputStream = context.contentResolver.openInputStream(uri)
                            ?: run {
                                postResult(context, callback, null, "Tidak bisa membuka sumber")
                                return@Thread
                            }
                    }
                    val entries = bacaStream(inputStream, source)
                    postResult(context, callback, entries, null)
                    return@Thread
                }

                // Kalau http, ambil dulu isi file
                conn = openWithRedirects(source, 5)
                val stream = conn.inputStream
                if (stream == null) {
                    postResult(context, callback, null, "Tidak bisa membuka sumber")
                    return@Thread
                }

                // Baca mentah dulu
                val rawBytes = readAllBytes(stream)
                stream.close()
                conn.disconnect()

                val text = String(rawBytes, Charsets.UTF_8)

                // Deteksi: apakah ini playlist M3U?
                val isPlaylist = text.contains("#EXTM3U") || text.contains("#EXTINF")
                if (!isPlaylist) {
                    // Bukan M3U -> single video
                    val single = ArrayList<MediaEntry>()
                    single.add(MediaEntry(source, null, null))
                    postResult(context, callback, single, null)
                    return@Thread
                }

                // Parse sebagai playlist
                val entries = parseText(text)

                // Simpan ke cache kalau ini HTTP
                if (entries != null && entries.isNotEmpty()) {
                    M3uCacheStore.save(context, source, entries)
                }
                postResult(context, callback, entries, null)

            } catch (e: Exception) {
                if (conn != null) {
                    try {
                        val err = conn.errorStream
                        err?.close()
                    } catch (ignored: Exception) {
                    }
                    conn.disconnect()
                }
                Log.e("M3uParser", "Parse error: " + e.message, e)
                postResult(context, callback, null, e.message)
            }
        }.start()
    }

    private fun bacaStream(inputStream: InputStream, source: String): List<MediaEntry> {
        val rawBytes = readAllBytes(inputStream)
        inputStream.close()
        val text = String(rawBytes, Charsets.UTF_8)
        return if (text.contains("#EXTM3U") || text.contains("#EXTINF")) {
            parseText(text)
        } else {
            val single = ArrayList<MediaEntry>()
            single.add(MediaEntry(source, null, null))
            single
        }
    }

    private fun parseText(text: String): List<MediaEntry> {
        val entries = ArrayList<MediaEntry>()
        val lines = text.split("\n")
        var pendingTitle: String? = null
        var pendingGroup: String? = null
        var pendingThumb: String? = null

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue
            if (line.startsWith("#EXTM3U")) continue
            if (line.startsWith("#EXTINF")) {
                pendingTitle = extractTitle(line)
                pendingThumb = extractThumb(line)
                pendingGroup = extractGroup(line)
                continue
            }
            if (line.startsWith("#")) continue

            val entry = MediaEntry(line, pendingTitle, pendingThumb)
            entry.groupTitle = pendingGroup
            entries.add(entry)
            pendingTitle = null
            pendingThumb = null
            pendingGroup = null
        }
        return entries
    }

    private fun extractTitle(extinfLine: String): String? {
        val lastComma = extinfLine.lastIndexOf(',')
        if (lastComma == -1 || lastComma == extinfLine.length - 1) return null
        val title = extinfLine.substring(lastComma + 1).trim()
        return if (title.isEmpty()) null else title
    }

    private fun extractGroup(extinfLine: String): String? {
        try {
            val m = GROUP_PATTERN.matcher(extinfLine)
            if (m.find()) {
                val g = m.group(1)
                return if (g == null || g.isEmpty()) null else g
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun extractThumb(extinfLine: String): String? {
        val m = LOGO_PATTERN.matcher(extinfLine)
        if (m.find()) {
            val logo = m.group(1)
            return if (logo == null || logo.isEmpty()) null else logo
        }
        return null
    }

    private fun readAllBytes(inputStream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(8192)
        var nRead: Int
        while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        return buffer.toByteArray()
    }

    private fun openWithRedirects(urlStr: String, maxRedirects: Int): HttpURLConnection {
        var currentUrl = urlStr
        for (i in 0 until maxRedirects) {
            val url = URL(currentUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = true
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.setRequestProperty("User-Agent", "VLC/3.0.16 LibVLC/3.0.16")
            conn.setRequestProperty("Accept", "*/*")
            conn.setRequestProperty("Connection", "close")
            try {
                val host = url.protocol + "://" + url.host + "/"
                conn.setRequestProperty("Referer", host)
                conn.setRequestProperty("Origin", host)
            } catch (ignoredRef: Exception) {
            }
            conn.connect()
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                code == HttpURLConnection.HTTP_MOVED_TEMP ||
                code == HttpURLConnection.HTTP_SEE_OTHER ||
                code == 307 || code == 308
            ) {
                val newUrl = conn.getHeaderField("Location")
                conn.disconnect()
                if (newUrl == null) {
                    throw Exception("Redirect tanpa Location header (kode $code)")
                }
                currentUrl = if (newUrl.startsWith("http")) {
                    newUrl
                } else {
                    URL(URL(currentUrl), newUrl).toString()
                }
                continue
            }
            if (code in 200..299) {
                return conn
            } else {
                val msg = "Server menolak dengan kode HTTP $code"
                conn.disconnect()
                throw Exception(msg)
            }
        }
        throw Exception("Terlalu banyak redirect (>$maxRedirects)")
    }

    private fun postResult(
        context: Context,
        callback: Callback,
        entries: List<MediaEntry>?,
        error: String?
    ) {
        if (context is Activity) {
            context.runOnUiThread {
                if (error != null) {
                    callback.onError(error)
                } else {
                    callback.onSuccess(entries)
                }
            }
        }
    }
}
