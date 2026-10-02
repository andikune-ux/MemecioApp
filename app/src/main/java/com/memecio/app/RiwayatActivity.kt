package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class RiwayatActivity : Activity() {

    private var allRiwayat: MutableList<String> = ArrayList()
    private var filteredRiwayat: MutableList<String> = ArrayList()
    private var adapter: RiwayatAdapter? = null
    private var listView: ListView? = null
    private var tvEmpty: TextView? = null
    private var etSearch: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val btnBack = findViewById<TextView>(R.id.btnBackRiwayat)
        val btnExport = findViewById<TextView>(R.id.btnExportRiwayat)
        val btnHapusSemua = findViewById<TextView>(R.id.btnHapusSemuaRiwayat)
        tvEmpty = findViewById(R.id.tvRiwayatEmpty)
        listView = findViewById(R.id.listRiwayat)
        etSearch = findViewById(R.id.etSearchRiwayat)

        btnBack.setOnClickListener { finish() }
        btnExport.setOnClickListener { exportRiwayat() }
        btnHapusSemua.setOnClickListener { confirmHapusSemua() }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter(s.toString())
            }
        })

        loadData()
    }

    private fun loadData() {
        allRiwayat = RiwayatStore.getAll(this).toMutableList()
        adapter = RiwayatAdapter(this, filteredRiwayat) { url ->
            RiwayatStore.hapus(this, url)
            loadData()
            Toast.makeText(this, "URL dihapus", Toast.LENGTH_SHORT).show()
        }
        listView?.adapter = adapter
        applyFilter(etSearch?.text.toString())
    }

    private fun applyFilter(query: String?) {
        filteredRiwayat.clear()
        if (query.isNullOrEmpty()) {
            filteredRiwayat.addAll(allRiwayat)
        } else {
            val q = query.lowercase()
            for (url in allRiwayat) {
                if (url.lowercase().contains(q)) filteredRiwayat.add(url)
            }
        }
        adapter?.notifyDataSetChanged()
        if (filteredRiwayat.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
            tvEmpty?.text = if (allRiwayat.isEmpty()) "Belum ada riwayat link" else "Tidak ada hasil untuk \"$query\""
        } else {
            tvEmpty?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        }
    }

    private fun exportRiwayat() {
        if (allRiwayat.isEmpty()) {
            Toast.makeText(this, "Riwayat kosong", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val dir = File(Environment.getExternalStorageDirectory(), "Termux/Riwayat")
            if (!dir.exists()) dir.mkdirs()
            val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val file = File(dir, "riwayat_$ts.txt")
            val sb = StringBuilder()
            sb.append("Riwayat Link Memec.io\n")
            sb.append("Total: ${allRiwayat.size}\n")
            sb.append("Waktu: $ts\n\n")
            for ((i, url) in allRiwayat.withIndex()) {
                sb.append("${i + 1}. $url\n")
            }
            val out = FileOutputStream(file)
            out.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
            out.close()
            Toast.makeText(this, "Riwayat diekspor ke:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmHapusSemua() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Riwayat")
            .setMessage("Hapus semua riwayat link?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                RiwayatStore.kosongkan(this)
                loadData()
                Toast.makeText(this, "Semua riwayat dihapus", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
