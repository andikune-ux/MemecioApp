package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object TestModesHelper {
    @JvmStatic
    fun showChoice(context: Context) {
        val current = DisplayModeStore.getMode(context)
        val effective = DisplayModeStore.getEffectiveMode(context)
        val info = "Mode Sekarang: $current\nEfektif: $effective\n\nPilih mode:"

        val options = arrayOf(
            "Auto (deteksi perangkat)",
            "Android (paksa)",
            "Android TV (paksa)"
        )

        AlertDialog.Builder(context)
            .setTitle("Test Modes")
            .setMessage(info)
            .setItems(options) { _, which ->
                val mode = when (which) {
                    0 -> DisplayModeStore.MODE_AUTO
                    1 -> DisplayModeStore.MODE_ANDROID
                    else -> DisplayModeStore.MODE_TV
                }

                DisplayModeStore.setMode(context, mode)
                Toast.makeText(context, "Mode di-set: $mode\nRestart aplikasi untuk efek penuh.", Toast.LENGTH_LONG).show()
            }
            .show()
    }
}
