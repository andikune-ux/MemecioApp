package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import java.io.File
import java.util.Locale

object CacheResetter {

    @JvmStatic
    fun confirmAndReset(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Reset Cache")
            .setMessage(
                "Hapus semua cache thumbnail, preview, dan file sementara?\n\n" +
                "Data playlist dan riwayat TIDAK akan terhapus."
            )
            .setNegativeButton("Batal", null)
            .setPositiveButton("Reset") { _, _ -> doReset(context) }
            .show()
    }

    private fun doReset(context: Context) {
        var totalFreed = 0L
        try {
            // 1. Cache dir
            val cacheDir = context.cacheDir
            totalFreed += deleteDir(cacheDir, false)

            // 2. External cache
            val extCache = context.externalCacheDir
            if (extCache != null) totalFreed += deleteDir(extCache, false)

            // 3. Preview cache (kalau ada custom folder)
            val filesDir = context.filesDir
            val previewDir = File(filesDir, "preview")
            if (previewDir.exists()) totalFreed += deleteDir(previewDir, true)

            val msg = "Cache dibersihkan: " + formatSize(totalFreed)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal reset: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteDir(dir: File?, deleteSelf: Boolean): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory) {
                    size += deleteDir(f, true)
                } else {
                    size += f.length()
                    f.delete()
                }
            }
        }
        if (deleteSelf) dir.delete()
        return size
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024))
        }
    }
}
