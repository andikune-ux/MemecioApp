package com.memecio.app

import java.util.ArrayList

object MixedPlaylistHolder {

    private var items: MutableList<MediaItem> = ArrayList()
    private var currentIndex = 0

    @JvmStatic
    fun set(list: List<MediaItem>, index: Int) {
        items = ArrayList(list)
        currentIndex = index
    }

    @JvmStatic
    fun getItems(): MutableList<MediaItem> = items

    @JvmStatic
    fun getCurrentIndex(): Int = currentIndex

    @JvmStatic
    fun setCurrentIndex(i: Int) {
        currentIndex = i
    }

    @JvmStatic
    fun getCurrent(): MediaItem? {
        if (items.isEmpty() || currentIndex < 0 || currentIndex >= items.size) return null
        return items[currentIndex]
    }

    @JvmStatic
    fun isActive(): Boolean = items.isNotEmpty()

    @JvmStatic
    fun clear() {
        items = ArrayList()
        currentIndex = 0
    }
}
