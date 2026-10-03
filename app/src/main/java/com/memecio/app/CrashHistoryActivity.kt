package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.regex.Pattern

class CrashHistoryActivity : Activity() {

    class CrashEntry {
        var timestamp: String? = null
        var type: String? = null
        var summary: String? = null
        var fullBody: String? = null
    }

    private var entries: MutableList<CrashEntry> = ArrayList()
    private var adapter: BaseAdapter? = null
    private var tvEmptyRef: TextView? = null
    private var listRef: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_history)

        findViewById<View>(R.id.btnBackCrashHistory).setOnClickListener { finish() }
        val list = findViewById<ListView>(R.id.listCrashHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvCrashHistoryEmpty)
        tvEmptyRef = tvEmpty
        listRef = list

        adapter = object : BaseAdapter() {
            override fun getCount(): Int = entries.size
            override fun getItem(pos: Int): Any = entries[pos]
            override fun getItemId(pos: Int): Long = pos.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var v = convertView
                if (v == null) {
                    v = LayoutInflater.from(this@CrashHistoryActivity)
                        .inflate(R.layout.item_crash_history, parent, false)
                }
                val e = entries[position]
                v.findViewById<TextView>(R.id.tvCrashHistoryTime).text = e.timestamp
                v.findViewById<TextView>(R.id.tvCrashHistoryType).text = e.type
                v.findViewById<TextView>(R.id.tvCrashHistorySummary).text = e.summary
                return v
            }
        }
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            val e = entries[position]
            val i = Intent(this, CrashLogDetailActivity::class.java)
            i.putExtra("crash_title", "${e.timestamp} • ${e.type}")
            i.putExtra("crash_body", e.fullBody)
            startActivity(i)
        }

        findViewById<View>(R.id.btnHapusCrashHistory).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat")
                .setMessage("Hapus semua riwayat crash?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus") { _, _ ->
                    val f = File(filesDir, "memecio_crash.txt")
                    if (f.exists()) f.delete()
                    loadEntries(tvEmptyRef!!, listRef!!)
                    Toast.makeText(this, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        loadEntries(tvEmpty, list)
    }

    private fun loadEntries(tvEmpty: TextView, list: ListView) {
        entries.clear()
        val file = File(filesDir, "memecio_crash.txt")
        if (file.exists()) {
            try {
                val sb = StringBuilder()
                val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                reader.close()
                parseContent(sb.toString())
            } catch (ignored: Exception) {
            }
        }
        adapter?.notifyDataSetChanged()
        if (entries.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            list.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            list.visibility = View.VISIBLE
        }
    }

    private fun parseContent(content: String) {
        val pattern = Pattern.compile("=== (.*?) ===")
        val matcher = pattern.matcher(content)
        val markers = ArrayList<IntArray>()
        val titles = ArrayList<String>()

        while (matcher.find()) {
            markers.add(intArrayOf(matcher.start(), matcher.end()))
            titles.add(matcher.group(1) ?: "")
        }

        for (i in markers.indices) {
            val bodyStart = markers[i][1]
            val bodyEnd = if (i + 1 < markers.size) markers[i + 1][0] else content.length
            val body = content.substring(bodyStart, bodyEnd).trim()
            val title = titles[i]

            val entry = CrashEntry()
            entry.timestamp = title
            entry.type = if (title.contains("FORCE CLOSE")) "FORCE CLOSE" else "BALIK KE KALKULATOR"
            entry.summary = body.lines().firstOrNull { it.contains("Exception") || it.contains("Error") } ?: body.take(80)
            entry.fullBody = body
            entries.add(entry)
        }

        // Urutkan terbaru dulu
        entries.reverse()
    }
}
