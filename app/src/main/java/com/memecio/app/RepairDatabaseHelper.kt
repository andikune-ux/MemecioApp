package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object RepairDatabaseHelper {

    @JvmStatic
    fun confirmAndRepair(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Repair Database")
            .setMessage("Scan dan perbaiki SharedPreferences yang rusak?\n\nProses ini hanya memperbaiki, tidak menghapus data valid.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Repair") { _, _ -> doRepair(context) }
            .show()
    }

    private fun doRepair(context: Context) {
        val log = StringBuilder()
        var fixed = 0

        fixed += cekPref(context, "memecio_settings", log)
        fixed += cekPref(context, "memecio_external_media", log)
        fixed += cekPref(context, "memecio_custom_playlists", log)
        fixed += cekPref(context, "memecio_watch_later", log)
        fixed += cekPref(context, "memecio_saved_links", log)
        fixed += cekPref(context, "memecio_riwayat", log)
        fixed += cekPref(context, "memecio_versioning", log)

        val msg = if (fixed == 0) {
            "Database bersih, tidak ada kerusakan."
        } else {
            "$fixed masalah ditemukan dan diperbaiki."
        }

        AlertDialog.Builder(context)
            .setTitle("Hasil Repair")
            .setMessage("$msg\n\nLog:\n$log")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun cekPref(context: Context, name: String, log: StringBuilder): Int {
        return try {
            val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            prefs.all
            log.append("OK: ").append(name).append("\n")
            0
        } catch (e: Exception) {
            try {
                context.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .edit().clear().apply()
                log.append("FIX: ").append(name).append(" (rusak, direset)\n")
                1
            } catch (ignored: Exception) {
                log.append("FAIL: ").append(name).append("\n")
                0
            }
        }
    }
}
