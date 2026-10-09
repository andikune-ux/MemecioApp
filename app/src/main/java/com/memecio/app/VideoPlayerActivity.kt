package com.memecio.app

import android.app.Activity
import android.app.PictureInPictureParams
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import java.util.ArrayList
import java.util.Locale

class VideoPlayerActivity : Activity() {

    private var audioManager: AudioManager? = null
    private var btnCloseVideo: View? = null
    private var btnMute: TextView? = null
    private var btnPlayPause: TextView? = null
    private var btnPlayPauseCenter: TextView? = null
    private var centerControls: View? = null
    private var controlBar: View? = null
    private var topControls: View? = null
    private var playlist: ArrayList<String> = ArrayList()
    private var seekBar: SeekBar? = null
    private var tvSpeed: TextView? = null
    private var tvTime: TextView? = null
    private var videoView: VideoView? = null
    private var mediaPlayerRef: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private val hideHandler = Handler(Looper.getMainLooper())
    private var isPlaying = true
    private var isMuted = false
    private var controlsVisible = true
    private var currentSpeed = 1.0f
    private var currentIndex = 0

    private val updateSeekRunnable = object : Runnable {
        override fun run() {
            videoView?.let { vv ->
                if (vv.isPlaying) {
                    val current = vv.currentPosition
                    val duration = vv.duration
                    seekBar?.max = duration
                    seekBar?.progress = current
                    tvTime?.text = "${formatTime(current)} / ${formatTime(duration)}"
                }
            }
            handler.postDelayed(this, 500L)
        }
    }

    private val hideControlsRunnable = Runnable {
        hideControls()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_video_player)

        audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        videoView = findViewById(R.id.videoView)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        tvSpeed = findViewById(R.id.tvSpeed)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPlayPauseCenter = findViewById(R.id.btnPlayPauseCenter)
        btnMute = findViewById(R.id.btnMute)
        btnCloseVideo = findViewById(R.id.btnCloseVideo)
        controlBar = findViewById(R.id.controlBar)
        centerControls = findViewById(R.id.centerControls)
        topControls = findViewById(R.id.topControls)

        val btnPrev = findViewById<View>(R.id.btnPrev)
        val btnNext = findViewById<View>(R.id.btnNext)
        val btnRewind10 = findViewById<View>(R.id.btnRewind10)
        val btnForward10 = findViewById<View>(R.id.btnForward10)
        val btnSettings = findViewById<View>(R.id.btnSettings)
        val btnRatio = findViewById<View>(R.id.btnRatio)

        val listExtra = intent.getStringArrayListExtra("playlist")
        playlist = listExtra ?: PlaylistHolder.get() ?: ArrayList()
        currentIndex = intent.getIntExtra("index", 0)

        val singleUri = intent.getStringExtra("uri")
        if (playlist.isEmpty() && singleUri != null) {
            playlist.add(singleUri)
            currentIndex = 0
        }

        setupControls(btnPrev, btnNext, btnRewind10, btnForward10, btnSettings, btnRatio)
        loadVideo(currentIndex)
    }

    private fun setupControls(
        btnPrev: View?,
        btnNext: View?,
        btnRewind10: View?,
        btnForward10: View?,
        btnSettings: View?,
        btnRatio: View?
    ) {
        videoView?.setOnClickListener {
            toggleControls()
        }

        btnCloseVideo?.setOnClickListener {
            finish()
        }

        val onPlayPauseClick = View.OnClickListener {
            videoView?.let { vv ->
                if (vv.isPlaying) {
                    vv.pause()
                    isPlaying = false
                    btnPlayPause?.text = "▶"
                    btnPlayPauseCenter?.text = "▶"
                } else {
                    vv.start()
                    isPlaying = true
                    btnPlayPause?.text = "⏸"
                    btnPlayPauseCenter?.text = "⏸"
                }
            }
            scheduleAutoHide()
        }

        btnPlayPause?.setOnClickListener(onPlayPauseClick)
        btnPlayPauseCenter?.setOnClickListener(onPlayPauseClick)

        btnPrev?.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadVideo(currentIndex)
            } else {
                Toast.makeText(this, "Video pertama", Toast.LENGTH_SHORT).show()
            }
            scheduleAutoHide()
        }

        btnNext?.setOnClickListener {
            if (currentIndex < playlist.size - 1) {
                currentIndex++
                loadVideo(currentIndex)
            } else {
                Toast.makeText(this, "Video terakhir", Toast.LENGTH_SHORT).show()
            }
            scheduleAutoHide()
        }

        btnRewind10?.setOnClickListener {
            videoView?.let { vv ->
                val target = (vv.currentPosition - 10000).coerceAtLeast(0)
                vv.seekTo(target)
            }
            scheduleAutoHide()
        }

        btnForward10?.setOnClickListener {
            videoView?.let { vv ->
                val target = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                vv.seekTo(target)
            }
            scheduleAutoHide()
        }

        btnMute?.setOnClickListener {
            audioManager?.let { am ->
                if (!isMuted) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                    btnMute?.text = "🔇"
                } else {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0)
                    btnMute?.text = "🔊"
                }
                isMuted = !isMuted
            }
            scheduleAutoHide()
        }

        btnSettings?.setOnClickListener { v ->
            showSpeedMenu(v)
            scheduleAutoHide()
        }

        tvSpeed?.setOnClickListener { v ->
            showSpeedMenu(v)
            scheduleAutoHide()
        }

        btnRatio?.setOnClickListener {
            enterPip()
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                scheduleAutoHide()
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                scheduleAutoHide()
            }
        })
    }

    private fun loadVideo(index: Int) {
        if (index < 0 || index >= playlist.size) return
        val uriStr = playlist[index]
        val uri = Uri.parse(uriStr)

        videoView?.setVideoURI(uri)
        videoView?.setOnPreparedListener { mp ->
            mediaPlayerRef = mp
            mp.isLooping = false
            videoView?.start()
            isPlaying = true
            btnPlayPause?.text = "⏸"
            btnPlayPauseCenter?.text = "⏸"
            seekBar?.max = videoView?.duration ?: 0
            applySpeed()
            handler.post(updateSeekRunnable)
            scheduleAutoHide()
        }

        videoView?.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Gagal memutar video", Toast.LENGTH_SHORT).show()
            true
        }

        videoView?.setOnCompletionListener {
            if (currentIndex < playlist.size - 1) {
                currentIndex++
                loadVideo(currentIndex)
            }
        }
    }

    private fun toggleControls() {
        if (controlsVisible) {
            hideControls()
        } else {
            showControls()
            scheduleAutoHide()
        }
    }

    private fun showControls() {
        controlsVisible = true
        controlBar?.visibility = View.VISIBLE
        centerControls?.visibility = View.VISIBLE
        topControls?.visibility = View.VISIBLE
    }

    private fun hideControls() {
        controlsVisible = false
        controlBar?.visibility = View.GONE
        centerControls?.visibility = View.GONE
        topControls?.visibility = View.GONE
    }

    private fun scheduleAutoHide() {
        hideHandler.removeCallbacks(hideControlsRunnable)
        hideHandler.postDelayed(hideControlsRunnable, 4000L)
    }

    private fun showSpeedMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("0.5x")
        popup.menu.add("1x")
        popup.menu.add("1.5x")
        popup.menu.add("2x")
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.title.toString()) {
                "0.5x" -> currentSpeed = 0.5f
                "1x" -> currentSpeed = 1.0f
                "1.5x" -> currentSpeed = 1.5f
                "2x" -> currentSpeed = 2.0f
            }
            applySpeed()
            true
        }
        popup.show()
    }

    private fun applySpeed() {
        tvSpeed?.text = "${currentSpeed}x"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayerRef?.let { mp ->
                    val params = PlaybackParams()
                    params.speed = currentSpeed
                    mp.playbackParams = params
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Kecepatan tidak didukung untuk video ini", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        } else {
            Toast.makeText(this, "PiP tidak didukung di versi Android ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(millis: Int): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekRunnable)
        hideHandler.removeCallbacks(hideControlsRunnable)
    }
}
