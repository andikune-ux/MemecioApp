package com.memecio.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

public class StorageAnalyzerActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_storage_analyzer);
        findViewById(R.id.btnBackStorage).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvStorageContent)).setText(buildInfo());
    }

    private String buildInfo() {
        StringBuilder sb = new StringBuilder();
        try {
            File filesDir = getFilesDir();
            File cacheDir = getCacheDir();
            File extDir = getExternalFilesDir(null);

            sb.append("=== STORAGE APLIKASI ===\n\n");
            sb.append("Internal files : ").append(formatSize(dirSize(filesDir))).append("\n");
            sb.append("   Path        : ").append(filesDir.getAbsolutePath()).append("\n\n");
            sb.append("Cache          : ").append(formatSize(dirSize(cacheDir))).append("\n");
            sb.append("   Path        : ").append(cacheDir.getAbsolutePath()).append("\n\n");

            if (extDir != null) {
                sb.append("External files : ").append(formatSize(dirSize(extDir))).append("\n");
                sb.append("   Path        : ").append(extDir.getAbsolutePath()).append("\n\n");
            }

            sb.append("=== FILE PENTING ===\n");
            File crash = new File(filesDir, "memecio_crash.txt");
            sb.append("Crash log      : ").append(crash.exists() ? formatSize(crash.length()) : "(tidak ada)").append("\n");

            File preview = new File(cacheDir, "preview");
            sb.append("Preview cache  : ").append(preview.exists() ? formatSize(dirSize(preview)) : "(tidak ada)").append("\n\n");

            sb.append("=== RUANG DISK ===\n");
            long free = filesDir.getFreeSpace();
            long total = filesDir.getTotalSpace();
            sb.append("Free           : ").append(formatSize(free)).append("\n");
            sb.append("Total          : ").append(formatSize(total)).append("\n");
            sb.append("Used           : ").append(formatSize(total - free)).append("\n");
        } catch (Exception e) {
            sb.append("\nError: ").append(e.getMessage());
        }
        return sb.toString();
    }

    private long dirSize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) size += dirSize(f);
                else size += f.length();
            }
        }
        return size;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
