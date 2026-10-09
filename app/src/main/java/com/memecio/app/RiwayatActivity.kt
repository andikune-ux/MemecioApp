package com.memecio.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView

class RiwayatActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val btnBack = findViewById<View>(R.id.btnBack)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val listView = findViewById<ListView>(R.id.listView)

        btnBack?.setOnClickListener {
            finish()
        }

        val riwayat = RiwayatStore.getAll(this)
        if (riwayat.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
            listView?.adapter = RiwayatAdapter(this, riwayat)
        }
    }
}
