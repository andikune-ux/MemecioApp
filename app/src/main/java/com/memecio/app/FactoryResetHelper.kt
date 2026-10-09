package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object FactoryResetHelper {
    fun confirmAndReset(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Factory Reset (789)")
            .setMessage("PERINGATAN: Seluruh pengaturan, preferensi, riwayat, dan data tersimpan akan direset ke bawaan awal. Lanjutkan?")
            .setPositiveButton("Reset Total") { _, _ ->
                try {
                    // Clear all shared preferences
                    val prefs = arrayOf(
                        "memecio_dev_prefs",
                        "memecio_crash_store",
                        "memecio_crash_seen",
                        "memecio_display_mode",
                        "memecio_links",
                        "memecio_riwayat",
                        "memecio_multi_source"
                    )
                    for (p in prefs) {
                        context.getSharedPreferences(p, Context.MODE_PRIVATE).edit().clear().apply()
                    }
                    Toast.makeText(context, "Aplikasi berhasil direset ke kondisi awal! 🔄", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal reset: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
