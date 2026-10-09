package com.memecio.app

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AutoExitManager private constructor() {

    private val handler = Handler(Looper.getMainLooper())
    private var exitRunnable: Runnable? = null
    private var activeMinutes = 0

    companion object {
        val instance = AutoExitManager()
    }

    fun start(activity: Activity, minutes: Int) {
        cancel(activity)
        activeMinutes = minutes
        val delayMs = minutes * 60 * 1000L

        exitRunnable = Runnable {
            Toast.makeText(activity, "Waktu Auto-Exit telah habis ($minutes menit). Aplikasi ditutup.", Toast.LENGTH_SHORT).show()
            activity.finishAffinity()
        }
        handler.postDelayed(exitRunnable!!, delayMs)
        Toast.makeText(activity, "Auto Exit disetel untuk $minutes menit ke depan.", Toast.LENGTH_SHORT).show()
    }

    fun cancel(activity: Activity) {
        exitRunnable?.let {
            handler.removeCallbacks(it)
            exitRunnable = null
        }
        activeMinutes = 0
    }

    fun getActiveMinutes(): Int = activeMinutes
}
