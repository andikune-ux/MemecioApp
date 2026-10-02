package com.memecio.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager

class SessionManager : Application.ActivityLifecycleCallbacks {

    private var startedCount = 0
    private var pendingLock = false
    private var lastStopTime = 0L
    private var lockImmediately = false
    private var lastAuthTime = 0L

    private fun isWhitelisted(activity: Activity): Boolean {
        return activity is CalculatorActivity ||
                activity is PinDialogActivity ||
                activity is CoverActivity
    }

    private fun shouldSkip(activity: Activity): Boolean {
        try {
            if (activity.isChangingConfigurations) return true
            if (Build.VERSION.SDK_INT >= 26 && activity.isInPictureInPictureMode) return true
        } catch (ignored: Exception) {
        }
        return false
    }

    private fun redirectToCover(activity: Activity) {
        try {
            SessionState.lastActivityClass = activity.javaClass.name
            if (activity is VideoPlayerActivity) {
                SessionState.lastVideoIndex = activity.currentIndexSafe
                activity.pauseForPrivacy()
            }
            val intent = Intent(activity, CoverActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
            // Tidak finish activity — biar user balik ke sini setelah PIN
        } catch (ignored: Exception) {
        }
    }

    override fun onActivityStarted(activity: Activity) {
        startedCount++

        // Cek transisi internal (1-shot)
        if (SessionState.consumeInternalTransition()) {
            pendingLock = false
            lockImmediately = false
            return
        }

        if (!pendingLock) return

        // Cek suppression window (untuk file picker, settings, dll)
        if (SessionState.isLockSuppressed()) {
            pendingLock = false
            lockImmediately = false
            return
        }

        if (isWhitelisted(activity)) return
        if (shouldSkip(activity)) return

        val sinceAuth = System.currentTimeMillis() - lastAuthTime
        if (sinceAuth < AUTH_GRACE_MS) {
            pendingLock = false
            lockImmediately = false
            return
        }

        val elapsed = System.currentTimeMillis() - lastStopTime
        val needLock = lockImmediately || elapsed >= GRACE_MS
        if (needLock) {
            pendingLock = false
            lockImmediately = false
            redirectToCover(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedCount--
        if (startedCount < 0) startedCount = 0

        if (isWhitelisted(activity)) return
        if (shouldSkip(activity)) return
        if (activity.isFinishing) return

        if (startedCount == 0) {
            lastStopTime = System.currentTimeMillis()
            pendingLock = true
        }
    }

    override fun onActivityPaused(activity: Activity) {
        try {
            if (isWhitelisted(activity)) return
            if (shouldSkip(activity)) return
            BlurHelper.apply(activity)
        } catch (ignored: Exception) {
        }
    }

    override fun onActivityResumed(activity: Activity) {
        try {
            BlurHelper.clear(activity)
        } catch (ignored: Exception) {
        }

        // FLAG_SECURE — block screenshot di aktivitas non-whitelist (kalau PrivacyStore ON)
        try {
            if (!isWhitelisted(activity) && PrivacyStore.isEnabled()) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else if (!isWhitelisted(activity)) {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    fun onScreenOff() {
        try {
            lockImmediately = true
            pendingLock = true
            lastStopTime = System.currentTimeMillis()
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private const val GRACE_MS = 1000L
        private const val AUTH_GRACE_MS = 3000L

        @Volatile
        private var instance: SessionManager? = null

        @JvmStatic
        fun getInstance(): SessionManager {
            if (instance == null) {
                synchronized(SessionManager::class.java) {
                    if (instance == null) {
                        instance = SessionManager()
                    }
                }
            }
            return instance!!
        }

        @JvmStatic
        fun markAuthPassed() {
            instance?.let {
                it.lastAuthTime = System.currentTimeMillis()
                it.pendingLock = false
                it.lockImmediately = false
            }
        }
    }
}
