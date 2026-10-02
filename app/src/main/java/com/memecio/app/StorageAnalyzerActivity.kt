package com.memecio.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import java.io.File
import java.util.Locale

class StorageAnalyzerActivity : Activity() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_storage_analyzer)
        findViewById<View>(R.id.btnBackStorage).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvStorageContent).text = buildInfo()
    }

    private fun buildInfo(): String {
        val sb = StringBuilder()
        try {
            val filesDir = filesDir
            val cacheDir = cacheDir
            val extDir = getExternalFilesDir(null)
            sb.append("=== STORAGE APLIKASI ===\n\n")
            sb.append("Internal files : ").append(formatSize(dirSize(filesDir))).append("\n")
            sb.append("  Path : ").append(filesDir.absolutePath).append("\n\n")
            sb.append("Cache : ").append(formatSize(dirSize(cacheDir))).append("\n")
            sb.append("  Path : ").append(cacheDir.absolutePath).append("\n\n")
            if (extDir != null) {
                sb.append("External files : ").append(formatSize(dirSize(extDir))).append("\n")
                sb.append("  Path : ").append(extDir.absolutePath).append("\n\n")
            }
            sb.append("=== FILE PENTING ===\n")
            val crash = File(filesDir, "memecio_crash.txt")
            sb.append("Crash log : ").append(if (crash.exists()) formatSize(crash.length()) else "(tidak ada)").append("\n")
            val preview = File(cacheDir, "preview")
            sb.append("Preview cache : ").append(if (preview.exists()) formatSize(dirSize(preview)) else "(tidak ada)").append("\n\n")
            sb.append("=== RUANG DISK ===\n")
            val free = filesDir.freeSpace
            val total = filesDir.totalSpace
            sb.append("Free : ").append(formatSize(free)).append("\n")
            sb.append("Total : ").append(formatSize(total)).append("\n")
            sb.append("Used : ").append(formatSize(total - free)).append("\n")
        } catch (e: Exception) {
            sb.append("\nError: ").append(e.message)
        }
        return sb.toString()
    }

    private fun dirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0
        var size = 0L
        val files = dir.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory) size += dirSize(f) else size += f.length()
            }
        }
        return size
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
