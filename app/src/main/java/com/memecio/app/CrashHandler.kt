package com.memecio.app

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(appContext: Context) : Thread.UncaughtExceptionHandler {

    private val appContext: Context = appContext.applicationContext
    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        pw.println("Thread: " + thread.name + " (id=" + thread.id + ")")
        pw.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL + " | Android " + Build.VERSION.RELEASE)
        pw.println("Exception:")
        ex.printStackTrace(pw)
        pw.flush()
        val stackTrace = sw.toString()

        Log.e(TAG, "========== CRASH ==========")
        Log.e(TAG, stackTrace)
        Log.e(TAG, "===========================")

        val entry = "=== CRASH at $timestamp ===\n$stackTrace\n\n"

        // 1. Internal (untuk fitur Log Crash di aplikasi)
        tryWrite(File(appContext.filesDir, "memecio_crash.txt"), entry)

        // 2. External /sdcard/Android/data/... (bisa diakses file manager)
        try {
            val ext = appContext.getExternalFilesDir(null)
            if (ext != null) tryWrite(File(ext, "memecio_crash.txt"), entry)
        } catch (ignored: Exception) {
        }

        // 3. Public Download folder (paling gampang dicari lewat file manager)
        try {
            val dl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (dl != null && (dl.exists() || dl.mkdirs())) {
                tryWrite(File(dl, "memecio_crash.txt"), entry)
            }
        } catch (ignored: Exception) {
        }

        try {
            Thread.sleep(500)
        } catch (ignored: InterruptedException) {
        }

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex)
        } else {
            Process.killProcess(Process.myPid())
            System.exit(1)
        }
    }

    private fun tryWrite(file: File, content: String) {
        var writer: BufferedWriter? = null
        try {
            val parent = file.parentFile
            if (parent != null && !parent.exists()) parent.mkdirs()
            writer = BufferedWriter(FileWriter(file, true))
            writer.write(content)
            writer.flush()
            Log.e(TAG, "Crash log tersimpan di: " + file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal tulis ke " + file.absolutePath + ": " + e.message)
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (ignored: Exception) {
                }
            }
        }
    }

    companion object {
        private const val TAG = "MEMECIO_CRASH"
    }
}
