package com.memecio.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent

object TestMediaHelper {
    fun showChoice(context: Context) {
        val samples = arrayOf(
            "Big Buck Bunny (HLS Stream)",
            "Tears of Steel (MP4)",
            "Sintel Trailer (HLS Stream)"
        )
        val urls = arrayOf(
            "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
        )

        AlertDialog.Builder(context)
            .setTitle("Test Media Player (444)")
            .setItems(samples) { _, which ->
                val chosenUrl = urls[which]
                val playlist = arrayListOf(chosenUrl)
                PlaylistHolder.set(playlist)
                val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                    putStringArrayListExtra("playlist", playlist)
                    putExtra("index", 0)
                }
                context.startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
