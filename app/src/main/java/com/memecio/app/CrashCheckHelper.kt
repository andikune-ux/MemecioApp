package com.memecio.app

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object CrashCheckHelper {

    private const val PREF_NAME = "memecio_settings"
    private const val KEY_LAST_SHOWN_LENGTH = "crash_last_shown_length"

    @JvmStatic
    fun checkAndShow(activity: Activity) {
        val file = File(activity.filesDir, "memecio_crash.txt")
        if (!file.exists() || file.length() == 0L) return

        val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastShownLength = prefs.getLong(KEY_LAST_SHOWN_LENGTH, 0)
        val currentLength = file.length()

        if (currentLength <= lastShownLength) return

        val fullContent = readFile(file)
        if (fullContent.isNullOrEmpty()) return

        showPopup(activity, fullContent)
        prefs.edit().putLong(KEY_LAST_SHOWN_LENGTH, currentLength).apply()
    }

    private fun readFile(file: File): String? {
        return try {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun showPopup(activity: Activity, content: String) {
        val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_crash_popup)
        dialog.setCancelable(true)

        val tvBody = dialog.findViewById<TextView>(R.id.tvCrashPopupBody)
        val btnSalin = dialog.findViewById<TextView>(R.id.btnSalinCrashPopup)
        val btnClose = dialog.findViewById<TextView>(R.id.btnCloseCrashPopup)

        tvBody.text = content

        btnSalin.setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("crash_log", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, "Log disalin", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
