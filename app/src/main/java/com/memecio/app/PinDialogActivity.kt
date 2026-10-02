package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class PinDialogActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_dialog)

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnOk = findViewById<Button>(R.id.btnPinOk)
        val btnCancel = findViewById<Button>(R.id.btnPinCancel)
        val target = intent.getStringExtra(EXTRA_TARGET)

        btnCancel.setOnClickListener { finish() }

        btnOk.setOnClickListener {
            val input = etPin.text.toString().trim()
            if (input == PIN_CODE) {
                if (TARGET_SERVER == target) {
                    startActivity(Intent(this, ServerSourceActivity::class.java))
                } else if (TARGET_RIWAYAT == target) {
                    startActivity(Intent(this, RiwayatActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(this, "PIN salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_TARGET = "target"
        const val TARGET_SERVER = "server"
        const val TARGET_RIWAYAT = "riwayat"
        private const val PIN_CODE = "808080"
    }
}
