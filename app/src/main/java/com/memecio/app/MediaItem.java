package com.memecio.app

import android.net.Uri

class MediaItem(var uri: Uri, var type: Int) {

    var isLocal: Boolean = true
    var title: String? = null
    var thumbUrl: String? = null
    var isM3u: Boolean = false
    var dateAdded: Long = 0L
    var folderPath: String? = null
    var sourceTitle: String? = null
    var size: Long = 0L
    var duration: Long = 0L

    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_FOLDER = 3
        const val TYPE_AUDIO = 4
    }
}
