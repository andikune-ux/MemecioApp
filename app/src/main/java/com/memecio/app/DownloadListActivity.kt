package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.Locale

class DownloadListActivity : Activity() {

    private class Item {
        var file: File? = null
        var nama: String? = null
        var info: String? = null
    }

    private val items: MutableList<Item> = ArrayList()
    private var adapter: BaseAdapter? = null
    private var listView: ListView? = null
    private var tvEmpty: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_list)

        findViewById<View>(R.id.btnBackDownload).setOnClickListener { finish() }
        findViewById<View>(R.id.btnRefreshDownload).setOnClickListener { loadFiles() }

        listView = findViewById(R.id.listDownload)
        tvEmpty = findViewById(R.id.tvDownloadEmpty)

        adapter = object : BaseAdapter() {
            override fun getCount(): Int = items.size
            override fun getItem(p: Int): Any = items[p]
            override fun getItemId(p: Int): Long = p.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var v = convertView
                if (v == null) {
                    v = LayoutInflater.from(this@DownloadListActivity)
                        .inflate(R.layout.item_download, parent, false)
                }
                val item = items[position]
                v.findViewById<TextView>(R.id.tvDownloadName).text = item.nama
                v.findViewById<TextView>(R.id.tvDownloadInfo).text = item.info
                v.findViewById<TextView>(R.id.btnHapusDownload).setOnClickListener {
                    konfirmasiHapus(item)
                }
                return v
            }
        }
        listView?.adapter = adapter
        listView?.setOnItemClickListener { _, _, position, _ ->
            bukaFile(items[position].file!!)
        }

        loadFiles()
    }

    override fun onResume() {
        super.onResume()
        loadFiles()
    }

    private fun loadFiles() {
        items.clear()
        try {
            val dir = File(Environment.getExternalStorageDirectory(), "Termux/Download")
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles()
                if (files != null) {
                    Arrays.sort(files) { a, b -> b.lastModified().compareTo(a.lastModified()) }
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    for (f in files) {
                        if (!f.isFile) continue
                        val it = Item()
                        it.file = f
                        it.nama = f.name
                        it.info = "${formatSize(f.length())} • ${sdf.format(Date(f.lastModified()))}"
                        items.add(it)
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        adapter?.notifyDataSetChanged()
        if (items.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        }
    }

    private fun bukaFile(f: File) {
        try {
            val name = f.name.lowercase()
            var mime = "*/*"
            if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".mov")) {
                mime = "video/*"
            } else if (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav")) {
                mime = "audio/*"
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
                mime = "image/*"
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(f), mime)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak bisa buka file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun konfirmasiHapus(it: Item) {
        AlertDialog.Builder(this)
            .setTitle("Hapus File")
            .setMessage("Hapus \"${it.nama}\"?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                if (it.file?.delete() == true) {
                    Toast.makeText(this, "File dihapus", Toast.LENGTH_SHORT).show()
                    loadFiles()
                } else {
                    Toast.makeText(this, "Gagal hapus file", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
