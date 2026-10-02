package com.memecio.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class ChangelogActivity : Activity() {

    private var content: LinearLayout? = null
    private var tabVersion: TextView? = null
    private var tabFeatures: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changelog)

        findViewById<View>(R.id.btnBackChangelog).setOnClickListener { finish() }
        content = findViewById(R.id.changelogContent)
        tabVersion = findViewById(R.id.tabChangelogVersion)
        tabFeatures = findViewById(R.id.tabChangelogFeatures)

        tabVersion?.setOnClickListener { showVersionTab() }
        tabFeatures?.setOnClickListener { showFeaturesTab() }

        showVersionTab()
    }

    private fun showVersionTab() {
        tabVersion?.setTextColor(0xFF3D5AFE.toInt())
        tabVersion?.setBackgroundResource(R.drawable.bg_glass_button_selected)
        tabFeatures?.setTextColor(0xFF888888.toInt())
        tabFeatures?.background = null
        content?.removeAllViews()

        // Versi Aktif
        val aktifCard = LinearLayout(this)
        aktifCard.orientation = LinearLayout.VERTICAL
        aktifCard.setBackgroundResource(R.drawable.bg_glass_card)
        aktifCard.setPadding(16, 16, 16, 16)

        val tvTitleAktif = TextView(this)
        tvTitleAktif.text = "Versi Aktif"
        tvTitleAktif.textSize = 11f
        tvTitleAktif.setTextColor(0xFF888888.toInt())
        aktifCard.addView(tvTitleAktif)

        val tvVersiAktif = TextView(this)
        var vName = "?"
        try {
            vName = packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
        } catch (ignored: Exception) {
        }
        tvVersiAktif.text = "V.$vName"
        tvVersiAktif.textSize = 20f
        tvVersiAktif.setTextColor(0xFF3D5AFE.toInt())
        tvVersiAktif.setTypeface(null, Typeface.BOLD)
        tvVersiAktif.setPadding(0, 4, 0, 0)
        aktifCard.addView(tvVersiAktif)

        val lpAktif = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lpAktif.setMargins(0, 0, 0, 16)
        aktifCard.layoutParams = lpAktif
        content?.addView(aktifCard)

        // Riwayat Build
        val tvHeader = TextView(this)
        tvHeader.text = "Riwayat Build"
        tvHeader.textSize = 13f
        tvHeader.setTypeface(null, Typeface.BOLD)
        tvHeader.setTextColor(0xFF1C1C1E.toInt())
        tvHeader.setPadding(8, 4, 8, 8)
        content?.addView(tvHeader)

        val builds = ChangelogStore.getBuilds()
        for (b in builds) {
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_changelog_version, content, false)
            card.findViewById<TextView>(R.id.tvVersionNumber).text = b.version
            card.findViewById<TextView>(R.id.tvVersionDate).text = b.timestamp
            val container = card.findViewById<LinearLayout>(R.id.versionFeaturesContainer)
            for (c in b.changes) {
                val tv = TextView(this)
                tv.text = "• $c"
                tv.textSize = 12f
                tv.setTextColor(0xFF555555.toInt())
                tv.setPadding(0, 4, 0, 4)
                container.addView(tv)
            }
            content?.addView(card)
        }
    }

    private fun showFeaturesTab() {
        tabVersion?.setTextColor(0xFF888888.toInt())
        tabVersion?.background = null
        tabFeatures?.setTextColor(0xFF3D5AFE.toInt())
        tabFeatures?.setBackgroundResource(R.drawable.bg_glass_button_selected)
        content?.removeAllViews()

        val features = ChangelogStore.getFeatureStatus()
        val kategori = arrayOf("Selesai", "Sebagian", "Belum")

        for (kat in kategori) {
            val tvKat = TextView(this)
            tvKat.text = kat
            tvKat.textSize = 13f
            tvKat.setTypeface(null, Typeface.BOLD)
            tvKat.setTextColor(0xFF1C1C1E.toInt())
            tvKat.setPadding(8, 12, 8, 4)
            content?.addView(tvKat)

            for (f in features) {
                if (f.status == kat) {
                    val tv = TextView(this)
                    val mark = if (kat == "Selesai") "[x]" else if (kat == "Sebagian") "[~]" else "[ ]"
                    tv.text = "$mark ${f.name} (${f.dateAdded})"
                    tv.textSize = 12f
                    tv.setTextColor(0xFF555555.toInt())
                    tv.setPadding(8, 4, 8, 4)
                    content?.addView(tv)
                }
            }
        }
    }
}
