package com.memecio.app

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.ui.PlayerView

class MultiviewActivity : Activity() {

    private val players = arrayOfNulls<ExoPlayer>(4)
    private val views = arrayOfNulls<PlayerView>(4)
    private val slots = arrayOfNulls<FrameLayout>(4)
    private val titles = arrayOfNulls<TextView>(4)
    private val badges = arrayOfNulls<ImageView>(4)
    private val closes = arrayOfNulls<ImageButton>(4)
    private val images = arrayOfNulls<ImageView>(4)
    private val playPauseBtns = arrayOfNulls<ImageButton>(4)
    private val muteBtns = arrayOfNulls<ImageButton>(4)
    private val fullscreenBtns = arrayOfNulls<ImageButton>(4)
    private val replayBtns = arrayOfNulls<ImageButton>(4)
    private val types = arrayOfNulls<String>(4)
    private val urls = arrayOfNulls<String>(4)
    private val muted = BooleanArray(4)
    private val controlsVisible = BooleanArray(4)
    private var activeAudioSlot = 0
    private var isBigLayout = false
    private val hideHandler = Handler()
    private val hideAll = Runnable {
        for (i in 0 until 4) hideControlsFor(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiview)

        views[0] = findViewById(R.id.player1)
        views[1] = findViewById(R.id.player2)
        views[2] = findViewById(R.id.player3)
        views[3] = findViewById(R.id.player4)
        images[0] = findViewById(R.id.image1)
        images[1] = findViewById(R.id.image2)
        images[2] = findViewById(R.id.image3)
        images[3] = findViewById(R.id.image4)
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
        playPauseBtns[0] = findViewById(R.id.playPause1)
        playPauseBtns[1] = findViewById(R.id.playPause2)
        playPauseBtns[2] = findViewById(R.id.playPause3)
        playPauseBtns[3] = findViewById(R.id.playPause4)
        muteBtns[0] = findViewById(R.id.mute1)
        muteBtns[1] = findViewById(R.id.mute2)
        muteBtns[2] = findViewById(R.id.mute3)
        muteBtns[3] = findViewById(R.id.mute4)
        fullscreenBtns[0] = findViewById(R.id.fullscreen1)
        fullscreenBtns[1] = findViewById(R.id.fullscreen2)
        fullscreenBtns[2] = findViewById(R.id.fullscreen3)
        fullscreenBtns[3] = findViewById(R.id.fullscreen4)
        replayBtns[0] = findViewById(R.id.replay1)
        replayBtns[1] = findViewById(R.id.replay2)
        replayBtns[2] = findViewById(R.id.replay3)
        replayBtns[3] = findViewById(R.id.replay4)

        for (i in 0 until 4) {
            val idx = i
            slots[idx]?.setOnClickListener { onSlotClick(idx) }
            slots[idx]?.setOnLongClickListener { onSlotLongClick(idx) }
            closes[idx]?.setOnClickListener { closeSlot(idx) }
            playPauseBtns[idx]?.setOnClickListener { togglePlayPause(idx) }
            muteBtns[idx]?.setOnClickListener { toggleMute(idx) }
            fullscreenBtns[idx]?.setOnClickListener { openFullscreen(idx) }
            replayBtns[idx]?.setOnClickListener { replaySlot(idx) }
            slots[idx]?.setOnDragListener { _, event -> onDrag(idx, event) }
        }

        findViewById<View>(R.id.btnCloseMultiview).setOnClickListener { finish() }
        findViewById<View>(R.id.btnToggleLayout).setOnClickListener { toggleLayout() }

        loadFromHolder()
    }

    private fun loadFromHolder() {
        for (i in 0 until 4) {
            val url = MultiviewPickHolder.getUrl(i)
            val type = MultiviewPickHolder.getType(i)
            if (url != null && type != null) {
                if (type == "video") {
                    playVideo(i, url)
                } else {
                    showImage(i, url)
                }
            }
        }
    }

    private fun onSlotClick(idx: Int) {
        if (urls[idx] == null) {
            pickForSlot(idx)
        } else {
            if (activeAudioSlot != idx) {
                activeAudioSlot = idx
                applyAudioFocus()
            }
            toggleControls(idx)
        }
    }

    private fun onSlotLongClick(idx: Int): Boolean {
        val url = urls[idx] ?: return false
        val data = ClipData.newPlainText("slot", idx.toString())
        slots[idx]?.startDragAndDrop(data, View.DragShadowBuilder(slots[idx]), idx, 0)
        return true
    }

    private fun onDrag(targetIdx: Int, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val fromIdx = event.localState as? Int ?: return false
                if (fromIdx != targetIdx) swapSlots(fromIdx, targetIdx)
                return true
            }
        }
        return true
    }

    private fun swapSlots(from: Int, to: Int) {
        val tmpUrl = urls[from]
        val tmpType = types[from]
        val tmpTitle = titles[from]?.text?.toString()
        urls[from] = urls[to]
        types[from] = types[to]
        titles[from]?.text = titles[to]?.text
        urls[to] = tmpUrl
        types[to] = tmpType
        titles[to]?.text = tmpTitle

        releaseSlot(from)
        releaseSlot(to)

        if (urls[from] != null) {
            if (types[from] == "video") playVideo(from, urls[from]!!) else showImage(from, urls[from]!!)
        }
        if (urls[to] != null) {
            if (types[to] == "video") playVideo(to, urls[to]!!) else showImage(to, urls[to]!!)
        }
        MultiviewPickHolder.swap(from, to)
    }

    private fun pickForSlot(idx: Int) {
        MultiviewPickHolder.setPickSlot(idx)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun playVideo(idx: Int, url: String) {
        releaseSlot(idx)
        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10_000, 30_000, 1000, 2000)
            .setTargetBufferBytes(20 * 1024 * 1024)
            .build()
        val player = ExoPlayer.Builder(this).setLoadControl(loadControl).build()
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player.setTrackSelectionParameters(
            TrackSelectionParameters.Builder()
                .setMaxVideoSizeSd()
                .build()
        )
        player.prepare()
        player.play()
        player.volume = if (muted[idx]) 0f else 1f
        players[idx] = player
        views[idx]?.player = player
        views[idx]?.visibility = View.VISIBLE
        images[idx]?.visibility = View.GONE
        urls[idx] = url
        types[idx] = "video"
        titles[idx]?.text = url.substringAfterLast('/').take(30)
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    replayBtns[idx]?.visibility = View.VISIBLE
                }
            }
        })
        applyAudioFocus()
        showControlsFor(idx)
    }

    private fun showImage(idx: Int, url: String) {
        releaseSlot(idx)
        views[idx]?.visibility = View.GONE
        images[idx]?.visibility = View.VISIBLE
        try {
            val bitmap = android.graphics.BitmapFactory.decodeStream(
                java.net.URL(url).openStream()
            )
            images[idx]?.setImageBitmap(bitmap)
        } catch (ignored: Exception) {
            images[idx]?.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        urls[idx] = url
        types[idx] = "image"
        titles[idx]?.text = url.substringAfterLast('/').take(30)
    }

    private fun releaseSlot(idx: Int) {
        players[idx]?.release()
        players[idx] = null
        views[idx]?.player = null
        urls[idx] = null
        types[idx] = null
        replayBtns[idx]?.visibility = View.GONE
        if (activeAudioSlot == idx) {
            activeAudioSlot = -1
        }
    }

    private fun closeSlot(idx: Int) {
        releaseSlot(idx)
        views[idx]?.visibility = View.GONE
        images[idx]?.visibility = View.GONE
        titles[idx]?.text = "Slot ${idx + 1}"
        MultiviewPickHolder.clear(idx)
        applyAudioFocus()
    }

    private fun togglePlayPause(idx: Int) {
        players[idx]?.let {
            if (it.isPlaying) it.pause() else it.play()
            playPauseBtns[idx]?.setImageResource(
                if (it.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
        }
        showControlsFor(idx)
    }

    private fun toggleMute(idx: Int) {
        muted[idx] = !muted[idx]
        players[idx]?.volume = if (muted[idx]) 0f else 1f
        muteBtns[idx]?.setImageResource(
            if (muted[idx]) android.R.drawable.ic_lock_silent_mode else android.R.drawable.ic_lock_silent_mode_off
        )
        if (!muted[idx]) {
            activeAudioSlot = idx
            applyAudioFocus()
        }
        showControlsFor(idx)
    }

    private fun openFullscreen(idx: Int) {
        val url = urls[idx] ?: return
        val intent = Intent(this, VideoPlayerActivity::class.java)
        intent.putExtra("index", 0)
        val list = ArrayList<Any>()
        list.add(url)
        PlaylistHolder.set(list)
        startActivity(intent)
    }

    private fun replaySlot(idx: Int) {
        players[idx]?.seekTo(0)
        players[idx]?.play()
        replayBtns[idx]?.visibility = View.GONE
        showControlsFor(idx)
    }

    private fun applyAudioFocus() {
        for (i in 0 until 4) {
            val player = players[i] ?: continue
            player.volume = if (i == activeAudioSlot && !muted[i]) 1f else 0f
        }
        for (i in 0 until 4) {
            badges[i]?.visibility = if (i == activeAudioSlot) View.VISIBLE else View.GONE
        }
    }

    private fun toggleLayout() {
        isBigLayout = !isBigLayout
        val lp = slots[0]?.layoutParams as? LinearLayout.LayoutParams
        if (isBigLayout) {
            // Layout 1 besar + 3 kecil (placeholder)
        } else {
            // Layout 2x2
        }
        Toast.makeText(this, if (isBigLayout) "Layout besar" else "Layout grid", Toast.LENGTH_SHORT).show()
    }

    private fun toggleControls(idx: Int) {
        controlsVisible[idx] = !controlsVisible[idx]
        if (controlsVisible[idx]) showControlsFor(idx) else hideControlsFor(idx)
    }

    private fun showControlsFor(idx: Int) {
        controlsVisible[idx] = true
        playPauseBtns[idx]?.visibility = View.VISIBLE
        muteBtns[idx]?.visibility = View.VISIBLE
        fullscreenBtns[idx]?.visibility = View.VISIBLE
        closes[idx]?.visibility = View.VISIBLE
        hideHandler.removeCallbacks(hideAll)
        hideHandler.postDelayed(hideAll, 3000)
    }

    private fun hideControlsFor(idx: Int) {
        controlsVisible[idx] = false
        playPauseBtns[idx]?.visibility = View.GONE
        muteBtns[idx]?.visibility = View.GONE
        fullscreenBtns[idx]?.visibility = View.GONE
        closes[idx]?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        for (i in 0 until 4) {
            players[i]?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (i in 0 until 4) {
            players[i]?.release()
        }
        hideHandler.removeCallbacks(hideAll)
    }
}
