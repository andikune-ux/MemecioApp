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

object FullBackupHelper {

    @JvmStatic
    fun eksporSemuaData(context: Context) {
        try {
            val sb = StringBuilder()
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

            // 1. Header
            sb.append("========================================\n")
            sb.append(" BACKUP LENGKAP MEMEC.IO\n")
            sb.append(" Waktu: ").append(timestamp).append("\n")
            sb.append("========================================\n\n")

            // 2. Info Aplikasi
            sb.append("=== INFORMASI APLIKASI ===\n")
            sb.append("Nama    : Memec.io\n")
            sb.append("Package : com.memecio.app\n")
            sb.append("Versi   : 1.0\n")
            sb.append("Mode Tampilan : ").append(DisplayModeStore.getEffectiveMode(context)).append("\n")
            sb.append("Tipe Perangkat: ").append(if (DisplayModeStore.isTvDevice(context)) "TV" else "HP").append("\n\n")

            // 3. Struktur UI / Daftar Activity
            sb.append("=== STRUKTUR UI / DAFTAR HALAMAN ===\n")
            sb.append("KalkulatorActivity - Pintu masuk aplikasi (kode 140399)\n")
            sb.append("MainActivity - Halaman utama dengan 3 tab\n")
            sb.append("  - Tab Beranda - Grid foto/video + sidebar playlist + filter\n")
            sb.append("  - Tab Sumber - Pilihan sumber media\n")
            sb.append("  - Tab Profil - Pengaturan & alat bantu\n")
            sb.append("PreviewImageActivity - Tampilan pratinjau gambar (swipe)\n")
            sb.append("VideoPlayerActivity - Pemutar video dengan PiP (swipe)\n")
            sb.append("StreamingSourceActivity - Input link streaming (M3U/MP4)\n")
            sb.append("DriveSourceActivity - Input link Google Drive (smart link)\n")
            sb.append("ServerSourceActivity - Upload file ke server pribadi\n")
            sb.append("CustomPlaylistActivity - Manajemen playlist manual\n")
            sb.append("AudioPlayerActivity - Pemutar audio latar belakang\n")
            sb.append("StatistikActivity - Statistik penggunaan\n")
            sb.append("RiwayatActivity - Daftar riwayat link (PIN 808080)\n")
            sb.append("PinDialogActivity - Dialog PIN\n")
            sb.append("CrashLogActivity - Daftar log crash\n")
            sb.append("CrashLogDetailActivity - Detail log crash\n\n")

            // 4. Preferensi
            sb.append("=== PREFERENSI TERSIMPAN ===\n")
            dumpSharedPreferences(context, "memecio_settings", sb)
            dumpSharedPreferences(context, "memecio_statistik", sb)
            sb.append("\n")

            // 5. Playlist Tersimpan
            sb.append("=== PLAYLIST TERSIMPAN (M3U/Streaming) ===\n")
            val saved = SavedLinksStore.getAll(context)
            if (saved.isEmpty()) {
                sb.append("(Kosong)\n")
            } else {
                for (i in saved.indices) {
                    val m = saved[i]
                    sb.append(i + 1).append(". ")
                        .append(m.title ?: "(tanpa judul)")
                        .append("\n    URL: ").append(m.uri.toString()).append("\n")
                }
            }
            sb.append("\n")

            // 6. Playlist Manual
            sb.append("=== PLAYLIST MANUAL ===\n")
            val customs = CustomPlaylistStore.getAll(context)
            if (customs.isEmpty()) {
                sb.append("(Kosong)\n")
            } else {
                for (i in customs.indices) {
                    val pl = customs[i]
                    sb.append(i + 1).append(". ").append(pl.name)
                        .append(" (").append(pl.items.size).append(" item)\n")
                    for (item in pl.items) {
                        sb.append("   - ")
                            .append(if (item.type == MediaItem.TYPE_VIDEO) "[VIDEO]" else "[FOTO]")
                            .append(" ").append(item.title ?: "(tanpa judul)")
                            .append(" | ").append(item.uri.toString()).append("\n")
                    }
                }
            }
            sb.append("\n")

            // 7. Media Eksternal
            sb.append("=== MEDIA EKSTERNAL AKTIF (Drive/Streaming/Server) ===\n")
            val external = ExternalMediaStore.getAll(context)
            val sourceLabel = ExternalMediaStore.getSourceLabel(context)
            sb.append("Sumber: ").append(if (sourceLabel.isNullOrEmpty()) "(kosong)" else sourceLabel).append("\n")
            if (external.isEmpty()) {
                sb.append("(Kosong)\n")
            } else {
                for (i in external.indices) {
                    val m = external[i]
                    sb.append(i + 1).append(". ")
                        .append(m.title ?: "(tanpa judul)")
                        .append("\n    URL: ").append(m.uri.toString()).append("\n")
                }
            }
            sb.append("\n")

            // 8. Riwayat Link
            sb.append("=== RIWAYAT LINK ===\n")
            val riwayat = RiwayatStore.getAll(context)
            if (riwayat.isEmpty()) {
                sb.append("(Kosong)\n")
            } else {
                for (i in riwayat.indices) {
                    sb.append(i + 1).append(". ").append(riwayat[i]).append("\n")
                }
            }
            sb.append("\n")

            // 9. Log Crash
            sb.append("=== RINGKASAN LOG CRASH ===\n")
            val crashFile = File(context.filesDir, "memecio_crash.txt")
            if (crashFile.exists()) {
                sb.append("Ada file crash log di: ").append(crashFile.absolutePath)
                    .append("\nUkuran: ").append(crashFile.length()).append(" bytes\n")
            } else {
                sb.append("Tidak ada log crash. Bersih!\n")
            }
            sb.append("\n")

            sb.append("========================================\n")
            sb.append("End of Backup\n")
            sb.append("========================================\n")

            // Simpan file - dengan fallback
            val targetDir = getWritableBackupDir(context)
            if (targetDir == null) {
                Toast.makeText(context, "Tidak ada folder yang bisa ditulis", Toast.LENGTH_LONG).show()
                return
            }

            val file = File(targetDir, "backup_memecio_$timestamp.txt")
            val out = FileOutputStream(file)
            out.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
            out.close()

            Toast.makeText(context, "Backup tersimpan di:\n" + file.absolutePath, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal backup: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getWritableBackupDir(context: Context): File? {
        // Prioritas 1: coba Termux/Backup Aman di internal storage
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

        // Prioritas 2: fallback ke app-specific external files
        try {
            val fallback = File(context.getExternalFilesDir(null), "Backup Aman")
            if (!fallback.exists()) fallback.mkdirs()
            if (fallback.exists() && fallback.canWrite()) return fallback
        } catch (ignored: Exception) {
        }

        // Prioritas 3: fallback ke internal files
        try {
            val fallback = File(context.filesDir, "Backup Aman")
            if (!fallback.exists()) fallback.mkdirs()
            if (fallback.exists() && fallback.canWrite()) return fallback
        } catch (ignored: Exception) {
        }

        return null
    }

    private fun dumpSharedPreferences(context: Context, prefName: String, sb: StringBuilder) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val all = prefs.all
        sb.append("[").append(prefName).append("]\n")
        if (all.isEmpty()) {
            sb.append("  (kosong)\n")
        } else {
            for ((key, value) in all) {
                sb.append("  ").append(key).append(" = ").append(value).append("\n")
            }
        }
    }
}
