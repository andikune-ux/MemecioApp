package com.memecio.app

import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProjectExportHelper {

    @JvmStatic
    fun export(context: Context) {
        try {
            val sb = StringBuilder()
            val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val tsHuman = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            sb.append("========================================\n")
            sb.append(" PANDUAN PROYEK MEMEC.IO\n")
            sb.append(" Serah Terima AI / Developer\n")
            sb.append("========================================\n\n")

            var vName = "?"
            try {
                vName = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
            } catch (ignored: Exception) {
            }
            sb.append("Versi Saat Ini : V.").append(vName).append("\n")
            sb.append("Waktu Export   : ").append(tsHuman).append("\n")
            sb.append("Package        : ").append(context.packageName).append("\n")
            sb.append("Device         : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
                .append(" | Android ").append(Build.VERSION.RELEASE)
                .append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n\n")

            sb.append("========================================\n")
            sb.append("CATATAN UNTUK AI / DEVELOPER LAIN\n")
            sb.append("========================================\n")
            sb.append("File ini dibuat otomatis oleh aplikasi Memec.io.\n")
            sb.append("Tujuannya: agar AI assistant lain bisa langsung nyambung.\n\n")
            sb.append("ATURAN WAJIB bagi AI yang menerima file ini:\n")
            sb.append("1. Setiap ada BUG BARU: update BugNotesStore.java (getBugsOpen)\n")
            sb.append("2. Setiap BUG DIPERBAIKI: pindah ke getBugsFixed + isi tanggal & solusi\n")
            sb.append("3. Setiap ada KENDALA baru: tambah di getLimitations\n")
            sb.append("4. Setiap ada FITUR BARU: update ChangelogStore.getFeatureStatus\n")
            sb.append("5. Setiap BUILD BARU: bump versionName + tambah BuildEntry di ChangelogStore\n")
            sb.append("6. Setiap update: update ProjectExportHelper ini agar tetap sinkron\n")
            sb.append("7. Aturan versioning: V...\n\n")

            sb.append("========================================\n")
            sb.append("1. ATURAN VERSIONING\n")
            sb.append("========================================\n")
            sb.append("Format: V...\n\n")
            sb.append("Penjelasan:\n")
            sb.append("- patch_inti : Naik otomatis saat update besar ATAU\n")
            sb.append("               otomatis setiap ganti bulan kalender\n")
            sb.append("- tanggal    : Tanggal realtime build (1-31),\n")
            sb.append("               auto ganti tiap pergantian tanggal\n")
            sb.append("- build_count: Hitungan build HARI ITU.\n")
            sb.append("               Auto naik tiap build, RESET ke 0\n")
            sb.append("               saat ganti tanggal\n\n")
            sb.append("Contoh:\n")
            sb.append("- V.1.14.57 = Patch 1, tgl 14, build ke-57\n")
            sb.append("- V.1.14.58 = Patch 1, tgl 14, build ke-58\n")
            sb.append("- V.1.15.0  = Patch 1, tgl 15, build ke-0 (RESET)\n")
            sb.append("- V.2.1.0   = Patch 2 (ganti bulan), tgl 1, build ke-0\n\n")

            sb.append("2. STRUKTUR FOLDER PROJECT\n")
            sb.append("========================================\n")
            sb.append("/root/MemecioApp/          <- Folder utama project\n")
            sb.append("  app/                     <- Modul aplikasi\n")
            sb.append("    build.gradle           <- Version, dependencies\n")
            sb.append("    src/main/\n")
            sb.append("      AndroidManifest.xml  <- Daftar Activity, permission\n")
            sb.append("      java/com/memecio/app/ <- Kode Java (~70 file .java)\n")
            sb.append("      res/raw/             <- File backup .txt\n")
            sb.append("      res/\n")
            sb.append("        layout/            <- XML layout (UI)\n")
            sb.append("        drawable/          <- Ikon & background\n")
            sb.append("        values/            <- Warna, string, style\n")
            sb.append("        mipmap/            <- Ikon launcher\n\n")

            sb.append("========================================\n")
            sb.append("3. DAFTAR ACTIVITY & FUNGSI\n")
            sb.append("========================================\n")
            sb.append("CalculatorActivity - Pintu masuk (kode 140399)\n")
            sb.append("MainActivity - Tab Beranda/Sumber/Profil\n")
            sb.append("PreviewImageActivity - Lihat foto dengan swipe\n")
            sb.append("VideoPlayerActivity - Pemutar video (gesture, preview, kunci)\n")
            sb.append("AudioPlayerActivity - Pemutar audio + service\n")
            sb.append("StreamingSourceActivity - Input M3U/MP4 streaming\n")
            sb.append("DriveSourceActivity - Input link Google Drive\n")
            sb.append("ServerSourceActivity - Upload ke server pribadi\n")
            sb.append("CustomPlaylistActivity - Manajemen playlist manual\n")
            sb.append("StatistikActivity - Statistik penggunaan\n")
            sb.append("RiwayatActivity - Riwayat link (PIN 808080)\n")
            sb.append("PinDialogActivity - Dialog PIN\n")
            sb.append("CrashLogActivity - Daftar crash lama\n")
            sb.append("CrashLogDetailActivity - Detail crash\n")
            sb.append("CrashHistoryActivity - Riwayat crash baru (kode 111)\n")
            sb.append("SecretCodesActivity - Daftar kode rahasia (kode 000)\n")
            sb.append("ChangelogActivity - Changelog & status fitur (kode 222)\n")
            sb.append("SystemInfoActivity - Info perangkat (kode 333)\n")
            sb.append("NetworkInfoActivity - Info jaringan (kode 555)\n")
            sb.append("PermissionInfoActivity - Daftar izin (kode 666)\n")
            sb.append("StorageAnalyzerActivity - Storage analyzer (kode 777)\n")
            sb.append("DownloadListActivity - Daftar file download (kode 808)\n")
            sb.append("MultiviewActivity - 4 video/foto sekaligus (grid 2x2)\n\n")

            sb.append("========================================\n")
            sb.append("4. ATURAN BUILD & INSTALL\n")
            sb.append("========================================\n")
            sb.append("Di Termux:\n")
            sb.append("  proot-distro login ubuntu\n")
            sb.append("  cd /root/MemecioApp\n\n")
            sb.append("Build:\n")
            sb.append("  rm -rf .gradle && rm -rf app/build\n")
            sb.append("  /root/gradle-8.10.2/bin/gradle assembleDebug --no-daemon\n\n")
            sb.append("Keluar & copy APK:\n")
            sb.append("  exit\n")
            sb.append("  cp /data/data/com.termux/files/usr/var/lib/proot-distro/containers/ubuntu/rootfs/root/MemecioApp/app/build/outputs/apk/debug/app-debug.apk ~/storage/shared/Termux/\"Pembaruan Aplikasi\"/MemecioApp.apk\n\n")

            sb.append("========================================\n")
            sb.append("5. CHANGELOG LENGKAP\n")
            sb.append("========================================\n")
            val builds = ChangelogStore.getBuilds()
            for (b in builds) {
                sb.append("\n").append(b.version).append(" | ").append(b.timestamp).append("\n")
                sb.append("------------------------------------------------\n")
                for (c in b.changes) {
                    sb.append("- ").append(c).append("\n")
                }
            }
            sb.append("\n")

            sb.append("========================================\n")
            sb.append("6. STATUS FITUR\n")
            sb.append("========================================\n")
            val features = ChangelogStore.getFeatureStatus()
            val kategori = arrayOf("Selesai", "Sebagian", "Belum")
            for (kat in kategori) {
                sb.append("\n--- ").append(kat).append(" ---\n")
                val mark = if ("Selesai" == kat) "[x]" else if ("Sebagian" == kat) "[~]" else "[ ]"
                for (f in features) {
                    if (f.status == kat) {
                        sb.append(mark).append(" ").append(f.name).append(" (").append(f.dateAdded).append(")\n")
                    }
                }
            }
            sb.append("\n")

            sb.append("========================================\n")
            sb.append("7. DAFTAR KODE RAHASIA (di Kalkulator + =)\n")
            sb.append("========================================\n")
            sb.append("140399 : Buka aplikasi utama\n")
            val codes = SecretCodeRegistry.getAll()
            for (c in codes) {
                if (!c.hidden) {
                    sb.append(c.code).append(" : ").append(c.title).append("\n")
                }
            }
            sb.append("\n")

            sb.append("========================================\n")
            sb.append("7b. FITUR DOWNLOAD (BATCH C)\n")
            sb.append("========================================\n")
            sb.append("Lokasi file download : /sdcard/Termux/Download/\n")
            sb.append("HLS cache            : /sdcard/Termux/Download/.hls/\n")
            sb.append("Akses daftar download: Kalkulator + kode 808\n\n")
            sb.append("Komponen utama:\n")
            sb.append("- DownloadHelper.java    : MP4 + Drive (pakai DownloadManager Android)\n")
            sb.append("- HlsDownloadHelper.java : HLS (.m3u8) pakai Media3 Offline\n")
            sb.append("- HlsDownloadService.java: Background service + notifikasi progress\n")
            sb.append("- DownloadCache.java     : SimpleCache 500MB untuk HLS\n")
            sb.append("- DownloadListActivity.java : Halaman lihat/hapus file download\n\n")
            sb.append("Cara pakai:\n")
            sb.append("- Buka video dari Beranda/Sumber -> overlay detail -> tombol Download\n")
            sb.append("- HLS otomatis dikenali dari URL (.m3u8)\n")
            sb.append("- File MP4/Drive -> DownloadManager Android\n")
            sb.append("- File HLS -> Media3 Offline (di folder .hls/)\n\n")
            sb.append("Catatan:\n")
            sb.append("- Download YouTube di-SKIP (library expired & tidak stabil)\n")
            sb.append("- Alternatif YouTube: share URL ke Seal/NewPipe\n\n")

            sb.append("========================================\n")
            sb.append("7c. FITUR PRIVASI (SESSION MANAGER)\n")
            sb.append("========================================\n")
            sb.append("Komponen:\n")
            sb.append("- SessionManager.java : Track activity lifecycle (counter-based)\n")
            sb.append("- SessionState.java   : Suppression window + internal transition flag\n")
            sb.append("- BlurHelper.java     : RenderEffect blur (Android 12+)\n\n")
            sb.append("Cara kerja:\n")
            sb.append("- Saat keluar app atau layar mati -> redirect ke Kalkulator\n")
            sb.append("- Grace period 1 detik untuk transisi internal\n")
            sb.append("- Suppression 30 detik saat buka file picker / settings\n")
            sb.append("- Pengecualian: PiP, rotasi, CalculatorActivity, PinDialogActivity\n")
            sb.append("- Blur recents: hanya jalan di ROM vanilla, tidak di XOS/Infinix\n\n")
            sb.append("Kode rahasia terkait:\n")
            sb.append("- 000 : Daftar kode rahasia\n")
            sb.append("- 101 : Toggle Developer Mode\n")
            sb.append("- 102 : Force Crash (hidden)\n")
            sb.append("- 200/201/202 : Settings Android shortcuts\n\n")

            sb.append("========================================\n")
            sb.append("8. CATATAN BUG & KENDALA\n")
            sb.append("========================================\n")
            sb.append("\n[BUG SUDAH DIPERBAIKI]\n")
            val fixed = BugNotesStore.getBugsFixed()
            for (b in fixed) {
                sb.append("\n[").append(b.status).append("] ").append(b.title).append("\n")
                sb.append("  Deskripsi  : ").append(b.description).append("\n")
                sb.append("  Ditemukan  : ").append(b.dateFound).append("\n")
                sb.append("  Diperbaiki : ").append(b.dateFixed).append("\n")
                sb.append("  Solusi     : ").append(b.solution).append("\n")
            }
            sb.append("\n[BUG TERBUKA]\n")
            val open = BugNotesStore.getBugsOpen()
            for (b in open) {
                sb.append("\n[").append(b.status).append("] ").append(b.title).append("\n")
                sb.append("  Deskripsi  : ").append(b.description).append("\n")
                sb.append("  Ditemukan  : ").append(b.dateFound).append("\n")
                sb.append("  Solusi     : ").append(b.solution).append("\n")
            }
            sb.append("\n[KENDALA TEKNIS]\n")
            val lims = BugNotesStore.getLimitations()
            for (l in lims) {
                sb.append("- ").append(l.title).append("\n")
                sb.append("  ").append(l.description).append("\n")
            }
            sb.append("\n")

            sb.append("========================================\n")
            sb.append("9. RENCANA FITUR SELANJUTNYA\n")
            sb.append("========================================\n")
            val roadmap = RoadmapStore.getRoadmap()
            for (r in roadmap) {
                sb.append("- ").append(r.name)
                if (r.note != null && r.note!!.isNotEmpty()) {
                    sb.append(" (").append(r.note).append(")")
                }
                sb.append("\n")
            }
            sb.append("\n")
            sb.append("End of Project Manual\n")
            sb.append("========================================\n")

            val content = sb.toString()

            // Simpan ke 2 lokasi
            var savedCount = 0
            val savedPaths = arrayOfNulls<String>(2)

            // Lokasi 1: internal app (untuk dibaca aplikasi)
            try {
                val internalDir = File(context.filesDir, "ProjectExport")
                if (!internalDir.exists()) internalDir.mkdirs()
                val f1 = File(internalDir, "panduan_terbaru.txt")
                val out1 = FileOutputStream(f1)
                out1.write(content.toByteArray(StandardCharsets.UTF_8))
                out1.close()
                savedPaths[0] = f1.absolutePath
                savedCount++
            } catch (ignored: Exception) {
            }

            // Lokasi 2: /sdcard untuk kirim ke AI
            val externalDir = getExternalWritableDir(context)
            if (externalDir != null) {
                try {
                    val f2 = File(externalDir, "panduan_proyek_$ts.txt")
                    val out2 = FileOutputStream(f2)
                    out2.write(content.toByteArray(StandardCharsets.UTF_8))
                    out2.close()
                    savedPaths[1] = f2.absolutePath
                    savedCount++
                } catch (ignored: Exception) {
                }
            }

            if (savedCount == 0) {
                Toast.makeText(context, "Gagal export", Toast.LENGTH_LONG).show()
                return
            }

            val msg = StringBuilder("Panduan tersimpan:\n")
            if (savedPaths[0] != null) msg.append("1. ").append(savedPaths[0]).append("\n")
            if (savedPaths[1] != null) msg.append("2. ").append(savedPaths[1]).append("\n")
            Toast.makeText(context, msg.toString(), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal export: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getExternalWritableDir(context: Context): File? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                try {
                    val dir = File(Environment.getExternalStorageDirectory(), "Termux/Backup Aman")
                    if (!dir.exists()) dir.mkdirs()
                    if (dir.exists() && dir.canWrite()) return dir
                } catch (ignored: Exception) {
                }
            }
        } else {
            try {
                val dir = File(Environment.getExternalStorageDirectory(), "Termux/Backup Aman")
                if (!dir.exists()) dir.mkdirs()
                if (dir.exists() && dir.canWrite()) return dir
            } catch (ignored: Exception) {
            }
        }

        try {
            val f = File(context.getExternalFilesDir(null), "Backup Aman")
            if (!f.exists()) f.mkdirs()
            if (f.exists() && f.canWrite()) return f
        } catch (ignored: Exception) {
        }
        return null
    }
}
