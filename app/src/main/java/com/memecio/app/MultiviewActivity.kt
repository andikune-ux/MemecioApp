package com.memecio.app

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MultiviewActivity : Activity() {

    private val players = arrayOfNulls<ExoPlayer>(4)
    private val views = arrayOfNulls<PlayerView>(4)
    private val slots = arrayOfNulls<FrameLayout>(4)
    private val titles = arrayOfNulls<TextView>(4)
    private val badges = arrayOfNulls<ImageView>(4)
    private val closes = arrayOfNulls<ImageButton>(4)
    private val urls = arrayOfNulls<String>(4)
    private var activeAudioSlot = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiview)

        views[0] = findViewById(R.id.player1)
        views[1] = findViewById(R.id.player2)
        views[2] = findViewById(R.id.player3)
        views[3] = findViewById(R.id.player4)

        slots[0] = findViewById(R.id.slot1)
        slots[1] = findViewById(R.id.slot2)
        slots[2] = findViewById(R.id.slot3)
        slots[3] = findViewById(R.id.slot4)

        titles[0] = findViewById(R.id.title1)
        titles[1] = findViewById(R.id.title2)
        titles[2] = findViewById(R.id.title3)
        titles[3] = findViewById(R.id.title4)

        badges[0] = findViewById(R.id.audioBadge1)
        badges[1] = findViewById(R.id.audioBadge2)
        badges[2] = findViewById(R.id.audioBadge3)
        badges[3] = findViewById(R.id.audioBadge4)

        closes[0] = findViewById(R.id.close1)
        closes[1] = findViewById(R.id.close2)
        closes[2] = findViewById(R.id.close3)
        closes[3] = findViewById(R.id.close4)

        for (i in 0 until 4) {
            val idx = i
            val player = ExoPlayer.Builder(this).build()
            players[idx] = player
            views[idx]?.player = player
            player.volume = if (idx == 0) 1f else 0f
            player.playWhenReady = true

            slots[idx]?.setOnClickListener {
                setActiveAudio(idx)
            }

            closes[idx]?.setOnClickListener {
                try {
                    player.stop()
                    player.clearMediaItems()
                } catch (ignored: Exception) {}
                urls[idx] = null
                titles[idx]?.text = ""
                badges[idx]?.visibility = View.GONE
            }
        }

        val playlist = PlaylistHolder.get()
        if (playlist != null && playlist.isNotEmpty()) {
            for (i in 0 until 4) {
                if (i < playlist.size) {
                    loadSlot(i, playlist[i])
                }
            }
        }

        setActiveAudio(0)
        updateBadges()

        findViewById<View>(R.id.btnCloseMulti)?.setOnClickListener {
            finish()
        }
    }

    private fun loadSlot(idx: Int, url: String) {
        try {
            urls[idx] = url
            players[idx]?.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            players[idx]?.prepare()
            val name = url.substring(url.lastIndexOf('/') + 1)
            titles[idx]?.text = if (name.length > 30) name.substring(0, 27) + "..." else name
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat slot ${idx + 1}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setActiveAudio(idx: Int) {
        activeAudioSlot = idx
        for (i in 0 until 4) {
            players[i]?.volume = if (i == idx) 1f else 0f
        }
        updateBadges()
    }

    private fun updateBadges() {
        for (i in 0 until 4) {
            badges[i]?.visibility = if (i == activeAudioSlot && urls[i] != null) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (i in 0 until 4) {
            try {
                players[i]?.release()
                players[i] = null
            } catch (ignored: Exception) {}
        }
    }
}
