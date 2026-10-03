package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

class StreamingSourceActivity : Activity() {

    private var etUrl: EditText? = null
    private var savedLinks: MutableList<MediaItem> = ArrayList()
    private var adapter: ArrayAdapter<MediaItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming_source)

        etUrl = findViewById(R.id.etStreamingUrl)
        val btnBack: TextView? = findViewById(R.id.btnBackStreaming)
        val btnUpload: Button? = findViewById(R.id.btnUploadM3u)
        val btnTampilkan: Button? = findViewById(R.id.btnTampilkanStreaming)
        val listSavedLinks: ListView? = findViewById(R.id.listSavedLinks)

        setupSavedList(listSavedLinks)

        btnBack?.setOnClickListener { finish() }

        btnUpload?.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            try {
                SessionState.suppressLock()
            } catch (_: Exception) {}
            startActivityForResult(intent, 200)
        }

        btnTampilkan?.setOnClickListener {
            val url = etUrl?.text?.toString()?.trim() ?: ""
            if (url.isEmpty()) {
                Toast.makeText(this, "Masukkan link terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidStreamingUrl(url)) {
                Toast.makeText(this, "Format link tidak dikenali", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            deteksiDanProses(url)
        }
    }

    private fun setupSavedList(listSavedLinks: ListView?) {
        if (listSavedLinks == null) return
        savedLinks = SavedLinksStore.getAll(this).toMutableList()

        adapter = object : ArrayAdapter<MediaItem>(this, 0, savedLinks) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_playlist_row, parent, false)
                val entry = getItem(position) ?: return view
                val tv = view.findViewById<TextView>(R.id.tvPlaylistTitle)
                val judul = if (entry.title.isNullOrBlank()) entry.uri.toString() else entry.title
                tv.text = judul
                tv.isSelected = true

                var longFired = false
                val longHandler = Handler(Looper.getMainLooper())
                val longRunnable = Runnable {
                    longFired = true
                    confirmDelete(entry)
                }

                view.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            longFired = false
                            longHandler.postDelayed(longRunnable, 4000)
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            longHandler.removeCallbacks(longRunnable)
                            if (!longFired) {
                                openInBeranda(entry)
                            }
                            true
                        }
                        MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_MOVE -> {
                            longHandler.removeCallbacks(longRunnable)
                            true
                        }
                        else -> false
                    }
                }

                val btnHapus = view.findViewById<TextView?>(R.id.btnHapusPlaylist)
                btnHapus?.setOnClickListener { confirmDelete(entry) }

                return view
            }
        }
        listSavedLinks.adapter = adapter
    }

    private fun confirmDelete(entry: MediaItem) {
        val judul = if (entry.title.isNullOrBlank()) "playlist ini" else entry.title
        AlertDialog.Builder(this)
            .setTitle("Hapus Playlist")
            .setMessage("Apakah anda ingin menghapus \"$judul\"?")
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Hapus") { _, _ ->
                SavedLinksStore.hapus(this, entry.uri.toString())
                savedLinks.clear()
                savedLinks.addAll(SavedLinksStore.getAll(this))
                adapter?.notifyDataSetChanged()
                Toast.makeText(this, "Playlist dihapus", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun openInBeranda(entry: MediaItem) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_playlist_url", entry.uri.toString())
            putExtra("open_playlist_title", entry.title)
        }
        startActivity(intent)
        finish()
    }

    private fun deteksiDanProses(url: String) {
        Toast.makeText(this, "Memeriksa link...", Toast.LENGTH_SHORT).show()
        M3uParser.parseAsync(this, url, object : M3uParser.Callback {
            override fun onSuccess(entries: List<M3uParser.MediaEntry>) {
                if (entries.size > 1) {
                    showJudulDialog(url)
                } else {
                    val judulVideo: String
                    var thumbVideo: String? = null
                    if (entries.size == 1) {
                        val e = entries[0]
                        judulVideo = if (!e.title.isNullOrBlank()) e.title else deriveTitleFromUrl(url)
                        thumbVideo = e.thumbUrl
                    } else {
                        judulVideo = deriveTitleFromUrl(url)
                    }
                    prosesVideoTunggal(url, judulVideo, thumbVideo)
                }
            }

            override fun onError(message: String) {
                Toast.makeText(this@StreamingSourceActivity, "Deteksi: $message", Toast.LENGTH_LONG).show()
                prosesVideoTunggal(url, deriveTitleFromUrl(url), null)
            }
        })
    }

    private fun prosesVideoTunggal(url: String, judul: String, thumbUrl: String?) {
        RiwayatStore.tambah(this, url)
        SavedLinksStore.tambah(this, judul, url)

        val mediaItems = ArrayList<MediaItem>()
        val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
            isLocal = false
            title = judul
            this.thumbUrl = thumbUrl
        }
        mediaItems.add(mi)

        ExternalMediaStore.gantiSemua(this, mediaItems, judul)
        gotoBeranda()
    }

    private fun deriveTitleFromUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            var last = uri.lastPathSegment ?: return "Video"
            val dot = last.lastIndexOf('.')
            if (dot > 0) last = last.substring(0, dot)
            last = last.replace('_', ' ').replace('-', ' ').trim()
            if (last.isEmpty()) "Video" else last
        } catch (_: Exception) {
            "Video"
        }
    }

    private fun showJudulDialog(url: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_input_judul)

        val etJudul = dialog.findViewById<EditText>(R.id.etJudulPopup)
        val btnTampilkanPopup = dialog.findViewById<TextView>(R.id.btnTampilkanPopup)

        btnTampilkanPopup?.setOnClickListener {
            var judul = etJudul?.text?.toString()?.trim() ?: ""
            if (judul.isEmpty()) judul = "Tanpa Judul"

            SavedLinksStore.tambah(this, judul, url)
            dialog.dismiss()
            prosesTampilkan(url, judul)
        }

        dialog.show()
    }

    private fun prosesTampilkan(url: String, judul: String) {
        RiwayatStore.tambah(this, url)
        Toast.makeText(this, "Memuat...", Toast.LENGTH_SHORT).show()

        M3uParser.parseAsync(this, url, object : M3uParser.Callback {
            override fun onSuccess(entries: List<M3uParser.MediaEntry>) {
                val mediaItems = ArrayList<MediaItem>()
                if (entries.isEmpty()) {
                    val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
                        isLocal = false
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
                ExternalMediaStore.gantiSemua(this@StreamingSourceActivity, mediaItems, judul)
                gotoBeranda()
            }

            override fun onError(message: String) {
                val mediaItems = ArrayList<MediaItem>()
                val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
                    isLocal = false
                }
                mediaItems.add(mi)
                ExternalMediaStore.gantiSemua(this@StreamingSourceActivity, mediaItems, judul)
                Toast.makeText(this@StreamingSourceActivity, "Gagal parsing: $message", Toast.LENGTH_LONG).show()
                gotoBeranda()
            }
        })
    }

    private fun gotoBeranda() {
        try {
            SessionState.markInternalTransition()
        } catch (_: Exception) {}
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("focus_source", true)
        }
        startActivity(intent)
        finish()
    }

    private fun isValidStreamingUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://")
            || lower.startsWith("content://") || lower.startsWith("file://")
        ) return true
        if (url.startsWith("/")) return true
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            if (fileUri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                val localPath = copyM3uToCache(fileUri)
                if (localPath != null) {
                    etUrl?.setText(localPath)
                    Toast.makeText(this, "File siap, tekan Tampilkan", Toast.LENGTH_SHORT).show()
                } else {
                    etUrl?.setText(fileUri.toString())
                    Toast.makeText(this, "File dipilih, tekan Tampilkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyM3uToCache(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { `in` ->
                val cacheFile = File(cacheDir, "picked_m3u_${System.currentTimeMillis()}.m3u")
                FileOutputStream(cacheFile).use { out ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (`in`.read(buf).also { n = it } != -1) {
                        out.write(buf, 0, n)
                    }
                    out.flush()
                }
                cacheFile.absolutePath
            }
        } catch (_: Exception) {
            null
        }
    }
}
