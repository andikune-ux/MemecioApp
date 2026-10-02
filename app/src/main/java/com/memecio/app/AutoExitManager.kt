package com.memecio.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AutoExitManager private constructor() {

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var currentActivity: Activity? = null
    private var active = false

    fun isActive(): Boolean = active

    fun getRemainingMs(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val end = prefs.getLong(KEY_END_TIME, 0)
        if (end == 0L) return 0
        return (end - System.currentTimeMillis()).coerceAtLeast(0)
    }

    fun start(activity: Activity, minutes: Int) {
        cancel(activity)
        currentActivity = activity
        active = true
        val endTime = System.currentTimeMillis() + (minutes * 60L * 1000L)
        activity.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putLong(KEY_END_TIME, endTime).apply()
        Toast.makeText(activity, "Auto Exit: $minutes menit", Toast.LENGTH_SHORT).show()

        timerRunnable = object : Runnable {
            override fun run() {
                if (!active || currentActivity == null) return
                val remaining = getRemainingMs(currentActivity!!)
                if (remaining <= 0) {
                    doExit()
                    return
                }
                try {
                    val min = remaining / 60000
                    val sec = (remaining % 60000) / 1000
                    currentActivity!!.title = "Auto Exit: ${min}m ${sec}s"
                } catch (ignored: Exception) {
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    fun cancel(context: Context?) {
        active = false
        timerRunnable?.let {
            handler.removeCallbacks(it)
            timerRunnable = null
        }
        context?.let {
            it.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().remove(KEY_END_TIME).apply()
        }
    }

    fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        val remaining = getRemainingMs(activity)
        if (remaining <= 0 && active) {
            // Sudah lewat saat app di-background
            doExit()
            return
        }
        if (active) {
            // Lanjutkan timer
            start(activity, (remaining / 60000).toInt())
        }
    }

    private fun doExit() {
        active = false
        try {
            val act = currentActivity ?: return
            try {
                act.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                    .edit().remove(KEY_END_TIME).apply()
            } catch (ignored: Exception) {
            }
            try {
                val home = Intent(Intent.ACTION_MAIN)
                home.addCategory(Intent.CATEGORY_HOME)
                home.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                act.startActivity(home)
            } catch (ignored: Exception) {
            }
            try {
                act.finishAffinity()
            } catch (ignored: Exception) {
            }
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private const val PREF = "memecio_auto_exit"
        private const val KEY_END_TIME = "end_time"

        @Volatile
        private var instance: AutoExitManager? = null

        @JvmStatic
        fun getInstance(): AutoExitManager {
            if (instance == null) {
                synchronized(AutoExitManager::class.java) {
                    if (instance == null) {
                        instance = AutoExitManager()
                    }
                }
            }
            return instance!!
        }
    }
}
