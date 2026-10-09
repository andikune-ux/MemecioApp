package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val PREF_NAME = "memecio_crash_store"
    private const val KEY_LAST_CRASH = "last_crash_text"
    private const val KEY_CRASH_HISTORY = "crash_history_entries"

    fun log(context: Context, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val timeStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "[$timeStr] ${throwable.javaClass.simpleName}: ${throwable.message}\n$stackTrace"

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val oldHistory = prefs.getString(KEY_CRASH_HISTORY, "") ?: ""
        val newHistory = if (oldHistory.isEmpty()) entry else "$entry\n---\n$oldHistory"

        prefs.edit()
            .putString(KEY_LAST_CRASH, entry)
            .putString(KEY_CRASH_HISTORY, newHistory)
            .apply()

        // Also write to file
        try {
            val file = File(context.filesDir, "last_crash.txt")
            val fw = FileWriter(file, false)
            fw.write(entry)
            fw.close()
        } catch (ignored: Exception) {}
    }

    fun getHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CRASH_HISTORY, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split("\n---\n").filter { it.isNotBlank() }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        try {
            File(context.filesDir, "last_crash.txt").delete()
        } catch (ignored: Exception) {}
    }
}

object CrashCheckHelper {
    fun checkAndShow(context: Context) {
        val history = CrashLogger.getHistory(context)
        if (history.isNotEmpty()) {
            val sp = context.getSharedPreferences("memecio_crash_seen", Context.MODE_PRIVATE)
            val lastSeen = sp.getString("last_seen_entry", "")
            val latest = history.first()
            if (latest != lastSeen) {
                sp.edit().putString("last_seen_entry", latest).apply()
                AlertDialog.Builder(context)
                    .setTitle("Pemberitahuan Sistem")
                    .setMessage("Aplikasi mendeteksi error pada sesi sebelumnya.\nIngin melihat log error?")
                    .setPositiveButton("Lihat Log (111)") { _, _ ->
                        context.startActivity(Intent(context, CrashHistoryActivity::class.java))
                    }
                    .setNegativeButton("Abaikan", null)
                    .show()
            }
        }
    }
}
