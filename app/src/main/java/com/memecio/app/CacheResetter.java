package com.memecio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import java.io.File;

public class CacheResetter {
    public static void confirmAndReset(Context context) {
        new AlertDialog.Builder(context)
            .setTitle("Reset Cache")
            .setMessage("Hapus semua cache thumbnail, preview, dan file sementara?\n\nData playlist dan riwayat TIDAK akan terhapus.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Reset", (d, w) -> doReset(context))
            .show();
    }

    private static void doReset(Context context) {
        long totalFreed = 0;
        try {
            // 1. Cache dir
            File cacheDir = context.getCacheDir();
            totalFreed += deleteDir(cacheDir, false);

            // 2. External cache
            File extCache = context.getExternalCacheDir();
            if (extCache != null) totalFreed += deleteDir(extCache, false);

            // 3. Preview cache (kalau ada custom folder)
            File filesDir = context.getFilesDir();
            File previewDir = new File(filesDir, "preview");
            if (previewDir.exists()) totalFreed += deleteDir(previewDir, true);

            String msg = "Cache dibersihkan: " + formatSize(totalFreed);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Gagal reset: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static long deleteDir(File dir, boolean deleteSelf) {
        if (dir == null || !dir.exists()) return 0;
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) size += deleteDir(f, true);
                else {
                    size += f.length();
                    f.delete();
                }
            }
        }
        if (deleteSelf) dir.delete();
        return size;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
