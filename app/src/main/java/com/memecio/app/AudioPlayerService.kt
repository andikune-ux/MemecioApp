package com.memecio.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

class AudioPlayerService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    private val binder = LocalBinder()
    private var exoPlayer: ExoPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var mediaSession: MediaSession? = null
    private var currentTitle: String? = null

    fun getPlayer(): ExoPlayer? = exoPlayer

    override fun onCreate() {
        super.onCreate()
        createChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    requestAudioFocus()
                    startForeground(NOTIFICATION_ID, buildNotification())
                } else {
                    if (Build.VERSION.SDK_INT >= 24) {
                        stopForeground(STOP_FOREGROUND_DETACH)
                    } else {
                        stopForeground(false)
                    }
                    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, buildNotification())
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                    nm.notify(NOTIFICATION_ID, buildNotification())
                }
            }
        })
        try {
            mediaSession = MediaSession.Builder(this, exoPlayer!!)
                .setId("MemecioAudioSession")
                .build()
        } catch (e: Exception) {
            mediaSession = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.action
            val uri = it.getStringExtra("audio_uri")
            val title = it.getStringExtra("audio_title")
            if (uri != null) {
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()
                if (title != null) currentTitle = title
            }
            when (action) {
                ACTION_PAUSE -> exoPlayer?.pause()
                ACTION_PLAY -> exoPlayer?.play()
                ACTION_NEXT -> exoPlayer?.seekToNext()
                ACTION_PREV -> exoPlayer?.seekToPrevious()
                ACTION_STOP -> {
                    exoPlayer?.stop()
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        mediaSession?.release()
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            val attrs = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { }
                .build()
            focusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Audio Player", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val b: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, AudioPlayerActivity::class.java),
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )
        b.setContentTitle(currentTitle ?: "Memec.io Audio")
            .setContentText("Sedang diputar")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi)
            .setOngoing(true)
        return b.build()
    }

    companion object {
        const val ACTION_PLAY = "com.memecio.app.PLAY"
        const val ACTION_PAUSE = "com.memecio.app.PAUSE"
        const val ACTION_NEXT = "com.memecio.app.NEXT"
        const val ACTION_PREV = "com.memecio.app.PREV"
        const val ACTION_STOP = "com.memecio.app.STOP"
        private const val CHANNEL_ID = "audio_channel"
        private const val NOTIFICATION_ID = 1
    }
}
