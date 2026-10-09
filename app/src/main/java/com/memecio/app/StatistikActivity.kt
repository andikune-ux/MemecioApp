package com.memecio.app

import android.app.Activity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class StatistikActivity : Activity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistik)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnRefreshStats).setOnClickListener {
            calculateStats()
        }

        container = findViewById(R.id.llStatsContainer)
        calculateStats()
    }

    private fun calculateStats() {
        container.removeAllViews()

        var photoCount = 0
        var videoCount = 0
        var totalBytes = 0L

        try {
            val imgCursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media.SIZE),
                null, null, null
            )
            imgCursor?.use {
                photoCount = it.count
                while (it.moveToNext()) {
                    totalBytes += it.getLong(0)
                }
            }
        } catch (ignored: Exception) {}

        try {
            val vidCursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media.SIZE),
                null, null, null
            )
            vidCursor?.use {
                videoCount = it.count
                while (it.moveToNext()) {
                    totalBytes += it.getLong(0)
                }
            }
        } catch (ignored: Exception) {}

        val totalMedia = photoCount + videoCount
        val totalMb = totalBytes / (1024f * 1024f)
        val totalGb = totalMb / 1024f

        val riwayatCount = RiwayatStore.getAll(this).size
        val savedLinksCount = SavedLinksStore.getAll(this).size
        val multiSourcesCount = MultiSourceStore.listSourceIds(this).size

        val items = listOf(
            "Total Seluruh Media" to "$totalMedia Berkas",
            "Berkas Video Terdeteksi" to "$videoCount Video",
            "Berkas Foto / Gambar" to "$photoCount Gambar",
            "Total Kapasitas Terpakai" to String.format("%.2f GB (%.1f MB)", totalGb, totalMb),
            "Riwayat Link Streaming & M3U" to "$riwayatCount Riwayat",
            "Playlist / Link Manual Tersimpan" to "$savedLinksCount Playlist",
            "Sumber Multi-Source Online" to "$multiSourcesCount Sumber Aktif"
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
                textSize = 15f
                setTextColor(0xFF222222.toInt())
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 6, 0, 0)
            }

            card.addView(tvTitle)
            card.addView(tvDesc)
            container.addView(card)
        }
    }
}
