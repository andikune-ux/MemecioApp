package com.memecio.app

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle

class MemecioApplication : Application() {

    private var screenOffReceiver: BroadcastReceiver? = null
    private var lastUpdateCheckTime = 0L

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))

        // Init DownloadCache untuk HLS offline
        try {
            DownloadCache.init(this)
        } catch (ignored: Exception) {
        }

        // Register lifecycle callbacks
        try {
            registerActivityLifecycleCallbacks(SessionManager.getInstance())
        } catch (ignored: Exception) {
        }

        // Register cek update otomatis saat Activity dibuka
        try {
            registerActivityLifecycleCallbacks(UpdateLifecycleCallbacks())
        } catch (ignored: Exception) {
        }

        // Register screen off receiver
        try {
            screenOffReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null && Intent.ACTION_SCREEN_OFF == intent.action) {
                        SessionManager.getInstance().onScreenOff()
                    }
                }
            }
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(screenOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenOffReceiver, filter)
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * Cek update otomatis saat MainActivity / AppInfoActivity dibuka.
     *
     * Skip kalau:
     * - Sedang buka VideoPlayerActivity (jangan ganggu nonton)
     * - Baru saja cek < 30 detik yang lalu
     */
    private inner class UpdateLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            // Skip VideoPlayerActivity — jangan ganggu saat nonton
            if (activity is VideoPlayerActivity) return

            // Hanya cek di MainActivity atau AppInfoActivity
            val shouldCheckHere = (activity is MainActivity) || (activity is AppInfoActivity)
            if (!shouldCheckHere) return

            // Throttle 30 detik supaya tidak spam request
            val now = System.currentTimeMillis()
            if (now - lastUpdateCheckTime < 30_000L) return
            lastUpdateCheckTime = now

            try {
                UpdateDialog.checkAndShow(activity)
            } catch (ignored: Exception) {
            }
        }

        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}
