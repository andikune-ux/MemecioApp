package com.memecio.app

import java.util.ArrayList

object PlaylistHolder {

    private var playlist: ArrayList<Any> = ArrayList()

    @JvmStatic
    fun set(list: ArrayList<Any>) {
        playlist = list
    }

    @JvmStatic
    fun get(): ArrayList<Any> {
        return playlist
    }
}
