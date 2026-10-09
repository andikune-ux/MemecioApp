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

    fun export(context: Context) {
        try {
            val sb = java.lang.StringBuilder()
            val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val tsHuman = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            sb.append("========================================\n")
            sb.append("     PANDUAN PROYEK MEMEC.IO\n")
            sb.append("     Serah Terima AI / Developer\n")
            sb.append("========================================\n\n")

            var vName = "1.14.65"
            try {
                vName = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.14.65"
            } catch (ignored: Exception) {}

            sb.append("Versi Saat Ini : V.").append(vName).append("\n")
            sb.append("Waktu Export   : ").append(tsHuman).append("\n")
            sb.append("Package        : ").append(context.packageName).append("\n")
            sb.append("Device         : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
                .append(" | Android ").append(Build.VERSION.RELEASE)
                .append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n\n")

            sb.append("========================================\n")
            sb.append("CATATAN UNTUK DEVELOPER / AI\n")
            sb.append("========================================\n")
            sb.append("File ini dibuat otomatis oleh aplikasi Memec.io.\n\n")

            sb.append("========================================\n")
            sb.append("DAFTAR KODE RAHASIA (di Kalkulator + =)\n")
            sb.append("========================================\n")
            sb.append("140399 : Buka aplikasi utama (MainActivity)\n")
            for (c in SecretCodeRegistry.getAll()) {
                if (!c.hidden) {
                    sb.append(c.code).append("    : ").append(c.title).append(" - ").append(c.description).append("\n")
                }
            }
            sb.append("\n")

            sb.append("========================================\n")
            sb.append("STATUS FITUR TERBARU\n")
            sb.append("========================================\n")
            for (f in ChangelogStore.getFeatureStatus()) {
                sb.append("[").append(f.status).append("] ").append(f.name).append(" (").append(f.dateAdded).append(")\n")
            }
            sb.append("\n========================================\n")

            val content = sb.toString()
            var savedCount = 0
            val savedPaths = arrayOfNulls<String>(2)

            // Lokasi 1: Internal App
            try {
                val internalDir = File(context.filesDir, "ProjectExport")
                if (!internalDir.exists()) internalDir.mkdirs()
                val f1 = File(internalDir, "panduan_terbaru.txt")
                FileOutputStream(f1).use { it.write(content.toByteArray(StandardCharsets.UTF_8)) }
                savedPaths[0] = f1.absolutePath
                savedCount++
            } catch (ignored: Exception) {}

            // Lokasi 2: External Storage
            try {
                val extDir = File(context.getExternalFilesDir(null), "Backup Aman")
                if (!extDir.exists()) extDir.mkdirs()
                val f2 = File(extDir, "panduan_proyek_$ts.txt")
                FileOutputStream(f2).use { it.write(content.toByteArray(StandardCharsets.UTF_8)) }
                savedPaths[1] = f2.absolutePath
                savedCount++
            } catch (ignored: Exception) {}

            if (savedCount == 0) {
                Toast.makeText(context, "Gagal mengekspor panduan proyek", Toast.LENGTH_SHORT).show()
                return
            }

            val msg = StringBuilder("Panduan tersimpan:\n")
            savedPaths[0]?.let { msg.append("1. ").append(it).append("\n") }
            savedPaths[1]?.let { msg.append("2. ").append(it) }
            Toast.makeText(context, msg.toString(), Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Gagal export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
