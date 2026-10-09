package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class ServerSourceActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private var progressBar: ProgressBar? = null
    private var selectedFileUri: Uri? = null
    private var tvPercent: TextView? = null
    private var tvSelectedFile: TextView? = null
    private var uploadedUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_source)

        val btnBack = findViewById<View>(R.id.btnBack)
        val btnUpload = findViewById<Button>(R.id.btnPickFile)
        val btnTampilkan = findViewById<Button>(R.id.btnStart)

        progressBar = findViewById(R.id.progressBar)
        tvPercent = findViewById(R.id.tvStatus)
        tvSelectedFile = findViewById(R.id.tvServerInfo)

        btnBack?.setOnClickListener {
            finish()
        }

        btnUpload?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, 300)
        }

        btnTampilkan?.setOnClickListener {
            val url = uploadedUrl
            if (url == null) {
                Toast.makeText(this, "Upload / pilih file terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RiwayatStore.tambah(this, url)
            val lower = url.lowercase()
            val type = if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                MediaItem.TYPE_IMAGE
            } else {
                MediaItem.TYPE_VIDEO
            }
            val mi = MediaItem(Uri.parse(url), type).apply {
                isLocal = false
                title = selectedFileUri?.lastPathSegment ?: "Server"
            }
            ExternalMediaStore.gantiSemua(this, listOf(mi), PinDialogActivity.TARGET_SERVER)
            SavedLinksStore.tambah(this, mi.title ?: "Server", url)
            Toast.makeText(this, "Ditampilkan di Beranda", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("focus_source", true)
            }
            startActivity(intent)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            selectedFileUri = uri
            if (uri != null) {
                tvSelectedFile?.text = uri.lastPathSegment ?: uri.toString()
                simulateUpload()
            }
        }
    }

    private fun simulateUpload() {
        progressBar?.visibility = View.VISIBLE
        tvPercent?.visibility = View.VISIBLE
        progressBar?.progress = 0

        var progress = 0
        val runnable = object : Runnable {
            override fun run() {
                progress += 10
                if (progress > 100) progress = 100
                progressBar?.progress = progress
                tvPercent?.text = "Mengunggah: $progress%"

                if (progress < 100) {
                    handler.postDelayed(this, 100L)
                } else {
                    uploadedUrl = selectedFileUri?.toString()
                    tvPercent?.text = "Upload selesai!"
                    Toast.makeText(this@ServerSourceActivity, "Upload selesai", Toast.LENGTH_SHORT).show()
                }
            }
        }
        handler.post(runnable)
    }
}
