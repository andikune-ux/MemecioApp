package com.memecio.app

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService

object HlsDownloadHelper {

    @JvmStatic
    fun startHlsDownload(context: Context, url: String?, filename: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "URL tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        if (DownloadCache.getDownloadManager() == null) {
            Toast.makeText(context, "Download manager belum siap", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val id = "hls_${System.currentTimeMillis()}"
            val request = DownloadRequest.Builder(id, Uri.parse(url))
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()

            DownloadService.sendAddDownload(
                context,
                HlsDownloadService::class.java,
                request,
                false
            )

            Toast.makeText(context, "Download HLS dimulai: ${filename ?: id}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal HLS download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    fun pauseAll(context: Context) {
        try {
            DownloadService.sendPauseDownloads(context, HlsDownloadService::class.java, false)
        } catch (_: Exception) {}
    }

    @JvmStatic
    fun resumeAll(context: Context) {
        try {
            DownloadService.sendResumeDownloads(context, HlsDownloadService::class.java, false)
        } catch (_: Exception) {}
    }
}
