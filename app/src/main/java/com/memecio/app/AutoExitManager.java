package com.memecio.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class AutoExitManager {

    private static final String PREF = "memecio_auto_exit";
    private static final String KEY_END_TIME = "end_time";
    private static AutoExitManager instance;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private Activity currentActivity;
    private boolean active = false;

    public static synchronized AutoExitManager getInstance() {
        if (instance == null) instance = new AutoExitManager();
        return instance;
    }

    public boolean isActive() {
        return active;
    }

    public long getRemainingMs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        long end = prefs.getLong(KEY_END_TIME, 0);
        if (end == 0) return 0;
        return Math.max(0, end - System.currentTimeMillis());
    }

    public void start(Activity activity, int minutes) {
        cancel(activity);
        currentActivity = activity;
        active = true;

        long endTime = System.currentTimeMillis() + (minutes * 60L * 1000L);
        activity.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putLong(KEY_END_TIME, endTime).apply();

        Toast.makeText(activity, "Auto Exit: " + minutes + " menit", Toast.LENGTH_SHORT).show();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!active || currentActivity == null) return;
                long remaining = getRemainingMs(currentActivity);
                if (remaining <= 0) {
                    doExit();
                    return;
                }
                try {
                    long min = remaining / 60000;
                    long sec = (remaining % 60000) / 1000;
                    currentActivity.setTitle("Auto Exit: " + min + "m " + sec + "s");
                } catch (Exception ignored) {}
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    public void cancel(Context context) {
        active = false;
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
        if (context != null) {
            context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().remove(KEY_END_TIME).apply();
        }
    }

    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        long remaining = getRemainingMs(activity);
        if (remaining <= 0 && active) {
            // Sudah lewat saat app di-background
            doExit();
            return;
        }
        if (active) {
            // Lanjutkan timer
            start(activity, (int)(remaining / 60000));
        }
    }

    private void doExit() {
        active = false;
        try {
            Activity act = currentActivity;
            if (act == null) return;
            try {
                act.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                    .edit().remove(KEY_END_TIME).apply();
            } catch (Exception ignored) {}

            try {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(home);
            } catch (Exception ignored) {}

            try { act.finishAffinity(); } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }
}
