package com.memecio.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class MemecioApplication : Application() {

    private var screenOffReceiver: BroadcastReceiver? = null

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
}
