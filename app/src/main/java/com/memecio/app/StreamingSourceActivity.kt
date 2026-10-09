package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class StreamingSourceActivity : Activity() {

    private lateinit var etUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming_source)

        etUrl = findViewById(R.id.etUrl)
        val btnBack = findViewById<View>(R.id.btnBack)
        val btnUpload = findViewById<Button>(R.id.btnPickM3u)
        val btnTampilkan = findViewById<Button>(R.id.btnPlay)

        btnBack?.setOnClickListener {
            finish()
        }

        btnUpload?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, 200)
        }

        btnTampilkan?.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                if (!isValidStreamingUrl(url)) {
                    Toast.makeText(this, "Format link tidak dikenali", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                RiwayatStore.tambah(this, url)
                btnTampilkan.isEnabled = false
                Toast.makeText(this, "Memuat...", Toast.LENGTH_SHORT).show()

                M3uParser.parseAsync(this, url, object : M3uParser.Callback {
                    override fun onSuccess(entries: List<M3uParser.MediaEntry>) {
                        btnTampilkan.isEnabled = true
                        val mediaItems = mutableListOf<MediaItem>()
                        if (entries.isEmpty()) {
                            val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
                                isLocal = false
                                title = "Streaming"
                            }
                            mediaItems.add(mi)
                        } else {
                            for (e in entries) {
                                val mi = MediaItem(Uri.parse(e.url), MediaItem.TYPE_VIDEO).apply {
                                    isLocal = false
                                    title = e.title
                                    thumbUrl = e.thumbUrl
                                    isM3u = true
                                }
                                mediaItems.add(mi)
                            }
                        }
                        ExternalMediaStore.gantiSemua(this@StreamingSourceActivity, mediaItems, "streaming")
                        val playlistName = if (entries.isNotEmpty()) "Playlist (${entries.size})" else "Stream"
                        SavedLinksStore.tambah(this@StreamingSourceActivity, playlistName, url)
                        gotoBeranda()
                    }

                    override fun onError(message: String) {
                        btnTampilkan.isEnabled = true
                        val mediaItems = mutableListOf<MediaItem>()
                        val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
                            isLocal = false
                            title = "Streaming"
                        }
                        mediaItems.add(mi)
                        ExternalMediaStore.gantiSemua(this@StreamingSourceActivity, mediaItems, "streaming")
                        SavedLinksStore.tambah(this@StreamingSourceActivity, "Stream", url)
                        Toast.makeText(this@StreamingSourceActivity, "Gagal parsing: $message", Toast.LENGTH_LONG).show()
                        gotoBeranda()
                    }
                })
            } else {
                Toast.makeText(this, "Masukkan link terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gotoBeranda() {
        Toast.makeText(this, "Ditampilkan di Beranda", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("focus_source", true)
        }
        startActivity(intent)
        finish()
    }

    private fun isValidStreamingUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("content://") || lower.startsWith("file://")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            if (fileUri != null) {
                etUrl.setText(fileUri.toString())
                Toast.makeText(this, "File dipilih, tekan Tampilkan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
