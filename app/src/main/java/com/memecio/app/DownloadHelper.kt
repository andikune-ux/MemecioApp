package com.memecio.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.util.regex.Pattern

object DownloadHelper {

    @JvmStatic
    fun startDownload(context: Context, url: String?, filename: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "URL tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Drive: ganti ke direct download link
        var downloadUrl = url
        if (url.contains("drive.google.com") || url.contains("usercontent.google.com")) {
            downloadUrl = fixDriveUrl(url)
        }

        // Deteksi HLS -> pakai HlsDownloadHelper
        if (downloadUrl.lowercase().contains(".m3u8")) {
            HlsDownloadHelper.startHlsDownload(context, downloadUrl, filename)
            return
        }

        try {
            val dir = File(Environment.getExternalStorageDirectory(), "Termux/Download")
            if (!dir.exists()) dir.mkdirs()

            var fname = filename
            if (fname.isNullOrBlank()) {
                fname = "video_" + System.currentTimeMillis() + ".mp4"
            }
            fname = fname.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            if (!fname.contains(".")) fname += ".mp4"

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
            request.setTitle(fname)
            request.setDescription("Memec.io Download")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir("Termux/Download", fname)
            request.allowScanningByMediaScanner()
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (dm == null) {
                Toast.makeText(context, "DownloadManager tidak tersedia", Toast.LENGTH_LONG).show()
                return
            }
            dm.enqueue(request)
            Toast.makeText(context, "Download dimulai: $fname", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal download: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun fixDriveUrl(url: String): String {
        try {
            var fileId: String? = null
            var m = Pattern.compile("/d/([a-zA-Z0-9_-]+)").matcher(url)
            if (m.find()) fileId = m.group(1)
            if (fileId == null) {
                m = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)").matcher(url)
                if (m.find()) fileId = m.group(1)
            }
            if (fileId != null) {
                return "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
            }
        } catch (ignored: Exception) {
        }
        return url
    }
}
