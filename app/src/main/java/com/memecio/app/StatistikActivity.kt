package com.memecio.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class StatistikActivity : Activity() {

    private var barContainer: LinearLayout? = null
    private val labels = ArrayList<String>()
    private val values = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistik)

        val btnBack: TextView? = findViewById(R.id.btnBackStatistik)
        barContainer = findViewById(R.id.barContainer)

        btnBack?.setOnClickListener { finish() }

        val prefs = getSharedPreferences("memecio_statistik", MODE_PRIVATE)
        val totalMedia = prefs.getInt("total_media", 0)
        val totalVideo = prefs.getInt("total_video", 0)
        val totalFoto = prefs.getInt("total_foto", 0)
        val totalAudio = prefs.getInt("total_audio", 0)

        val tvTotalMedia: TextView? = findViewById(R.id.tvTotalMedia)
        tvTotalMedia?.text = "Total Media: $totalMedia\nVideo: $totalVideo\nFoto: $totalFoto\nAudio: $totalAudio"

        labels.add("Video")
        labels.add("Foto")
        labels.add("Audio")
        values.add(totalVideo)
        values.add(totalFoto)
        values.add(totalAudio)

        renderBarChart()
    }

    private fun renderBarChart() {
        val container = barContainer ?: return
        var maxVal = 1
        for (v in values) {
            if (v > maxVal) maxVal = v
        }

        for (i in labels.indices) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 16)
                gravity = Gravity.CENTER_VERTICAL
            }

            val label = TextView(this).apply {
                text = labels[i]
                setTextColor(Color.BLACK)
                textSize = 14f
            }
            row.addView(label, LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT))

            val bar = View(this).apply {
                setBackgroundColor(Color.parseColor("#5B6EF5"))
            }
            var width = (300f * (values[i] / maxVal.toFloat())).toInt()
            if (width < 10) width = 10
            val params = LinearLayout.LayoutParams(width, 40).apply {
                setMargins(16, 0, 0, 0)
            }
            row.addView(bar, params)

            val valueText = TextView(this).apply {
                text = "  ${values[i]}"
                setTextColor(Color.BLACK)
                textSize = 14f
            }
            row.addView(valueText)

            container.addView(row)
        }
    }
}
