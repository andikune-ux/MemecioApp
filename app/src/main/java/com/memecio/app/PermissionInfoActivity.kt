package com.memecio.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView

class PermissionInfoActivity : Activity() {

    data class PermItem(val name: String, val description: String, val isGranted: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_info)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }

        loadPermissions()
    }

    override fun onResume() {
        super.onResume()
        loadPermissions()
    }

    private fun loadPermissions() {
        val list = ArrayList<PermItem>()

        val hasInternet = checkSelfPermission(android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
        list.add(PermItem("INTERNET", "Akses jaringan internet untuk streaming dan unduhan", hasInternet))

        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        list.add(PermItem("MANAGE_EXTERNAL_STORAGE / READ_STORAGE", "Akses penuh membaca file galeri & unduhan", hasStorage))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasVideo = checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            list.add(PermItem("READ_MEDIA_VIDEO", "Izin membaca file video lokal", hasVideo))

            val hasImages = checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            list.add(PermItem("READ_MEDIA_IMAGES", "Izin membaca file gambar & foto lokal", hasImages))

            val hasAudio = checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            list.add(PermItem("READ_MEDIA_AUDIO", "Izin membaca file audio lokal", hasAudio))
        }

        val hasForeground = checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
        list.add(PermItem("FOREGROUND_SERVICE", "Layanan latar belakang untuk pemutaran audio & unduhan", hasForeground))

        val listView = findViewById<ListView>(R.id.lvPermissions)
        listView.adapter = object : ArrayAdapter<PermItem>(this, 0, list) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val card = android.widget.LinearLayout(this@PermissionInfoActivity).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.bg_glass_card)
                    setPadding(32, 24, 32, 24)
                }

                val item = list[position]

                val tvTitle = TextView(this@PermissionInfoActivity).apply {
                    text = "${if (item.isGranted) "✅" else "❌"} ${item.name}"
                    textSize = 14f
                    setTextColor(if (item.isGranted) 0xFF2E7D32.toInt() else 0xFFC62828.toInt())
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }

                val tvStatus = TextView(this@PermissionInfoActivity).apply {
                    text = if (item.isGranted) "Status: DIIZINKAN" else "Status: TIDAK DIIZINKAN (Perlu Izin)"
                    textSize = 12f
                    setTextColor(if (item.isGranted) 0xFF388E3C.toInt() else 0xFFD32F2F.toInt())
                    setPadding(0, 4, 0, 4)
                }

                val tvDesc = TextView(this@PermissionInfoActivity).apply {
                    text = item.description
                    textSize = 12f
                    setTextColor(0xFF666666.toInt())
                }

                card.addView(tvTitle)
                card.addView(tvStatus)
                card.addView(tvDesc)
                return card
            }
        }
    }
}
