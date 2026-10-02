package com.memecio.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemInfoActivity : Activity() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_system_info)

        findViewById<View>(R.id.btnBackSystem).setOnClickListener { finish() }
        val info = buildInfo()
        val tv = findViewById<TextView>(R.id.tvSystemContent)
        tv.text = info

        findViewById<View>(R.id.btnSalinSystem).setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("system_info", info))
            Toast.makeText(this, "Disalin", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildInfo(): String {
        val sb = StringBuilder()
        try {
            sb.append("=== APLIKASI ===\n")
            sb.append("Package : ").append(packageName).append("\n")
            sb.append("Versi : ").append(packageManager.getPackageInfo(packageName, 0).versionName).append("\n")
            sb.append("Build : ").append(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())).append("\n\n")
            sb.append("=== PERANGKAT ===\n")
            sb.append("Model : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n")
            sb.append("Android : ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n")
            sb.append("Brand : ").append(Build.BRAND).append("\n")
            sb.append("Device : ").append(Build.DEVICE).append("\n")
            sb.append("Product : ").append(Build.PRODUCT).append("\n")
            sb.append("Board : ").append(Build.BOARD).append("\n")
            sb.append("CPU ABI : ").append(Build.SUPPORTED_ABIS[0]).append("\n\n")
            sb.append("=== LAYAR ===\n")
            val dm = resources.displayMetrics
            sb.append("Resolusi : ").append(dm.widthPixels).append(" x ").append(dm.heightPixels).append(" px\n")
            sb.append("Density : ").append(dm.densityDpi).append(" dpi\n\n")
            sb.append("=== MEMORI ===\n")
            val rt = Runtime.getRuntime()
            val maxMB = rt.maxMemory() / 1048576
            val totalMB = rt.totalMemory() / 1048576
            val freeMB = rt.freeMemory() / 1048576
            sb.append("Max heap : ").append(maxMB).append(" MB\n")
            sb.append("Total : ").append(totalMB).append(" MB\n")
            sb.append("Free : ").append(freeMB).append(" MB\n\n")
            sb.append("=== STORAGE ===\n")
            val dir = filesDir
            sb.append("Internal : ").append(dir.absolutePath).append("\n")
            dir.parentFile?.let { parent ->
                sb.append("Free app : ").append(parent.freeSpace / 1048576).append(" MB\n")
                sb.append("Total app : ").append(parent.totalSpace / 1048576).append(" MB\n")
            }
            sb.append("\n=== PREFERENSI ===\n")
            sb.append("Mode : ").append(DisplayModeStore.getEffectiveMode(this)).append("\n")
        } catch (e: Exception) {
            sb.append("\nError: ").append(e.message)
        }
        return sb.toString()
    }
}
