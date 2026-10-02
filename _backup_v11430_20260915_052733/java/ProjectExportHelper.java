package com.memecio.app;

import android.content.Context;
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

public class ProjectExportHelper {

    public static void export(Context context) {
        try {
            StringBuilder sb = new StringBuilder();
            String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
            String tsHuman = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());

            sb.append("========================================\n");
            sb.append("     PANDUAN PROYEK MEMEC.IO\n");
            sb.append("     Serah Terima AI / Developer\n");
            sb.append("========================================\n\n");
            String vName = "?";
            try { vName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName; } catch (Exception ignored) {}
            sb.append("Versi Saat Ini : V.").append(vName).append("\n");
            sb.append("Waktu Export   : ").append(tsHuman).append("\n");
            sb.append("Package        : ").append(context.getPackageName()).append("\n");
            sb.append("Device         : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
              .append(" | Android ").append(Build.VERSION.RELEASE)
              .append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n\n");

            sb.append("========================================\n");
            sb.append("CATATAN UNTUK AI / DEVELOPER LAIN\n");
            sb.append("========================================\n");
            sb.append("File ini dibuat otomatis oleh aplikasi Memec.io.\n");
            sb.append("Tujuannya: agar AI assistant lain bisa langsung nyambung.\n\n");
            sb.append("ATURAN WAJIB bagi AI yang menerima file ini:\n");
            sb.append("1. Setiap ada BUG BARU: update BugNotesStore.java (getBugsOpen)\n");
            sb.append("2. Setiap BUG DIPERBAIKI: pindah ke getBugsFixed + isi tanggal & solusi\n");
            sb.append("3. Setiap ada KENDALA baru: tambah di getLimitations\n");
            sb.append("4. Setiap ada FITUR BARU: update ChangelogStore.getFeatureStatus\n");
            sb.append("5. Setiap BUILD BARU: bump versionName + tambah BuildEntry di ChangelogStore\n");
            sb.append("6. Setiap update: update ProjectExportHelper ini agar tetap sinkron\n");
            sb.append("7. Aturan versioning: V.<patch_inti>.<tanggal>.<build_count>\n\n");

            sb.append("========================================\n");
            sb.append("1. ATURAN VERSIONING\n");
            sb.append("========================================\n");
            sb.append("Format: V.<patch_inti>.<tanggal>.<build_count>\n\n");
            sb.append("Penjelasan:\n");
            sb.append("- patch_inti  : Naik saat rombak UI besar atau ganti bulan\n");
            sb.append("                (jika >1 bulan tidak rombak, tetap naik)\n");
            sb.append("- tanggal     : Auto dari tanggal realtime build (1-31)\n");
            sb.append("- build_count : Auto, hitung build hari ini\n\n");
            sb.append("Contoh:\n");
            sb.append("- V.1.12.0 = Patch 1, tanggal 12, build ke-0\n");
            sb.append("- V.1.12.1 = Patch 1, tanggal 12, build ke-1\n");
            sb.append("- V.2.1.0  = Patch 2 (manual), tanggal 1, build ke-0\n\n");

            sb.append("========================================\n");
            sb.append("2. STRUKTUR FOLDER PROJECT\n");
            sb.append("========================================\n");
            sb.append("/root/MemecioApp/                <- Folder utama project\n");
            sb.append("  app/                           <- Modul aplikasi\n");
            sb.append("    build.gradle                 <- Version, dependencies\n");
            sb.append("    src/main/\n");
            sb.append("      AndroidManifest.xml        <- Daftar Activity, permission\n");
            sb.append("      java/com/memecio/app/      <- Kode Java (~70 file .java)\n");
            sb.append("      res/raw/                   <- File backup .txt\n");
            sb.append("      res/\n");
            sb.append("        layout/                  <- XML layout (UI)\n");
            sb.append("        drawable/                <- Ikon & background\n");
            sb.append("        values/                  <- Warna, string, style\n");
            sb.append("        mipmap/                  <- Ikon launcher\n\n");

            sb.append("========================================\n");
            sb.append("3. DAFTAR ACTIVITY & FUNGSI\n");
            sb.append("========================================\n");
            sb.append("CalculatorActivity      - Pintu masuk (kode 140399)\n");
            sb.append("MainActivity            - Tab Beranda/Sumber/Profil\n");
            sb.append("PreviewImageActivity    - Lihat foto dengan swipe\n");
            sb.append("VideoPlayerActivity     - Pemutar video (gesture, preview, kunci)\n");
            sb.append("AudioPlayerActivity     - Pemutar audio + service\n");
            sb.append("StreamingSourceActivity - Input M3U/MP4 streaming\n");
            sb.append("DriveSourceActivity     - Input link Google Drive\n");
            sb.append("ServerSourceActivity    - Upload ke server pribadi\n");
            sb.append("CustomPlaylistActivity  - Manajemen playlist manual\n");
            sb.append("StatistikActivity       - Statistik penggunaan\n");
            sb.append("RiwayatActivity         - Riwayat link (PIN 808080)\n");
            sb.append("PinDialogActivity       - Dialog PIN\n");
            sb.append("CrashLogActivity        - Daftar crash lama\n");
            sb.append("CrashLogDetailActivity  - Detail crash\n");
            sb.append("CrashHistoryActivity    - Riwayat crash baru (kode 111)\n");
            sb.append("SecretCodesActivity     - Daftar kode rahasia (kode 000)\n");
            sb.append("ChangelogActivity       - Changelog & status fitur (kode 222)\n");
            sb.append("SystemInfoActivity      - Info perangkat (kode 333)\n");
            sb.append("NetworkInfoActivity     - Info jaringan (kode 555)\n");
            sb.append("PermissionInfoActivity  - Daftar izin (kode 666)\n");
            sb.append("StorageAnalyzerActivity - Storage analyzer (kode 777)\n");
            sb.append("DownloadListActivity    - Daftar file download (kode 808)\n");
            sb.append("MultiviewActivity       - 4 video/foto sekaligus (grid 2x2)\n\n");

            sb.append("========================================\n");
            sb.append("4. ATURAN BUILD & INSTALL\n");
            sb.append("========================================\n");
            sb.append("Di Termux:\n");
            sb.append("  proot-distro login ubuntu\n");
            sb.append("  cd /root/MemecioApp\n\n");
            sb.append("Build:\n");
            sb.append("  rm -rf .gradle && rm -rf app/build\n");
            sb.append("  /root/gradle-8.10.2/bin/gradle assembleDebug --no-daemon\n\n");
            sb.append("Keluar & copy APK:\n");
            sb.append("  exit\n");
            sb.append("  cp /data/data/com.termux/files/usr/var/lib/proot-distro/containers/ubuntu/rootfs/root/MemecioApp/app/build/outputs/apk/debug/app-debug.apk ~/storage/shared/Termux/\"Pembaruan Aplikasi\"/MemecioApp.apk\n\n");

            sb.append("========================================\n");
            sb.append("5. CHANGELOG LENGKAP\n");
            sb.append("========================================\n");
            List<ChangelogStore.BuildEntry> builds = ChangelogStore.getBuilds();
            for (ChangelogStore.BuildEntry b : builds) {
                sb.append("\n").append(b.version).append("  |  ").append(b.timestamp).append("\n");
                sb.append("------------------------------------------------\n");
                for (String c : b.changes) {
                    sb.append("- ").append(c).append("\n");
                }
            }
            sb.append("\n");

            sb.append("========================================\n");
            sb.append("6. STATUS FITUR\n");
            sb.append("========================================\n");
            List<ChangelogStore.FeatureStatus> features = ChangelogStore.getFeatureStatus();
            String[] kategori = {"Selesai", "Sebagian", "Belum"};
            for (String kat : kategori) {
                sb.append("\n--- ").append(kat).append(" ---\n");
                String mark = "Selesai".equals(kat) ? "[x]" : ("Sebagian".equals(kat) ? "[~]" : "[ ]");
                for (ChangelogStore.FeatureStatus f : features) {
                    if (f.status.equals(kat)) {
                        sb.append(mark).append(" ").append(f.name).append(" (").append(f.dateAdded).append(")\n");
                    }
                }
            }
            sb.append("\n");

            sb.append("========================================\n");
            sb.append("7. DAFTAR KODE RAHASIA (di Kalkulator + =)\n");
            sb.append("========================================\n");
            sb.append("140399 : Buka aplikasi utama\n");
            List<SecretCodeRegistry.Code> codes = SecretCodeRegistry.getAll();
            for (SecretCodeRegistry.Code c : codes) {
                if (!c.hidden) {
                    sb.append(c.code).append("    : ").append(c.title).append("\n");
                }
            }
            sb.append("\n");

            sb.append("========================================\n");
            sb.append("7b. FITUR DOWNLOAD (BATCH C)\n");
            sb.append("========================================\n");
            sb.append("Lokasi file download : /sdcard/Termux/Download/\n");
            sb.append("HLS cache            : /sdcard/Termux/Download/.hls/\n");
            sb.append("Akses daftar download: Kalkulator + kode 808\n\n");
            sb.append("Komponen utama:\n");
            sb.append("- DownloadHelper.java        : MP4 + Drive (pakai DownloadManager Android)\n");
            sb.append("- HlsDownloadHelper.java     : HLS (.m3u8) pakai Media3 Offline\n");
            sb.append("- HlsDownloadService.java    : Background service + notifikasi progress\n");
            sb.append("- DownloadCache.java         : SimpleCache 500MB untuk HLS\n");
            sb.append("- DownloadListActivity.java  : Halaman lihat/hapus file download\n\n");
            sb.append("Cara pakai:\n");
            sb.append("- Buka video dari Beranda/Sumber → overlay detail → tombol Download\n");
            sb.append("- HLS otomatis dikenali dari URL (.m3u8)\n");
            sb.append("- File MP4/Drive → DownloadManager Android\n");
            sb.append("- File HLS → Media3 Offline (di folder .hls/)\n\n");
            sb.append("Catatan:\n");
            sb.append("- Download YouTube di-SKIP (library expired & tidak stabil)\n");
            sb.append("- Alternatif YouTube: share URL ke Seal/NewPipe\n\n");

            sb.append("========================================\n");
            sb.append("7c. FITUR PRIVASI (SESSION MANAGER)\n");
            sb.append("========================================\n");
            sb.append("Komponen:\n");
            sb.append("- SessionManager.java  : Track activity lifecycle (counter-based)\n");
            sb.append("- SessionState.java    : Suppression window + internal transition flag\n");
            sb.append("- BlurHelper.java      : RenderEffect blur (Android 12+)\n\n");
            sb.append("Cara kerja:\n");
            sb.append("- Saat keluar app atau layar mati → redirect ke Kalkulator\n");
            sb.append("- Grace period 1 detik untuk transisi internal\n");
            sb.append("- Suppression 30 detik saat buka file picker / settings\n");
            sb.append("- Pengecualian: PiP, rotasi, CalculatorActivity, PinDialogActivity\n");
            sb.append("- Blur recents: hanya jalan di ROM vanilla, tidak di XOS/Infinix\n\n");
            sb.append("Kode rahasia terkait:\n");
            sb.append("- 000 : Daftar kode rahasia\n");
            sb.append("- 101 : Toggle Developer Mode\n");
            sb.append("- 102 : Force Crash (hidden)\n");
            sb.append("- 200/201/202 : Settings Android shortcuts\n\n");

            sb.append("========================================\n");
            sb.append("8. CATATAN BUG & KENDALA\n");
            sb.append("========================================\n");

            sb.append("\n[BUG SUDAH DIPERBAIKI]\n");
            List<BugNotesStore.BugNote> fixed = BugNotesStore.getBugsFixed();
            for (BugNotesStore.BugNote b : fixed) {
                sb.append("\n[").append(b.status).append("] ").append(b.title).append("\n");
                sb.append("  Deskripsi : ").append(b.description).append("\n");
                sb.append("  Ditemukan : ").append(b.dateFound).append("\n");
                sb.append("  Diperbaiki: ").append(b.dateFixed).append("\n");
                sb.append("  Solusi    : ").append(b.solution).append("\n");
            }

            sb.append("\n[BUG TERBUKA]\n");
            List<BugNotesStore.BugNote> open = BugNotesStore.getBugsOpen();
            for (BugNotesStore.BugNote b : open) {
                sb.append("\n[").append(b.status).append("] ").append(b.title).append("\n");
                sb.append("  Deskripsi : ").append(b.description).append("\n");
                sb.append("  Ditemukan : ").append(b.dateFound).append("\n");
                sb.append("  Solusi    : ").append(b.solution).append("\n");
            }

            sb.append("\n[KENDALA TEKNIS]\n");
            List<BugNotesStore.Limitation> lims = BugNotesStore.getLimitations();
            for (BugNotesStore.Limitation l : lims) {
                sb.append("- ").append(l.title).append("\n");
                sb.append("  ").append(l.description).append("\n");
            }
            sb.append("\n");

            sb.append("========================================\n");
            sb.append("9. RENCANA FITUR SELANJUTNYA\n");
            sb.append("========================================\n");
            sb.append("- Widget channel favorit\n");
            sb.append("- Multi-audio track (pilih audio track film)\n");
            sb.append("- Dual subtitle (2 bahasa sekaligus)\n");
            sb.append("- Subtitle gesture control\n");
            sb.append("- Audio equalizer + preset\n");
            sb.append("- Video frame capture (screenshot resolusi asli)\n");
            sb.append("- Kids lock (kunci layar anak)\n");
            sb.append("- Headset control\n");
            sb.append("- Android TV support\n");
            sb.append("- Mode Gelap/Terang\n");
            sb.append("- Sleep timer audio\n");
            sb.append("- Dynamic theme (tema ikut warna poster)\n");
            sb.append("- Widget audio control di home screen\n");
            sb.append("- Update aplikasi otomatis\n\n");

            sb.append("========================================\n");
            sb.append("End of Project Manual\n");
            sb.append("========================================\n");

            String content = sb.toString();

            // Simpan ke 2 lokasi
            int savedCount = 0;
            String[] savedPaths = new String[2];

            // Lokasi 1: internal app (untuk dibaca aplikasi)
            try {
                File internalDir = new File(context.getFilesDir(), "ProjectExport");
                if (!internalDir.exists()) internalDir.mkdirs();
                File f1 = new File(internalDir, "panduan_terbaru.txt");
                FileOutputStream out1 = new FileOutputStream(f1);
                out1.write(content.getBytes(StandardCharsets.UTF_8));
                out1.close();
                savedPaths[0] = f1.getAbsolutePath();
                savedCount++;
            } catch (Exception ignored) {}

            // Lokasi 2: /sdcard untuk kirim ke AI
            File externalDir = getExternalWritableDir(context);
            if (externalDir != null) {
                try {
                    File f2 = new File(externalDir, "panduan_proyek_" + ts + ".txt");
                    FileOutputStream out2 = new FileOutputStream(f2);
                    out2.write(content.getBytes(StandardCharsets.UTF_8));
                    out2.close();
                    savedPaths[1] = f2.getAbsolutePath();
                    savedCount++;
                } catch (Exception ignored) {}
            }

            if (savedCount == 0) {
                Toast.makeText(context, "Gagal export", Toast.LENGTH_LONG).show();
                return;
            }

            StringBuilder msg = new StringBuilder("Panduan tersimpan:\n");
            if (savedPaths[0] != null) msg.append("1. ").append(savedPaths[0]).append("\n");
            if (savedPaths[1] != null) msg.append("2. ").append(savedPaths[1]).append("\n");
            Toast.makeText(context, msg.toString(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, "Gagal export: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static File getExternalWritableDir(Context context) {
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
        try {
            File f = new File(context.getExternalFilesDir(null), "Backup Aman");
            if (!f.exists()) f.mkdirs();
            if (f.exists() && f.canWrite()) return f;
        } catch (Exception ignored) {}
        return null;
    }
}
