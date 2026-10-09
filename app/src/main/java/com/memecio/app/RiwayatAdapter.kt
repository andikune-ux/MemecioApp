package com.memecio.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast

class RiwayatAdapter(
    private val context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_riwayat, parent, false)
        val url = items[position]

        val tvUrl = view.findViewById<TextView>(R.id.tvUrl)
        val btnCopy = view.findViewById<View>(R.id.btnCopy)

        tvUrl?.text = url
        btnCopy?.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("url", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "URL disalin", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
