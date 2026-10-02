package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.util.ArrayList
import java.util.HashSet
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HiddenMediaListActivity : Activity() {

    private var items: MutableList<MediaItem> = ArrayList()
    private var selected: MutableSet<String> = HashSet()
    private var selectionMode = false
    private var adapter: BaseAdapter? = null
    private var gridView: GridView? = null
    private var tvEmpty: TextView? = null
    private var tvInfo: TextView? = null
    private var tvTitle: TextView? = null
    private var bottomBar: View? = null
    private var btnPulihkanTop: View? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val thumbExecutor: ExecutorService = Executors.newFixedThreadPool(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_media)

        findViewById<View>(R.id.btnBackHidden).setOnClickListener {
            if (selectionMode) exitSelectionMode() else finish()
        }
        findViewById<View>(R.id.btnToggleSelect).setOnClickListener { toggleSelectionMode() }
        btnPulihkanTop = findViewById(R.id.btnPulihkanTerpilih)
        btnPulihkanTop?.setOnClickListener { pulihkanTerpilih() }
        findViewById<View>(R.id.btnPulihkanBottom).setOnClickListener { pulihkanTerpilih() }
        findViewById<View>(R.id.btnHapusTerpilih).setOnClickListener { confirmHapusTerpilih() }

        gridView = findViewById(R.id.gridHidden)
        tvEmpty = findViewById(R.id.tvHiddenEmpty)
        tvInfo = findViewById(R.id.tvHiddenInfo)
        tvTitle = findViewById(R.id.tvHiddenTitle)
        bottomBar = findViewById(R.id.bottomBar)

        adapter = object : BaseAdapter() {
            override fun getCount(): Int = items.size
            override fun getItem(p: Int): Any = items[p]
            override fun getItemId(p: Int): Long = p.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var v = convertView
                if (v == null) {
                    v = LayoutInflater.from(this@HiddenMediaListActivity)
                        .inflate(R.layout.item_hidden_grid, parent, false)
                }
                val item = items[position]
                val thumb = v.findViewById<ImageView>(R.id.ivHiddenThumb)
                val playIcon = v.findViewById<ImageView>(R.id.ivHiddenPlay)
                val checkmark = v.findViewById<ImageView>(R.id.ivCheckmark)
                val name = v.findViewById<TextView>(R.id.tvHiddenName)
                name.text = item.title ?: "media"
                if (item.type == MediaItem.TYPE_VIDEO) {
                    playIcon.visibility = View.VISIBLE
                } else {
                    playIcon.visibility = View.GONE
                }
                checkmark.visibility = if (selected.contains(item.uri.toString())) View.VISIBLE else View.GONE
                thumb.setImageBitmap(null)
                val cacheKey = item.uri.toString()
                thumb.tag = cacheKey
                loadThumb(item, thumb, cacheKey)
                return v
            }
        }
        gridView?.adapter = adapter
        gridView?.setOnItemClickListener { _, _, pos, _ ->
            val item = items[pos]
            if (selectionMode) {
                toggleItemSelection(item)
            } else {
                val i = Intent(this, PreviewImageActivity::class.java)
                i.putExtra("uri", item.uri.toString())
                startActivity(i)
            }
        }
        gridView?.setOnItemLongClickListener { _, _, pos, _ ->
            if (!selectionMode) {
                enterSelectionMode()
            }
            toggleItemSelection(items[pos])
            true
        }
        loadItems()
    }

    private fun loadItems() {
        items = HiddenMediaStore.scanHidden(this)
        adapter?.notifyDataSetChanged()
        updateUI()
    }

    private fun updateUI() {
        if (items.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            gridView?.visibility = View.GONE
            tvInfo?.text = "0 file"
        } else {
            tvEmpty?.visibility = View.GONE
            gridView?.visibility = View.VISIBLE
            tvInfo?.text = "${items.size} file"
        }
        tvTitle?.text = if (selectionMode) "${selected.size} dipilih" else "Media Tersembunyi"
    }

    private fun toggleSelectionMode() {
        if (selectionMode) exitSelectionMode() else enterSelectionMode()
    }

    private fun enterSelectionMode() {
        selectionMode = true
        selected.clear()
        bottomBar?.visibility = View.VISIBLE
        btnPulihkanTop?.visibility = View.VISIBLE
        updateUI()
        adapter?.notifyDataSetChanged()
    }

    private fun exitSelectionMode() {
        selectionMode = false
        selected.clear()
        bottomBar?.visibility = View.GONE
        btnPulihkanTop?.visibility = View.GONE
        updateUI()
        adapter?.notifyDataSetChanged()
    }

    private fun toggleItemSelection(item: MediaItem) {
        val key = item.uri.toString()
        if (selected.contains(key)) selected.remove(key) else selected.add(key)
        updateUI()
        adapter?.notifyDataSetChanged()
    }

    private fun pulihkanTerpilih() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Pilih file dulu", Toast.LENGTH_SHORT).show()
            return
        }
        var ok = 0
        for (item in items) {
            if (selected.contains(item.uri.toString())) {
                if (HideHelper.kembalikan(this, item)) ok++
            }
        }
        Toast.makeText(this, "$ok file dipulihkan", Toast.LENGTH_SHORT).show()
        exitSelectionMode()
        loadItems()
    }

    private fun confirmHapusTerpilih() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Pilih file dulu", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Hapus Permanen")
            .setMessage("Hapus ${selected.size} file terpilih secara permanen?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                var ok = 0
                for (item in items) {
                    if (selected.contains(item.uri.toString())) {
                        try {
                            val f = File(item.uri.path ?: continue)
                            if (f.exists()) {
                                f.delete()
                                ok++
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                }
                Toast.makeText(this, "$ok file dihapus", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
                loadItems()
            }
            .show()
    }

    private fun loadThumb(item: MediaItem, iv: ImageView, cacheKey: String) {
        thumbExecutor.execute {
            try {
                val path = item.uri.path ?: return@execute
                val f = File(path)
                if (!f.exists()) return@execute
                val bmp = if (item.type == MediaItem.TYPE_IMAGE) {
                    BitmapFactory.decodeFile(path)
                } else {
                    android.media.ThumbnailUtils.createVideoThumbnail(
                        path, android.provider.MediaStore.Images.Thumbnails.MINI_KIND
                    )
                }
                if (bmp != null) {
                    mainHandler.post {
                        if (cacheKey == iv.tag) {
                            iv.setImageBitmap(bmp)
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }
    }
}
