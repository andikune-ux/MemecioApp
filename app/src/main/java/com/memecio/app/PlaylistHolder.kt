package com.memecio.app

import java.util.ArrayList

object PlaylistHolder {

    private var playlist: ArrayList<String> = ArrayList()

    @JvmStatic
    fun set(list: List<*>) {
        val strList = ArrayList<String>()
        for (item in list) {
            strList.add(item?.toString() ?: "")
        }
        playlist = strList
    }

    @JvmStatic
    fun get(): ArrayList<String> {
        return playlist
    }
}
