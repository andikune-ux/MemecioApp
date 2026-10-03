package com.memecio.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

object VideoDescriptionFetcher {

    fun interface Callback {
        fun onResult(description: String)
    }

    private const val PREF_NAME = "memecio_video_desc"
    private const val EXPIRE_MS = 24 * 60 * 60 * 1000L // 24 jam
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val META_DESC = Pattern.compile(
        "<meta[^>]+(?:name|property)=[\"'](?:description|og:description)[\"'][^>]+content=[\"']([^\"']+)[\"']",
        Pattern.CASE_INSENSITIVE
    )
    private val META_DESC2 = Pattern.compile(
        "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+(?:name|property)=[\"'](?:description|og:description)[\"']",
        Pattern.CASE_INSENSITIVE
    )
    private val TITLE_TAG = Pattern.compile(
        "<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE
    )

    @JvmStatic
    fun fetch(context: Context, videoUrl: String?, videoTitle: String?, callback: Callback?) {
        if (videoUrl == null) {
            callback?.onResult(defaultDescription(videoTitle, null))
            return
        }

        // Cek cache
        val cached = loadCache(context, videoUrl)
        if (!cached.isNullOrEmpty()) {
            callback?.onResult(cached)
            return
        }

        // Kalau video lokal (content:// atau file://) -> langsung default
        if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
            val desc = defaultDescription(videoTitle, videoUrl)
            saveCache(context, videoUrl, desc)
            callback?.onResult(desc)
            return
        }

        // Kalau bukan http, langsung default
        if (!videoUrl.startsWith("http")) {
            val desc = defaultDescription(videoTitle, videoUrl)
            saveCache(context, videoUrl, desc)
            callback?.onResult(desc)
            return
        }

        // Fetch dari internet
        executor.execute {
            var description: String? = null
            try {
                if (!isDirectVideoUrl(videoUrl)) {
                    description = fetchMetaDescription(videoUrl)
                }

                var isDefault = false
                if (description.isNullOrBlank()) {
                    description = defaultDescription(videoTitle, videoUrl)
                    isDefault = true
                }

                if (!isDefault && description != null) {
                    val translated = tryTranslate(description)
                    if (!translated.isNullOrEmpty() && isValidDescription(translated)) {
                        description = translated
                    }
                }

                saveCache(context, videoUrl, description ?: "")
            } catch (_: Exception) {
                description = defaultDescription(videoTitle, videoUrl)
            }

            val result = description ?: ""
            mainHandler.post {
                callback?.onResult(result)
            }
        }
    }

    private fun isDirectVideoUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm") ||
            lower.endsWith(".m3u8") || lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
            lower.endsWith(".mov") || lower.endsWith(".avi") ||
            lower.contains("googlevideo.com") ||
            lower.contains("usercontent.google.com") ||
            lower.contains(".m3u8?")
    }

    private fun fetchMetaDescription(pageUrl: String): String? {
        var conn: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(pageUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.instanceFollowRedirects = true

            val code = conn.responseCode
            if (code < 200 || code >= 300) return null

            inputStream = conn.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val sb = StringBuilder()
            var line: String?
            var total = 0
            while (reader.readLine().also { line = it } != null && total < 50000) {
                sb.append(line).append("\n")
                total += (line?.length ?: 0)
            }
            reader.close()
            inputStream.close()
            conn.disconnect()

            val html = sb.toString()

            val m = META_DESC.matcher(html)
            if (m.find()) {
                val d = unescapeHtml(m.group(1)?.trim() ?: "")
                if (isValidDescription(d)) return d
            }
            val m2 = META_DESC2.matcher(html)
            if (m2.find()) {
                val d = unescapeHtml(m2.group(1)?.trim() ?: "")
                if (isValidDescription(d)) return d
            }

            val t = TITLE_TAG.matcher(html)
            if (t.find()) {
                val title = unescapeHtml(t.group(1)?.trim() ?: "")
                if (title.length in 6..199) return title
            }
        } catch (_: Exception) {
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { conn?.disconnect() } catch (_: Exception) {}
        }
        return null
    }

    private fun unescapeHtml(s: String): String {
        return s.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
    }

    private fun tryTranslate(text: String?): String? {
        if (text == null || text.length < 3) return null
        var queryText = text
        if (queryText.length > 3000) queryText = queryText.substring(0, 3000)
        try {
            val url = "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&dt=t&sl=auto&tl=id&q=" + URLEncoder.encode(queryText, "UTF-8")

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")

            val code = conn.responseCode
            if (code < 200 || code >= 300) {
                conn.disconnect()
                return null
            }

            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) sb.append(line)
            reader.close()
            conn.disconnect()

            val response = sb.toString()
            val start = response.indexOf("[[[\"")
            if (start < 0) return null

            val result = StringBuilder()
            var i = start + 4
            while (i < response.length) {
                val q1 = response.indexOf("\"", i)
                if (q1 < 0) break
                var q2 = q1 + 1
                val piece = StringBuilder()
                while (q2 < response.length) {
                    val c = response[q2]
                    if (c == '\\' && q2 + 1 < response.length) {
                        val next = response[q2 + 1]
                        if (next == 'n') piece.append('\n')
                        else if (next == 't') piece.append('\t')
                        else if (next == '"') piece.append('"')
                        else if (next == '\\') piece.append('\\')
                        else if (next == 'u' && q2 + 5 < response.length) {
                            try {
                                val hex = response.substring(q2 + 2, q2 + 6)
                                piece.append(hex.toInt(16).toChar())
                                q2 += 4
                            } catch (_: Exception) {
                                piece.append(next)
                            }
                        } else {
                            piece.append(next)
                        }
                        q2 += 2
                    } else if (c == '"') {
                        break
                    } else {
                        piece.append(c)
                        q2++
                    }
                }
                if (piece.isNotEmpty()) result.append(piece)
                val comma = response.indexOf(",", q2)
                if (comma < 0) break
                val nextStart = response.indexOf("[\"", q2 + 1)
                if (nextStart < 0) break
                i = nextStart
            }

            val out = result.toString().trim()
            return if (out.isEmpty()) null else out
        } catch (_: Exception) {}
        return null
    }

    private fun isValidDescription(desc: String?): Boolean {
        if (desc == null) return false
        val d = desc.trim()
        if (d.length < 15 || d.length > 2000) return false

        var spaces = 0
        for (c in d.toCharArray()) if (c == ' ') spaces++
        if (spaces < 3) return false

        var validChars = 0
        var totalChars = 0
        var hexLike = 0
        for (c in d.toCharArray()) {
            totalChars++
            if (Character.isLetterOrDigit(c) || c in " ,.!?-'\"():;&") {
                validChars++
            }
            if (c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F') {
                hexLike++
            }
        }

        if (totalChars == 0) return false
        val validRatio = validChars.toDouble() / totalChars
        if (validRatio < 0.85) return false

        val spaceRatio = spaces.toDouble() / totalChars
        if (spaceRatio < 0.05) return false

        var letters = 0
        for (c in d.toCharArray()) if (Character.isLetter(c)) letters++
        if (letters < totalChars * 0.5) return false

        return true
    }

    private fun defaultDescription(title: String?, url: String?): String {
        val sb = StringBuilder()
        var judulBersih = title?.trim() ?: "Video"
        if (judulBersih.isEmpty()) judulBersih = "Video"

        sb.append("Judul: ").append(judulBersih).append("\n\n")

        if (url != null) {
            try {
                val u = URL(url)
                val host = u.host
                val path = u.path
                if (!host.isNullOrEmpty()) {
                    sb.append("Sumber: ").append(host).append("\n")
                }
                if (!path.isNullOrEmpty()) {
                    val lastSlash = path.lastIndexOf('/')
                    if (lastSlash in 0 until path.length - 1) {
                        var filename = path.substring(lastSlash + 1)
                        val q = filename.indexOf('?')
                        if (q > 0) filename = filename.substring(0, q)
                        if (filename.isNotEmpty() && filename.length < 80) {
                            sb.append("File: ").append(filename).append("\n")
                        }
                    }
                }
            } catch (_: Exception) {}

            val lower = url.lowercase()
            when {
                lower.contains(".m3u8") -> sb.append("Format: HLS Streaming\n")
                lower.contains(".mp4") -> sb.append("Format: MP4\n")
                lower.contains(".mp3") -> sb.append("Format: Audio MP3\n")
                lower.contains("drive.google.com") -> sb.append("Sumber: Google Drive\n")
            }
        }

        return sb.toString()
    }

    private fun loadCache(context: Context, key: String): String? {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val time = prefs.getLong("t_" + key.hashCode(), 0)
            if (time == 0L || System.currentTimeMillis() - time > EXPIRE_MS) return null
            prefs.getString("d_" + key.hashCode(), null)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveCache(context: Context, key: String, value: String) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("d_" + key.hashCode(), value)
                .putLong("t_" + key.hashCode(), System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {}
    }
}
