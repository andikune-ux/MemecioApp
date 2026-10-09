package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.io.File

class DownloadListActivity : Activity() {

    private lateinit var lvDownloads: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_list)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        lvDownloads = findViewById(R.id.lvDownloads)
        tvEmpty = findViewById(R.id.tvEmptyDownloads)
        tvCount = findViewById(R.id.tvDownloadCount)

        loadDownloads()
    }

    override fun onResume() {
        super.onResume()
        loadDownloads()
    }

    private fun loadDownloads() {
        val files = ArrayList<File>()

        // Search directory 1: /sdcard/Termux/Download/
        val d1 = File("/sdcard/Termux/Download")
        if (d1.exists() && d1.isDirectory) {
            d1.listFiles()?.filter { it.isFile && !it.name.startsWith(".") }?.let { files.addAll(it) }
        }

        // Search directory 2: app download dir
        val d2 = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (d2 != null && d2.exists()) {
            d2.listFiles()?.filter { it.isFile && !it.name.startsWith(".") }?.let { files.addAll(it) }
        }

        // Search directory 3: standard external download dir
        val d3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (d3 != null && d3.exists()) {
            d3.listFiles()?.filter {
                it.isFile && !it.name.startsWith(".") && (it.name.endsWith(".mp4", true) || it.name.endsWith(".mkv", true) || it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true))
            }?.let { files.addAll(it) }
        }

        val uniqueFiles = files.distinctBy { it.absolutePath }
        tvCount.text = "${uniqueFiles.size} File"

        if (uniqueFiles.isEmpty()) {
            lvDownloads.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            lvDownloads.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            lvDownloads.adapter = object : ArrayAdapter<File>(this, 0, uniqueFiles) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val file = getItem(position)!!
                    val card = android.widget.LinearLayout(context).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        setBackgroundResource(R.drawable.bg_glass_card)
                        setPadding(24, 20, 24, 20)
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }

                    val infoLayout = android.widget.LinearLayout(context).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val tvName = TextView(context).apply {
                        text = file.name
                        textSize = 14f
                        setTextColor(0xFF222222.toInt())
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }

                    val sizeMb = file.length() / (1024f * 1024f)
                    val tvDetails = TextView(context).apply {
                        text = String.format("%.2f MB • %s", sizeMb, file.parent)
                        textSize = 11f
                        setTextColor(0xFF777777.toInt())
                    }

                    infoLayout.addView(tvName)
                    infoLayout.addView(tvDetails)

                    val btnPlay = Button(context).apply {
                        text = "Buka"
                        textSize = 11f
                        setBackgroundResource(R.drawable.bg_button_primary)
                        setTextColor(0xFFFFFFFF.toInt())
                        layoutParams = android.widget.LinearLayout.LayoutParams(160, 90).apply {
                            marginEnd = 16
                        }
                        setOnClickListener {
                            openFile(file)
                        }
                    }

                    val btnDel = Button(context).apply {
                        text = "Hapus"
                        textSize = 11f
                        setBackgroundResource(R.drawable.bg_button_outline)
                        setTextColor(0xFFD32F2F.toInt())
                        layoutParams = android.widget.LinearLayout.LayoutParams(160, 90)
                        setOnClickListener {
                            AlertDialog.Builder(context)
                                .setTitle("Hapus File")
                                .setMessage("Hapus file '${file.name}'?")
                                .setPositiveButton("Hapus") { _, _ ->
                                    file.delete()
                                    Toast.makeText(context, "File dihapus", Toast.LENGTH_SHORT).show()
                                    loadDownloads()
                                }
                                .setNegativeButton("Batal", null)
                                .show()
                        }
                    }

                    card.addView(infoLayout)
                    card.addView(btnPlay)
                    card.addView(btnDel)
                    return card
                }
            }
        }
    }

    private fun openFile(file: File) {
        val uri = Uri.fromFile(file)
        val name = file.name.lowercase()
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) {
            val intent = Intent(this, PreviewImageActivity::class.java).apply {
                putExtra("uri", uri.toString())
            }
            startActivity(intent)
        } else {
            val playlist = arrayListOf(uri.toString())
            PlaylistHolder.set(playlist)
            val intent = Intent(this, VideoPlayerActivity::class.java).apply {
                putStringArrayListExtra("playlist", playlist)
                putExtra("index", 0)
            }
            startActivity(intent)
        }
    }
}
