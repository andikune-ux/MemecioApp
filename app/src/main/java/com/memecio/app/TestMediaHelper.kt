package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent

object TestMediaHelper {

    @JvmStatic
    fun showChoice(context: Context) {
        val options = arrayOf(
            "Video lokal (dari galeri HP)",
            "Video sample online (MP4)"
        )

        AlertDialog.Builder(context)
            .setTitle("Test Media Player")
            .setItems(options) { _: DialogInterface, which: Int ->
                if (which == 0) {
                    // Buka player biasa, user pilih sendiri
                    val i = Intent(context, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)
                } else {
                    // Video sample online
                    val url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    val list = ArrayList<Any>()
                    list.add(url)
                    PlaylistHolder.set(list)
                    val i = Intent(context, VideoPlayerActivity::class.java)
                    i.putExtra("index", 0)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)
                }
            }
            .show()
    }
}
