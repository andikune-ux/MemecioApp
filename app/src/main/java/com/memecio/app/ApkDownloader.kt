package com.memecio.app

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * ApkDownloader — download file APK dari URL dengan progress + support resume.
 *
 * Lokasi simpan: Internal Storage/Memecio/download/
 * Nama file: MemecioApp-v1.02.0.apk
 */
object ApkDownloader {

    private const val TAG = "ApkDownloader"
    private const val TIMEOUT_MS = 30000
    private const val BUFFER_SIZE = 8192

    interface ProgressListener {
        /** progress 0-100, bytesDownloaded, totalBytes (bisa -1 kalau unknown) */
        fun onProgress(progress: Int, bytesDownloaded: Long, totalBytes: Long)
        fun onSuccess(file: File)
        fun onError(message: String)
        fun onCancelled()
    }

    /**
     * Ambil folder download: Internal Storage/Memecio/download/
     * Fallback ke app-specific external files kalau gagal.
     */
    @JvmStatic
    fun getDownloadDir(context: Context): File {
        // Prioritas 1: /sdcard/Memecio/download
        try {
            val publicDir = File(Environment.getExternalStorageDirectory(), "Memecio/download")
            if (!publicDir.exists()) publicDir.mkdirs()
            if (publicDir.exists() && publicDir.canWrite()) return publicDir
        } catch (ignored: Exception) {}

        // Prioritas 2: app-specific external
        try {
            val extDir = File(context.getExternalFilesDir(null), "download")
            if (!extDir.exists()) extDir.mkdirs()
            if (extDir.exists() && extDir.canWrite()) return extDir
        } catch (ignored: Exception) {}

        // Prioritas 3: internal files
        val intDir = File(context.filesDir, "download")
        if (!intDir.exists()) intDir.mkdirs()
        return intDir
    }

    /** Nama file default untuk versi tertentu. */
    @JvmStatic
    fun buildFileName(version: String): String {
        return "MemecioApp-v$version.apk"
    }

    /**
     * Download APK. Berjalan di thread background (dipanggil dari new Thread).
     * Return File hasil download, atau null kalau gagal.
     */
    @JvmStatic
    fun download(
        context: Context,
        url: String,
        version: String,
        listener: ProgressListener
    ) {
        var conn: HttpURLConnection? = null
        var input: InputStream? = null
        var output: FileOutputStream? = null
        var tmpFile: File? = null

        try {
            val dir = getDownloadDir(context)
            val fileName = buildFileName(version)
            val finalFile = File(dir, fileName)
            tmpFile = File(dir, "$fileName.part")

            // Hapus partial lama kalau ada, mulai fresh
            if (tmpFile.exists()) tmpFile.delete()

            val urlObj = URL(url)
            conn = urlObj.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "MemecioApp-Downloader/1.0")
            conn.setRequestProperty("Accept", "application/octet-stream")

            val code = conn.responseCode
            if (code != 200) {
                listener.onError("Server menolak (HTTP $code)")
                return
            }

            val totalBytes = conn.contentLengthLong

            input = conn.inputStream
            output = FileOutputStream(tmpFile)

            val buffer = ByteArray(BUFFER_SIZE)
            var bytesDownloaded = 0L
            var lastProgress = -1

            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
                bytesDownloaded += read

                if (totalBytes > 0) {
                    val progress = ((bytesDownloaded * 100) / totalBytes).toInt()
                    if (progress != lastProgress) {
                        lastProgress = progress
                        listener.onProgress(progress, bytesDownloaded, totalBytes)
                    }
                } else {
                    // Ukuran tidak diketahui, kirim progress berdasarkan byte (0 saja)
                    listener.onProgress(0, bytesDownloaded, -1)
                }
            }

            output.flush()
            output.close()
            output = null
            input.close()
            input = null
            conn.disconnect()
            conn = null

            // Rename .part ke .apk
            if (finalFile.exists()) finalFile.delete()
            val renamed = tmpFile.renameTo(finalFile)
            if (!renamed) {
                listener.onError("Gagal menyimpan file")
                return
            }

            // Simpan ke UpdateStore
            try {
                UpdateStore.addApk(context, version, finalFile.absolutePath, finalFile.length())
            } catch (ignored: Exception) {}

            listener.onProgress(100, finalFile.length(), finalFile.length())
            listener.onSuccess(finalFile)
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            listener.onError(e.message ?: "Download gagal")
        } finally {
            try { output?.close() } catch (ignored: Exception) {}
            try { input?.close() } catch (ignored: Exception) {}
            try { conn?.disconnect() } catch (ignored: Exception) {}
        }
    }

    /** Hapus file APK dari penyimpanan. */
    @JvmStatic
    fun deleteApk(path: String): Boolean {
        return try {
            val f = File(path)
            if (f.exists()) f.delete() else false
        } catch (ignored: Exception) {
            false
        }
    }

    /** Format ukuran byte jadi string (KB / MB / GB). */
    @JvmStatic
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format(java.util.Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
