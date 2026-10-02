package com.memecio.app

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * UpdateChecker — cek versi terbaru dari GitHub Releases.
 *
 * Endpoint:
 *   https://api.github.com/repos/andikune-ux/MemecioApp/releases/latest
 *
 * Format tag di GitHub WAJIB: "vX.YY.ZZ" (contoh: v1.02.0)
 * Contoh release title: "andikune-ux"
 */
object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val REPO_OWNER = "andikune-ux"
    private const val REPO_NAME = "MemecioApp"
    private const val API_URL =
        "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
    private const val TIMEOUT_MS = 15000

    class ReleaseInfo {
        var version: String = ""
        var versionTag: String = ""
        var title: String = ""
        var body: String = ""
        var apkUrl: String = ""
        var apkSize: Long = 0L
        var apkName: String = ""
        var publishedAt: String = ""
    }

    interface Callback {
        fun onResult(info: ReleaseInfo?)
    }

    @JvmStatic
    fun checkAsync(context: Context, callback: Callback) {
        Thread {
            val result = fetchLatestRelease()
            callback.onResult(result)
        }.start()
    }

    @JvmStatic
    fun getCurrentVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    @JvmStatic
    fun isNewerVersion(currentVersion: String, remoteVersion: String): Boolean {
        return compareVersion(remoteVersion, currentVersion) > 0
    }

    private fun compareVersion(v1: String, v2: String): Int {
        try {
            val a = v1.split(".").map { it.toIntOrNull() ?: 0 }
            val b = v2.split(".").map { it.toIntOrNull() ?: 0 }
            val len = maxOf(a.size, b.size)
            for (i in 0 until len) {
                val x = a.getOrElse(i) { 0 }
                val y = b.getOrElse(i) { 0 }
                if (x != y) return x - y
            }
        } catch (e: Exception) {
        }
        return 0
    }

    @JvmStatic
    fun fetchLatestRelease(): ReleaseInfo? {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.requestMethod = "GET"
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "MemecioApp/" + Build.VERSION.SDK_INT)
            conn.setRequestProperty("Accept", "application/vnd.github+json")

            val code = conn.responseCode
            if (code == 404) {
                Log.w(TAG, "Release belum ada di GitHub (404)")
                return null
            }
            if (code != 200) {
                Log.w(TAG, "GitHub API error: HTTP $code")
                return null
            }

            val br = BufferedReader(InputStreamReader(conn.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            br.close()

            val json = JSONObject(sb.toString())
            val info = ReleaseInfo()
            info.versionTag = json.optString("tag_name", "")
            info.version = info.versionTag.removePrefix("v").removePrefix("V").trim()
            info.title = json.optString("name", "")
            info.body = json.optString("body", "")
            info.publishedAt = json.optString("published_at", "")

            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        info.apkUrl = asset.optString("browser_download_url", "")
                        info.apkSize = asset.optLong("size", 0L)
                        info.apkName = name
                        break
                    }
                }
            }

            Log.d(TAG, "Release info: ${info.version} (${info.title})")
            return info
        } catch (e: Exception) {
            Log.e(TAG, "fetchLatestRelease error: ${e.message}")
            return null
        } finally {
            try { conn?.disconnect() } catch (ignored: Exception) {}
        }
    }
}
