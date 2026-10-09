package com.memecio.app

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class M3uParser {

    class MediaEntry(
        @JvmField var url: String,
        @JvmField var title: String?,
        @JvmField var thumbUrl: String?
    )

    interface Callback {
        fun onSuccess(entries: List<MediaEntry>)
        fun onError(message: String)
    }

    companion object {
        private val LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"")

        @JvmStatic
        fun parseAsync(context: Context, source: String, callback: Callback) {
            Thread {
                var conn: HttpURLConnection? = null
                try {
                    val inputStream: InputStream?
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
                        }
                        if (inputStream == null) {
                            postResult(context, callback, null, "Tidak bisa membuka sumber")
                            return@Thread
                        }
                        val entries = bacaStream(inputStream, source)
                        postResult(context, callback, entries, null)
                        return@Thread
                    }

                    conn = openWithRedirects(source, 5)
                    inputStream = conn.inputStream
                    if (inputStream == null) {
                        postResult(context, callback, null, "Tidak bisa membuka sumber")
                        return@Thread
                    }

                    val rawBytes = readAllBytes(inputStream)
                    inputStream.close()
                    conn.disconnect()

                    val text = String(rawBytes, Charsets.UTF_8)
                    val isPlaylist = text.contains("#EXTM3U") || text.contains("#EXTINF")
                    if (!isPlaylist) {
                        val single = listOf(MediaEntry(source, null, null))
                        postResult(context, callback, single, null)
                        return@Thread
                    }

                    val entries = parseText(text)
                    postResult(context, callback, entries, null)
                } catch (e: Exception) {
                    if (conn != null) {
                        try {
                            conn.errorStream?.close()
                        } catch (ignored: Exception) {}
                        conn.disconnect()
                    }
                    Log.e("M3uParser", "Parse error: ${e.message}", e)
                    postResult(context, callback, null, e.message ?: "Unknown error")
                }
            }.start()
        }

        private fun bacaStream(inputStream: InputStream, source: String): List<MediaEntry> {
            val rawBytes = readAllBytes(inputStream)
            inputStream.close()
            val text = String(rawBytes, Charsets.UTF_8)
            if (text.contains("#EXTM3U") || text.contains("#EXTINF")) {
                return parseText(text)
            }
            return listOf(MediaEntry(source, null, null))
        }

        private fun parseText(text: String): List<MediaEntry> {
            val entries = mutableListOf<MediaEntry>()
            val lines = text.split("\n")
            var pendingTitle: String? = null
            var pendingThumb: String? = null
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#EXTM3U")) continue
                if (line.startsWith("#EXTINF")) {
                    pendingTitle = extractTitle(line)
                    pendingThumb = extractThumb(line)
                    continue
                }
                if (line.startsWith("#")) continue
                entries.add(MediaEntry(line, pendingTitle, pendingThumb))
                pendingTitle = null
                pendingThumb = null
            }
            return entries
        }

        private fun extractTitle(extinfLine: String): String? {
            val lastComma = extinfLine.lastIndexOf(',')
            if (lastComma == -1 || lastComma == extinfLine.length - 1) return null
            val title = extinfLine.substring(lastComma + 1).trim()
            return if (title.isEmpty()) null else title
        }

        private fun extractThumb(extinfLine: String): String? {
            val m = LOGO_PATTERN.matcher(extinfLine)
            if (m.find()) {
                val logo = m.group(1)
                return if (logo.isNullOrEmpty()) null else logo
            }
            return null
        }

        private fun readAllBytes(ins: InputStream): ByteArray {
            val buffer = java.io.ByteArrayOutputStream()
            val data = ByteArray(8192)
            var nRead: Int
            while (ins.read(data, 0, data.size).also { nRead = it } != -1) {
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
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) Memecio/1.0")
                conn.setRequestProperty("Accept", "*/*")
                conn.connect()

                val code = conn.responseCode
                if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                    val newUrl = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (newUrl == null) {
                        throw Exception("Redirect tanpa Location header")
                    }
                    currentUrl = if (newUrl.startsWith("http")) newUrl else URL(URL(currentUrl), newUrl).toString()
                    continue
                }
                if (code in 200..299) {
                    return conn
                } else {
                    conn.disconnect()
                    throw Exception("Server menolak dengan kode HTTP $code")
                }
            }
            throw Exception("Terlalu banyak redirect (>$maxRedirects)")
        }

        private fun postResult(context: Context, callback: Callback, entries: List<MediaEntry>?, error: String?) {
            if (context is Activity) {
                context.runOnUiThread {
                    if (error != null) {
                        callback.onError(error)
                    } else {
                        callback.onSuccess(entries ?: emptyList())
                    }
                }
            }
        }
    }
}
