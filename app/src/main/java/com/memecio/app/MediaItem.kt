package com.memecio.app

import android.net.Uri

class MediaItem(
    @JvmField var uri: Uri,
    @JvmField var type: Int
) {
    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_AUDIO = 3
    }

    @JvmField var isLocal: Boolean = true
    @JvmField var isM3u: Boolean = false
    @JvmField var thumbUrl: String? = null
    @JvmField var title: String? = null
    @JvmField var sourceTitle: String? = null
    @JvmField var folderPath: String? = null
    @JvmField var dateAdded: Long = 0L

    override fun toString(): String {
        return title ?: uri.lastPathSegment ?: uri.toString()
    }
}
