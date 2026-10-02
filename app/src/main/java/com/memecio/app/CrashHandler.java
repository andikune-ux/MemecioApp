package com.memecio.app;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "MEMECIO_CRASH";
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context appContext;

    public CrashHandler(Context appContext) {
        this.appContext = appContext.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Thread: " + thread.getName() + " (id=" + thread.getId() + ")");
        pw.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL + " | Android " + Build.VERSION.RELEASE);
        pw.println("Exception:");
        ex.printStackTrace(pw);
        pw.flush();
        String stackTrace = sw.toString();

        Log.e(TAG, "========== CRASH ==========");
        Log.e(TAG, stackTrace);
        Log.e(TAG, "===========================");

        String entry = "=== CRASH at " + timestamp + " ===\n" + stackTrace + "\n\n";

        // 1. Internal (untuk fitur Log Crash di aplikasi)
        tryWrite(new File(appContext.getFilesDir(), "memecio_crash.txt"), entry);

        // 2. External /sdcard/Android/data/... (bisa diakses file manager)
        try {
            File ext = appContext.getExternalFilesDir(null);
            if (ext != null) tryWrite(new File(ext, "memecio_crash.txt"), entry);
        } catch (Exception ignored) {}

        // 3. Public Download folder (paling gampang dicari lewat file manager)
        try {
            File dl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dl != null && (dl.exists() || dl.mkdirs())) {
                tryWrite(new File(dl, "memecio_crash.txt"), entry);
            }
        } catch (Exception ignored) {}

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private void tryWrite(File file, String content) {
        BufferedWriter writer = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(content);
            writer.flush();
            Log.e(TAG, "Crash log tersimpan di: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Gagal tulis ke " + file.getAbsolutePath() + ": " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) {}
            }
        }
    }
}
