package com.memecio.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL

class PreviewImageActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)

        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val btnClose = findViewById<View>(R.id.btnClose)

        val uriString = intent.getStringExtra("uri")
        if (uriString != null) {
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                loadRemote(uriString, imgPreview)
            } else {
                imgPreview?.setImageURI(Uri.parse(uriString))
            }
        }

        btnClose?.setOnClickListener {
            finish()
        }
    }

    private fun loadRemote(url: String, imgPreview: ImageView?) {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            var bitmap: Bitmap? = null
            try {
                val u = URL(url)
                val conn = u.openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.inputStream.use { ins ->
                    bitmap = BitmapFactory.decodeStream(ins)
                }
            } catch (ignored: Exception) {}

            handler.post {
                if (bitmap != null) {
                    imgPreview?.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this@PreviewImageActivity, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
