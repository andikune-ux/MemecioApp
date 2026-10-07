package com.memecio.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler

class HlsDownloadService : DownloadService(1, 1000) {

    override fun onCreate() {
        super.onCreate()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ch = NotificationChannel(
                    CHANNEL_ID, "HLS Download", NotificationManager.IMPORTANCE_LOW
                )
                val nm = getSystemService(NotificationManager::class.java)
                nm?.createNotificationChannel(ch)
            }
        } catch (ignored: Exception) {
        }
    }

    override fun getDownloadManager(): DownloadManager {
        DownloadCache.init(this)
        return DownloadCache.getDownloadManager() ?: throw IllegalStateException("DownloadManager not initialized")
    }

    override fun getScheduler(): Scheduler? {
        return null
    }

    override fun getForegroundNotification(downloads: List<Download>, notMetRequirements: Int): Notification {
        return try {
            val total = downloads.size
            var done = 0
            var progressAvg = 0
            var judul = "Download HLS"
            for (d in downloads) {
                if (d.state == Download.STATE_COMPLETED) done++
                progressAvg += d.percentDownloaded.toInt()
                d.request?.uri?.toString()?.let { u ->
                    val slash = u.lastIndexOf('/')
                    if (slash >= 0 && slash < u.length - 1) judul = u.substring(slash + 1)
                }
            }
            if (total > 0) progressAvg /= total
            if (progressAvg < 0) progressAvg = 0
            if (progressAvg > 100) progressAvg = 100

            val openIntent = Intent(this, DownloadListActivity::class.java)
            openIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pi = PendingIntent.getActivity(
                this, 0, openIntent,
                if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
            )

            val b: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                Notification.Builder(this)
            }
            b.setContentTitle("Memec.io Download")
                .setContentText("$judul • $done/$total • $progressAvg%")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentIntent(pi)
                .setOngoing(true)
                .setProgress(100, progressAvg, false)
            b.build()
        } catch (e: Exception) {
            val b: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                Notification.Builder(this)
            }
            b.setContentTitle("Memec.io Download")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build()
        }
    }

    companion object {
        private const val CHANNEL_ID = "hls_download_channel"
    }
}
