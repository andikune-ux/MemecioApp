package com.memecio.app

import java.util.ArrayList

object PlaylistHolder {
    private var playlist: ArrayList<String>? = null

    @JvmStatic
    fun set(list: ArrayList<String>) {
        playlist = list
    }

    @JvmStatic
    fun get(): ArrayList<String>? = playlist
}
