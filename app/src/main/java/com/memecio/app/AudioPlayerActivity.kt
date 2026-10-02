package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.Player

class AudioPlayerActivity : Activity() {

    private var service: AudioPlayerService? = null
    private var bound = false
    private var seekBar: SeekBar? = null
    private var tvCurrent: TextView? = null
    private var tvDuration: TextView? = null
    private var btnPlayPause: ImageView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var userSeeking = false

    private val sleepHandler = Handler(Looper.getMainLooper())
    private var sleepTimerRunnable: Runnable? = null
    private var tvSleepStatus: TextView? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val lb = binder as AudioPlayerService.LocalBinder
            service = lb.getService()
            bound = true
            setupPlayerListener()
            startProgressLoop()
            updateUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (service?.getPlayer() != null && !userSeeking) {
                val pos = service!!.getPlayer()!!.currentPosition
                val dur = service!!.getPlayer()!!.duration
                if (dur > 0) {
                    seekBar?.max = dur.toInt()
                    seekBar?.progress = pos.toInt()
                    tvCurrent?.text = formatTime(pos.toInt())
                    tvDuration?.text = formatTime(dur.toInt())
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundHelper.init(this)
        setContentView(R.layout.activity_audio_player)

        val title = intent.getStringExtra("audio_title")
        val uri = intent.getStringExtra("audio_uri")
        val tvTitle = findViewById<TextView>(R.id.tvAudioTitle)
        tvTitle.text = title ?: "Audio"

        seekBar = findViewById(R.id.seekAudio)
        tvCurrent = findViewById(R.id.tvAudioCurrent)
        tvDuration = findViewById(R.id.tvAudioDuration)
        btnPlayPause = findViewById(R.id.btnAudioPlayPause)

        findViewById<View>(R.id.btnBackAudio).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAudioPrev).setOnClickListener {
            service?.getPlayer()?.seekToPrevious()
        }
        findViewById<View>(R.id.btnAudioNext).setOnClickListener {
            service?.getPlayer()?.seekToNext()
        }
        findViewById<View>(R.id.btnAudioRewind).setOnClickListener {
            val pos = (service?.getPlayer()?.currentPosition ?: 0L) - 10000
            service?.getPlayer()?.seekTo(Math.max(0, pos))
        }
        findViewById<View>(R.id.btnAudioForward).setOnClickListener {
            val pos = (service?.getPlayer()?.currentPosition ?: 0L) + 10000
            service?.getPlayer()?.seekTo(Math.min(pos, service?.getPlayer()?.duration ?: 0L))
        }

        tvSleepStatus = findViewById(R.id.btnAudioSleep)
        tvSleepStatus?.setOnClickListener { showSleepTimerDialog() }

        btnPlayPause?.setOnClickListener {
            if (service?.getPlayer()?.isPlaying == true) {
                service?.getPlayer()?.pause()
            } else {
                service?.getPlayer()?.play()
            }
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrent?.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                userSeeking = true
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                userSeeking = false
                service?.getPlayer()?.seekTo(seekBar?.progress?.toLong() ?: 0L)
            }
        })

        if (uri != null) {
            val i = Intent(this, AudioPlayerService::class.java)
            i.action = AudioPlayerService.ACTION_PLAY
            i.putExtra("audio_uri", uri)
            i.putExtra("audio_title", title)
            startService(i)
        }
    }

    override fun onStart() {
        super.onStart()
        val i = Intent(this, AudioPlayerService::class.java)
        bindService(i, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    private fun setupPlayerListener() {
        service?.getPlayer()?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateUI()
            }
        })
    }

    private fun startProgressLoop() {
        handler.post(progressRunnable)
    }

    private fun updateUI() {
        val playing = service?.getPlayer()?.isPlaying == true
        btnPlayPause?.setImageResource(
            if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
    }

    private fun showSleepTimerDialog() {
        val options = arrayOf("5 menit", "10 menit", "15 menit", "30 menit", "Matikan timer")
        AlertDialog.Builder(this)
            .setTitle("Sleep Timer")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setSleepTimer(5)
                    1 -> setSleepTimer(10)
                    2 -> setSleepTimer(15)
                    3 -> setSleepTimer(30)
                    4 -> cancelSleepTimer()
                }
            }
            .show()
    }

    private fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        sleepTimerRunnable = Runnable {
            service?.getPlayer()?.pause()
            Toast.makeText(this, "Sleep timer selesai", Toast.LENGTH_SHORT).show()
            tvSleepStatus?.text = "Sleep: OFF"
        }
        sleepHandler.postDelayed(sleepTimerRunnable!!, minutes * 60_000L)
        tvSleepStatus?.text = "Sleep: $minutes menit"
        Toast.makeText(this, "Sleep timer $minutes menit", Toast.LENGTH_SHORT).show()
    }

    private fun cancelSleepTimer() {
        sleepTimerRunnable?.let { sleepHandler.removeCallbacks(it) }
        sleepTimerRunnable = null
        tvSleepStatus?.text = "Sleep: OFF"
    }

    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    override fun onDestroy() {
        handler.removeCallbacks(progressRunnable)
        super.onDestroy()
    }
}
