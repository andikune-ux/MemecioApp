package com.memecio.app

import android.app.AlertDialog
import android.content.Context

object TestGestureHelper {
    fun showGuide(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Panduan Gestur Video Player (103)")
            .setMessage(
                "• Geser Sisi Kiri (Naik/Turun) : Mengatur Kecerahan Layar\n" +
                "• Geser Sisi Kanan (Naik/Turun): Mengatur Volume Audio\n" +
                "• Tekan & Tahan Sisi Kanan     : Mempercepat Video 3x (Fast Forward)\n" +
                "• Geser Horizontal / Progress  : Mencari Durasi (Seek)\n" +
                "• Pinch 2 Jari                 : Zoom & Pan Video\n" +
                "• Tombol Kunci                 : Mengunci Layar dari Sentuhan Tak Sengaja\n" +
                "• Tombol PiP                   : Memutar dalam Mode Picture-in-Picture"
            )
            .setPositiveButton("Mengerti", null)
            .show()
    }
}
