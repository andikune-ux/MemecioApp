package com.memecio.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;

public class AudioPlayerService extends Service {

    public static final String ACTION_PLAY = "com.memecio.app.PLAY";
    public static final String ACTION_PAUSE = "com.memecio.app.PAUSE";
    public static final String ACTION_NEXT = "com.memecio.app.NEXT";
    public static final String ACTION_PREV = "com.memecio.app.PREV";
    public static final String ACTION_STOP = "com.memecio.app.STOP";

    private static final String CHANNEL_ID = "audio_channel";
    private static final int NOTIFICATION_ID = 1;

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() { return AudioPlayerService.this; }
    }

    private final IBinder binder = new LocalBinder();
    private ExoPlayer exoPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private MediaSession mediaSession;

    public ExoPlayer getPlayer() { return exoPlayer; }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayer.setAudioAttributes(
            new androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.CONTENT_TYPE_MUSIC)
                .build(),
            true
        );

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    requestAudioFocus();
                    startForeground(NOTIFICATION_ID, buildNotification());
                } else {
                    if (Build.VERSION.SDK_INT >= 24) {
                        stopForeground(STOP_FOREGROUND_DETACH);
                    } else {
                        stopForeground(false);
                    }
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (nm != null) nm.notify(NOTIFICATION_ID, buildNotification());
                }
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (nm != null && (state == Player.STATE_READY || state == Player.STATE_BUFFERING)) {
                    nm.notify(NOTIFICATION_ID, buildNotification());
                }
            }
        });

        // MediaSession untuk kontrol dari lockscreen & bluetooth
        try {
            mediaSession = new MediaSession.Builder(this, exoPlayer)
                .setId("MemecioAudioSession")
                .build();
        } catch (Exception e) {
            mediaSession = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String uri = intent.getStringExtra("audio_uri");
            String title = intent.getStringExtra("audio_title");

            if (uri != null) {
                MediaItem mediaItem = MediaItem.fromUri(uri);
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
                if (title != null) currentTitle = title;
            }

            if (ACTION_PLAY.equals(action)) {
                exoPlayer.play();
            } else if (ACTION_PAUSE.equals(action)) {
                exoPlayer.pause();
            } else if (ACTION_NEXT.equals(action)) {
                exoPlayer.seekToNext();
            } else if (ACTION_PREV.equals(action)) {
                exoPlayer.seekToPrevious();
            } else if (ACTION_STOP.equals(action)) {
                exoPlayer.stop();
                stopSelf();
            }
        }
        return START_STICKY;
    }

    private String currentTitle = "Audio Memec.io";

    public void setTitle(String title) {
        this.currentTitle = title != null ? title : "Audio Memec.io";
    }

    private Notification buildNotification() {
        Intent openIntent = new Intent(this, AudioPlayerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        boolean isPlaying = exoPlayer != null && exoPlayer.isPlaying();

        // Action: Prev
        PendingIntent prevPending = PendingIntent.getService(
            this, 1, new Intent(this, AudioPlayerService.class).setAction(ACTION_PREV),
            Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        // Action: Play/Pause
        PendingIntent playPausePending = PendingIntent.getService(
            this, 2, new Intent(this, AudioPlayerService.class).setAction(isPlaying ? ACTION_PAUSE : ACTION_PLAY),
            Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        // Action: Next
        PendingIntent nextPending = PendingIntent.getService(
            this, 3, new Intent(this, AudioPlayerService.class).setAction(ACTION_NEXT),
            Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle(currentTitle)
               .setContentText(isPlaying ? "Sedang diputar" : "Dijeda")
               .setSmallIcon(android.R.drawable.ic_media_play)
               .setContentIntent(openPending)
               .setOngoing(isPlaying)
               .setShowWhen(false)
               .addAction(new Notification.Action.Builder(
                   android.R.drawable.ic_media_previous, "Previous", prevPending).build())
               .addAction(new Notification.Action.Builder(
                   isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                   isPlaying ? "Pause" : "Play", playPausePending).build())
               .addAction(new Notification.Action.Builder(
                   android.R.drawable.ic_media_next, "Next", nextPending).build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return builder.build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Audio Playback", NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestAudioFocus() {
        if (audioManager == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (focusRequest == null) {
                    focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setOnAudioFocusChangeListener(focusChange -> {
                            if (exoPlayer == null) return;
                            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                                exoPlayer.pause();
                            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                                exoPlayer.pause();
                            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                                exoPlayer.play();
                            }
                        })
                        .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                        .build();
                }
                audioManager.requestAudioFocus(focusRequest);
            } else {
                audioManager.requestAudioFocus(null,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        try {
            if (audioManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                    audioManager.abandonAudioFocusRequest(focusRequest);
                } else {
                    audioManager.abandonAudioFocus(null);
                }
            }
        } catch (Exception ignored) {}

        if (mediaSession != null) {
            try { mediaSession.release(); } catch (Exception ignored) {}
            mediaSession = null;
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
