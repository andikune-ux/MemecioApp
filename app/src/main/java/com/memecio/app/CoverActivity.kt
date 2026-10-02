package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class CoverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } catch (ignored: Exception) {
        }

        try {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        } catch (ignored: Exception) {
        }

        setContentView(R.layout.activity_cover)

        val cover = findViewById<ImageView>(R.id.coverImage)
        try {
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            cover.setImageResource(if (isLandscape) R.drawable.cover_landscape else R.drawable.cover_portrait)
        } catch (ignored: Exception) {
        }

        findViewById<View>(R.id.coverRoot).setOnClickListener {
            try {
                val intent = Intent(this, CalculatorActivity::class.java)
                intent.putExtra("reauth", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (ignored: Exception) {
            }
            finish()
        }
    }

    override fun onBackPressed() {
        // Block back button
    }
}
