package com.memecio.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public class MemecioApplication extends Application {

    private BroadcastReceiver screenOffReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

        // Init DownloadCache untuk HLS offline
        try { DownloadCache.init(this); } catch (Exception ignored) {}


        // Register lifecycle callbacks
        try {
            registerActivityLifecycleCallbacks(SessionManager.getInstance());
        } catch (Exception ignored) {}

        // Register screen off receiver
        try {
            screenOffReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        SessionManager.getInstance().onScreenOff();
                    }
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(screenOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(screenOffReceiver, filter);
            }
        } catch (Exception ignored) {}
    }
}
