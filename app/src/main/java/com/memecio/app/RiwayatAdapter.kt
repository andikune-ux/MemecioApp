package com.memecio.app

import android.app.AlertDialog
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
    context: Context,
    private val items: List<String>,
    private val deleteListener: OnDeleteListener?
) : ArrayAdapter<String>(context, 0, items) {

    fun interface OnDeleteListener {
        fun onDelete(url: String)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_riwayat, parent, false)
        val url = items[position]

        val tvUrl = view.findViewById<TextView>(R.id.tvRiwayatUrl)
        val btnSalin = view.findViewById<TextView>(R.id.btnSalinUrl)
        val btnHapus = view.findViewById<TextView>(R.id.btnHapusUrl)

        tvUrl.text = url

        btnSalin.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("url", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "URL disalin", Toast.LENGTH_SHORT).show()
        }

        btnHapus.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Hapus URL")
                .setMessage("Hapus URL ini dari riwayat?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus") { _, _ ->
                    deleteListener?.onDelete(url)
                }
                .show()
        }

        return view
    }
}
