package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.ArrayList
import java.util.regex.Pattern

class DriveSourceActivity : Activity() {

    private var etUrl: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundHelper.init(this)
        setContentView(R.layout.activity_drive_source)
        etUrl = findViewById(R.id.etDriveUrl)
        val btnBack = findViewById<TextView>(R.id.btnBackDrive)
        val btnTampilkan = findViewById<Button>(R.id.btnTampilkanDrive)

        btnBack.setOnClickListener { finish() }

        btnTampilkan.setOnClickListener {
            val url = etUrl?.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Masukkan link terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val folderId = extractFolderId(url)
            val fileId = extractFileId(url)
            RiwayatStore.tambah(this, url)
            if (folderId != null) {
                val namaDefault = "Drive Folder " + folderId.substring(0, Math.min(6, folderId.length))
                tampilkanDialogNama(namaDefault, url)
            } else if (fileId != null) {
                val directUrl = "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
                val thumbUrl = "https://drive.google.com/thumbnail?id=$fileId&sz=w400"
                val namaFile = "Drive File " + fileId.substring(0, Math.min(6, fileId.length))
                var type = MediaItem.TYPE_VIDEO
                val lower = url.lowercase()
                if (lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png") || lower.contains(".webp") || lower.contains(".gif")) {
                    type = MediaItem.TYPE_IMAGE
                } else if (lower.contains(".mp3") || lower.contains(".wav") || lower.contains(".m4a")) {
                    type = MediaItem.TYPE_AUDIO
                }
                val mi = MediaItem(Uri.parse(directUrl), type)
                mi.isLocal = false
                mi.title = namaFile
                mi.thumbUrl = thumbUrl
                val entries = ArrayList<MediaItem>()
                entries.add(mi)
                ExternalMediaStore.gantiSemua(this, entries, "drive")
                SoundHelper.success()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("focus_source", true)
                startActivity(intent)
                Toast.makeText(this, "File ditampilkan di Beranda", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                SoundHelper.error()
                Toast.makeText(this, "Link Drive tidak valid. Pastikan mengandung /d/FILE_ID/ atau /drive/folders/FOLDER_ID", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun tampilkanDialogNama(namaDefault: String, url: String) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_input_nama)
        val etNama = dialog.findViewById<EditText>(R.id.etNamaPlaylist)
        etNama.setText(namaDefault)
        val btnBatal = dialog.findViewById<Button>(R.id.btnBatalNama)
        val btnSimpan = dialog.findViewById<Button>(R.id.btnSimpanNama)
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val folderId = extractFolderId(url)
            val directUrl = "https://drive.google.com/embeddedfolderview?id=$folderId#grid"
            val entries = ArrayList<MediaItem>()
            val mi = MediaItem(Uri.parse(directUrl), MediaItem.TYPE_FOLDER)
            mi.isLocal = false
            mi.title = nama
            entries.add(mi)
            ExternalMediaStore.gantiSemua(this, entries, nama)
            SoundHelper.success()
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("focus_source", true)
            startActivity(intent)
            Toast.makeText(this, "Folder ditampilkan di Beranda", Toast.LENGTH_SHORT).show()
            finish()
        }
        dialog.show()
    }

    private fun extractFolderId(url: String): String? {
        val p = Pattern.compile("/drive/folders/([a-zA-Z0-9_-]+)")
        val m = p.matcher(url)
        return if (m.find()) m.group(1) else null
    }

    private fun extractFileId(url: String): String? {
        var m = Pattern.compile("/d/([a-zA-Z0-9_-]+)").matcher(url)
        if (m.find()) return m.group(1)
        m = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)").matcher(url)
        return if (m.find()) m.group(1) else null
    }
}
