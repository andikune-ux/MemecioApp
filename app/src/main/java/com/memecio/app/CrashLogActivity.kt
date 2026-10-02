package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.regex.Pattern

class CrashLogActivity : Activity() {

    private class CrashEntry {
        var title: String? = null
        var body: String? = null
    }

    private val entries: MutableList<CrashEntry> = ArrayList()
    private var adapter: ArrayAdapter<CrashEntry>? = null
    private var listView: ListView? = null
    private var tvEmpty: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_log)

        val btnBack = findViewById<TextView>(R.id.btnBackCrashLog)
        val btnHapus = findViewById<TextView>(R.id.btnHapusRiwayatCrash)
        listView = findViewById(R.id.listCrashLog)
        tvEmpty = findViewById(R.id.tvCrashEmpty)

        btnBack.setOnClickListener { finish() }
        btnHapus.setOnClickListener {
            val f = File(filesDir, "memecio_crash.txt")
            if (f.exists()) f.delete()
            loadEntries()
        }

        adapter = object : ArrayAdapter<CrashEntry>(this, 0, entries) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var view = convertView
                if (view == null) {
                    view = LayoutInflater.from(context)
                        .inflate(R.layout.item_crash_entry, parent, false)
                }
                val entry = getItem(position)
                view!!.findViewById<TextView>(R.id.tvCrashEntryTitle).text = entry?.title
                return view
            }
        }
        listView?.adapter = adapter
        listView?.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val entry = entries[position]
            val intent = Intent(this, CrashLogDetailActivity::class.java)
            intent.putExtra("crash_title", entry.title)
            intent.putExtra("crash_body", entry.body)
            startActivity(intent)
        }

        loadEntries()
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }

    private fun loadEntries() {
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
                val content = sb.toString()
                val pattern = Pattern.compile("=== .*?(?:CRASH at |crash )?([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}).*?===\\n")
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
                    val entry = CrashEntry()
                    entry.title = titles[i]
                    entry.body = body
                    entries.add(0, entry)
                }
            } catch (ignored: Exception) {
            }
        }
        adapter?.notifyDataSetChanged()
        if (entries.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        }
    }
}
