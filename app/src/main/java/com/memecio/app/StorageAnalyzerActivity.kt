package com.memecio.app

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class StorageAnalyzerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_analyzer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val container = findViewById<LinearLayout>(R.id.llStorageContainer)
        populateStorageInfo(container)
    }

    private fun populateStorageInfo(container: LinearLayout) {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val internalTotalMb = (internalStat.blockCountLong * internalStat.blockSizeLong) / (1024 * 1024)
        val internalAvailMb = (internalStat.availableBlocksLong * internalStat.blockSizeLong) / (1024 * 1024)
        val internalUsedMb = internalTotalMb - internalAvailMb

        val extStat = StatFs(Environment.getExternalStorageDirectory().path)
        val extTotalMb = (extStat.blockCountLong * extStat.blockSizeLong) / (1024 * 1024)
        val extAvailMb = (extStat.availableBlocksLong * extStat.blockSizeLong) / (1024 * 1024)
        val extUsedMb = extTotalMb - extAvailMb

        val internalTotalGb = internalTotalMb / 1024f
        val internalAvailGb = internalAvailMb / 1024f
        val internalUsedGb = internalUsedMb / 1024f

        val extTotalGb = extTotalMb / 1024f
        val extAvailGb = extAvailMb / 1024f
        val extUsedGb = extUsedMb / 1024f

        val items = listOf(
            "Penyimpanan Internal Data" to String.format(
                "Total: %.2f GB\nTerpakai: %.2f GB (%.1f%%)\nSisa Bebas: %.2f GB",
                internalTotalGb, internalUsedGb, (internalUsedMb * 100f / internalTotalMb), internalAvailGb
            ),
            "Penyimpanan Bersama (SDCard / Emulated)" to String.format(
                "Total: %.2f GB\nTerpakai: %.2f GB (%.1f%%)\nSisa Bebas: %.2f GB",
                extTotalGb, extUsedGb, (extUsedMb * 100f / extTotalMb), extAvailGb
            ),
            "Direktori Unduhan Memec.io" to "/sdcard/Termux/Download/ & Android/data/${packageName}/files/",
            "Cache Aplikasi" to "Cache internal & eksternal tersimpan di cacheDir"
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
                setPadding(0, 8, 0, 0)
            }

            card.addView(tvTitle)
            card.addView(tvDesc)
            container.addView(card)
        }
    }
}
