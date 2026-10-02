package com.memecio.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import java.util.ArrayList

class PermissionInfoActivity : Activity() {

    private class Item(val name: String, val desc: String, val granted: Boolean)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_permission_info)
        findViewById<View>(R.id.btnBackPerm).setOnClickListener { finish() }

        val items = ArrayList<Item>()
        add(items, "READ_EXTERNAL_STORAGE", "Baca file dari penyimpanan (Android 12-)", Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= 33) {
            add(items, "READ_MEDIA_IMAGES", "Baca foto dari galeri", "android.permission.READ_MEDIA_IMAGES")
            add(items, "READ_MEDIA_VIDEO", "Baca video dari galeri", "android.permission.READ_MEDIA_VIDEO")
            add(items, "READ_MEDIA_AUDIO", "Baca audio dari galeri", "android.permission.READ_MEDIA_AUDIO")
        }
        add(items, "INTERNET", "Akses internet", Manifest.permission.INTERNET)
        if (Build.VERSION.SDK_INT >= 30) {
            add(items, "MANAGE_EXTERNAL_STORAGE", "Akses semua file (khusus)", "android.permission.MANAGE_EXTERNAL_STORAGE")
        }

        val list = findViewById<ListView>(R.id.listPerm)
        list.adapter = object : BaseAdapter() {
            override fun getCount(): Int = items.size
            override fun getItem(p: Int): Any = items[p]
            override fun getItemId(p: Int): Long = p.toLong()
            override fun getView(pos: Int, cv: View?, parent: ViewGroup): View {
                var v = cv
                if (v == null) {
                    v = LayoutInflater.from(this@PermissionInfoActivity)
                        .inflate(R.layout.item_permission, parent, false)
                }
                val it = items[pos]
                v.findViewById<TextView>(R.id.tvPermName).text = it.name
                v.findViewById<TextView>(R.id.tvPermDesc).text = it.desc
                val st = v.findViewById<TextView>(R.id.tvPermStatus)
                st.text = if (it.granted) "Granted" else "Denied"
                st.setBackgroundColor(if (it.granted) 0xFF4CAF50.toInt() else 0xFFE53935.toInt())
                return v
            }
        }

        list.setOnItemClickListener { _, _, _, _ ->
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.data = Uri.parse("package:$packageName")
            startActivity(i)
        }
    }

    private fun add(list: MutableList<Item>, name: String, desc: String, permission: String) {
        var g = false
        try {
            g = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } catch (ignored: Exception) {
        }
        list.add(Item(name, desc, g))
    }
}
