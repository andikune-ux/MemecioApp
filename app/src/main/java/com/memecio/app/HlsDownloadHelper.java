package com.memecio.app;

import android.content.Context;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadRequest;
import androidx.media3.exoplayer.offline.DownloadService;

public class HlsDownloadHelper {

    public static void startHlsDownload(Context context, String url, String filename) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "URL tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }
        if (DownloadCache.getDownloadManager() == null) {
            Toast.makeText(context, "Download manager belum siap", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String id = "hls_" + System.currentTimeMillis();
            MediaItem mediaItem = new MediaItem.Builder()
                .setUri(url)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();

            android.net.Uri uri = android.net.Uri.parse(url);
            DownloadRequest request = new DownloadRequest.Builder(id, uri)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();

            DownloadService.sendAddDownload(
                context,
                HlsDownloadService.class,
                request,
                false
            );

            Toast.makeText(context, "Download HLS dimulai: " + (filename != null ? filename : id), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Gagal HLS download: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void pauseAll(Context context) {
        try {
            DownloadService.sendPauseDownloads(context, HlsDownloadService.class, false);
        } catch (Exception ignored) {}
    }

    public static void resumeAll(Context context) {
        try {
            DownloadService.sendResumeDownloads(context, HlsDownloadService.class, false);
        } catch (Exception ignored) {}
    }
}
