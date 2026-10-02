package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.io.File

object FactoryResetHelper {

    @JvmStatic
    fun confirmAndReset(context: Context) {
        // Step 1: Peringatan awal
        AlertDialog.Builder(context)
            .setTitle("⚠️ Factory Reset")
            .setMessage(
                "Ini akan MENGHAPUS SEMUA DATA aplikasi:\n\n" +
                        "• Playlist & riwayat link\n" +
                        "• Media eksternal aktif\n" +
                        "• Pengaturan aplikasi\n" +
                        "• Semua file tersembunyi\n" +
                        "• Log crash\n\n" +
                        "Tindakan ini TIDAK BISA DIBATALKAN.\n\n" +
                        "Disarankan Export JSON dulu sebelum lanjut."
            )
            .setNegativeButton("Batal", null)
            .setPositiveButton("Lanjut") { _: DialogInterface, _: Int -> step2(context) }
            .show()
    }

    private fun step2(context: Context) {
        val input = EditText(context)
        input.hint = "Ketik HAPUS di sini"

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 30, 50, 10)
        layout.addView(input)

        AlertDialog.Builder(context)
            .setTitle("Konfirmasi Terakhir")
            .setMessage("Ketik 'HAPUS' (huruf kapital) untuk melanjutkan:")
            .setView(layout)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Reset") { _: DialogInterface, _: Int ->
                val value = input.text.toString().trim()
                if (value != "HAPUS") {
                    Toast.makeText(context, "Kata konfirmasi salah. Dibatalkan.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                doReset(context)
            }
            .show()
    }

    private fun doReset(context: Context) {
        try {
            // 1. Hapus semua SharedPreferences
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (sharedPrefsDir.exists()) {
                val files = sharedPrefsDir.listFiles()
                files?.forEach { it.delete() }
            }

            // 2. Hapus semua file di filesDir
            val filesDir = context.filesDir
            if (filesDir.exists()) {
                val files = filesDir.listFiles()
                files?.forEach { f ->
                    if (f.isDirectory) deleteRecursive(f) else f.delete()
                }
            }

            // 3. Hapus cache
            val cacheDir = context.cacheDir
            if (cacheDir.exists()) {
                val files = cacheDir.listFiles()
                files?.forEach { f ->
                    if (f.isDirectory) deleteRecursive(f) else f.delete()
                }
            }

            Toast.makeText(context, "Semua data dihapus. Aplikasi akan ditutup.", Toast.LENGTH_LONG).show()

            // Force close
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal reset: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteRecursive(dir: File) {
        val files = dir.listFiles()
        files?.forEach { f ->
            if (f.isDirectory) deleteRecursive(f) else f.delete()
        }
        dir.delete()
    }
}
