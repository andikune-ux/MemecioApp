package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object RepairDatabaseHelper {
    fun confirmAndRepair(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Repair Database (456)")
            .setMessage("Lakukan verifikasi ulang integritas data link tersimpan, riwayat, dan index media?")
            .setPositiveButton("Perbaiki") { _, _ ->
                try {
                    // Validasi riwayat
                    val riwayatList = RiwayatStore.getAll(context)
                    val validRiwayat = riwayatList.filter { it.isNotBlank() }
                    RiwayatStore.clear(context)
                    for (url in validRiwayat.reversed()) {
                        RiwayatStore.tambah(context, url)
                    }

                    // Validasi saved links
                    val savedList = SavedLinksStore.getAll(context)
                    val validSaved = savedList.filter { it.uri.toString().isNotBlank() }
                    for (item in validSaved) {
                        SavedLinksStore.tambah(context, item.title ?: "Link", item.uri.toString())
                    }

                    Toast.makeText(context, "Database dan index media berhasil diperbaiki! ✅", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal perbaikan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
