package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.Date

class PreviewImageActivity : Activity() {

    private var imgPreview: ImageView? = null
    private var tvCounter: TextView? = null
    private var imageList: ArrayList<String> = ArrayList()
    private var currentIndex = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun debugLog(msg: String) {
        try {
            var dir = File("/sdcard/Download")
            if (!dir.exists()) dir = getExternalFilesDir(null) ?: return
            val f = File(dir, "memecio_preview_debug.txt")
            val fw = FileWriter(f, true)
            fw.write("${Date()} | $msg\n")
            fw.close()
        } catch (ignored: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (MixedPlaylistHolder.isActive()) {
            val idx = MixedPlaylistHolder.getCurrentIndex()
            val mixed = MixedPlaylistHolder.getItems()
            if (idx >= 0 && idx < mixed.size) {
                val m = mixed[idx]
                if (m.type == MediaItem.TYPE_VIDEO) {
                    val vp = Intent(this, VideoPlayerActivity::class.java)
                    vp.putExtra("index", idx)
                    startActivity(vp)
                    finish()
                    return
                }
            }
        }

        debugLog("onCreate START")
        try {
            setContentView(R.layout.activity_preview_image)
        } catch (t: Throwable) {
            debugLog("setContentView GAGAL: $t")
            throw RuntimeException(t)
        }
        debugLog("setContentView OK")

        imgPreview = findViewById(R.id.imgPreview)
        tvCounter = findViewById(R.id.tvPreviewCounter)
        val btnClose = findViewById<View>(R.id.btnClosePreview)

        val fromIntent = intent.getStringArrayListExtra("image_list")
        currentIndex = intent.getIntExtra("index", 0)

        if (fromIntent != null && fromIntent.isNotEmpty()) {
            imageList = fromIntent
        } else {
            val single = intent.getStringExtra("uri")
            if (single != null) {
                imageList.add(single)
                currentIndex = 0
            }
        }

        debugLog("imageList.size=${imageList.size} currentIndex=$currentIndex")

        if (imageList.isEmpty() && !MixedPlaylistHolder.isActive()) {
            debugLog("imageList KOSONG & Mixed kosong -> finish()")
            finish()
            return
        }

        if (currentIndex < 0 || currentIndex >= imageList.size) currentIndex = 0

        loadCurrent()

        btnClose.setOnClickListener { finish() }

        imgPreview?.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0f
            var startTime = 0L
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        startTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = event.x - startX
                        val dt = System.currentTimeMillis() - startTime
                        if (Math.abs(dx) > 100 && dt < 800) {
                            if (dx > 0) showPrevious() else showNext()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun loadCurrent() {
        if (imageList.isEmpty()) return
        val uriStr = imageList[currentIndex]
        tvCounter?.text = "${currentIndex + 1} / ${imageList.size}"
        try {
            val bitmap = BitmapFactory.decodeStream(
                contentResolver.openInputStream(Uri.parse(uriStr))
            )
            imgPreview?.setImageBitmap(bitmap)
        } catch (e: Exception) {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(uriStr).openStream())
                imgPreview?.setImageBitmap(bitmap)
            } catch (ignored: Exception) {
                imgPreview?.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun showNext() {
        if (currentIndex < imageList.size - 1) {
            currentIndex++
            loadCurrent()
        }
    }

    private fun showPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            loadCurrent()
        }
    }
}
