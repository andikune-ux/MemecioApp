package com.memecio.app

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL

class ThumbnailAdapter(
    private val context: Context,
    private val items: List<MediaItem>,
    private val listener: OnThumbnailClickListener?
) : BaseAdapter() {

    interface OnThumbnailClickListener {
        fun onThumbnailClick(mediaItem: MediaItem)
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = (maxMemory / 8).coerceAtLeast(1024)
        private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false)
        val imgThumb = view.findViewById<ImageView>(R.id.imgThumb)
        val playBadge = view.findViewById<TextView>(R.id.tvPlayBadge)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvM3uBadge = view.findViewById<TextView>(R.id.tvM3uBadge)
        val item = items[position]

        val cacheKey = item.uri.toString()
        imgThumb.setImageBitmap(null)
        imgThumb.tag = cacheKey

        playBadge?.visibility = if (item.type == MediaItem.TYPE_VIDEO) View.VISIBLE else View.GONE
        tvM3uBadge?.visibility = if (item.isM3u) View.VISIBLE else View.GONE

        if (!item.title.isNullOrEmpty()) {
            tvTitle?.text = item.title
            tvTitle?.visibility = View.VISIBLE
        } else {
            tvTitle?.text = ""
            tvTitle?.visibility = View.GONE
        }

        val cached = memoryCache.get(cacheKey)
        if (cached != null) {
            imgThumb.setImageBitmap(cached)
        } else if (item.isLocal) {
            loadLocalThumbnail(item, imgThumb, cacheKey)
        } else if (item.type == MediaItem.TYPE_IMAGE) {
            loadRemoteThumbUrl(item.uri.toString(), imgThumb, cacheKey)
        } else if (!item.thumbUrl.isNullOrEmpty()) {
            loadRemoteThumbUrl(item.thumbUrl!!, imgThumb, cacheKey)
        }

        view.setOnClickListener {
            listener?.onThumbnailClick(item)
        }

        return view
    }

    private fun loadLocalThumbnail(item: MediaItem, imgThumb: ImageView, cacheKey: String) {
        Thread {
            var bitmap: Bitmap? = null
            try {
                val id = item.uri.lastPathSegment?.toLongOrNull()
                if (id != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val size = Size(200, 200)
                        bitmap = if (item.type == MediaItem.TYPE_VIDEO) {
                            val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                            context.contentResolver.loadThumbnail(contentUri, size, null)
                        } else {
                            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                            context.contentResolver.loadThumbnail(contentUri, size, null)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        bitmap = if (item.type == MediaItem.TYPE_VIDEO) {
                            MediaStore.Video.Thumbnails.getThumbnail(context.contentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, null)
                        } else {
                            MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null)
                        }
                    }
                }
            } catch (ignored: Exception) {}
            applyResult(bitmap, imgThumb, cacheKey)
        }.start()
    }

    private fun loadRemoteThumbUrl(urlStr: String, imgThumb: ImageView, cacheKey: String) {
        Thread {
            var bitmap: Bitmap? = null
            try {
                if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    conn.inputStream.use { ins ->
                        bitmap = BitmapFactory.decodeStream(ins)
                    }
                } else {
                    val uri = Uri.parse(urlStr)
                    context.contentResolver.openInputStream(uri)?.use { ins ->
                        bitmap = BitmapFactory.decodeStream(ins)
                    }
                }
            } catch (ignored: Exception) {}
            applyResult(bitmap, imgThumb, cacheKey)
        }.start()
    }

    private fun applyResult(bitmap: Bitmap?, imgThumb: ImageView, cacheKey: String) {
        if (bitmap != null) {
            memoryCache.put(cacheKey, bitmap)
        }
        mainHandler.post {
            if (cacheKey == imgThumb.tag && bitmap != null) {
                imgThumb.setImageBitmap(bitmap)
            }
        }
    }
}
