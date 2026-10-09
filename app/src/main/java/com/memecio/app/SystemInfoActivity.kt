package com.memecio.app

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class SystemInfoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_info)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val container = findViewById<LinearLayout>(R.id.llSystemInfoContainer)
        populateInfo(container)
    }

    private fun populateInfo(container: LinearLayout) {
        val dm = resources.displayMetrics

        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)

        val items = listOf(
            "Perangkat & Manufaktur" to "${Build.MANUFACTURER} ${Build.MODEL} (${Build.BRAND})",
            "Hardware / Board" to "${Build.HARDWARE} / ${Build.BOARD}",
            "Versi Android" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Build Fingerprint" to Build.FINGERPRINT,
            "RAM Sistem" to "Tersedia: $availRamMb MB / Total: $totalRamMb MB",
            "Resolusi Layar" to "${dm.widthPixels} x ${dm.heightPixels} piksel",
            "Kepadatan Layar (DPI)" to "${dm.densityDpi} dpi (${dm.density}x scale)",
            "Arsitektur CPU" to Build.SUPPORTED_ABIS.joinToString(", "),
            "Package Aplikasi" to packageName
        )

        for ((title, desc) in items) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_glass_card)
                setPadding(32, 24, 32, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                layoutParams = params
            }

            val tvTitle = TextView(this).apply {
                text = title
                textSize = 14f
                setTextColor(0xFF3F51B5.toInt())
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val tvDesc = TextView(this).apply {
                text = desc
                textSize = 13f
                setTextColor(0xFF222222.toInt())
                setPadding(0, 6, 0, 0)
            }

            card.addView(tvTitle)
            card.addView(tvDesc)
            container.addView(card)
        }
    }
}
