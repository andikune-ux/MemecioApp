package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import java.util.regex.Pattern

class DriveSourceActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_source)

        val etUrl = findViewById<EditText>(R.id.etDriveUrl)
        val radioVideo = findViewById<RadioButton>(R.id.rbVideo)
        val btnBack = findViewById<View>(R.id.btnBack)
        val btnTampilkan = findViewById<Button>(R.id.btnSubmit)

        btnBack?.setOnClickListener {
            finish()
        }

        btnTampilkan?.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                val fileId = extractFileId(url)
                if (fileId == null) {
                    Toast.makeText(this, "Link Google Drive tidak valid. Pastikan format /d/FILEID/ atau ?id=FILEID", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                RiwayatStore.tambah(this, url)
                val directUrl = "https://drive.google.com/uc?export=download&id=$fileId"
                val type = if (radioVideo?.isChecked == true) MediaItem.TYPE_VIDEO else MediaItem.TYPE_IMAGE
                val mi = MediaItem(Uri.parse(directUrl), type)
                mi.isLocal = false
                mi.title = "Google Drive"

                ExternalMediaStore.gantiSemua(this, listOf(mi), "drive")
                SavedLinksStore.tambah(this, "Drive: $fileId", url)
                Toast.makeText(this, "Ditampilkan di Beranda", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("focus_source", true)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Masukkan link terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractFileId(url: String): String? {
        val pattern1 = Pattern.compile("/d/([a-zA-Z0-9_-]+)")
        val matcher1 = pattern1.matcher(url)
        if (matcher1.find()) {
            return matcher1.group(1)
        }
        val pattern2 = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)")
        val matcher2 = pattern2.matcher(url)
        if (matcher2.find()) {
            return matcher2.group(1)
        }
        return null
    }
}
