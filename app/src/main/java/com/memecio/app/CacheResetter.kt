package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import java.io.File

object CacheResetter {
    fun confirmAndReset(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Reset Cache (123)")
            .setMessage("Bersihkan semua cache thumbnail dan data sementara aplikasi?")
            .setPositiveButton("Bersihkan") { _, _ ->
                try {
                    deleteDir(context.cacheDir)
                    context.externalCacheDir?.let { deleteDir(it) }
                    Toast.makeText(context, "Cache berhasil dibersihkan! 🧹", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membersihkan cache: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false
            for (child in children) {
                deleteDir(File(dir, child))
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }
}
