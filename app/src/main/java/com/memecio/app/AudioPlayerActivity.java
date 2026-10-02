package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.Player;

public class AudioPlayerActivity extends Activity {

    private AudioPlayerService service;
    private boolean bound = false;
    private SeekBar seekBar;
    private TextView tvCurrent, tvDuration;
    private ImageView btnPlayPause;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean userSeeking = false;

    // Sleep Timer
    private Handler sleepHandler = new Handler(Looper.getMainLooper());
    private Runnable sleepTimerRunnable;
    private TextView tvSleepStatus;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            AudioPlayerService.LocalBinder lb = (AudioPlayerService.LocalBinder) binder;
            service = lb.getService();
            bound = true;
            setupPlayerListener();
            startProgressLoop();
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            bound = false;
        }
    };

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (service != null && service.getPlayer() != null && !userSeeking) {
                long pos = service.getPlayer().getCurrentPosition();
                long dur = service.getPlayer().getDuration();
                if (dur > 0) {
                    seekBar.setMax((int) dur);
                    seekBar.setProgress((int) pos);
                    tvCurrent.setText(formatTime((int) pos));
                    tvDuration.setText(formatTime((int) dur));
                }
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoundHelper.init(this);
        setContentView(R.layout.activity_audio_player);

        String title = getIntent().getStringExtra("audio_title");
        String uri = getIntent().getStringExtra("audio_uri");

        TextView tvTitle = findViewById(R.id.tvAudioTitle);
        tvTitle.setText(title != null ? title : "Audio");

        seekBar = findViewById(R.id.seekAudio);
        tvCurrent = findViewById(R.id.tvAudioCurrent);
        tvDuration = findViewById(R.id.tvAudioDuration);
        btnPlayPause = findViewById(R.id.btnAudioPlayPause);

        findViewById(R.id.btnBackAudio).setOnClickListener(v -> finish());
        findViewById(R.id.btnAudioPrev).setOnClickListener(v -> {
            if (service != null) service.getPlayer().seekToPrevious();
        });
        findViewById(R.id.btnAudioNext).setOnClickListener(v -> {
            if (service != null) service.getPlayer().seekToNext();
        });
        findViewById(R.id.btnAudioRewind).setOnClickListener(v -> {
            if (service != null) {
                long pos = service.getPlayer().getCurrentPosition() - 10000;
                service.getPlayer().seekTo(Math.max(0, pos));
            }
        });
        findViewById(R.id.btnAudioForward).setOnClickListener(v -> {
            if (service != null) {
                long pos = service.getPlayer().getCurrentPosition() + 10000;
                service.getPlayer().seekTo(Math.min(pos, service.getPlayer().getDuration()));
            }
        });

        // Sleep Timer
        tvSleepStatus = findViewById(R.id.btnAudioSleep);
        if (tvSleepStatus != null) {
            tvSleepStatus.setOnClickListener(v -> showSleepTimerDialog());
        }

        btnPlayPause.setOnClickListener(v -> {
            if (service != null) {
                if (service.getPlayer().isPlaying()) {
                    service.getPlayer().pause();
                } else {
                    service.getPlayer().play();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                if (fromUser) tvCurrent.setText(formatTime(p));
            }
            @Override public void onStartTrackingTouch(SeekBar s) { userSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar s) {
                userSeeking = false;
                if (service != null) service.getPlayer().seekTo(s.getProgress());
            }
        });

        // Mulai service dan bind
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        if (uri != null) {
            serviceIntent.putExtra("audio_uri", uri);
            serviceIntent.putExtra("audio_title", title);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(serviceIntent);
            else startService(serviceIntent);
        }
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void setupPlayerListener() {
        if (service == null) return;
        service.getPlayer().addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayPauseIcon(isPlaying);
            }
        });
    }

    private void updatePlayPauseIcon(boolean isPlaying) {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_w : R.drawable.ic_play_w);
    }

    private void updateUI() {
        if (service != null && service.getPlayer() != null) {
            updatePlayPauseIcon(service.getPlayer().isPlaying());
        }
    }

    private void startProgressLoop() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    private String formatTime(int ms) {
        int totalSec = ms / 1000;
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s);
    }

    private void showSleepTimerDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sleep_timer);

        android.view.Window w = dialog.getWindow();
        if (w != null) w.setBackgroundDrawableResource(android.R.color.transparent);

        android.view.View btn15 = dialog.findViewById(R.id.btnSleep15);
        android.view.View btn30 = dialog.findViewById(R.id.btnSleep30);
        android.view.View btn60 = dialog.findViewById(R.id.btnSleep60);
        android.view.View btn90 = dialog.findViewById(R.id.btnSleep90);
        android.view.View btnCancel = dialog.findViewById(R.id.btnSleepCancel);
        android.view.View btnBatal = dialog.findViewById(R.id.btnSleepBatal);

        if (btn15 != null) btn15.setOnClickListener(v -> { startSleepTimer(15); dialog.dismiss(); });
        if (btn30 != null) btn30.setOnClickListener(v -> { startSleepTimer(30); dialog.dismiss(); });
        if (btn60 != null) btn60.setOnClickListener(v -> { startSleepTimer(60); dialog.dismiss(); });
        if (btn90 != null) btn90.setOnClickListener(v -> { startSleepTimer(90); dialog.dismiss(); });
        if (btnCancel != null) btnCancel.setOnClickListener(v -> { stopSleepTimer(); dialog.dismiss(); });
        if (btnBatal != null) btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void startSleepTimer(int minutes) {
        stopSleepTimer();
        final long totalMs = minutes * 60L * 1000L;
        final long startTime = System.currentTimeMillis();

        sleepTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = totalMs - elapsed;
                if (remaining <= 0) {
                    try {
                        if (service != null && service.getPlayer() != null) {
                            service.getPlayer().pause();
                        }
                    } catch (Exception ignored) {}
                    if (tvSleepStatus != null) tvSleepStatus.setText("\uD83D\uDE34");
                    Toast.makeText(AudioPlayerActivity.this, "Sleep timer selesai - audio dijeda", Toast.LENGTH_LONG).show();
                    sleepTimerRunnable = null;
                    return;
                }
                int remMin = (int)(remaining / 60000);
                int remSec = (int)((remaining % 60000) / 1000);
                if (tvSleepStatus != null) {
                    tvSleepStatus.setText(String.format(java.util.Locale.getDefault(), "\u23F1 %d:%02d", remMin, remSec));
                }
                sleepHandler.postDelayed(this, 1000);
            }
        };
        sleepHandler.post(sleepTimerRunnable);
        Toast.makeText(this, "Sleep timer: " + minutes + " menit", Toast.LENGTH_SHORT).show();
    }

    private void stopSleepTimer() {
        if (sleepTimerRunnable != null) {
            sleepHandler.removeCallbacks(sleepTimerRunnable);
            sleepTimerRunnable = null;
        }
        if (tvSleepStatus != null) tvSleepStatus.setText("\uD83D\uDE34");
    }

    @Override
    protected void onDestroy() {
        stopSleepTimer();
        handler.removeCallbacks(progressRunnable);
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        super.onDestroy();
    }
}
