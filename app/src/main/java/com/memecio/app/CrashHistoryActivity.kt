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
import android.widget.Toast

class CrashHistoryActivity : Activity() {

    private lateinit var lvCrashList: ListView
    private lateinit var tvEmptyCrash: TextView
    private lateinit var tvCrashSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        lvCrashList = findViewById(R.id.lvCrashList)
        tvEmptyCrash = findViewById(R.id.tvEmptyCrash)
        tvCrashSummary = findViewById(R.id.tvCrashSummary)

        findViewById<Button>(R.id.btnClearCrash).setOnClickListener {
            CrashLogger.clear(this)
            Toast.makeText(this, "Riwayat crash berhasil dibersihkan", Toast.LENGTH_SHORT).show()
            loadHistory()
        }

        loadHistory()
    }

    private fun loadHistory() {
        val history = CrashLogger.getHistory(this)
        if (history.isEmpty()) {
            lvCrashList.visibility = View.GONE
            tvEmptyCrash.visibility = View.VISIBLE
            tvCrashSummary.text = "Tidak ada crash yang tercatat. Sistem beroperasi stabil."
        } else {
            lvCrashList.visibility = View.VISIBLE
            tvEmptyCrash.visibility = View.GONE
            tvCrashSummary.text = "Tercatat ${history.size} kejadian crash/error pada sistem."

            lvCrashList.adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, history) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent) as TextView
                    v.text = history[position]
                    v.textSize = 12f
                    v.setTextColor(0xFF333333.toInt())
                    v.setBackgroundResource(R.drawable.bg_glass_card)
                    v.setPadding(32, 24, 32, 24)
                    return v
                }
            }
        }
    }
}
