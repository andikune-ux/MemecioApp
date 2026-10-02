package com.memecio.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class SessionManager implements Application.ActivityLifecycleCallbacks {

    private static final long GRACE_MS = 1000;
    private static final long AUTH_GRACE_MS = 3000;

    private static SessionManager instance;

    private int startedCount = 0;
    private boolean pendingLock = false;
    private long lastStopTime = 0;
    private boolean lockImmediately = false;
    private long lastAuthTime = 0;

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public static void markAuthPassed() {
        if (instance != null) {
            instance.lastAuthTime = System.currentTimeMillis();
            instance.pendingLock = false;
            instance.lockImmediately = false;
        }
    }

    public void onScreenOff() {
        try {
            lockImmediately = true;
            pendingLock = true;
            lastStopTime = System.currentTimeMillis();
        } catch (Exception ignored) {}
    }

    private boolean isWhitelisted(Activity activity) {
        return (activity instanceof CalculatorActivity)
            || (activity instanceof PinDialogActivity)
            || (activity instanceof CoverActivity);
    }

    private boolean shouldSkip(Activity activity) {
        try {
            if (activity.isChangingConfigurations()) return true;
            if (Build.VERSION.SDK_INT >= 26 && activity.isInPictureInPictureMode()) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private void redirectToCover(Activity activity) {
        try {
            SessionState.lastActivityClass = activity.getClass().getName();
            if (activity instanceof VideoPlayerActivity) {
                SessionState.lastVideoIndex = ((VideoPlayerActivity) activity).getCurrentIndexSafe();
                ((VideoPlayerActivity) activity).pauseForPrivacy();
            }

            Intent intent = new Intent(activity, CoverActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            // Tidak finish activity — biar user balik ke sini setelah PIN
        } catch (Exception ignored) {}
    }

    @Override
    public void onActivityStarted(Activity activity) {
        startedCount++;

        // Cek transisi internal (1-shot)
        if (SessionState.consumeInternalTransition()) {
            pendingLock = false;
            lockImmediately = false;
            return;
        }

        if (!pendingLock) return;

        // Cek suppression window (untuk file picker, settings, dll)
        if (SessionState.isLockSuppressed()) {
            pendingLock = false;
            lockImmediately = false;
            return;
        }

        if (isWhitelisted(activity)) return;
        if (shouldSkip(activity)) return;

        long sinceAuth = System.currentTimeMillis() - lastAuthTime;
        if (sinceAuth < AUTH_GRACE_MS) {
            pendingLock = false;
            lockImmediately = false;
            return;
        }

        long elapsed = System.currentTimeMillis() - lastStopTime;
        boolean needLock = lockImmediately || elapsed >= GRACE_MS;

        if (needLock) {
            pendingLock = false;
            lockImmediately = false;
            redirectToCover(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        startedCount--;
        if (startedCount < 0) startedCount = 0;

        if (isWhitelisted(activity)) return;
        if (shouldSkip(activity)) return;
        if (activity.isFinishing()) return;

        if (startedCount == 0) {
            lastStopTime = System.currentTimeMillis();
            pendingLock = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        try {
            if (isWhitelisted(activity)) return;
            if (shouldSkip(activity)) return;
            BlurHelper.apply(activity);
        } catch (Exception ignored) {}
    }

    @Override
    public void onActivityResumed(Activity activity) {
        try {
            BlurHelper.clear(activity);
        } catch (Exception ignored) {}
        // FLAG_SECURE — block screenshot di aktivitas non-whitelist (kalau PrivacyStore ON)
        try {
            if (!isWhitelisted(activity) && PrivacyStore.isEnabled()) {
                activity.getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE);
            } else if (!isWhitelisted(activity)) {
                activity.getWindow().clearFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch (Exception ignored) {}
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
