package com.memecio.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView

class ChangelogActivity : Activity() {

    private lateinit var btnTabBuilds: Button
    private lateinit var btnTabFeatures: Button
    private lateinit var lvChangelog: ListView
    private var isBuildTab = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changelog)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnTabBuilds = findViewById(R.id.btnTabBuilds)
        btnTabFeatures = findViewById(R.id.btnTabFeatures)
        lvChangelog = findViewById(R.id.lvChangelog)

        btnTabBuilds.setOnClickListener {
            if (!isBuildTab) {
                isBuildTab = true
                updateTabs()
            }
        }

        btnTabFeatures.setOnClickListener {
            if (isBuildTab) {
                isBuildTab = false
                updateTabs()
            }
        }

        updateTabs()
    }

    private fun updateTabs() {
        if (isBuildTab) {
            btnTabBuilds.setBackgroundResource(R.drawable.bg_button_primary)
            btnTabBuilds.setTextColor(0xFFFFFFFF.toInt())
            btnTabFeatures.setBackgroundResource(R.drawable.bg_glass_button)
            btnTabFeatures.setTextColor(0xFF333333.toInt())

            val builds = ChangelogStore.getBuilds()
            val items = ArrayList<String>()
            for (b in builds) {
                val sb = StringBuilder()
                sb.append("📦 ").append(b.version).append(" (").append(b.timestamp).append(")\n")
                for (c in b.changes) {
                    sb.append("• ").append(c).append("\n")
                }
                items.add(sb.toString().trimEnd())
            }

            lvChangelog.adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent) as TextView
                    v.text = items[position]
                    v.textSize = 13f
                    v.setTextColor(0xFF222222.toInt())
                    v.setBackgroundResource(R.drawable.bg_glass_card)
                    v.setPadding(28, 20, 28, 20)
                    return v
                }
            }
        } else {
            btnTabFeatures.setBackgroundResource(R.drawable.bg_button_primary)
            btnTabFeatures.setTextColor(0xFFFFFFFF.toInt())
            btnTabBuilds.setBackgroundResource(R.drawable.bg_glass_button)
            btnTabBuilds.setTextColor(0xFF333333.toInt())

            val features = ChangelogStore.getFeatureStatus()
            val items = ArrayList<String>()
            for (f in features) {
                val icon = when (f.status) {
                    "Selesai" -> "✅ [Selesai]"
                    "Sebagian" -> "⏳ [Sebagian]"
                    else -> "📋 [Rencana]"
                }
                items.add("$icon ${f.name}\nDitambahkan: ${f.dateAdded}")
            }

            lvChangelog.adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent) as TextView
                    v.text = items[position]
                    v.textSize = 13f
                    v.setTextColor(0xFF222222.toInt())
                    v.setBackgroundResource(R.drawable.bg_glass_card)
                    v.setPadding(28, 20, 28, 20)
                    return v
                }
            }
        }
    }
}
