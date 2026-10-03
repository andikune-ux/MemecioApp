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

    private var selectedFileUri: Uri? = null
    private var uploadedUrl: String? = null
    private var progressBar: ProgressBar? = null
    private var tvPercent: TextView? = null
    private var tvSelectedFile: TextView? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_source)

        val btnBack: TextView? = findViewById(R.id.btnBackServer)
        val btnUpload: Button? = findViewById(R.id.btnUploadServer)
        val btnTampilkan: Button? = findViewById(R.id.btnTampilkanServer)
        progressBar = findViewById(R.id.progressUpload)
        tvPercent = findViewById(R.id.tvUploadPercent)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)

        btnBack?.setOnClickListener { finish() }

        btnUpload?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, 300)
        }

        btnTampilkan?.setOnClickListener {
            val url = uploadedUrl
            if (url == null) {
                Toast.makeText(this, "Upload file terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RiwayatStore.tambah(this, url)

            val lower = url.lowercase()
            var type = MediaItem.TYPE_VIDEO
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                type = MediaItem.TYPE_IMAGE
            }

            val entries = mutableListOf<MediaItem>()
            val mi = MediaItem(Uri.parse(url), type).apply {
                isLocal = false
                sourceTitle = "Server"
            }
            entries.add(mi)
            ExternalMediaStore.gantiSemua(this, entries, "server")

            Toast.makeText(this, "Ditampilkan di Beranda", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("focus_source", true)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.data
            selectedFileUri?.let { uri ->
                tvSelectedFile?.text = uri.lastPathSegment
                simulateUpload()
            }
        }
    }

    private fun simulateUpload() {
        progressBar?.visibility = View.VISIBLE
        tvPercent?.visibility = View.VISIBLE
        progressBar?.progress = 0

        var progress = 0
        val uploadRunnable = object : Runnable {
            override fun run() {
                progress += 5
                if (progress > 100) progress = 100
                progressBar?.progress = progress
                tvPercent?.text = "$progress%"

                if (progress < 100) {
                    handler.postDelayed(this, 100)
                } else {
                    uploadedUrl = selectedFileUri.toString()
                    Toast.makeText(this@ServerSourceActivity, "Upload selesai", Toast.LENGTH_SHORT).show()
                }
            }
        }
        handler.post(uploadRunnable)
    }
}
