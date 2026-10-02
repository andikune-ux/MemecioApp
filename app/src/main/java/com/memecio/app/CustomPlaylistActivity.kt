package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class CustomPlaylistActivity : Activity() {

    private var listView: ListView? = null
    private var tvEmpty: TextView? = null
    private var btnTambah: Button? = null
    private var playlists: MutableList<CustomPlaylistStore.Playlist> = mutableListOf()
    private var adapter: ArrayAdapter<CustomPlaylistStore.Playlist>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_playlist)

        val btnBack = findViewById<TextView>(R.id.btnBackCustomPlaylist)
        tvEmpty = findViewById(R.id.tvCustomPlaylistEmpty)
        listView = findViewById(R.id.listCustomPlaylist)
        btnTambah = findViewById(R.id.btnTambahPlaylist)

        btnBack.setOnClickListener { finish() }

        btnTambah?.setOnClickListener {
            showDialogTambah()
        }

        listView?.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val pl = playlists[position]
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("open_custom_playlist", position)
            startActivity(intent)
            finish()
        }

        listView?.setOnItemLongClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            confirmHapus(position)
            true
        }

        loadData()
    }

    private fun loadData() {
        playlists = CustomPlaylistStore.getAll(this)
        adapter = object : ArrayAdapter<CustomPlaylistStore.Playlist>(
            this, 0, playlists
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var view = convertView
                if (view == null) {
                    view = LayoutInflater.from(this@CustomPlaylistActivity)
                        .inflate(android.R.layout.simple_list_item_2, parent, false)
                }
                val pl = getItem(position)
                val tv1 = view!!.findViewById<TextView>(android.R.id.text1)
                val tv2 = view.findViewById<TextView>(android.R.id.text2)
                tv1.text = pl?.name
                tv1.setTextColor(0xFF1C1C1E.toInt())
                tv2.text = (pl?.items?.size ?: 0).toString() + " item"
                tv2.setTextColor(0xFF8A8A8E.toInt())
                return view
            }
        }
        listView?.adapter = adapter

        if (playlists.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        }
    }

    private fun showDialogTambah() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_input_nama)

        val etNama = dialog.findViewById<EditText>(R.id.etNamaPlaylist)
        val btnBatal = dialog.findViewById<Button>(R.id.btnBatalNama)
        val btnSimpan = dialog.findViewById<Button>(R.id.btnSimpanNama)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CustomPlaylistStore.tambahPlaylist(this, nama)
            dialog.dismiss()
            loadData()
            Toast.makeText(this, "Playlist dibuat", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun confirmHapus(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Playlist")
            .setMessage("Hapus \"" + playlists[position].name + "\"?")
            .setNegativeButton("Batal") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setPositiveButton("Hapus") { _: DialogInterface, _: Int ->
                CustomPlaylistStore.hapusPlaylist(this, position)
                loadData()
            }
            .show()
    }
}
