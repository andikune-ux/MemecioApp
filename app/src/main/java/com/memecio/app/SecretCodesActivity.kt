package com.memecio.app

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

class SecretCodesActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_codes)
        findViewById<View>(R.id.btnBackSecret).setOnClickListener { finish() }

        val list = findViewById<ListView>(R.id.listSecretCodes)
        val codes = SecretCodeRegistry.getVisible()

        list.adapter = object : BaseAdapter() {
            override fun getCount(): Int = codes.size
            override fun getItem(pos: Int): Any = codes[pos]
            override fun getItemId(pos: Int): Long = pos.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var v = convertView
                if (v == null) {
                    v = LayoutInflater.from(this@SecretCodesActivity)
                        .inflate(R.layout.item_secret_code, parent, false)
                }
                val c = codes[position]
                v.findViewById<TextView>(R.id.tvSecretCode).text = c.code
                v.findViewById<TextView>(R.id.tvSecretTitle).text = c.title
                v.findViewById<TextView>(R.id.tvSecretDesc).text = c.description
                return v
            }
        }
    }
}
