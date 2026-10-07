package com.memecio.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

class CrashLogDetailActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_log_detail)

        val title = intent.getStringExtra("crash_title")
        val body = intent.getStringExtra("crash_body")

        val btnBack = findViewById<TextView>(R.id.btnBackCrashDetail)
        val tvTitle = findViewById<TextView>(R.id.tvCrashDetailTitle)
        val tvBody = findViewById<TextView>(R.id.tvCrashDetailBody)
        val btnCopy = findViewById<TextView>(R.id.btnCopyCrash)

        tvTitle.text = title ?: "Detail Crash"
        tvBody.text = body ?: ""

        btnBack.setOnClickListener { finish() }
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("crash_log", body ?: "")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Log disalin", Toast.LENGTH_SHORT).show()
        }
    }
}
