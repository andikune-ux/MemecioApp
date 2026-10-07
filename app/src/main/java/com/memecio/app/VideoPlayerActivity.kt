package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.util.Rational
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.GestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TabHost
import android.widget.TextView
import android.widget.Toast
import android.animation.ObjectAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.ui.PlayerView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class VideoPlayerActivity : Activity() {

    private var btnCloseVideo: View? = null
    private var btnMute: ImageButton? = null
    private var btnPlayPause: ImageButton? = null
    private var btnPlayPauseCenter: ImageButton? = null
    private var centerControls: View? = null
    private var controlBar: View? = null
    private var playlist: ArrayList<String>? = null
    private var seekBar: SeekBar? = null
    private var topBar: View? = null
    private var tvSpeed: TextView? = null
    private var tvTimecode: TextView? = null
    private var playerView: PlayerView? = null
    private var exoPlayer: ExoPlayer? = null
    private var handler = Handler()
    private var hideHandler = Handler()
    private var tvJamVideoPlayer: TextView? = null
    private var clockRunnable: Runnable? = null
    private var isPlaying = true
    private var isMuted = false
    private var controlsVisible = true
    private var currentSpeed = 1.0f
    private var currentIndex = 0
    private var isSeekBarTracking = false
    private var prefPosisi: SharedPreferences? = null
    private var isLocked = false
    private var previewRetriever: MediaMetadataRetriever? = null
    private var startY = 0f
    private var touchZone = 1
    private var adjustingVertical = false
    private var speedBoostActive = false
    private var longPressCandidate = false
    private var gestureIndicator: View? = null
    private var gestureIcon: ImageView? = null
    private var gestureText: TextView? = null
    private var gestureBar: ProgressBar? = null
    private var audioManager: AudioManager? = null
    private var startBrightness = 0.5f
    private var startVolume = 0
    private var maxVolume = 0
    private var gestureHandler = Handler()
    private var speedBoostRunnable: Runnable? = null
    private var previewExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var previewHandler = Handler()
    private var previewRequestId = AtomicInteger(0)
    private var lastPreviewBitmap: Bitmap? = null
    private var previewContainer: View? = null
    private var previewImage: ImageView? = null
    private var previewTime: TextView? = null
    private var autoSkipIntroMs = 0L
    private var hideLockIndicatorRunnable: Runnable? = null

    // Drawer
    private var drawerPlaylist: View? = null
    private var listVideoTitles: ListView? = null
    private var tvPlaylistName: TextView? = null
    private var drawerOpen = false

    // Detail overlay
    private var detailOverlay: View? = null
    private var isHdrVideo = false
    private var tvHdrBadge: TextView? = null
    private var zoomScale = 1f
    private var zoomPanX = 0f
    private var zoomPanY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var scaleDetector: ScaleGestureDetector? = null
    private var doubleTapDetector: GestureDetector? = null
    private var detailPoster: ImageView? = null
    private var detailPosterBg: ImageView? = null
    private var detailTitle: TextView? = null
    private var detailDescription: TextView? = null
    private var detailShownOnce = false
    private var isFullscreen = false

    // Sleep timer
    private val videoSleepHandler = Handler(Looper.getMainLooper())
    private var videoSleepRunnable: Runnable? = null

    private val updateSeekRunnable: Runnable = object : Runnable {
        override fun run() {
            if (exoPlayer != null && !isSeekBarTracking) {
                val current = exoPlayer!!.currentPosition
                val duration = exoPlayer!!.duration
                if (duration > 0) {
                    seekBar?.max = duration.toInt()
                    seekBar?.progress = current.toInt()
                    tvTimecode?.text = "${formatTime(current.toInt())} / ${formatTime(duration.toInt())}"
                }
            }
            handler.postDelayed(this, 500L)
        }
    }

    private val hideControlsRunnable = Runnable { hideControls() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(128)
        SoundHelper.init(this)
        setContentView(R.layout.activity_video_player)

        setupDetailOverlay()

        prefPosisi = getSharedPreferences("memecio_playback", MODE_PRIVATE)
        val prefSet = getSharedPreferences("memecio_settings", MODE_PRIVATE)
        autoSkipIntroMs = prefSet.getLong("auto_skip_intro", 0)

        playerView = findViewById(R.id.videoView)
        playerView?.useController = false
        seekBar = findViewById(R.id.seekBar)
        tvTimecode = findViewById(R.id.tvTimecode)
        tvSpeed = findViewById(R.id.tvSpeed)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPlayPauseCenter = findViewById(R.id.btnPlayPauseCenter)
        btnMute = findViewById(R.id.btnMute)
        btnCloseVideo = findViewById(R.id.btnCloseVideo)
        controlBar = findViewById(R.id.controlBar)
        centerControls = findViewById(R.id.centerControls)
        topBar = findViewById(R.id.topBar)
        drawerPlaylist = findViewById(R.id.drawerPlaylist)
        listVideoTitles = findViewById(R.id.listVideoTitles)
        tvPlaylistName = findViewById(R.id.tvPlaylistName)

        val btnFullscreen = findViewById<View>(R.id.btnFullscreen)
        previewContainer = findViewById(R.id.previewContainer)
        gestureIndicator = findViewById(R.id.gestureIndicator)
        gestureIcon = findViewById(R.id.gestureIcon)
        gestureText = findViewById(R.id.gestureText)
        gestureBar = findViewById(R.id.gestureBar)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.let {
            maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            startVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
        }
        previewImage = findViewById(R.id.previewImage)
        previewTime = findViewById(R.id.previewTime)
        val btnRewind = findViewById<View>(R.id.btnRewind)
        val btnForward = findViewById<View>(R.id.btnForward)
        val btnPrevious = findViewById<View>(R.id.btnPrevious)
        val btnNext = findViewById<View>(R.id.btnNext)
        val btnSettings = findViewById<View>(R.id.btnSettings)
        val btnLock = findViewById<View>(R.id.btnLock)
        val btnPlaylist = findViewById<View>(R.id.btnPlaylist)
        val btnPlaylistPrev = findViewById<View>(R.id.btnPlaylistPrev)
        val btnPlaylistNext = findViewById<View>(R.id.btnPlaylistNext)

        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(15000, 120000, 3000, 8000)
            .setTargetBufferBytes(50 * 1024 * 1024)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        exoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()

        try {
            val trackParams = exoPlayer!!.trackSelectionParameters
                .buildUpon()
                .setMaxVideoSize(1920, 1080)
                .build()
            exoPlayer!!.trackSelectionParameters = trackParams
        } catch (ignored: Exception) {}
        playerView?.player = exoPlayer

        exoPlayer?.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                if (videoSize.width <= 0 || videoSize.height <= 0) return
                val target = if (videoSize.width > videoSize.height)
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                if (requestedOrientation != target) {
                    requestedOrientation = target
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                isHdrVideo = false
                try {
                    for (group in tracks.groups) {
                        if (group.type == C.TRACK_TYPE_VIDEO) {
                            val fmt: Format = group.mediaTrackGroup.getFormat(0)
                            if (fmt.colorInfo != null) {
                                val transfer = fmt.colorInfo!!.colorTransfer
                                if (transfer == C.COLOR_TRANSFER_ST2084 || transfer == C.COLOR_TRANSFER_HLG) {
                                    isHdrVideo = true
                                }
                            }
                        }
                    }
                } catch (ignored: Exception) {}
                updateHdrBadge()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    seekBar?.max = Math.max(exoPlayer!!.duration, 0).toInt()
                    applySpeed()
                    handler.removeCallbacks(updateSeekRunnable)
                    handler.post(updateSeekRunnable)
                    tvJamVideoPlayer = findViewById(R.id.tvJamVideoPlayer)
                    if (tvJamVideoPlayer != null) {
                        clockRunnable = object : Runnable {
                            override fun run() {
                                val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                tvJamVideoPlayer?.text = fmt.format(Date())
                                handler.postDelayed(this, 1000)
                            }
                        }
                        handler.post(clockRunnable!!)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@VideoPlayerActivity, "Gagal memutar video", Toast.LENGTH_SHORT).show()
            }
        })

        this.playlist = PlaylistHolder.get()
        this.currentIndex = intent.getIntExtra("index", 0)
        if (playlist == null || playlist!!.isEmpty()) {
            playlist = ArrayList()
            val fromIntent = intent.getStringArrayListExtra("playlist")
            if (fromIntent != null && fromIntent.isNotEmpty()) playlist = fromIntent
            val single = intent.getStringExtra("uri")
            if (single != null) playlist!!.add(single)
            if (playlist!!.isEmpty()) currentIndex = 0
        }

        loadVideo(currentIndex)

        if (savedInstanceState == null) {
            try {
                detailOverlay?.postDelayed({ showDetailOverlayIfFirst() }, 400)
            } catch (ignored: Exception) {}
        }
        scheduleAutoHide()

        val rootTouchListener = object : View.OnTouchListener {
            var startX = 0f
            var startTime = 0L
            var isLandscape = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (isLocked) return true
                if (handleZoomPan(event)) return true
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        startY = event.y
                        startTime = System.currentTimeMillis()
                        isLandscape = resources.configuration.orientation ==
                                Configuration.ORIENTATION_LANDSCAPE
                        if (isLandscape && v.width > 0) {
                            val ratio = event.x / v.width
                            touchZone = when {
                                ratio < 0.33f -> 0
                                ratio > 0.67f -> 2
                                else -> 1
                            }
                        } else {
                            touchZone = 1
                        }
                        adjustingVertical = false
                        speedBoostActive = false
                        longPressCandidate = (isLandscape && touchZone == 2)
                        if (longPressCandidate) {
                            if (speedBoostRunnable == null) {
                                speedBoostRunnable = Runnable {
                                    if (!isLocked) {
                                        speedBoostActive = true
                                        try {
                                            exoPlayer?.playbackParameters = PlaybackParameters(3.0f)
                                        } catch (ignored: Exception) {}
                                        showGestureIndicator(R.drawable.ic_forward_w, "3x", 100)
                                    }
                                }
                            }
                            gestureHandler.postDelayed(speedBoostRunnable!!, 600)
                        }
                        startBrightness = window.attributes.screenBrightness
                        if (startBrightness < 0) startBrightness = 0.5f
                        audioManager?.let { startVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC) }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!isLandscape || touchZone == 1) return true
                        val dxMove = event.x - startX
                        val dyMove = event.y - startY
                        if (speedBoostActive) return true
                        if (!adjustingVertical && Math.abs(dyMove) > 30 &&
                            Math.abs(dyMove) > Math.abs(dxMove) * 1.3f
                        ) {
                            adjustingVertical = true
                            speedBoostRunnable?.let { gestureHandler.removeCallbacks(it) }
                            longPressCandidate = false
                        }
                        if (adjustingVertical) {
                            val deltaPx = startY - event.y
                            val screenH = resources.displayMetrics.heightPixels.toFloat()
                            val deltaRatio = deltaPx / screenH
                            if (touchZone == 0) {
                                val newB = Math.max(0.01f, Math.min(1f, startBrightness + deltaRatio))
                                val lp = window.attributes
                                lp.screenBrightness = newB
                                window.attributes = lp
                                showGestureIndicator(
                                    android.R.drawable.ic_menu_view,
                                    "${(newB * 100).toInt()}%",
                                    (newB * 100).toInt()
                                )
                            } else {
                                audioManager?.let { am ->
                                    if (maxVolume > 0) {
                                        val deltaVol = (deltaRatio * maxVolume).toInt()
                                        val newV = Math.max(0, Math.min(maxVolume, startVolume + deltaVol))
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, newV, 0)
                                        val pct = if (maxVolume > 0) (newV * 100f / maxVolume).toInt() else 0
                                        val icon = if (newV == 0) R.drawable.ic_mute_w else R.drawable.ic_unmute_w
                                        showGestureIndicator(icon, "$pct%", pct)
                                    }
                                }
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        speedBoostRunnable?.let { gestureHandler.removeCallbacks(it) }
                        val dt = System.currentTimeMillis() - startTime
                        if (isLocked) {
                            showLockIndicatorTemporarily()
                            return true
                        }
                        if (speedBoostActive) {
                            speedBoostActive = false
                            try {
                                exoPlayer?.playbackParameters = PlaybackParameters(currentSpeed)
                            } catch (e: Exception) {
                                try {
                                    exoPlayer?.playbackParameters = PlaybackParameters(currentSpeed)
                                } catch (ignored: Exception) {}
                            }
                            hideGestureIndicator()
                            return true
                        }
                        if (adjustingVertical) {
                            adjustingVertical = false
                            hideGestureIndicator()
                            return true
                        }
                        val dx = event.x - startX
                        if (Math.abs(dx) > 150 && dt < 800) {
                            if (dx > 0) {
                                if (trySwitchToImage(-1)) return true
                                if (currentIndex > 0) {
                                    currentIndex--
                                    loadVideo(currentIndex)
                                } else {
                                    Toast.makeText(this@VideoPlayerActivity, "Video pertama", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (trySwitchToImage(1)) return true
                                if (currentIndex < playlist!!.size - 1) {
                                    currentIndex++
                                    loadVideo(currentIndex)
                                } else {
                                    Toast.makeText(this@VideoPlayerActivity, "Video terakhir", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else if (Math.abs(dx) < 15 && dt < 300) {
                            toggleControls()
                        }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        speedBoostRunnable?.let { gestureHandler.removeCallbacks(it) }
                        if (speedBoostActive) {
                            speedBoostActive = false
                            try {
                                exoPlayer?.playbackParameters = PlaybackParameters(currentSpeed)
                            } catch (ignored: Exception) {}
                        }
                        adjustingVertical = false
                        hideGestureIndicator()
                        return true
                    }
                }
                return false
            }
        }
        playerView?.setOnTouchListener(rootTouchListener)

        btnCloseVideo?.setOnClickListener { finish() }
        btnFullscreen?.setOnClickListener { toggleFullscreen() }
        btnLock?.setOnClickListener { toggleLock() }

        val togglePlay = View.OnClickListener {
            if (isPlaying) {
                exoPlayer?.pause()
                btnPlayPause?.setImageResource(R.drawable.ic_play_w)
                btnPlayPauseCenter?.setImageResource(R.drawable.ic_play_w)
            } else {
                exoPlayer?.play()
                btnPlayPause?.setImageResource(R.drawable.ic_pause_w)
                btnPlayPauseCenter?.setImageResource(R.drawable.ic_pause_w)
            }
            isPlaying = !isPlaying
            scheduleAutoHide()
        }
        btnPlayPause?.setOnClickListener(togglePlay)
        btnPlayPauseCenter?.setOnClickListener(togglePlay)

        btnRewind?.setOnClickListener {
            val pos = exoPlayer!!.currentPosition - 50000
            exoPlayer?.seekTo(Math.max(pos, 0))
            scheduleAutoHide()
        }
        btnForward?.setOnClickListener {
            val pos = exoPlayer!!.currentPosition + 10000
            exoPlayer?.seekTo(Math.min(pos, exoPlayer!!.duration))
            scheduleAutoHide()
        }
        btnPrevious?.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadVideo(currentIndex)
            } else {
                Toast.makeText(this, "Ini video pertama", Toast.LENGTH_SHORT).show()
            }
            scheduleAutoHide()
        }
        btnNext?.setOnClickListener {
            if (currentIndex < playlist!!.size - 1) {
                currentIndex++
                loadVideo(currentIndex)
            } else {
                Toast.makeText(this, "Ini video terakhir", Toast.LENGTH_SHORT).show()
            }
            scheduleAutoHide()
        }

        btnMute?.setOnClickListener {
            if (!isMuted) {
                exoPlayer?.volume = 0f
                btnMute?.setImageResource(R.drawable.ic_mute_w)
            } else {
                exoPlayer?.volume = 1f
                btnMute?.setImageResource(R.drawable.ic_unmute_w)
            }
            isMuted = !isMuted
            scheduleAutoHide()
        }

        btnSettings?.setOnClickListener {
            showPlayerMenu(it)
            scheduleAutoHide()
        }

        btnPlaylist?.setOnClickListener { toggleDrawer() }
        btnPlaylistPrev?.setOnClickListener {
            if (currentIndex > 0) { currentIndex--; loadVideo(currentIndex) }
        }
        btnPlaylistNext?.setOnClickListener {
            if (currentIndex < playlist!!.size - 1) { currentIndex++; loadVideo(currentIndex) }
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer?.seekTo(progress.toLong())
                    updatePreview(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeekBarTracking = true
                scheduleAutoHide()
                try {
                    seekBar?.thumb = getDrawable(R.drawable.bg_seekbar_thumb_drag)
                } catch (ignored: Exception) {}
                showPreviewStart()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeekBarTracking = false
                scheduleAutoHide()
                try {
                    seekBar?.thumb = getDrawable(R.drawable.bg_seekbar_thumb_normal)
                } catch (ignored: Exception) {}
                hidePreviewEnd()
            }
        })
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (ev.action == MotionEvent.ACTION_DOWN) {
        SoundHelper.click()
    }
    return super.dispatchTouchEvent(ev)
}

override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN) {
        val code = event.keyCode
        if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN ||
            code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT
        ) {
            SoundHelper.nav()
        } else if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) {
            SoundHelper.click()
        }
    }
    return super.dispatchKeyEvent(event)
}

private fun toggleDrawer() {
    val dp = drawerPlaylist ?: return
    if (drawerOpen) {
        val anim = ObjectAnimator.ofFloat(dp, "translationY", 0f, -1500f)
        anim.duration = 350
        anim.interpolator = DecelerateInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                dp.visibility = View.GONE
                drawerOpen = false
            }
        })
        anim.start()
    } else {
        dp.visibility = View.VISIBLE
        dp.translationY = -1500f
        val anim = ObjectAnimator.ofFloat(dp, "translationY", -1500f, 0f)
        anim.duration = 400
        anim.interpolator = OvershootInterpolator(1.1f)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                drawerOpen = true
            }
        })
        anim.start()
    }
}

private fun updateDrawerContent() {
    val lvt = listVideoTitles ?: return
    val tvn = tvPlaylistName ?: return
    var nama = "Playlist"
    if (playlist?.isNotEmpty() == true) {
        nama = "Video ${currentIndex + 1} / ${playlist!!.size}"
    }
    tvn.text = nama
    tvn.isSelected = true

    val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = super.getView(position, convertView, parent)
            val tv = v.findViewById<TextView>(android.R.id.text1)
            tv.setTextColor(0xFF1C1C1E.toInt())
            tv.textSize = 13f
            tv.setSingleLine(true)
            tv.ellipsize = TextUtils.TruncateAt.MARQUEE
            tv.marqueeRepeatLimit = -1
            tv.isSelected = true
            tv.setHorizontallyScrolling(true)
            return v
        }
    }
    for (i in playlist!!.indices) {
        adapter.add("  ${i + 1}.  ${getNamaVideo(playlist!![i])}")
    }
    lvt.adapter = adapter
    lvt.setSelection(currentIndex)
    lvt.setOnItemClickListener { _, _, position, _ ->
        currentIndex = position
        loadVideo(currentIndex)
        toggleDrawer()
    }
}

private fun getNamaVideo(url: String): String {
    return try {
        val uri = Uri.parse(url)
        var last = uri.lastPathSegment
        if (last == null || last.trim().isEmpty()) return url
        val dot = last.lastIndexOf('.')
        if (dot > 0) last = last.substring(0, dot)
        last = last.replace('_', ' ').replace('-', ' ').trim()
        if (last.isEmpty()) url else last
    } catch (e: Exception) {
        url
    }
}

private fun showLockIndicatorTemporarily() {
    val lockedIndicator = findViewById<View>(R.id.tvLockedIndicator) ?: return
    lockedIndicator.visibility = View.VISIBLE
    lockedIndicator.alpha = 1f
    if (hideLockIndicatorRunnable == null) {
        hideLockIndicatorRunnable = Runnable {
            if (isLocked && lockedIndicator != null) {
                ObjectAnimator.ofFloat(lockedIndicator, "alpha", 1f, 0f)
                    .setDuration(400).start()
            }
        }
    }
    hideLockIndicatorRunnable?.let { hideHandler.removeCallbacks(it) }
    hideHandler.postDelayed(hideLockIndicatorRunnable!!, 2000)
}

private fun toggleLock() {
    isLocked = !isLocked
    val lockedIndicator = findViewById<View>(R.id.tvLockedIndicator)
    if (isLocked) {
        hideControls()
        topBar?.visibility = View.GONE
        controlBar?.visibility = View.GONE
        centerControls?.visibility = View.GONE
        if (lockedIndicator != null) {
            lockedIndicator.setOnClickListener { toggleLock() }
            showLockIndicatorTemporarily()
        }
        Toast.makeText(this, "Layar terkunci", Toast.LENGTH_SHORT).show()
    } else {
        showControls()
        topBar?.visibility = View.VISIBLE
        if (lockedIndicator != null) {
            lockedIndicator.visibility = View.GONE
            lockedIndicator.setOnClickListener(null)
        }
        hideLockIndicatorRunnable?.let { hideHandler.removeCallbacks(it) }
        Toast.makeText(this, "Layar terbuka", Toast.LENGTH_SHORT).show()
    }
}

private fun simpanKeTontonNanti() {
    try {
        val url = playlist!![currentIndex]
        val judul = "Video ${currentIndex + 1}"
        WatchLaterStore.add(this, url, judul)
        Toast.makeText(this, "Ditambahkan ke Tonton Nanti", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
    }
}

private fun toggleControls() {
    if (controlsVisible) hideControls() else { showControls(); scheduleAutoHide() }
}

private fun showControls() {
    controlsVisible = true
    controlBar?.visibility = View.VISIBLE
    centerControls?.visibility = View.VISIBLE
    btnCloseVideo?.visibility = View.VISIBLE
    topBar?.visibility = View.VISIBLE
}

private fun hideControls() {
    controlsVisible = false
    controlBar?.visibility = View.GONE
    centerControls?.visibility = View.GONE
    btnCloseVideo?.visibility = View.GONE
    topBar?.visibility = View.GONE
}

private fun scheduleAutoHide() {
    hideHandler.removeCallbacks(hideControlsRunnable)
    hideHandler.postDelayed(hideControlsRunnable, 4000L)
}

private fun fixDriveUrl(url: String?): String? {
    if (url == null) return null
    try {
        if (url.contains("drive.google.com/uc?export=download") ||
            url.contains("drive.google.com/uc?id=")
        ) {
            var fileId: String? = null
            if (url.contains("id=")) {
                fileId = url.split("id=")[1].split("&")[0]
            }
            if (fileId != null) {
                return "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
            }
        }
    } catch (ignored: Exception) {}
    return url
}

private fun getMixedIndexForCurrentVideo(): Int {
    try {
        if (playlist == null || currentIndex < 0 || currentIndex >= playlist!!.size) return -1
        val currentUri = playlist!![currentIndex]
        val mixed = MixedPlaylistHolder.getItems()
        for (i in mixed.indices) {
            if (mixed[i].uri.toString() == currentUri) return i
        }
    } catch (ignored: Exception) {}
    return -1
}

private fun trySwitchToImage(direction: Int): Boolean {
    try {
        if (!MixedPlaylistHolder.isActive()) return false
        val mixedIdx = getMixedIndexForCurrentVideo()
        if (mixedIdx < 0) return false
        val mixed = MixedPlaylistHolder.getItems()
        val newIdx = mixedIdx + direction
        if (newIdx < 0 || newIdx >= mixed.size) return false
        val nextItem = mixed[newIdx]
        if (nextItem.type != com.memecio.app.MediaItem.TYPE_IMAGE) return false

        MixedPlaylistHolder.setCurrentIndex(newIdx)
        simpanPosisi()
        val img = Intent(this, PreviewImageActivity::class.java)
        img.putExtra("index", newIdx)
        startActivity(img)
        finish()
        return true
    } catch (ignored: Exception) {}
    return false
}

private fun setupDetailOverlay() {
    detailOverlay = findViewById(R.id.detailOverlay)
    detailPoster = findViewById(R.id.detailPoster)
    detailPosterBg = findViewById(R.id.detailPosterBg)
    detailTitle = findViewById(R.id.detailTitle)
    detailDescription = findViewById(R.id.detailDescription)

    if (detailOverlay == null) return

    val btnClose = findViewById<View>(R.id.detailBtnClose)
    val btnPlay = findViewById<View>(R.id.detailBtnPlay)
    val btnDownload = findViewById<View>(R.id.detailBtnDownload)

    btnClose?.setOnClickListener { hideDetailOverlay() }
    btnPlay?.setOnClickListener { hideDetailOverlay() }
    btnDownload?.setOnClickListener {
        try {
            if (playlist == null || currentIndex < 0 || currentIndex >= playlist!!.size) {
                Toast.makeText(this, "Tidak ada video untuk diunduh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val url = playlist!![currentIndex]

            var filename: String? = null
            try {
                if (MixedPlaylistHolder.isActive()) {
                    val cur = MixedPlaylistHolder.getCurrent()
                    if (cur != null && !cur.title.isNullOrEmpty()) {
                        filename = cur.title
                    }
                }
            } catch (ignored: Exception) {}

            if (filename.isNullOrEmpty()) {
                filename = url
                val slash = filename.lastIndexOf('/')
                if (slash >= 0 && slash < filename.length - 1) {
                    filename = filename.substring(slash + 1)
                    val q = filename.indexOf('?')
                    if (q > 0) filename = filename.substring(0, q)
                } else {
                    filename = "video_${System.currentTimeMillis()}.mp4"
                }
            }

            if (filename != null && !filename.contains(".")) {
                filename += if (url.lowercase().contains(".mp3")) ".mp3"
                else if (url.lowercase().contains(".m3u8")) ".mp4"
                else ".mp4"
            }

            DownloadHelper.startDownload(this, url, filename)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

private fun showDetailOverlayIfFirst() {
    if (detailShownOnce) return
    val overlay = detailOverlay ?: return
    detailShownOnce = true
    try {
        var judul = "Video"
        if (playlist != null && currentIndex >= 0 && currentIndex < playlist!!.size) {
            val url = playlist!![currentIndex]
            if (url != null) {
                val slash = url.lastIndexOf('/')
                if (slash >= 0 && slash < url.length - 1) {
                    judul = url.substring(slash + 1)
                    val q = judul.indexOf('?')
                    if (q > 0) judul = judul.substring(0, q)
                    val dot = judul.lastIndexOf('.')
                    if (dot > 0) judul = judul.substring(0, dot)
                    judul = judul.replace('_', ' ').replace('-', ' ')
                }
            }
        }

        val urlVideo = if (playlist != null && currentIndex >= 0 && currentIndex < playlist!!.size)
            playlist!![currentIndex] else null

        var thumbUrl: String? = null
        var titleDariMediaItem: String? = null

        try {
            if (MixedPlaylistHolder.isActive()) {
                val cur = MixedPlaylistHolder.getCurrent()
                if (cur != null) {
                    if (!cur.thumbUrl.isNullOrEmpty()) thumbUrl = cur.thumbUrl
                    if (!cur.title.isNullOrEmpty()) titleDariMediaItem = cur.title
                }
            }
        } catch (ignored: Exception) {}

        if ((thumbUrl == null || titleDariMediaItem == null) && urlVideo != null) {
            try {
                val ext = ExternalMediaStore.getAll(this)
                for (m in ext) {
                    if (m.uri != null && m.uri.toString() == urlVideo) {
                        if (thumbUrl == null && !m.thumbUrl.isNullOrEmpty()) thumbUrl = m.thumbUrl
                        if (titleDariMediaItem == null && !m.title.isNullOrEmpty()) titleDariMediaItem = m.title
                        break
                    }
                }
            } catch (ignored: Exception) {}
        }

        if (!titleDariMediaItem.isNullOrEmpty()) {
            judul = titleDariMediaItem!!
        }

        detailTitle?.text = judul
        detailDescription?.text = "Memuat deskripsi..."

        val finalThumbUrl = thumbUrl

        detailPoster?.let { poster ->
            try {
                val pad = (40 * resources.displayMetrics.density).toInt()
                poster.setPadding(pad, pad, pad, pad)
                poster.scaleType = ImageView.ScaleType.FIT_CENTER
                poster.setImageResource(android.R.drawable.ic_media_play)
                detailPosterBg?.setImageDrawable(null)
            } catch (ignored: Exception) {}

            if (!finalThumbUrl.isNullOrEmpty()) {
                loadPosterFromM3uLogo(finalThumbUrl)
            } else if (urlVideo != null &&
                (urlVideo.startsWith("content://") || urlVideo.startsWith("file://"))
            ) {
                loadPosterFromVideoFile(urlVideo)
            }
        }

        VideoDescriptionFetcher.fetch(this, urlVideo, judul) { desc ->
            if (desc != null && desc.isNotEmpty()) {
                detailDescription?.text = desc
            }
        }

        overlay.visibility = View.VISIBLE
        overlay.alpha = 0f
        overlay.animate().alpha(1f).setDuration(250).start()
    } catch (ignored: Exception) {}
}

private fun hideDetailOverlay() {
    val overlay = detailOverlay ?: return
    overlay.animate().alpha(0f).setDuration(200)
        .withEndAction { overlay.visibility = View.GONE }
        .start()
}

val currentIndexSafe: Int get() = currentIndex

fun pauseForPrivacy() {
    try {
        if (exoPlayer != null && exoPlayer!!.isPlaying) {
            exoPlayer!!.pause()
            isPlaying = false
        }
    } catch (ignored: Exception) {}
}

private fun loadPosterFromUrl(url: String?) {
    if (url.isNullOrEmpty()) return
    Thread {
        var bmp: Bitmap? = null
        try {
            val u = URL(url)
            val conn = u.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.instanceFollowRedirects = true
            val `is` = conn.inputStream
            bmp = BitmapFactory.decodeStream(`is`)
            `is`.close()
            conn.disconnect()
        } catch (ignored: Exception) {}
        val fb = bmp
        if (fb != null) {
            runOnUiThread {
                try {
                    detailPoster?.let {
                        it.setPadding(0, 0, 0, 0)
                        it.scaleType = ImageView.ScaleType.FIT_CENTER
                        it.setImageBitmap(fb)
                    }
                    detailPosterBg?.let {
                        val blurred = blurBitmap(fb, 20f)
                        if (blurred != null) {
                            it.scaleType = ImageView.ScaleType.CENTER_CROP
                            it.setImageBitmap(blurred)
                        }
                    }
                } catch (ignored: Exception) {}
            }
        }
    }.start()
}

private fun loadPosterFromVideoFile(videoUrl: String?) {
    if (videoUrl.isNullOrEmpty()) return
    Thread {
        var bmp: Bitmap? = null

        if (videoUrl.startsWith("content://")) {
            try {
                val uri = Uri.parse(videoUrl)
                val id = uri.lastPathSegment!!.toLong()
                bmp = android.provider.MediaStore.Video.Thumbnails.getThumbnail(
                    contentResolver, id,
                    android.provider.MediaStore.Video.Thumbnails.MINI_KIND, null
                )
            } catch (ignored: Exception) {}
        }

        if (bmp == null) {
            var mmr: MediaMetadataRetriever? = null
            try {
                mmr = MediaMetadataRetriever()
                mmr.setDataSource(this, Uri.parse(videoUrl))
                bmp = mmr.getFrameAtTime(
                    2 * 1000000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                if (bmp == null) {
                    bmp = mmr.getFrameAtTime(
                        0,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                }
            } catch (ignored: Exception) {
            } finally {
                try {
                    mmr?.release()
                } catch (ignored: Exception) {}
            }
        }

        val fb = bmp
        if (fb != null) {
            runOnUiThread {
                try {
                    detailPoster?.let {
                        it.setPadding(0, 0, 0, 0)
                        it.scaleType = ImageView.ScaleType.CENTER_CROP
                        it.setImageBitmap(fb)
                    }
                } catch (ignored: Exception) {}
            }
        }
    }.start()
}

private fun loadPosterFromM3uLogo(logoUrl: String?) {
    if (logoUrl.isNullOrEmpty()) return
    Thread {
        var bmp: Bitmap? = null
        try {
            val u = URL(logoUrl)
            val conn = u.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "VLC/3.0.16")
            conn.instanceFollowRedirects = true
            val `is` = conn.inputStream
            bmp = BitmapFactory.decodeStream(`is`)
            `is`.close()
            conn.disconnect()
        } catch (ignored: Exception) {}
        val fb = bmp
        if (fb != null) {
            runOnUiThread {
                try {
                    detailPoster?.let {
                        it.setPadding(0, 0, 0, 0)
                        it.scaleType = ImageView.ScaleType.FIT_CENTER
                        it.setImageBitmap(fb)
                    }
                    detailPosterBg?.let {
                        val blurred = blurBitmap(fb, 20f)
                        if (blurred != null) {
                            it.scaleType = ImageView.ScaleType.CENTER_CROP
                            it.setImageBitmap(blurred)
                        }
                    }
                } catch (ignored: Exception) {}
            }
        }
    }.start()
}

private fun blurBitmap(src: Bitmap, radius: Float): Bitmap? {
    return try {
        val scale = 25
        val w = Math.max(1, src.width / scale)
        val h = Math.max(1, src.height / scale)
        val small = Bitmap.createScaledBitmap(src, w, h, true)
        val result = Bitmap.createScaledBitmap(small, src.width, src.height, true)
        if (small !== result && !small.isRecycled) small.recycle()
        result
    } catch (e: Exception) {
        src
    }
}
private fun loadVideo(index: Int) {
    resetZoom()
    if (index < 0 || index >= playlist!!.size) return
    try {
        val originalUrl = playlist!![index]
        val fixedUrl = fixDriveUrl(originalUrl)
        val uri = Uri.parse(fixedUrl)
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()

        val lastPos = prefPosisi?.getLong("pos_" + playlist!![index], 0) ?: 0L
        if (lastPos > 3000) exoPlayer?.seekTo(lastPos)
        else if (autoSkipIntroMs > 0) exoPlayer?.seekTo(autoSkipIntroMs)

        exoPlayer?.play()
        isPlaying = true
        btnPlayPause?.setImageResource(R.drawable.ic_pause_w)
        btnPlayPauseCenter?.setImageResource(R.drawable.ic_pause_w)
        ViewCountStore.tambah(this, playlist!![index])
        updateDrawerContent()
    } catch (e: Exception) {
        Toast.makeText(this, "Gagal memuat video", Toast.LENGTH_SHORT).show()
        if (index < playlist!!.size - 1) loadVideo(index + 1)
        else finish()
    }
}

private fun simpanPosisi() {
    try {
        if (exoPlayer != null && playlist != null && currentIndex < playlist!!.size) {
            val pos = exoPlayer!!.currentPosition
            prefPosisi?.edit()?.putLong("pos_" + playlist!![currentIndex], pos)?.apply()
        }
    } catch (ignored: Exception) {}
}

private fun showPlayerMenu(anchor: View) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.popup_player_menu)

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    dialog.findViewById<View>(R.id.menuPip).setOnClickListener {
        dialog.dismiss()
        enterPip()
    }
    dialog.findViewById<View>(R.id.menuAddPlaylist).setOnClickListener {
        dialog.dismiss()
        showAddToPlaylistDialog()
    }
    dialog.findViewById<View>(R.id.menuSpeed).setOnClickListener {
        dialog.dismiss()
        showSpeedDialog()
    }
    dialog.findViewById<View>(R.id.menuAutoSkip).setOnClickListener {
        dialog.dismiss()
        showAutoSkipDialog()
    }
    dialog.findViewById<View>(R.id.menuSleepTimer)?.setOnClickListener {
        dialog.dismiss()
        showVideoSleepTimer()
    }

    dialog.show()
}

private fun showVideoSleepTimer() {
    val items = arrayOf("15 menit", "30 menit", "60 menit", "90 menit", "Matikan Sleep Timer")
    AlertDialog.Builder(this)
        .setTitle("Sleep Timer")
        .setItems(items) { _, which ->
            if (which == 4) {
                stopVideoSleepTimer()
                Toast.makeText(this, "Sleep timer dimatikan", Toast.LENGTH_SHORT).show()
            } else {
                val minutes = when (which) {
                    1 -> 30
                    2 -> 60
                    3 -> 90
                    else -> 15
                }
                startVideoSleepTimer(minutes)
            }
        }
        .setNegativeButton("Batal", null)
        .show()
}

private fun startVideoSleepTimer(minutes: Int) {
    stopVideoSleepTimer()
    val totalMs = minutes * 60L * 1000L
    val startTime = System.currentTimeMillis()
    Toast.makeText(this, "Sleep timer: $minutes menit", Toast.LENGTH_SHORT).show()

    videoSleepRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = totalMs - elapsed
            if (remaining <= 0) {
                try { exoPlayer?.pause() } catch (ignored: Exception) {}
                try {
                    SessionState.markInternalTransition()
                    val i = Intent(this@VideoPlayerActivity, CalculatorActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(i)
                } catch (ignored: Exception) {}
                finish()
                return
            }
            val remMin = (remaining / 60000).toInt()
            val remSec = ((remaining % 60000) / 1000).toInt()
            try {
                title = String.format(Locale.getDefault(), "Sleep: %d:%02d", remMin, remSec)
            } catch (ignored: Exception) {}
            videoSleepHandler.postDelayed(this, 1000)
        }
    }
    videoSleepHandler.post(videoSleepRunnable!!)
}

private fun stopVideoSleepTimer() {
    videoSleepRunnable?.let {
        videoSleepHandler.removeCallbacks(it)
        videoSleepRunnable = null
    }
}

private fun showAddToPlaylistDialog() {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_add_playlist)

    val listView = dialog.findViewById<ListView>(R.id.listPlaylistChoice)
    val tvEmpty = dialog.findViewById<TextView>(R.id.tvEmptyPlaylist)

    val playlists = CustomPlaylistStore.getAll(this)

    if (playlists.isEmpty()) {
        tvEmpty.visibility = View.VISIBLE
        listView.visibility = View.GONE
    } else {
        tvEmpty.visibility = View.GONE
        listView.visibility = View.VISIBLE

        val adapter = object : ArrayAdapter<CustomPlaylistStore.Playlist>(this, 0, playlists) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var v = convertView
                if (v == null) v = layoutInflater.inflate(R.layout.item_playlist_choice, parent, false)
                val pl = getItem(position)
                val tvName = v.findViewById<TextView>(R.id.tvPlaylistChoiceName)
                val tvCount = v.findViewById<TextView>(R.id.tvPlaylistChoiceCount)
                if (pl != null) {
                    tvName.text = pl.name
                    tvCount.text = "${pl.items.size} item"
                }
                return v
            }
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            addCurrentToPlaylist(playlists[position])
            dialog.dismiss()
        }
    }

    dialog.findViewById<View>(R.id.btnBuatPlaylistBaru).setOnClickListener {
        dialog.dismiss()
        showCreatePlaylistDialog()
    }

    dialog.findViewById<View>(R.id.btnBatalPlaylist).setOnClickListener { dialog.dismiss() }

    dialog.show()
}

private fun addCurrentToPlaylist(playlistTarget: CustomPlaylistStore.Playlist?) {
    try {
        if (playlistTarget == null) return
        val all = CustomPlaylistStore.getAll(this)
        for (pl in all) {
            if (pl.name == playlistTarget.name) {
                if (currentIndex >= 0 && currentIndex < playlist!!.size) {
                    val uri = playlist!![currentIndex]
                    val item = com.memecio.app.MediaItem(Uri.parse(uri), com.memecio.app.MediaItem.TYPE_VIDEO)
                    item.title = uri
                    pl.items.add(item)
                    CustomPlaylistStore.saveAll(this, all)
                    Toast.makeText(this, "Ditambahkan ke ${pl.name}", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    } catch (e: Exception) {
        Toast.makeText(this, "Gagal menambahkan", Toast.LENGTH_SHORT).show()
    }
}

private fun showCreatePlaylistDialog() {
    val b = AlertDialog.Builder(this)
    b.setTitle("Playlist Baru")
    val input = EditText(this)
    input.hint = "Nama playlist"
    b.setView(input)
    b.setPositiveButton("Buat") { _, _ ->
        val name = input.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama kosong", Toast.LENGTH_SHORT).show()
            return@setPositiveButton
        }
        val all = CustomPlaylistStore.getAll(this)
        val pl = CustomPlaylistStore.Playlist()
        pl.name = name
        if (currentIndex >= 0 && currentIndex < playlist!!.size) {
            val uri = playlist!![currentIndex]
            val item = com.memecio.app.MediaItem(Uri.parse(uri), com.memecio.app.MediaItem.TYPE_VIDEO)
            item.title = uri
            pl.items.add(item)
        }
        all.add(pl)
        CustomPlaylistStore.saveAll(this, all)
        Toast.makeText(this, "Playlist '$name' dibuat", Toast.LENGTH_SHORT).show()
    }
    b.setNegativeButton("Batal", null)
    b.show()
}

private fun showSpeedDialog() {
    val speeds = arrayOf("0.5x", "1x", "1.5x", "2x", "3x")
    AlertDialog.Builder(this)
        .setTitle("Kecepatan Playback")
        .setItems(speeds) { _, which ->
            val v = when (which) {
                0 -> 0.5f
                2 -> 1.5f
                3 -> 2.0f
                4 -> 3.0f
                else -> 1.0f
            }
            applySpeedValue(v)
        }
        .show()
}

private fun showAutoSkipDialog() {
    val layout = LinearLayout(this)
    layout.orientation = LinearLayout.VERTICAL
    layout.setPadding(40, 20, 40, 20)

    val etIntro = EditText(this)
    etIntro.hint = "Detik intro (contoh: 15)"
    etIntro.inputType = InputType.TYPE_CLASS_NUMBER
    layout.addView(etIntro)

    val etOutro = EditText(this)
    etOutro.hint = "Detik outro (contoh: 30)"
    etOutro.inputType = InputType.TYPE_CLASS_NUMBER
    layout.addView(etOutro)

    val prefSet = getSharedPreferences("memecio_settings", MODE_PRIVATE)
    val introSec = prefSet.getLong("auto_skip_intro", 0) / 1000
    val outroSec = prefSet.getLong("auto_skip_outro", 0) / 1000
    if (introSec > 0) etIntro.setText(introSec.toString())
    if (outroSec > 0) etOutro.setText(outroSec.toString())

    AlertDialog.Builder(this)
        .setTitle("Auto Skip Intro/Outro")
        .setView(layout)
        .setPositiveButton("Simpan") { _, _ ->
            var inVal = 0L
            var outVal = 0L
            try { inVal = etIntro.text.toString().trim().toLong() } catch (ignored: Exception) {}
            try { outVal = etOutro.text.toString().trim().toLong() } catch (ignored: Exception) {}
            prefSet.edit()
                .putLong("auto_skip_intro", inVal * 1000)
                .putLong("auto_skip_outro", outVal * 1000)
                .apply()
            autoSkipIntroMs = inVal * 1000
            Toast.makeText(this, "Intro ${inVal}s, Outro ${outVal}s", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("Batal", null)
        .show()
}

private fun showSettingsDialog() {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_set)

    val tabHost = dialog.findViewById<TabHost>(android.R.id.tabhost)
    tabHost.setup()

    val speedTab = tabHost.newTabSpec("Speed")
    speedTab.setIndicator("Speed")
    speedTab.setContent(R.id.tabSpeedContent)
    tabHost.addTab(speedTab)

    val skipTab = tabHost.newTabSpec("Auto-Skip")
    skipTab.setIndicator("Auto-Skip")
    skipTab.setContent(R.id.tabAutoSkipContent)
    tabHost.addTab(skipTab)

    val tampilanTab = tabHost.newTabSpec("Tampilan")
    tampilanTab.setIndicator("Tampilan")
    tampilanTab.setContent(R.id.tabTampilanContent)
    tabHost.addTab(tampilanTab)

    val tampilanContent = dialog.findViewById<LinearLayout>(R.id.tabTampilanContent)
    tampilanContent?.let { tc ->
        val tampilanView = layoutInflater.inflate(R.layout.tab_tampilan, tc, false)
        tc.addView(tampilanView)
        val tvHdr = tampilanView.findViewById<TextView>(R.id.tvHdrStatus)
        tvHdr?.text = if (isHdrVideo) "Video: HDR (detected)" else "Video: SDR"
        val swHdr = tampilanView.findViewById<Switch>(R.id.switchHdr)
        swHdr?.let { sw ->
            val saved = getSharedPreferences("memecio_settings", MODE_PRIVATE)
                .getBoolean("hdr_enabled", true)
            sw.isChecked = saved
            sw.setOnCheckedChangeListener { _, isChecked ->
                getSharedPreferences("memecio_settings", MODE_PRIVATE)
                    .edit().putBoolean("hdr_enabled", isChecked).apply()
                Toast.makeText(
                    this,
                    "HDR " + (if (isChecked) "ON" else "OFF"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val speedContent = dialog.findViewById<LinearLayout>(R.id.tabSpeedContent)
    if (speedContent != null) {
        val speedView = layoutInflater.inflate(R.layout.tab_speed, speedContent, false)
        speedContent.addView(speedView)

        speedView.findViewById<View>(R.id.btnSpeed05).setOnClickListener {
            applySpeedValue(0.5f); dialog.dismiss()
        }
        speedView.findViewById<View>(R.id.btnSpeed1).setOnClickListener {
            applySpeedValue(1.0f); dialog.dismiss()
        }
        speedView.findViewById<View>(R.id.btnSpeed15).setOnClickListener {
            applySpeedValue(1.5f); dialog.dismiss()
        }
        speedView.findViewById<View>(R.id.btnSpeed2).setOnClickListener {
            applySpeedValue(2.0f); dialog.dismiss()
        }
        speedView.findViewById<View>(R.id.btnSpeed3).setOnClickListener {
            applySpeedValue(3.0f); dialog.dismiss()
        }
    }

    val skipContent = dialog.findViewById<LinearLayout>(R.id.tabAutoSkipContent)
    if (skipContent != null) {
        val skipView = layoutInflater.inflate(R.layout.tab_auto_skip, skipContent, false)
        skipContent.addView(skipView)

        val etIntro = skipView.findViewById<EditText>(R.id.etSkipIntro)
        val etOutro = skipView.findViewById<EditText>(R.id.etSkipOutro)
        val btnBatal = skipView.findViewById<Button>(R.id.btnSkipBatal)
        val btnSimpan = skipView.findViewById<Button>(R.id.btnSkipSimpan)

        val prefSet = getSharedPreferences("memecio_settings", MODE_PRIVATE)
        val introSec = prefSet.getLong("auto_skip_intro", 0) / 1000
        val outroSec = prefSet.getLong("auto_skip_outro", 0) / 1000
        if (introSec > 0) etIntro.setText(introSec.toString())
        if (outroSec > 0) etOutro.setText(outroSec.toString())

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnSimpan.setOnClickListener {
            var introVal = 0L
            var outroVal = 0L
            try { introVal = etIntro.text.toString().trim().toLong() } catch (ignored: Exception) {}
            try { outroVal = etOutro.text.toString().trim().toLong() } catch (ignored: Exception) {}

            prefSet.edit()
                .putLong("auto_skip_intro", introVal * 1000)
                .putLong("auto_skip_outro", outroVal * 1000)
                .apply()

            autoSkipIntroMs = introVal * 1000
            Toast.makeText(
                this,
                "Auto-Skip: Intro ${introVal}s, Outro ${outroVal}s",
                Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }
    }

    dialog.show()
}

private fun applySpeedValue(value: Float) {
    currentSpeed = value
    applySpeed()
    Toast.makeText(this, "Kecepatan: ${value}x", Toast.LENGTH_SHORT).show()
}

private fun showSpeedMenu(anchor: View) {
    val popup = PopupMenu(this, anchor)
    popup.menu.add("0.5x")
    popup.menu.add("1x")
    popup.menu.add("1.5x")
    popup.menu.add("2x")
    popup.menu.add("3x")
    popup.setOnMenuItemClickListener { item ->
        when (item.title.toString()) {
            "0.5x" -> currentSpeed = 0.5f
            "1x" -> currentSpeed = 1.0f
            "1.5x" -> currentSpeed = 1.5f
            "2x" -> currentSpeed = 2.0f
            "3x" -> currentSpeed = 3.0f
        }
        applySpeed()
        true
    }
    popup.show()
}

private fun applySpeed() {
    tvSpeed?.text = "${currentSpeed}x"
    try {
        exoPlayer?.playbackParameters = PlaybackParameters(currentSpeed)
    } catch (ignored: Exception) {}
}

private fun toggleFullscreen() {
    isFullscreen = !isFullscreen
    if (isFullscreen) {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        exoPlayer?.let { p ->
            val vs = p.videoSize
            if (vs.width > 0 && vs.height > 0) {
                if (vs.width > vs.height) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}

private fun enterPip() {
    if (Build.VERSION.SDK_INT >= 26) {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9)).build()
        enterPictureInPictureMode(params)
    } else {
        Toast.makeText(this, "PiP tidak didukung", Toast.LENGTH_SHORT).show()
    }
}

private fun formatTime(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

override fun onPause() {
    super.onPause()
    simpanPosisi()
}

override fun onStop() {
    super.onStop()
    simpanPosisi()
}
    private val gestureHideRunnable = Runnable {
        try {
            gestureIndicator?.visibility = View.GONE
        } catch (ignored: Exception) {}
    }

    private fun applyZoomPan() {
        playerView?.let { pv ->
            pv.scaleX = zoomScale
            pv.scaleY = zoomScale
            pv.translationX = zoomPanX
            pv.translationY = zoomPanY
        }
    }

    private fun resetZoom() {
        zoomScale = 1f
        zoomPanX = 0f
        zoomPanY = 0f
        applyZoomPan()
    }

    private fun handleZoomPan(event: MotionEvent): Boolean {
        if (event.pointerCount >= 2) {
            if (scaleDetector == null) {
                scaleDetector = ScaleGestureDetector(this,
                    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(d: ScaleGestureDetector): Boolean {
                            val ns = zoomScale * d.scaleFactor
                            zoomScale = Math.max(1f, Math.min(4f, ns))
                            applyZoomPan()
                            val pct = (zoomScale * 100).toInt()
                            showGestureIndicator(
                                android.R.drawable.ic_menu_crop,
                                "$pct%",
                                Math.min(100, pct / 4)
                            )
                            return true
                        }
                    })
            }
            scaleDetector?.onTouchEvent(event)
            return true
        }

        if (zoomScale > 1.01f) {
            if (doubleTapDetector == null) {
                doubleTapDetector = GestureDetector(this,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            resetZoom()
                            Toast.makeText(
                                this@VideoPlayerActivity,
                                "Zoom direset",
                                Toast.LENGTH_SHORT
                            ).show()
                            return true
                        }
                    })
            }
            doubleTapDetector?.onTouchEvent(event)
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            } else if (action == MotionEvent.ACTION_MOVE) {
                zoomPanX += event.x - lastTouchX
                zoomPanY += event.y - lastTouchY
                lastTouchX = event.x
                lastTouchY = event.y
                applyZoomPan()
                return true
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                return true
            }
            return true
        }
        return false
    }

    private fun updateHdrBadge() {
        try {
            tvHdrBadge?.visibility = if (isHdrVideo) View.VISIBLE else View.GONE
        } catch (ignored: Exception) {}
    }

    private fun showGestureIndicator(iconRes: Int, text: String, progress: Int) {
        try {
            val gi = gestureIndicator ?: return
            gi.visibility = View.VISIBLE
            gi.alpha = 1f
            gestureIcon?.setImageResource(iconRes)
            gestureText?.text = text
            gestureBar?.progress = progress
            gestureHandler.removeCallbacks(gestureHideRunnable)
            gestureHandler.postDelayed(gestureHideRunnable, 1200)
        } catch (ignored: Exception) {}
    }

    private fun hideGestureIndicator() {
        try {
            gestureIndicator?.visibility = View.GONE
        } catch (ignored: Exception) {}
    }

    private fun showPreviewStart() {
        val pc = previewContainer ?: return
        initPreviewRetrieverIfNeeded()
        pc.visibility = View.VISIBLE
        pc.alpha = 0f
        pc.animate().alpha(1f).setDuration(150).start()
    }

    private fun hidePreviewEnd() {
        val pc = previewContainer ?: return
        pc.animate().alpha(0f).setDuration(150)
            .withEndAction { pc.visibility = View.GONE }
            .start()
    }

    private fun initPreviewRetrieverIfNeeded() {
        try {
            if (previewRetriever != null) return
            if (playlist == null || currentIndex < 0 || currentIndex >= playlist!!.size) return
            val url = playlist!![currentIndex] ?: return
            if (url.startsWith("http://") || url.startsWith("https://")) return
            val fixed = fixDriveUrl(url)
            previewRetriever = MediaMetadataRetriever()
            previewRetriever?.setDataSource(this, Uri.parse(fixed))
        } catch (e: Exception) {
            previewRetriever = null
        }
    }

    private fun updatePreview(progressMs: Int) {
        previewTime?.text = formatTime(progressMs)
        positionPreview(progressMs)
        extractFrameAsync(progressMs)
    }

    private fun extractFrameAsync(progressMs: Int) {
        if (previewRetriever == null) return
        val myId = previewRequestId.incrementAndGet()
        try {
            previewExecutor.execute {
                var bmp: Bitmap? = null
                try {
                    if (myId == previewRequestId.get() && previewRetriever != null) {
                        bmp = previewRetriever?.getFrameAtTime(
                            progressMs * 1000L,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                    }
                } catch (ignored: Exception) {}
                if (myId != previewRequestId.get()) {
                    bmp?.recycle()
                    return@execute
                }
                val fb = bmp
                previewHandler.post {
                    if (myId != previewRequestId.get()) {
                        fb?.recycle()
                        return@post
                    }
                    if (previewImage != null && fb != null) {
                        lastPreviewBitmap?.let {
                            if (!it.isRecycled) it.recycle()
                        }
                        lastPreviewBitmap = fb
                        previewImage?.setImageBitmap(fb)
                    }
                }
            }
        } catch (ignored: Exception) {}
    }

    private fun positionPreview(progressMs: Int) {
        val pc = previewContainer ?: return
        val sb = seekBar ?: return
        val max = sb.max
        if (max <= 0) return

        var pw = pc.width
        if (pw == 0) pw = (120 * resources.displayMetrics.density).toInt()
        var ph = pc.height
        if (ph == 0) ph = (90 * resources.displayMetrics.density).toInt()

        val seekLoc = IntArray(2)
        sb.getLocationInWindow(seekLoc)
        val seekWidth = sb.width

        val ratio = progressMs.toFloat() / max
        val thumbCenter = seekLoc[0] + ratio * seekWidth

        var x = thumbCenter - pw / 2.0f
        val screenWidth = resources.displayMetrics.widthPixels
        if (x < 8) x = 8f
        if (x + pw > screenWidth - 8) x = (screenWidth - pw - 8).toFloat()

        var y = seekLoc[1] - ph - 8f
        if (y < 8) y = 8f

        pc.x = x
        pc.y = y
    }

    override fun onDestroy() {
        super.onDestroy()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        simpanPosisi()
        handler.removeCallbacks(updateSeekRunnable)
        hideHandler.removeCallbacks(hideControlsRunnable)
        clockRunnable?.let { handler.removeCallbacks(it) }
        exoPlayer?.release()
        exoPlayer = null
        try {
            previewRetriever?.release()
            previewRetriever = null
        } catch (ignored: Exception) {}
        try {
            lastPreviewBitmap?.let {
                if (!it.isRecycled) it.recycle()
            }
            lastPreviewBitmap = null
        } catch (ignored: Exception) {}
        try { previewExecutor.shutdownNow() } catch (ignored: Exception) {}
    }
}
