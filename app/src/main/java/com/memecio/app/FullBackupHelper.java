package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FullBackupHelper {

    public static void eksporSemuaData(Context context) {
        try {
            StringBuilder sb = new StringBuilder();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());

            // 1. Header
            sb.append("========================================\n");
            sb.append("     BACKUP LENGKAP MEMEC.IO\n");
            sb.append("     Waktu: ").append(timestamp).append("\n");
            sb.append("========================================\n\n");

            // 2. Info Aplikasi
            sb.append("=== INFORMASI APLIKASI ===\n");
            sb.append("Nama          : Memec.io\n");
            sb.append("Package       : com.memecio.app\n");
            sb.append("Versi         : 1.0\n");
            sb.append("Mode Tampilan : ").append(DisplayModeStore.getEffectiveMode(context)).append("\n");
            sb.append("Tipe Perangkat: ").append(DisplayModeStore.isTvDevice(context) ? "TV" : "HP").append("\n\n");

            // 3. Struktur UI / Daftar Activity
            sb.append("=== STRUKTUR UI / DAFTAR HALAMAN ===\n");
            sb.append("KalkulatorActivity      - Pintu masuk aplikasi (kode 140399)\n");
            sb.append("MainActivity            - Halaman utama dengan 3 tab\n");
            sb.append("  - Tab Beranda         - Grid foto/video + sidebar playlist + filter\n");
            sb.append("  - Tab Sumber          - Pilihan sumber media\n");
            sb.append("  - Tab Profil          - Pengaturan & alat bantu\n");
            sb.append("PreviewImageActivity    - Tampilan pratinjau gambar (swipe)\n");
            sb.append("VideoPlayerActivity     - Pemutar video dengan PiP (swipe)\n");
            sb.append("StreamingSourceActivity - Input link streaming (M3U/MP4)\n");
            sb.append("DriveSourceActivity     - Input link Google Drive (smart link)\n");
            sb.append("ServerSourceActivity    - Upload file ke server pribadi\n");
            sb.append("CustomPlaylistActivity  - Manajemen playlist manual\n");
            sb.append("AudioPlayerActivity     - Pemutar audio latar belakang\n");
            sb.append("StatistikActivity       - Statistik penggunaan\n");
            sb.append("RiwayatActivity         - Daftar riwayat link (PIN 808080)\n");
            sb.append("PinDialogActivity       - Dialog PIN\n");
            sb.append("CrashLogActivity        - Daftar log crash\n");
            sb.append("CrashLogDetailActivity  - Detail log crash\n\n");

            // 4. Preferensi
            sb.append("=== PREFERENSI TERSIMPAN ===\n");
            dumpSharedPreferences(context, "memecio_settings", sb);
            dumpSharedPreferences(context, "memecio_statistik", sb);
            sb.append("\n");

            // 5. Playlist Tersimpan
            sb.append("=== PLAYLIST TERSIMPAN (M3U/Streaming) ===\n");
            List<MediaItem> saved = SavedLinksStore.getAll(context);
            if (saved.isEmpty()) {
                sb.append("(Kosong)\n");
            } else {
                for (int i = 0; i < saved.size(); i++) {
                    MediaItem m = saved.get(i);
                    sb.append(i + 1).append(". ")
                      .append(m.title == null ? "(tanpa judul)" : m.title)
                      .append("\n   URL: ").append(m.uri.toString()).append("\n");
                }
            }
            sb.append("\n");

            // 6. Playlist Manual
            sb.append("=== PLAYLIST MANUAL ===\n");
            List<CustomPlaylistStore.Playlist> customs = CustomPlaylistStore.getAll(context);
            if (customs.isEmpty()) {
                sb.append("(Kosong)\n");
            } else {
                for (int i = 0; i < customs.size(); i++) {
                    CustomPlaylistStore.Playlist pl = customs.get(i);
                    sb.append(i + 1).append(". ").append(pl.name)
                      .append(" (").append(pl.items.size()).append(" item)\n");
                    for (MediaItem item : pl.items) {
                        sb.append("   - ").append(item.type == MediaItem.TYPE_VIDEO ? "[VIDEO]" : "[FOTO]")
                          .append(" ").append(item.title == null ? "(tanpa judul)" : item.title)
                          .append(" | ").append(item.uri.toString()).append("\n");
                    }
                }
            }
            sb.append("\n");

            // 7. Media Eksternal
            sb.append("=== MEDIA EKSTERNAL AKTIF (Drive/Streaming/Server) ===\n");
            List<MediaItem> external = ExternalMediaStore.getAll(context);
            String sourceLabel = ExternalMediaStore.getSourceLabel(context);
            sb.append("Sumber: ").append(sourceLabel == null || sourceLabel.isEmpty() ? "(kosong)" : sourceLabel).append("\n");
            if (external.isEmpty()) {
                sb.append("(Kosong)\n");
            } else {
                for (int i = 0; i < external.size(); i++) {
                    MediaItem m = external.get(i);
                    sb.append(i + 1).append(". ")
                      .append(m.title == null ? "(tanpa judul)" : m.title)
                      .append("\n   URL: ").append(m.uri.toString()).append("\n");
                }
            }
            sb.append("\n");

            // 8. Riwayat Link
            sb.append("=== RIWAYAT LINK ===\n");
            List<String> riwayat = RiwayatStore.getAll(context);
            if (riwayat.isEmpty()) {
                sb.append("(Kosong)\n");
            } else {
                for (int i = 0; i < riwayat.size(); i++) {
                    sb.append(i + 1).append(". ").append(riwayat.get(i)).append("\n");
                }
            }
            sb.append("\n");

            // 9. Log Crash
            sb.append("=== RINGKASAN LOG CRASH ===\n");
            File crashFile = new File(context.getFilesDir(), "memecio_crash.txt");
            if (crashFile.exists()) {
                sb.append("Ada file crash log di: ").append(crashFile.getAbsolutePath())
                  .append("\nUkuran: ").append(crashFile.length()).append(" bytes\n");
            } else {
                sb.append("Tidak ada log crash. Bersih!\n");
            }
            sb.append("\n");

            sb.append("========================================\n");
            sb.append("End of Backup\n");
            sb.append("========================================\n");

            // Simpan file - dengan fallback
            File targetDir = getWritableBackupDir(context);
            if (targetDir == null) {
                Toast.makeText(context, "Tidak ada folder yang bisa ditulis", Toast.LENGTH_LONG).show();
                return;
            }
            File file = new File(targetDir, "backup_memecio_" + timestamp + ".txt");
            FileOutputStream out = new FileOutputStream(file);
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.close();

            Toast.makeText(context, "Backup tersimpan di:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Gagal backup: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static File getWritableBackupDir(Context context) {
        // Prioritas 1: coba Termux/Backup Aman di internal storage (butuh MANAGE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                try {
                    File dir = new File(Environment.getExternalStorageDirectory(), "Termux/Backup Aman");
                    if (!dir.exists()) dir.mkdirs();
                    if (dir.exists() && dir.canWrite()) return dir;
                } catch (Exception ignored) {}
            }
        } else {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "Termux/Backup Aman");
                if (!dir.exists()) dir.mkdirs();
                if (dir.exists() && dir.canWrite()) return dir;
            } catch (Exception ignored) {}
        }

        // Prioritas 2: fallback ke app-specific external files (tidak butuh izin)
        try {
            File fallback = new File(context.getExternalFilesDir(null), "Backup Aman");
            if (!fallback.exists()) fallback.mkdirs();
            if (fallback.exists() && fallback.canWrite()) return fallback;
        } catch (Exception ignored) {}

        // Prioritas 3: fallback ke internal files
        try {
            File fallback = new File(context.getFilesDir(), "Backup Aman");
            if (!fallback.exists()) fallback.mkdirs();
            if (fallback.exists() && fallback.canWrite()) return fallback;
        } catch (Exception ignored) {}

        return null;
    }

    private static void dumpSharedPreferences(Context context, String prefName, StringBuilder sb) {
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Map<String, ?> all = prefs.getAll();
        sb.append("[").append(prefName).append("]\n");
        if (all.isEmpty()) {
            sb.append("  (kosong)\n");
        } else {
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
        }
    }
}
