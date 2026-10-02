package com.memecio.app;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.media.AudioManager;
import android.view.WindowManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.ui.PlayerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VideoPlayerActivity extends Activity {

    private View btnCloseVideo;
    private ImageButton btnMute;
    private ImageButton btnPlayPause;
    private ImageButton btnPlayPauseCenter;
    private View centerControls;
    private View controlBar;
    private ArrayList<String> playlist;
    private SeekBar seekBar;
    private View topBar;
    private TextView tvSpeed;
    private TextView tvTimecode;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private Handler handler = new Handler();
    private Handler hideHandler = new Handler();
    private TextView tvJamVideoPlayer;
    private Runnable clockRunnable;
    private boolean isPlaying = true;
    private boolean isMuted = false;
    private boolean controlsVisible = true;
    private float currentSpeed = 1.0f;
    private int currentIndex = 0;
    private boolean isSeekBarTracking = false;
    private SharedPreferences prefPosisi;
    private boolean isLocked = false;
    private android.media.MediaMetadataRetriever previewRetriever;
    private float startY = 0f;
    private int touchZone = 1;
    private boolean adjustingVertical = false;
    private boolean speedBoostActive = false;
    private boolean longPressCandidate = false;
    private View gestureIndicator;
    private android.widget.ImageView gestureIcon;
    private android.widget.TextView gestureText;
    private android.widget.ProgressBar gestureBar;
    private AudioManager audioManager;
    private float startBrightness = 0.5f;
    private int startVolume = 0;
    private int maxVolume = 0;
    private Handler gestureHandler = new Handler();
    private Runnable speedBoostRunnable;
    private java.util.concurrent.ExecutorService previewExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private Handler previewHandler = new Handler();
    private java.util.concurrent.atomic.AtomicInteger previewRequestId = new java.util.concurrent.atomic.AtomicInteger(0);
    private Bitmap lastPreviewBitmap;
    private View previewContainer;
    private android.widget.ImageView previewImage;
    private TextView previewTime;
    private long autoSkipIntroMs = 0;
    private Runnable hideLockIndicatorRunnable;

    // Drawer
    private View drawerPlaylist;
    private ListView listVideoTitles;
    private TextView tvPlaylistName;
    private boolean drawerOpen = false;

    private Runnable updateSeekRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null && !isSeekBarTracking) {
                long current = exoPlayer.getCurrentPosition();
                long duration = exoPlayer.getDuration();
                if (duration > 0) {
                    seekBar.setMax((int) duration);
                    seekBar.setProgress((int) current);
                    tvTimecode.setText(formatTime((int) current) + " / " + formatTime((int) duration));
                }
            }
            handler.postDelayed(this, 500L);
        }
    };

    private Runnable hideControlsRunnable = new Runnable() {
        @Override
        public void run() { hideControls(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        SoundHelper.init(this);
        setContentView(R.layout.activity_video_player);

        // Init detail overlay
        setupDetailOverlay();

        prefPosisi = getSharedPreferences("memecio_playback", MODE_PRIVATE);
        SharedPreferences prefSet = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        autoSkipIntroMs = prefSet.getLong("auto_skip_intro", 0);

        playerView = findViewById(R.id.videoView);
        playerView.setUseController(false);
        seekBar = findViewById(R.id.seekBar);
        tvTimecode = findViewById(R.id.tvTimecode);
        tvSpeed = findViewById(R.id.tvSpeed);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPlayPauseCenter = findViewById(R.id.btnPlayPauseCenter);
        btnMute = findViewById(R.id.btnMute);
        btnCloseVideo = findViewById(R.id.btnCloseVideo);
        controlBar = findViewById(R.id.controlBar);
        centerControls = findViewById(R.id.centerControls);
        topBar = findViewById(R.id.topBar);
        drawerPlaylist = findViewById(R.id.drawerPlaylist);
        listVideoTitles = findViewById(R.id.listVideoTitles);
        tvPlaylistName = findViewById(R.id.tvPlaylistName);

        View btnFullscreen = findViewById(R.id.btnFullscreen);
        previewContainer = findViewById(R.id.previewContainer);
        gestureIndicator = findViewById(R.id.gestureIndicator);
        gestureIcon = findViewById(R.id.gestureIcon);
        gestureText = findViewById(R.id.gestureText);
        gestureBar = findViewById(R.id.gestureBar);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        previewImage = findViewById(R.id.previewImage);
        previewTime = findViewById(R.id.previewTime);
        View btnRewind = findViewById(R.id.btnRewind);
        View btnForward = findViewById(R.id.btnForward);
        View btnPrevious = findViewById(R.id.btnPrevious);
        View btnNext = findViewById(R.id.btnNext);
        View btnSettings = findViewById(R.id.btnSettings);
        View btnLock = findViewById(R.id.btnLock);
        View btnPlaylist = findViewById(R.id.btnPlaylist);
        View btnPlaylistPrev = findViewById(R.id.btnPlaylistPrev);
        View btnPlaylistNext = findViewById(R.id.btnPlaylistNext);

        // Buffer tuning untuk playback stabil
        LoadControl loadControl = new DefaultLoadControl.Builder()
            .setBufferDurationsMs(15000, 120000, 3000, 8000)
            .setTargetBufferBytes(50 * 1024 * 1024)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build();
        exoPlayer = new ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build();

        // Batasi resolusi max 1080p — hemat bandwidth
        try {
            TrackSelectionParameters trackParams = exoPlayer.getTrackSelectionParameters()
                .buildUpon()
                .setMaxVideoSize(1920, 1080)
                .build();
            exoPlayer.setTrackSelectionParameters(trackParams);
        } catch (Exception ignored) {}
        playerView.setPlayer(exoPlayer);

        exoPlayer.addListener(new Player.Listener() {

            @Override
            public void onVideoSizeChanged(androidx.media3.common.VideoSize videoSize) {
                Player.Listener.super.onVideoSizeChanged(videoSize);
                if (videoSize.width <= 0 || videoSize.height <= 0) return;
                int target;
                if (videoSize.width > videoSize.height) {
                    target = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
                } else {
                    target = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
                }
                if (getRequestedOrientation() != target) {
                    setRequestedOrientation(target);
                }
            }
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    seekBar.setMax((int) Math.max(exoPlayer.getDuration(), 0));
                    applySpeed();
                    handler.removeCallbacks(updateSeekRunnable);
                    handler.post(updateSeekRunnable);
                    tvJamVideoPlayer = findViewById(R.id.tvJamVideoPlayer);
                    if (tvJamVideoPlayer != null) {
                        clockRunnable = new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                tvJamVideoPlayer.setText(fmt.format(new Date()));
                                handler.postDelayed(this, 1000);
                            }
                        };
                        handler.post(clockRunnable);
                    }
                }
            }
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Gagal memutar video", Toast.LENGTH_SHORT).show();
            }
        });

        this.playlist = PlaylistHolder.get();
        this.currentIndex = getIntent().getIntExtra("index", 0);
        if (playlist == null || playlist.isEmpty()) {
            playlist = new ArrayList<>();
            ArrayList<String> fromIntent = getIntent().getStringArrayListExtra("playlist");
            if (fromIntent != null && !fromIntent.isEmpty()) playlist = fromIntent;
            String single = getIntent().getStringExtra("uri");
            if (single != null) playlist.add(single);
            if (playlist.isEmpty()) currentIndex = 0;
        }

        loadVideo(currentIndex);

        // Tampilkan overlay detail hanya saat fresh open (bukan recreate)
        if (savedInstanceState == null) {
            try {
                detailOverlay.postDelayed(() -> showDetailOverlayIfFirst(), 400);
            } catch (Exception ignored) {}
        }
        scheduleAutoHide();

        // Touch root: swipe + toggle
        View.OnTouchListener rootTouchListener = new View.OnTouchListener() {
            float startX = 0;
            long startTime = 0;
            boolean isLandscape = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isLocked) return true;
                if (handleZoomPan(event)) return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        startTime = System.currentTimeMillis();
                        isLandscape = getResources().getConfiguration().orientation
                            == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
                        if (isLandscape && v.getWidth() > 0) {
                            float ratio = event.getX() / v.getWidth();
                            if (ratio < 0.33f) touchZone = 0;
                            else if (ratio > 0.67f) touchZone = 2;
                            else touchZone = 1;
                        } else {
                            touchZone = 1;
                        }
                        adjustingVertical = false;
                        speedBoostActive = false;
                        longPressCandidate = (isLandscape && touchZone == 2);
                        if (longPressCandidate) {
                            if (speedBoostRunnable == null) {
                                speedBoostRunnable = () -> {
                                    if (!isLocked) {
                                        speedBoostActive = true;
                                        try { exoPlayer.setPlaybackParameters(new androidx.media3.common.PlaybackParameters(3.0f)); } catch (Exception ignored) {}
                                        showGestureIndicator(R.drawable.ic_forward_w, "3x", 100);
                                    }
                                };
                            }
                            gestureHandler.postDelayed(speedBoostRunnable, 600);
                        }
                        startBrightness = getWindow().getAttributes().screenBrightness;
                        if (startBrightness < 0) startBrightness = 0.5f;
                        if (audioManager != null) startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (!isLandscape || touchZone == 1) return true;
                        float dxMove = event.getX() - startX;
                        float dyMove = event.getY() - startY;
                        if (speedBoostActive) return true;
                        if (!adjustingVertical && Math.abs(dyMove) > 30 && Math.abs(dyMove) > Math.abs(dxMove) * 1.3f) {
                            adjustingVertical = true;
                            gestureHandler.removeCallbacks(speedBoostRunnable);
                            longPressCandidate = false;
                        }
                        if (adjustingVertical) {
                            float dens = getResources().getDisplayMetrics().density;
                            float deltaPx = (startY - event.getY());
                            float screenH = getResources().getDisplayMetrics().heightPixels;
                            float deltaRatio = deltaPx / screenH;
                            if (touchZone == 0) {
                                float newB = Math.max(0.01f, Math.min(1f, startBrightness + deltaRatio));
                                WindowManager.LayoutParams lp = getWindow().getAttributes();
                                lp.screenBrightness = newB;
                                getWindow().setAttributes(lp);
                                showGestureIndicator(android.R.drawable.ic_menu_view,
                                    ((int)(newB * 100)) + "%", (int)(newB * 100));
                            } else {
                                if (audioManager != null && maxVolume > 0) {
                                    int deltaVol = (int)(deltaRatio * maxVolume);
                                    int newV = Math.max(0, Math.min(maxVolume, startVolume + deltaVol));
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newV, 0);
                                    int pct = maxVolume > 0 ? (int)((newV * 100f) / maxVolume) : 0;
                                    int icon = newV == 0 ? R.drawable.ic_mute_w : R.drawable.ic_unmute_w;
                                    showGestureIndicator(icon, pct + "%", pct);
                                }
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        gestureHandler.removeCallbacks(speedBoostRunnable);
                        long dt = System.currentTimeMillis() - startTime;
                        if (isLocked) {
                            showLockIndicatorTemporarily();
                            return true;
                        }
                        if (speedBoostActive) {
                            speedBoostActive = false;
                            try { exoPlayer.setPlaybackParameters(new androidx.media3.common.PlaybackParameters(currentSpeed)); } catch (Exception e) {
                                try { exoPlayer.setPlaybackParameters(new androidx.media3.common.PlaybackParameters(currentSpeed)); } catch (Exception ignored) {}
                            }
                            hideGestureIndicator();
                            return true;
                        }
                        if (adjustingVertical) {
                            adjustingVertical = false;
                            hideGestureIndicator();
                            return true;
                        }
                        float dx = event.getX() - startX;
                        if (Math.abs(dx) > 150 && dt < 800) {
                            if (dx > 0) {
                                if (trySwitchToImage(-1)) return true;
                                if (currentIndex > 0) { currentIndex--; loadVideo(currentIndex); }
                                else Toast.makeText(VideoPlayerActivity.this, "Video pertama", Toast.LENGTH_SHORT).show();
                            } else {
                                if (trySwitchToImage(1)) return true;
                                if (currentIndex < playlist.size() - 1) { currentIndex++; loadVideo(currentIndex); }
                                else Toast.makeText(VideoPlayerActivity.this, "Video terakhir", Toast.LENGTH_SHORT).show();
                            }
                        } else if (Math.abs(dx) < 15 && dt < 300) {
                            toggleControls();
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        gestureHandler.removeCallbacks(speedBoostRunnable);
                        if (speedBoostActive) {
                            speedBoostActive = false;
                            try { exoPlayer.setPlaybackParameters(new androidx.media3.common.PlaybackParameters(currentSpeed)); } catch (Exception ignored) {}
                        }
                        adjustingVertical = false;
                        hideGestureIndicator();
                        return true;
                }
                return false;
            }
        };
        playerView.setOnTouchListener(rootTouchListener);

        btnCloseVideo.setOnClickListener(v -> finish());

        if (btnFullscreen != null) btnFullscreen.setOnClickListener(v -> toggleFullscreen());
        if (btnLock != null) btnLock.setOnClickListener(v -> toggleLock());

        View.OnClickListener togglePlay = v -> {
            if (isPlaying) {
                exoPlayer.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play_w);
                btnPlayPauseCenter.setImageResource(R.drawable.ic_play_w);
            } else {
                exoPlayer.play();
                btnPlayPause.setImageResource(R.drawable.ic_pause_w);
                btnPlayPauseCenter.setImageResource(R.drawable.ic_pause_w);
            }
            isPlaying = !isPlaying;
            scheduleAutoHide();
        };
        btnPlayPause.setOnClickListener(togglePlay);
        btnPlayPauseCenter.setOnClickListener(togglePlay);

        btnRewind.setOnClickListener(v -> {
            long pos = exoPlayer.getCurrentPosition() - 50000;
            exoPlayer.seekTo(Math.max(pos, 0));
            scheduleAutoHide();
        });
        btnForward.setOnClickListener(v -> {
            long pos = exoPlayer.getCurrentPosition() + 10000;
            exoPlayer.seekTo(Math.min(pos, exoPlayer.getDuration()));
            scheduleAutoHide();
        });
        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) { currentIndex--; loadVideo(currentIndex); }
            else Toast.makeText(VideoPlayerActivity.this, "Ini video pertama", Toast.LENGTH_SHORT).show();
            scheduleAutoHide();
        });
        btnNext.setOnClickListener(v -> {
            if (currentIndex < playlist.size() - 1) { currentIndex++; loadVideo(currentIndex); }
            else Toast.makeText(VideoPlayerActivity.this, "Ini video terakhir", Toast.LENGTH_SHORT).show();
            scheduleAutoHide();
        });

        btnMute.setOnClickListener(v -> {
            if (!isMuted) {
                exoPlayer.setVolume(0f);
                btnMute.setImageResource(R.drawable.ic_mute_w);
            } else {
                exoPlayer.setVolume(1f);
                btnMute.setImageResource(R.drawable.ic_unmute_w);
            }
            isMuted = !isMuted;
            scheduleAutoHide();
        });

        if (btnSettings != null) btnSettings.setOnClickListener(v -> { showPlayerMenu(v); scheduleAutoHide(); });

        // Drawer playlist
        if (btnPlaylist != null) btnPlaylist.setOnClickListener(v -> toggleDrawer());
        if (btnPlaylistPrev != null) btnPlaylistPrev.setOnClickListener(v -> {
            if (currentIndex > 0) { currentIndex--; loadVideo(currentIndex); }
        });
        if (btnPlaylistNext != null) btnPlaylistNext.setOnClickListener(v -> {
            if (currentIndex < playlist.size() - 1) { currentIndex++; loadVideo(currentIndex); }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    exoPlayer.seekTo(progress);
                    updatePreview(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTracking = true;
                scheduleAutoHide();
                try { seekBar.setThumb(getDrawable(R.drawable.bg_seekbar_thumb_drag)); } catch (Exception ignored) {}
                showPreviewStart();
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTracking = false;
                scheduleAutoHide();
                try { seekBar.setThumb(getDrawable(R.drawable.bg_seekbar_thumb_normal)); } catch (Exception ignored) {}
                hidePreviewEnd();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            SoundHelper.click();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int code = event.getKeyCode();
            if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) {
                SoundHelper.nav();
            } else if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) {
                SoundHelper.click();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void toggleDrawer() {
        if (drawerPlaylist == null) return;
        if (drawerOpen) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(drawerPlaylist, "translationY", 0f, -1500f);
            anim.setDuration(350);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    drawerPlaylist.setVisibility(View.GONE);
                    drawerOpen = false;
                }
            });
            anim.start();
        } else {
            drawerPlaylist.setVisibility(View.VISIBLE);
            drawerPlaylist.setTranslationY(-1500f);
            ObjectAnimator anim = ObjectAnimator.ofFloat(drawerPlaylist, "translationY", -1500f, 0f);
            anim.setDuration(400);
            anim.setInterpolator(new OvershootInterpolator(1.1f));
            anim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    drawerOpen = true;
                }
            });
            anim.start();
        }
    }

    private void updateDrawerContent() {
        if (listVideoTitles == null || tvPlaylistName == null) return;
        String nama = "Playlist";
        if (!playlist.isEmpty()) nama = "Video " + (currentIndex + 1) + " / " + playlist.size();
        tvPlaylistName.setText(nama);
        tvPlaylistName.setSelected(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = v.findViewById(android.R.id.text1);
                tv.setTextColor(0xFF1C1C1E);
                tv.setTextSize(13f);
                tv.setSingleLine(true);
                tv.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
                tv.setMarqueeRepeatLimit(-1);
                tv.setSelected(true);
                tv.setHorizontallyScrolling(true);
                return v;
            }
        };
        for (int i = 0; i < playlist.size(); i++) {
            adapter.add("  " + (i + 1) + ".  " + getNamaVideo(playlist.get(i)));
        }
        listVideoTitles.setAdapter(adapter);
        listVideoTitles.setSelection(currentIndex);
        listVideoTitles.setOnItemClickListener((parent, view, position, id) -> {
            currentIndex = position;
            loadVideo(currentIndex);
            toggleDrawer();
        });
    }

    private String getNamaVideo(String url) {
        try {
            Uri uri = Uri.parse(url);
            String last = uri.getLastPathSegment();
            if (last == null || last.trim().isEmpty()) return url;
            int dot = last.lastIndexOf('.');
            if (dot > 0) last = last.substring(0, dot);
            last = last.replace('_', ' ').replace('-', ' ').trim();
            return last.isEmpty() ? url : last;
        } catch (Exception e) {
            return url;
        }
    }

    private void showLockIndicatorTemporarily() {
        final View lockedIndicator = findViewById(R.id.tvLockedIndicator);
        if (lockedIndicator == null) return;
        lockedIndicator.setVisibility(View.VISIBLE);
        lockedIndicator.setAlpha(1f);
        if (hideLockIndicatorRunnable == null) {
            hideLockIndicatorRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLocked && lockedIndicator != null) {
                        android.animation.ObjectAnimator.ofFloat(lockedIndicator, "alpha", 1f, 0f)
                            .setDuration(400).start();
                    }
                }
            };
        }
        hideHandler.removeCallbacks(hideLockIndicatorRunnable);
        hideHandler.postDelayed(hideLockIndicatorRunnable, 2000);
    }

    private void toggleLock() {
        isLocked = !isLocked;
        final View lockedIndicator = findViewById(R.id.tvLockedIndicator);
        if (isLocked) {
            hideControls();
            if (topBar != null) topBar.setVisibility(View.GONE);
            if (controlBar != null) controlBar.setVisibility(View.GONE);
            if (centerControls != null) centerControls.setVisibility(View.GONE);
            if (lockedIndicator != null) {
                lockedIndicator.setOnClickListener(v -> toggleLock());
                showLockIndicatorTemporarily();
            }
            Toast.makeText(this, "Layar terkunci", Toast.LENGTH_SHORT).show();
        } else {
            showControls();
            if (topBar != null) topBar.setVisibility(View.VISIBLE);
            if (lockedIndicator != null) {
                lockedIndicator.setVisibility(View.GONE);
                lockedIndicator.setOnClickListener(null);
            }
            hideHandler.removeCallbacks(hideLockIndicatorRunnable);
            Toast.makeText(this, "Layar terbuka", Toast.LENGTH_SHORT).show();
        }
    }

    private void simpanKeTontonNanti() {
        try {
            String url = playlist.get(currentIndex);
            String judul = "Video " + (currentIndex + 1);
            WatchLaterStore.add(this, url, judul);
            Toast.makeText(this, "Ditambahkan ke Tonton Nanti", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleControls() {
        if (controlsVisible) hideControls();
        else { showControls(); scheduleAutoHide(); }
    }

    private void showControls() {
        controlsVisible = true;
        controlBar.setVisibility(View.VISIBLE);
        centerControls.setVisibility(View.VISIBLE);
        btnCloseVideo.setVisibility(View.VISIBLE);
        if (topBar != null) topBar.setVisibility(View.VISIBLE);
    }

    private void hideControls() {
        controlsVisible = false;
        controlBar.setVisibility(View.GONE);
        centerControls.setVisibility(View.GONE);
        btnCloseVideo.setVisibility(View.GONE);
        if (topBar != null) topBar.setVisibility(View.GONE);
    }

    private void scheduleAutoHide() {
        hideHandler.removeCallbacks(hideControlsRunnable);
        hideHandler.postDelayed(hideControlsRunnable, 4000L);
    }

    private String fixDriveUrl(String url) {
        if (url == null) return url;
        try {
            if (url.contains("drive.google.com/uc?export=download") || url.contains("drive.google.com/uc?id=")) {
                String fileId = null;
                if (url.contains("id=")) {
                    fileId = url.split("id=")[1].split("&")[0];
                }
                if (fileId != null) {
                    return "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&confirm=t";
                }
            }
        } catch (Exception ignored) {}
        return url;
    }


    private int getMixedIndexForCurrentVideo() {
        try {
            if (playlist == null || currentIndex < 0 || currentIndex >= playlist.size()) return -1;
            String currentUri = playlist.get(currentIndex);
            java.util.List<com.memecio.app.MediaItem> mixed = MixedPlaylistHolder.getItems();
            for (int i = 0; i < mixed.size(); i++) {
                if (mixed.get(i).uri.toString().equals(currentUri)) return i;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private boolean trySwitchToImage(int direction) {
        try {
            if (!MixedPlaylistHolder.isActive()) return false;
            int mixedIdx = getMixedIndexForCurrentVideo();
            if (mixedIdx < 0) return false;
            java.util.List<com.memecio.app.MediaItem> mixed = MixedPlaylistHolder.getItems();
            int newIdx = mixedIdx + direction;
            if (newIdx < 0 || newIdx >= mixed.size()) return false;
            com.memecio.app.MediaItem nextItem = mixed.get(newIdx);
            if (nextItem.type != com.memecio.app.MediaItem.TYPE_IMAGE) return false;

            MixedPlaylistHolder.setCurrentIndex(newIdx);
            simpanPosisi();
            Intent img = new Intent(this, PreviewImageActivity.class);
            img.putExtra("index", newIdx);
            startActivity(img);
            finish();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private View detailOverlay;
    private float zoomScale = 1f;
    private float zoomPanX = 0f, zoomPanY = 0f;
    private float lastTouchX = 0f, lastTouchY = 0f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector doubleTapDetector;
    private android.widget.ImageView detailPoster;
    private android.widget.ImageView detailPosterBg;
    private TextView detailTitle;
    private TextView detailDescription;
    private boolean detailShownOnce = false;

    private void setupDetailOverlay() {
        detailOverlay = findViewById(R.id.detailOverlay);
        detailPoster = findViewById(R.id.detailPoster);
        detailPosterBg = findViewById(R.id.detailPosterBg);
        detailTitle = findViewById(R.id.detailTitle);
        detailDescription = findViewById(R.id.detailDescription);

        if (detailOverlay == null) return;

        View btnClose = findViewById(R.id.detailBtnClose);
        View btnPlay = findViewById(R.id.detailBtnPlay);
        View btnDownload = findViewById(R.id.detailBtnDownload);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> hideDetailOverlay());
        }
        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> hideDetailOverlay());
        }
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                try {
                    if (playlist == null || currentIndex < 0 || currentIndex >= playlist.size()) {
                        Toast.makeText(VideoPlayerActivity.this, "Tidak ada video untuk diunduh", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String url = playlist.get(currentIndex);


                    // Ambil nama file dari judul
                    String filename = null;
                    try {
                        if (MixedPlaylistHolder.isActive()) {
                            com.memecio.app.MediaItem cur = MixedPlaylistHolder.getCurrent();
                            if (cur != null && cur.title != null && !cur.title.trim().isEmpty()) {
                                filename = cur.title;
                            }
                        }
                    } catch (Exception ignored) {}

                    if (filename == null || filename.trim().isEmpty()) {
                        // Ambil dari URL
                        filename = url;
                        int slash = filename.lastIndexOf('/');
                        if (slash >= 0 && slash < filename.length() - 1) {
                            filename = filename.substring(slash + 1);
                            int q = filename.indexOf('?');
                            if (q > 0) filename = filename.substring(0, q);
                        } else {
                            filename = "video_" + System.currentTimeMillis() + ".mp4";
                        }
                    }

                    // Tambah ekstensi kalau belum ada
                    if (!filename.contains(".")) {
                        if (url.toLowerCase().contains(".mp3")) filename += ".mp3";
                        else if (url.toLowerCase().contains(".m3u8")) filename += ".mp4";
                        else filename += ".mp4";
                    }

                    DownloadHelper.startDownload(VideoPlayerActivity.this, url, filename);
                } catch (Exception e) {
                    Toast.makeText(VideoPlayerActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showDetailOverlayIfFirst() {
        if (detailShownOnce) return;
        if (detailOverlay == null) return;
        detailShownOnce = true;
        try {
            // Judul
            String judul = "Video";
            if (playlist != null && currentIndex >= 0 && currentIndex < playlist.size()) {
                String url = playlist.get(currentIndex);
                if (url != null) {
                    int slash = url.lastIndexOf('/');
                    if (slash >= 0 && slash < url.length() - 1) {
                        judul = url.substring(slash + 1);
                        int q = judul.indexOf('?');
                        if (q > 0) judul = judul.substring(0, q);
                        int dot = judul.lastIndexOf('.');
                        if (dot > 0) judul = judul.substring(0, dot);
                        judul = judul.replace('_', ' ').replace('-', ' ');
                    }
                }
            }

            final String urlVideo = (playlist != null && currentIndex >= 0 && currentIndex < playlist.size())
                ? playlist.get(currentIndex) : null;

            // Cari thumbUrl dengan berbagai cara
            String thumbUrl = null;
            String titleDariMediaItem = null;

            // Cara 1: MixedPlaylistHolder
            try {
                if (MixedPlaylistHolder.isActive()) {
                    com.memecio.app.MediaItem cur = MixedPlaylistHolder.getCurrent();
                    if (cur != null) {
                        if (cur.thumbUrl != null && !cur.thumbUrl.isEmpty()) thumbUrl = cur.thumbUrl;
                        if (cur.title != null && !cur.title.trim().isEmpty()) titleDariMediaItem = cur.title;
                    }
                }
            } catch (Exception ignored) {}

            // Cara 2: ExternalMediaStore
            if ((thumbUrl == null || titleDariMediaItem == null) && urlVideo != null) {
                try {
                    java.util.List<com.memecio.app.MediaItem> ext = ExternalMediaStore.getAll(VideoPlayerActivity.this);
                    for (com.memecio.app.MediaItem m : ext) {
                        if (m.uri != null && m.uri.toString().equals(urlVideo)) {
                            if (thumbUrl == null && m.thumbUrl != null && !m.thumbUrl.isEmpty()) {
                                thumbUrl = m.thumbUrl;
                            }
                            if (titleDariMediaItem == null && m.title != null && !m.title.trim().isEmpty()) {
                                titleDariMediaItem = m.title;
                            }
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Pakai judul dari MediaItem kalau ada
            if (titleDariMediaItem != null && !titleDariMediaItem.trim().isEmpty()) {
                judul = titleDariMediaItem;
            }

            if (detailTitle != null) detailTitle.setText(judul);
            if (detailDescription != null) detailDescription.setText("Memuat deskripsi...");

            final String finalThumbUrl = thumbUrl;

            if (detailPoster != null) {
                // Reset ke ikon play dulu
                try {
                    int pad = (int)(40 * getResources().getDisplayMetrics().density);
                    detailPoster.setPadding(pad, pad, pad, pad);
                    detailPoster.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                    detailPoster.setImageResource(android.R.drawable.ic_media_play);
                    if (detailPosterBg != null) detailPosterBg.setImageDrawable(null);
                } catch (Exception ignored) {}

                if (finalThumbUrl != null && !finalThumbUrl.isEmpty()) {
                    loadPosterFromM3uLogo(finalThumbUrl);
                } else if (urlVideo != null && (urlVideo.startsWith("content://") || urlVideo.startsWith("file://"))) {
                    loadPosterFromVideoFile(urlVideo);
                }
            }

            // Fetch deskripsi async
            VideoDescriptionFetcher.fetch(VideoPlayerActivity.this, urlVideo, judul, desc -> {
                if (detailDescription != null && desc != null && !desc.isEmpty()) {
                    detailDescription.setText(desc);
                }
            });

            detailOverlay.setVisibility(View.VISIBLE);
            detailOverlay.setAlpha(0f);
            detailOverlay.animate().alpha(1f).setDuration(250).start();
        } catch (Exception ignored) {}
    }

    private void hideDetailOverlay() {
        if (detailOverlay == null) return;
        detailOverlay.animate().alpha(0f).setDuration(200)
            .withEndAction(() -> {
                if (detailOverlay != null) detailOverlay.setVisibility(View.GONE);
            })
            .start();
    }

    public int getCurrentIndexSafe() {
        return currentIndex;
    }

    public void pauseForPrivacy() {
        try {
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                exoPlayer.pause();
                isPlaying = false;
            }
        } catch (Exception ignored) {}
    }

    private void loadPosterFromUrl(final String url) {
        if (url == null || url.isEmpty()) return;
        new Thread(() -> {
            android.graphics.Bitmap bmp = null;
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setInstanceFollowRedirects(true);
                java.io.InputStream is = conn.getInputStream();
                bmp = android.graphics.BitmapFactory.decodeStream(is);
                is.close();
                conn.disconnect();
            } catch (Exception ignored) {}
            final android.graphics.Bitmap fb = bmp;
            if (fb != null) {
                runOnUiThread(() -> {
                    try {
                        if (detailPoster != null) {
                            detailPoster.setPadding(0, 0, 0, 0);
                            detailPoster.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                            detailPoster.setImageBitmap(fb);
                        }
                        if (detailPosterBg != null) {
                            android.graphics.Bitmap blurred = blurBitmap(fb, 20f);
                            if (blurred != null) {
                                detailPosterBg.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                                detailPosterBg.setImageBitmap(blurred);
                            }
                        }
                    } catch (Exception ignored) {}
                });
            }
        }).start();
    }

    private void loadPosterFromVideoFile(final String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) return;
        new Thread(() -> {
            android.graphics.Bitmap bmp = null;

            // Cara 1: kalau content:// → pakai MediaStore (reliable)
            if (videoUrl.startsWith("content://")) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(videoUrl);
                    long id = Long.parseLong(uri.getLastPathSegment());
                    bmp = android.provider.MediaStore.Video.Thumbnails.getThumbnail(
                        getContentResolver(), id,
                        android.provider.MediaStore.Video.Thumbnails.MINI_KIND, null);
                } catch (Exception ignored) {}
            }

            // Cara 2: MediaMetadataRetriever (fallback)
            if (bmp == null) {
                android.media.MediaMetadataRetriever mmr = null;
                try {
                    mmr = new android.media.MediaMetadataRetriever();
                    mmr.setDataSource(VideoPlayerActivity.this, android.net.Uri.parse(videoUrl));
                    bmp = mmr.getFrameAtTime(2 * 1000000L,
                        android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (bmp == null) {
                        bmp = mmr.getFrameAtTime(0,
                            android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    }
                } catch (Exception ignored) {
                } finally {
                    try { if (mmr != null) mmr.release(); } catch (Exception ignored) {}
                }
            }

            final android.graphics.Bitmap fb = bmp;
            if (fb != null) {
                runOnUiThread(() -> {
                    try {
                        if (detailPoster != null) {
                            detailPoster.setPadding(0, 0, 0, 0);
                            detailPoster.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                            detailPoster.setImageBitmap(fb);
                        }
                    } catch (Exception ignored) {}
                });
            }
        }).start();
    }

    private void loadPosterFromM3uLogo(final String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) return;
        new Thread(() -> {
            android.graphics.Bitmap bmp = null;
            try {
                java.net.URL u = new java.net.URL(logoUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "VLC/3.0.16");
                conn.setInstanceFollowRedirects(true);
                java.io.InputStream is = conn.getInputStream();
                bmp = android.graphics.BitmapFactory.decodeStream(is);
                is.close();
                conn.disconnect();
            } catch (Exception ignored) {}
            final android.graphics.Bitmap fb = bmp;
            if (fb != null) {
                runOnUiThread(() -> {
                    try {
                        if (detailPoster != null) {
                            detailPoster.setPadding(0, 0, 0, 0);
                            detailPoster.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                            detailPoster.setImageBitmap(fb);
                        }
                        // Background blur
                        if (detailPosterBg != null) {
                            android.graphics.Bitmap blurred = blurBitmap(fb, 20f);
                            if (blurred != null) {
                                detailPosterBg.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                                detailPosterBg.setImageBitmap(blurred);
                            }
                        }
                    } catch (Exception ignored) {}
                });
            }
        }).start();
    }

    private android.graphics.Bitmap blurBitmap(android.graphics.Bitmap src, float radius) {
        try {
            // Fallback universal: downscale drastis + upscale (efek blur)
            int scale = 25;
            int w = Math.max(1, src.getWidth() / scale);
            int h = Math.max(1, src.getHeight() / scale);
            android.graphics.Bitmap small = android.graphics.Bitmap.createScaledBitmap(src, w, h, true);
            android.graphics.Bitmap result = android.graphics.Bitmap.createScaledBitmap(small, src.getWidth(), src.getHeight(), true);
            if (small != result && !small.isRecycled()) small.recycle();
            return result;
        } catch (Exception e) {
            return src;
        }
    }


    private void loadVideo(int index) {
        resetZoom();
        if (index < 0 || index >= playlist.size()) return;
        try {
            String originalUrl = playlist.get(index);
            String fixedUrl = fixDriveUrl(originalUrl);
            Uri uri = Uri.parse(fixedUrl);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();

            long lastPos = prefPosisi.getLong("pos_" + playlist.get(index), 0);
            if (lastPos > 3000) exoPlayer.seekTo(lastPos);
            else if (autoSkipIntroMs > 0) exoPlayer.seekTo(autoSkipIntroMs);

            exoPlayer.play();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause_w);
            btnPlayPauseCenter.setImageResource(R.drawable.ic_pause_w);
            ViewCountStore.tambah(this, playlist.get(index));
            updateDrawerContent();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memuat video", Toast.LENGTH_SHORT).show();
            if (index < playlist.size() - 1) loadVideo(index + 1);
            else finish();
        }
    }

    private void simpanPosisi() {
        try {
            if (exoPlayer != null && currentIndex < playlist.size()) {
                long pos = exoPlayer.getCurrentPosition();
                prefPosisi.edit().putLong("pos_" + playlist.get(currentIndex), pos).apply();
            }
        } catch (Exception ignored) {}
    }

    private void showPlayerMenu(View anchor) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_player_menu);

        android.view.Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.menuPip).setOnClickListener(v -> { dialog.dismiss(); enterPip(); });

        dialog.findViewById(R.id.menuAddPlaylist).setOnClickListener(v -> {
            dialog.dismiss();
            showAddToPlaylistDialog();
        });

        dialog.findViewById(R.id.menuSpeed).setOnClickListener(v -> {
            dialog.dismiss();
            showSpeedDialog();
        });

        dialog.findViewById(R.id.menuAutoSkip).setOnClickListener(v -> {
            dialog.dismiss();
            showAutoSkipDialog();
        });

        View menuSleep = dialog.findViewById(R.id.menuSleepTimer);
        if (menuSleep != null) {
            menuSleep.setOnClickListener(v -> {
                dialog.dismiss();
                showVideoSleepTimer();
            });
        }

        dialog.show();
    }

    private android.os.Handler videoSleepHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable videoSleepRunnable;

    private void showVideoSleepTimer() {
        final String[] items = {"15 menit", "30 menit", "60 menit", "90 menit", "Matikan Sleep Timer"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Sleep Timer")
            .setItems(items, (d, which) -> {
                if (which == 4) {
                    stopVideoSleepTimer();
                    Toast.makeText(this, "Sleep timer dimatikan", Toast.LENGTH_SHORT).show();
                } else {
                    int minutes = 15;
                    if (which == 1) minutes = 30;
                    else if (which == 2) minutes = 60;
                    else if (which == 3) minutes = 90;
                    startVideoSleepTimer(minutes);
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void startVideoSleepTimer(int minutes) {
        stopVideoSleepTimer();
        final long totalMs = minutes * 60L * 1000L;
        final long startTime = System.currentTimeMillis();
        Toast.makeText(this, "Sleep timer: " + minutes + " menit", Toast.LENGTH_SHORT).show();

        videoSleepRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = totalMs - elapsed;
                if (remaining <= 0) {
                    // Waktu habis: pause video + tutup player + redirect ke kalkulator
                    try {
                        if (exoPlayer != null) exoPlayer.pause();
                    } catch (Exception ignored) {}
                    try {
                        SessionState.markInternalTransition();
                        Intent i = new Intent(VideoPlayerActivity.this, CalculatorActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                    } catch (Exception ignored) {}
                    finish();
                    return;
                }
                int remMin = (int)(remaining / 60000);
                int remSec = (int)((remaining % 60000) / 1000);
                try { setTitle(String.format(java.util.Locale.getDefault(), "Sleep: %d:%02d", remMin, remSec)); } catch (Exception ignored) {}
                videoSleepHandler.postDelayed(this, 1000);
            }
        };
        videoSleepHandler.post(videoSleepRunnable);
    }

    private void stopVideoSleepTimer() {
        if (videoSleepRunnable != null) {
            videoSleepHandler.removeCallbacks(videoSleepRunnable);
            videoSleepRunnable = null;
        }
    }

    private void showAddToPlaylistDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_playlist);

        android.widget.ListView listView = dialog.findViewById(R.id.listPlaylistChoice);
        android.widget.TextView tvEmpty = dialog.findViewById(R.id.tvEmptyPlaylist);

        final java.util.List<CustomPlaylistStore.Playlist> playlists = CustomPlaylistStore.getAll(this);

        if (playlists.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            listView.setVisibility(android.view.View.GONE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            listView.setVisibility(android.view.View.VISIBLE);

            android.widget.ArrayAdapter<CustomPlaylistStore.Playlist> adapter =
                new android.widget.ArrayAdapter<CustomPlaylistStore.Playlist>(this, 0, playlists) {
                    @Override
                    public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                        android.view.View v = convertView;
                        if (v == null) v = getLayoutInflater().inflate(R.layout.item_playlist_choice, parent, false);
                        CustomPlaylistStore.Playlist pl = getItem(position);
                        android.widget.TextView tvName = v.findViewById(R.id.tvPlaylistChoiceName);
                        android.widget.TextView tvCount = v.findViewById(R.id.tvPlaylistChoiceCount);
                        if (pl != null) {
                            tvName.setText(pl.name);
                            tvCount.setText(pl.items.size() + " item");
                        }
                        return v;
                    }
                };
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                addCurrentToPlaylist(playlists.get(position));
                dialog.dismiss();
            });
        }

        dialog.findViewById(R.id.btnBuatPlaylistBaru).setOnClickListener(v -> {
            dialog.dismiss();
            showCreatePlaylistDialog();
        });

        dialog.findViewById(R.id.btnBatalPlaylist).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addCurrentToPlaylist(CustomPlaylistStore.Playlist playlist) {
        try {
            if (playlist == null) return;
            java.util.List<CustomPlaylistStore.Playlist> all = CustomPlaylistStore.getAll(this);
            for (CustomPlaylistStore.Playlist pl : all) {
                if (pl.name.equals(playlist.name)) {
                    if (currentIndex >= 0 && currentIndex < VideoPlayerActivity.this.playlist.size()) {
                        String uri = VideoPlayerActivity.this.playlist.get(currentIndex);
                        com.memecio.app.MediaItem item = new com.memecio.app.MediaItem(android.net.Uri.parse(uri), com.memecio.app.MediaItem.TYPE_VIDEO);
                        item.title = uri;
                        pl.items.add(item);
                        CustomPlaylistStore.saveAll(this, all);
                        android.widget.Toast.makeText(this, "Ditambahkan ke " + pl.name, android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Gagal menambahkan", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreatePlaylistDialog() {
        final android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle("Playlist Baru");
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nama playlist");
        b.setView(input);
        b.setPositiveButton("Buat", (d, w) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                android.widget.Toast.makeText(this, "Nama kosong", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.List<CustomPlaylistStore.Playlist> all = CustomPlaylistStore.getAll(this);
            CustomPlaylistStore.Playlist pl = new CustomPlaylistStore.Playlist();
            pl.name = name;
            if (currentIndex >= 0 && currentIndex < VideoPlayerActivity.this.playlist.size()) {
                String uri = VideoPlayerActivity.this.playlist.get(currentIndex);
                com.memecio.app.MediaItem item = new com.memecio.app.MediaItem(android.net.Uri.parse(uri), com.memecio.app.MediaItem.TYPE_VIDEO);
                item.title = uri;
                pl.items.add(item);
            }
            all.add(pl);
            CustomPlaylistStore.saveAll(this, all);
            android.widget.Toast.makeText(this, "Playlist '" + name + "' dibuat", android.widget.Toast.LENGTH_SHORT).show();
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private void showSpeedDialog() {
        final String[] speeds = {"0.5x", "1x", "1.5x", "2x", "3x"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Kecepatan Playback")
            .setItems(speeds, (d, which) -> {
                float v = 1.0f;
                if (which == 0) v = 0.5f;
                else if (which == 1) v = 1.0f;
                else if (which == 2) v = 1.5f;
                else if (which == 3) v = 2.0f;
                else if (which == 4) v = 3.0f;
                applySpeedValue(v);
            })
            .show();
    }

    private void showAutoSkipDialog() {
        final android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etIntro = new android.widget.EditText(this);
        etIntro.setHint("Detik intro (contoh: 15)");
        etIntro.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etIntro);

        final android.widget.EditText etOutro = new android.widget.EditText(this);
        etOutro.setHint("Detik outro (contoh: 30)");
        etOutro.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etOutro);

        android.content.SharedPreferences prefSet = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        long introSec = prefSet.getLong("auto_skip_intro", 0) / 1000;
        long outroSec = prefSet.getLong("auto_skip_outro", 0) / 1000;
        if (introSec > 0) etIntro.setText(String.valueOf(introSec));
        if (outroSec > 0) etOutro.setText(String.valueOf(outroSec));

        new android.app.AlertDialog.Builder(this)
            .setTitle("Auto Skip Intro/Outro")
            .setView(layout)
            .setPositiveButton("Simpan", (d, w) -> {
                long inVal = 0, outVal = 0;
                try { inVal = Long.parseLong(etIntro.getText().toString().trim()); } catch (Exception ignored) {}
                try { outVal = Long.parseLong(etOutro.getText().toString().trim()); } catch (Exception ignored) {}
                prefSet.edit().putLong("auto_skip_intro", inVal * 1000).putLong("auto_skip_outro", outVal * 1000).apply();
                autoSkipIntroMs = inVal * 1000;
                android.widget.Toast.makeText(this, "Intro " + inVal + "s, Outro " + outVal + "s", android.widget.Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void showSettingsDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_set);

        android.widget.TabHost tabHost = dialog.findViewById(android.R.id.tabhost);
        tabHost.setup();

        android.widget.TabHost.TabSpec speedTab = tabHost.newTabSpec("Speed");
        speedTab.setIndicator("Speed");
        speedTab.setContent(R.id.tabSpeedContent);
        tabHost.addTab(speedTab);

        android.widget.TabHost.TabSpec skipTab = tabHost.newTabSpec("Auto-Skip");
        skipTab.setIndicator("Auto-Skip");
        skipTab.setContent(R.id.tabAutoSkipContent);
        tabHost.addTab(skipTab);

        LinearLayout speedContent = dialog.findViewById(R.id.tabSpeedContent);
        View speedView = getLayoutInflater().inflate(R.layout.tab_speed, speedContent, false);
        speedContent.addView(speedView);

        speedView.findViewById(R.id.btnSpeed05).setOnClickListener(v -> { applySpeedValue(0.5f); dialog.dismiss(); });
        speedView.findViewById(R.id.btnSpeed1).setOnClickListener(v -> { applySpeedValue(1.0f); dialog.dismiss(); });
        speedView.findViewById(R.id.btnSpeed15).setOnClickListener(v -> { applySpeedValue(1.5f); dialog.dismiss(); });
        speedView.findViewById(R.id.btnSpeed2).setOnClickListener(v -> { applySpeedValue(2.0f); dialog.dismiss(); });
        speedView.findViewById(R.id.btnSpeed3).setOnClickListener(v -> { applySpeedValue(3.0f); dialog.dismiss(); });

        LinearLayout skipContent = dialog.findViewById(R.id.tabAutoSkipContent);
        View skipView = getLayoutInflater().inflate(R.layout.tab_auto_skip, skipContent, false);
        skipContent.addView(skipView);

        final EditText etIntro = skipView.findViewById(R.id.etSkipIntro);
        final EditText etOutro = skipView.findViewById(R.id.etSkipOutro);
        Button btnBatal = skipView.findViewById(R.id.btnSkipBatal);
        Button btnSimpan = skipView.findViewById(R.id.btnSkipSimpan);

        SharedPreferences prefSet = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        long introSec = prefSet.getLong("auto_skip_intro", 0) / 1000;
        long outroSec = prefSet.getLong("auto_skip_outro", 0) / 1000;
        if (introSec > 0) etIntro.setText(String.valueOf(introSec));
        if (outroSec > 0) etOutro.setText(String.valueOf(outroSec));

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            long introVal = 0;
            long outroVal = 0;
            try { introVal = Long.parseLong(etIntro.getText().toString().trim()); } catch (Exception ignored) {}
            try { outroVal = Long.parseLong(etOutro.getText().toString().trim()); } catch (Exception ignored) {}

            prefSet.edit()
                .putLong("auto_skip_intro", introVal * 1000)
                .putLong("auto_skip_outro", outroVal * 1000)
                .apply();

            autoSkipIntroMs = introVal * 1000;
            Toast.makeText(VideoPlayerActivity.this,
                "Auto-Skip: Intro " + introVal + "s, Outro " + outroVal + "s",
                Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applySpeedValue(float value) {
        currentSpeed = value;
        applySpeed();
        Toast.makeText(this, "Kecepatan: " + value + "x", Toast.LENGTH_SHORT).show();
    }

    private void showSpeedMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("0.5x");
        popup.getMenu().add("1x");
        popup.getMenu().add("1.5x");
        popup.getMenu().add("2x");
        popup.getMenu().add("3x");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("0.5x")) currentSpeed = 0.5f;
            else if (title.equals("1x")) currentSpeed = 1.0f;
            else if (title.equals("1.5x")) currentSpeed = 1.5f;
            else if (title.equals("2x")) currentSpeed = 2.0f;
            else if (title.equals("3x")) currentSpeed = 3.0f;
            applySpeed();
            return true;
        });
        popup.show();
    }

    private void applySpeed() {
        tvSpeed.setText(currentSpeed + "x");
        try { exoPlayer.setPlaybackParameters(new PlaybackParameters(currentSpeed)); } catch (Exception ignored) {}
    }

    private boolean isFullscreen = false;

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        if (isFullscreen) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (exoPlayer != null) {
                androidx.media3.common.VideoSize vs = exoPlayer.getVideoSize();
                if (vs.width > 0 && vs.height > 0) {
                    if (vs.width > vs.height) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    }
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        }
    }

    private void enterPip() {
        if (Build.VERSION.SDK_INT >= 26) {
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9)).build();
            enterPictureInPictureMode(params);
        } else {
            Toast.makeText(this, "PiP tidak didukung", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override protected void onPause() { super.onPause(); simpanPosisi(); }
    @Override protected void onStop() { super.onStop(); simpanPosisi(); }

    private final Runnable gestureHideRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (gestureIndicator != null) gestureIndicator.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        }
    };

    private void applyZoomPan() {
        if (playerView == null) return;
        playerView.setScaleX(zoomScale);
        playerView.setScaleY(zoomScale);
        playerView.setTranslationX(zoomPanX);
        playerView.setTranslationY(zoomPanY);
    }

    private void resetZoom() {
        zoomScale = 1f;
        zoomPanX = 0f;
        zoomPanY = 0f;
        applyZoomPan();
    }

    private boolean handleZoomPan(MotionEvent event) {
        // Pinch 2 jari
        if (event.getPointerCount() >= 2) {
            if (scaleDetector == null) {
                scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector d) {
                        float ns = zoomScale * d.getScaleFactor();
                        zoomScale = Math.max(1f, Math.min(4f, ns));
                        applyZoomPan();
                        int pct = (int)(zoomScale * 100);
                        showGestureIndicator(android.R.drawable.ic_menu_crop, pct + "%", Math.min(100, pct / 4));
                        return true;
                    }
                });
            }
            scaleDetector.onTouchEvent(event);
            return true;
        }

        // Pan (zoom > 1) + double tap reset
        if (zoomScale > 1.01f) {
            if (doubleTapDetector == null) {
                doubleTapDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        resetZoom();
                        android.widget.Toast.makeText(VideoPlayerActivity.this, "Zoom direset", android.widget.Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
            doubleTapDetector.onTouchEvent(event);
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
                zoomPanX += event.getX() - lastTouchX;
                zoomPanY += event.getY() - lastTouchY;
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                applyZoomPan();
                return true;
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                return true;
            }
            return true;
        }

        return false;
    }

    private void showGestureIndicator(int iconRes, String text, int progress) {
        try {
            if (gestureIndicator == null) return;
            gestureIndicator.setVisibility(View.VISIBLE);
            gestureIndicator.setAlpha(1f);
            if (gestureIcon != null) gestureIcon.setImageResource(iconRes);
            if (gestureText != null) gestureText.setText(text);
            if (gestureBar != null) gestureBar.setProgress(progress);
            gestureHandler.removeCallbacks(gestureHideRunnable);
            gestureHandler.postDelayed(gestureHideRunnable, 1200);
        } catch (Exception ignored) {}
    }

    private void hideGestureIndicator() {
        try {
            if (gestureIndicator != null) gestureIndicator.setVisibility(View.GONE);
        } catch (Exception ignored) {}
    }

    private void showPreviewStart() {
        if (previewContainer == null) return;
        initPreviewRetrieverIfNeeded();
        previewContainer.setVisibility(View.VISIBLE);
        previewContainer.setAlpha(0f);
        previewContainer.animate().alpha(1f).setDuration(150).start();
    }

    private void hidePreviewEnd() {
        if (previewContainer == null) return;
        previewContainer.animate().alpha(0f).setDuration(150)
            .withEndAction(() -> { if (previewContainer != null) previewContainer.setVisibility(View.GONE); })
            .start();
    }

    private void initPreviewRetrieverIfNeeded() {
        try {
            if (previewRetriever != null) return;
            if (playlist == null || currentIndex < 0 || currentIndex >= playlist.size()) return;
            String url = playlist.get(currentIndex);
            if (url == null) return;
            if (url.startsWith("http://") || url.startsWith("https://")) return;
            String fixed = fixDriveUrl(url);
            previewRetriever = new android.media.MediaMetadataRetriever();
            previewRetriever.setDataSource(this, android.net.Uri.parse(fixed));
        } catch (Exception e) {
            previewRetriever = null;
        }
    }

    private void updatePreview(int progressMs) {
        if (previewTime != null) previewTime.setText(formatTime(progressMs));
        positionPreview(progressMs);
        extractFrameAsync(progressMs);
    }

    private void extractFrameAsync(final int progressMs) {
        if (previewRetriever == null) return;
        final int myId = previewRequestId.incrementAndGet();
        try {
            previewExecutor.execute(() -> {
                Bitmap bmp = null;
                try {
                    if (myId == previewRequestId.get() && previewRetriever != null) {
                        bmp = previewRetriever.getFrameAtTime(progressMs * 1000L,
                            android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    }
                } catch (Exception ignored) {}
                if (myId != previewRequestId.get()) {
                    if (bmp != null) bmp.recycle();
                    return;
                }
                final Bitmap fb = bmp;
                previewHandler.post(() -> {
                    if (myId != previewRequestId.get()) {
                        if (fb != null) fb.recycle();
                        return;
                    }
                    if (previewImage != null && fb != null) {
                        if (lastPreviewBitmap != null && !lastPreviewBitmap.isRecycled()) {
                            lastPreviewBitmap.recycle();
                        }
                        lastPreviewBitmap = fb;
                        previewImage.setImageBitmap(fb);
                    }
                });
            });
        } catch (Exception ignored) {}
    }

    private void positionPreview(int progressMs) {
        if (previewContainer == null || seekBar == null) return;
        int max = seekBar.getMax();
        if (max <= 0) return;

        int pw = previewContainer.getWidth();
        if (pw == 0) pw = (int)(120 * getResources().getDisplayMetrics().density);
        int ph = previewContainer.getHeight();
        if (ph == 0) ph = (int)(90 * getResources().getDisplayMetrics().density);

        int[] seekLoc = new int[2];
        seekBar.getLocationInWindow(seekLoc);
        int seekWidth = seekBar.getWidth();

        float ratio = (float) progressMs / max;
        float thumbCenter = seekLoc[0] + ratio * seekWidth;

        float x = thumbCenter - pw / 2.0f;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        if (x < 8) x = 8;
        if (x + pw > screenWidth - 8) x = screenWidth - pw - 8;

        float y = seekLoc[1] - ph - 8;
        if (y < 8) y = 8;

        previewContainer.setX(x);
        previewContainer.setY(y);
    }

    protected void onDestroy() {
        super.onDestroy();
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);simpanPosisi();
        handler.removeCallbacks(updateSeekRunnable);
        hideHandler.removeCallbacks(hideControlsRunnable);
        if (clockRunnable != null) handler.removeCallbacks(clockRunnable);
        if (exoPlayer != null) { exoPlayer.release(); exoPlayer = null; }
        try {
            if (previewRetriever != null) { previewRetriever.release(); previewRetriever = null; }
        } catch (Exception ignored) {}
        try {
            if (lastPreviewBitmap != null && !lastPreviewBitmap.isRecycled()) lastPreviewBitmap.recycle();
            lastPreviewBitmap = null;
        } catch (Exception ignored) {}
        try { previewExecutor.shutdownNow(); } catch (Exception ignored) {}
    }
}
