package com.memecio.app;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class DownloadHelper {

    public static void startDownload(Context context, String url, String filename) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "URL tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Drive: ganti ke direct download link
        String downloadUrl = url;
        if (url.contains("drive.google.com") || url.contains("usercontent.google.com")) {
            downloadUrl = fixDriveUrl(url);
        }

        // Deteksi HLS → pakai HlsDownloadHelper
        if (downloadUrl.toLowerCase().contains(".m3u8")) {
            HlsDownloadHelper.startHlsDownload(context, downloadUrl, filename);
            return;
        }

        try {
            File dir = new File(Environment.getExternalStorageDirectory(), "Termux/Download");
            if (!dir.exists()) dir.mkdirs();

            if (filename == null || filename.trim().isEmpty()) {
                filename = "video_" + System.currentTimeMillis() + ".mp4";
            }
            filename = filename.replaceAll("[\\\\/:*?\"<>|]", "_");
            if (!filename.contains(".")) filename += ".mp4";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle(filename);
            request.setDescription("Memec.io Download");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("Termux/Download", filename);
            request.allowScanningByMediaScanner();
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) {
                Toast.makeText(context, "DownloadManager tidak tersedia", Toast.LENGTH_LONG).show();
                return;
            }
            dm.enqueue(request);
            Toast.makeText(context, "Download dimulai: " + filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Gagal download: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String fixDriveUrl(String url) {
        try {
            String fileId = null;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("/d/([a-zA-Z0-9_-]+)").matcher(url);
            if (m.find()) fileId = m.group(1);
            if (fileId == null) {
                m = java.util.regex.Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)").matcher(url);
                if (m.find()) fileId = m.group(1);
            }
            if (fileId != null) {
                return "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&confirm=t";
            }
        } catch (Exception ignored) {}
        return url;
    }
}
