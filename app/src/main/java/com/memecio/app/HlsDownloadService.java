package com.memecio.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadService;
import androidx.media3.exoplayer.scheduler.Scheduler;

import java.util.List;

public class HlsDownloadService extends DownloadService {

    private static final String CHANNEL_ID = "hls_download_channel";
    private static final int NOTIF_ID = 771;

    public HlsDownloadService() {
        super(1, 1000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "HLS Download", NotificationManager.IMPORTANCE_LOW);
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) nm.createNotificationChannel(ch);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return DownloadCache.getDownloadManager();
    }

    @Override
    protected Scheduler getScheduler() {
        return null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads, int notMetRequirements) {
        try {
            int total = downloads.size();
            int done = 0;
            int progressAvg = 0;
            String judul = "Download HLS";
            for (Download d : downloads) {
                if (d.state == Download.STATE_COMPLETED) done++;
                progressAvg += (int)(d.getPercentDownloaded());
                if (d.request != null && d.request.uri != null) {
                    String u = d.request.uri.toString();
                    int slash = u.lastIndexOf('/');
                    if (slash >= 0 && slash < u.length() - 1) judul = u.substring(slash + 1);
                }
            }
            if (total > 0) progressAvg = progressAvg / total;
            if (progressAvg < 0) progressAvg = 0;
            if (progressAvg > 100) progressAvg = 100;

            Intent openIntent = new Intent(this, DownloadListActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(
                this, 0, openIntent,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);

            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                b = new Notification.Builder(this, CHANNEL_ID);
            } else {
                b = new Notification.Builder(this);
            }
            b.setContentTitle("Memec.io Download")
             .setContentText(judul + " • " + done + "/" + total + " • " + progressAvg + "%")
             .setSmallIcon(android.R.drawable.stat_sys_download)
             .setContentIntent(pi)
             .setOngoing(true)
             .setProgress(100, progressAvg, false);

            return b.build();
        } catch (Exception e) {
            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                b = new Notification.Builder(this, CHANNEL_ID);
            } else {
                b = new Notification.Builder(this);
            }
            return b.setContentTitle("Memec.io Download")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .build();
        }
    }
}
