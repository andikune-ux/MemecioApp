package com.memecio.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ThumbnailAdapter(
    private val context: Context,
    private val items: List<MediaItem>,
    private val listener: OnThumbnailClickListener?
) : BaseAdapter() {

    fun interface OnThumbnailClickListener {
        fun onThumbnailClick(item: MediaItem)
    }

    fun interface OnHideListener {
        fun onHide(item: MediaItem)
    }

    private var hideListener: OnHideListener? = null

    fun setOnHideListener(listener: OnHideListener?) {
        this.hideListener = listener
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var dataSaver = false

    init {
        val prefs = context.getSharedPreferences("memecio_settings", Context.MODE_PRIVATE)
        this.dataSaver = prefs.getBoolean("data_saver", false)
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false)

        val imgThumb: ImageView = view.findViewById(R.id.imgThumb)
        val playBadge: TextView = view.findViewById(R.id.tvPlayBadge)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvM3uBadge: TextView? = view.findViewById(R.id.tvM3uBadge)
        val btnHide: TextView? = view.findViewById(R.id.btnHide)

        val item = items[position]
        val cacheKey = "${item.uri}_${item.title}"

        try {
            val numCols = if (parent is GridView) parent.numColumns else 3
            var colWidth = parent.width / (if (numCols > 0) numCols else 3)
            if (colWidth <= 0) colWidth = (110 * context.resources.displayMetrics.density).toInt()
            val itemHeight = (colWidth * 1.35f).toInt()
            var lp = view.layoutParams
            if (lp == null) {
                lp = AbsListView.LayoutParams(colWidth, itemHeight)
            } else {
                lp.width = colWidth
                lp.height = itemHeight
            }
            view.layoutParams = lp
        } catch (_: Exception) {}

        if (btnHide != null) {
            if (item.isLocal && item.type != MediaItem.TYPE_FOLDER) {
                btnHide.visibility = View.VISIBLE
                btnHide.setOnClickListener {
                    hideListener?.onHide(item)
                }
            } else {
                btnHide.visibility = View.GONE
            }
        }

        imgThumb.setImageBitmap(null)
        imgThumb.tag = cacheKey

        playBadge.visibility = if (item.type == MediaItem.TYPE_VIDEO) View.VISIBLE else View.GONE
        tvM3uBadge?.visibility = if (item.isM3u) View.VISIBLE else View.GONE

        if (!item.title.isNullOrEmpty()) {
            tvTitle.text = item.title
            tvTitle.visibility = View.VISIBLE
            tvTitle.isSelected = true
            tvTitle.isSingleLine = true
            tvTitle.ellipsize = TextUtils.TruncateAt.MARQUEE
            tvTitle.marqueeRepeatLimit = -1
            tvTitle.isHorizontallyScrolling = true
        } else {
            tvTitle.text = ""
            tvTitle.visibility = View.GONE
        }

        val cached = memoryCache.get(cacheKey)
        if (cached != null) {
            imgThumb.setImageBitmap(cached)
        } else if (item.isLocal) {
            loadLocalThumbnail(item, imgThumb, cacheKey)
        } else if (dataSaver) {
            imgThumb.setImageResource(android.R.drawable.ic_menu_gallery)
        } else {
            if (futures.containsKey(cacheKey)) {
                view.setOnClickListener {
                    listener?.onThumbnailClick(item)
                }
                return view
            }

            val urlToLoad = when {
                item.type == MediaItem.TYPE_IMAGE -> item.uri.toString()
                !item.thumbUrl.isNullOrEmpty() -> item.thumbUrl
                else -> null
            }

            if (urlToLoad != null) {
                val future = threadPool.submit {
                    val bmp = loadRemote(urlToLoad)
                    if (bmp != null) memoryCache.put(cacheKey, bmp)
                    mainHandler.post {
                        futures.remove(cacheKey)
                        if (cacheKey == imgThumb.tag && bmp != null) {
                            imgThumb.setImageBitmap(bmp)
                        }
                    }
                }
                futures[cacheKey] = future
            }
        }

        view.setOnClickListener {
            listener?.onThumbnailClick(item)
        }

        return view
    }

    private fun loadLocalThumbnail(item: MediaItem, imgThumb: ImageView, cacheKey: String) {
        if (futures.containsKey(cacheKey)) return
        val future = threadPool.submit {
            var bitmap: Bitmap? = null
            try {
                val id = item.uri.lastPathSegment?.toLongOrNull() ?: 0L
                if (item.type == MediaItem.TYPE_VIDEO) {
                    bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver, id,
                        MediaStore.Video.Thumbnails.MINI_KIND, null
                    )
                } else if (item.type == MediaItem.TYPE_IMAGE) {
                    bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                        context.contentResolver, id,
                        MediaStore.Images.Thumbnails.MINI_KIND, null
                    )
                }
            } catch (_: Exception) {}
            if (bitmap != null) memoryCache.put(cacheKey, bitmap)
            val fb = bitmap
            mainHandler.post {
                futures.remove(cacheKey)
                if (cacheKey == imgThumb.tag && fb != null) {
                    imgThumb.setImageBitmap(fb)
                } else if (item.type == 4) {
                    imgThumb.setImageResource(android.R.drawable.ic_media_play)
                }
            }
        }
        futures[cacheKey] = future
    }

    private fun loadRemote(urlStr: String): Bitmap? {
        return try {
            val inputStream: InputStream? = if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                conn.inputStream
            } else {
                context.contentResolver.openInputStream(Uri.parse(urlStr))
            }
            val bytes = inputStream?.use { readAllBytes(it) } ?: return null
            val opts = BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        } catch (_: Exception) {
            null
        }
    }

    private fun readAllBytes(input: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(8192)
        var nRead: Int
        while (input.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        return buffer.toByteArray()
    }

    companion object {
        private val threadPool: ExecutorService = Executors.newFixedThreadPool(6)
        private val futures = ConcurrentHashMap<String, Future<*>>()

        private val MAX_CACHE_KB = (Runtime.getRuntime().maxMemory() / 1024 / 4).toInt()
        private val memoryCache = object : LruCache<String, Bitmap>(MAX_CACHE_KB) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
}
