package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object TestModesHelper {
    fun showChoice(activity: Activity) {
        val current = DisplayModeStore.getEffectiveMode(activity)
        val modes = arrayOf("Otomatis (Ikuti Perangkat)", "HP / Mobile (Portrait)", "Android TV / Layar Lebar (Landscape)")
        val values = arrayOf("auto", "android", "tv")

        var selectedIndex = when (current) {
            "tv" -> 2
            "android" -> 1
            else -> 0
        }

        AlertDialog.Builder(activity)
            .setTitle("Pilih Mode Tampilan (104)")
            .setSingleChoiceItems(modes, selectedIndex) { dialog, which ->
                val chosen = values[which]
                DisplayModeStore.setMode(activity, chosen)
                Toast.makeText(activity, "Mode diubah ke: ${modes[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                activity.recreate()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
