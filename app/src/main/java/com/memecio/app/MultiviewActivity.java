package com.memecio.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;

public class MultiviewActivity extends Activity {

    private ExoPlayer[] players = new ExoPlayer[4];
    private PlayerView[] views = new PlayerView[4];
    private FrameLayout[] slots = new FrameLayout[4];
    private TextView[] titles = new TextView[4];
    private ImageView[] badges = new ImageView[4];
    private ImageButton[] closes = new ImageButton[4];
    private ImageView[] images = new ImageView[4];
    private ImageButton[] playPauseBtns = new ImageButton[4];
    private ImageButton[] muteBtns = new ImageButton[4];
    private ImageButton[] fullscreenBtns = new ImageButton[4];
    private ImageButton[] replayBtns = new ImageButton[4];

    private String[] types = new String[4];
    private String[] urls = new String[4];
    private boolean[] muted = new boolean[4];
    private boolean[] controlsVisible = new boolean[4];

    private int activeAudioSlot = 0;
    private boolean isBigLayout = false;
    private final Handler hideHandler = new Handler();

    private final Runnable hideAll = new Runnable() {
        @Override public void run() {
            for (int i = 0; i < 4; i++) hideControlsFor(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiview);

        views[0]=findViewById(R.id.player1); views[1]=findViewById(R.id.player2);
        views[2]=findViewById(R.id.player3); views[3]=findViewById(R.id.player4);
        images[0]=findViewById(R.id.image1); images[1]=findViewById(R.id.image2);
        images[2]=findViewById(R.id.image3); images[3]=findViewById(R.id.image4);
        slots[0]=findViewById(R.id.slot1); slots[1]=findViewById(R.id.slot2);
        slots[2]=findViewById(R.id.slot3); slots[3]=findViewById(R.id.slot4);
        titles[0]=findViewById(R.id.title1); titles[1]=findViewById(R.id.title2);
        titles[2]=findViewById(R.id.title3); titles[3]=findViewById(R.id.title4);
        badges[0]=findViewById(R.id.audioBadge1); badges[1]=findViewById(R.id.audioBadge2);
        badges[2]=findViewById(R.id.audioBadge3); badges[3]=findViewById(R.id.audioBadge4);
        closes[0]=findViewById(R.id.close1); closes[1]=findViewById(R.id.close2);
        closes[2]=findViewById(R.id.close3); closes[3]=findViewById(R.id.close4);
        playPauseBtns[0]=findViewById(R.id.playPause1); playPauseBtns[1]=findViewById(R.id.playPause2);
        playPauseBtns[2]=findViewById(R.id.playPause3); playPauseBtns[3]=findViewById(R.id.playPause4);
        muteBtns[0]=findViewById(R.id.muteBtn1); muteBtns[1]=findViewById(R.id.muteBtn2);
        muteBtns[2]=findViewById(R.id.muteBtn3); muteBtns[3]=findViewById(R.id.muteBtn4);
        fullscreenBtns[0]=findViewById(R.id.fullscreen1); fullscreenBtns[1]=findViewById(R.id.fullscreen2);
        fullscreenBtns[2]=findViewById(R.id.fullscreen3); fullscreenBtns[3]=findViewById(R.id.fullscreen4);
        replayBtns[0]=findViewById(R.id.replay1); replayBtns[1]=findViewById(R.id.replay2);
        replayBtns[2]=findViewById(R.id.replay3); replayBtns[3]=findViewById(R.id.replay4);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            // Buffer tuning multiview — lebih kecil karena 4 player bareng
            LoadControl mvLoadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(10000, 30000, 2500, 5000)
                .setTargetBufferBytes(20 * 1024 * 1024)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build();
            players[i] = new ExoPlayer.Builder(this)
                .setLoadControl(mvLoadControl)
                .build();

            // Batasi resolusi max 720p — hemat bandwidth untuk 4 player
            try {
                TrackSelectionParameters mvTrack = players[i].getTrackSelectionParameters()
                    .buildUpon()
                    .setMaxVideoSize(1280, 720)
                    .build();
                players[i].setTrackSelectionParameters(mvTrack);
            } catch (Exception ignored) {}
            views[i].setPlayer(players[i]);
            players[i].setVolume(i == 0 ? 1f : 0f);
            players[i].setPlayWhenReady(true);
            muted[i] = (i != 0);

            players[i].addListener(new Player.Listener() {
                @Override public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) {
                        replayBtns[idx].setVisibility(View.VISIBLE);
                        playPauseBtns[idx].setVisibility(View.GONE);
                    } else if (state == Player.STATE_READY) {
                        replayBtns[idx].setVisibility(View.GONE);
                    }
                }
                @Override public void onIsPlayingChanged(boolean isPlaying) {
                    if (isPlaying) playPauseBtns[idx].setImageResource(R.drawable.ic_pause_w);
                    else playPauseBtns[idx].setImageResource(R.drawable.ic_play_w);
                }
            });

            slots[i].setOnClickListener(v -> {
                setActiveAudio(idx);
                toggleControlsFor(idx);
            });

            slots[i].setOnLongClickListener(v -> {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(data, shadow, idx, 0);
                } else {
                    v.startDrag(data, shadow, idx, 0);
                }
                Toast.makeText(MultiviewActivity.this, "Geser ke slot tujuan", Toast.LENGTH_SHORT).show();
                return true;
            });

            slots[i].setOnDragListener((v, event) -> {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    Object localState = event.getLocalState();
                    if (localState instanceof Integer) {
                        int from = (Integer) localState;
                        if (from != idx) swapSlots(from, idx);
                    }
                    return true;
                }
                return true;
            });

            playPauseBtns[i].setOnClickListener(v -> {
                try {
                    if (players[idx].isPlaying()) {
                        players[idx].pause();
                        playPauseBtns[idx].setImageResource(R.drawable.ic_play_w);
                    } else {
                        players[idx].play();
                        playPauseBtns[idx].setImageResource(R.drawable.ic_pause_w);
                    }
                } catch (Exception ignored) {}
                scheduleHide(idx);
            });

            muteBtns[i].setOnClickListener(v -> {
                muted[idx] = !muted[idx];
                updateMuteButtons();
                applyAudioFocus();
            });

            fullscreenBtns[i].setOnClickListener(v -> openFullscreen(idx));

            replayBtns[i].setOnClickListener(v -> {
                try {
                    players[idx].seekTo(0);
                    players[idx].play();
                    replayBtns[idx].setVisibility(View.GONE);
                } catch (Exception ignored) {}
            });

            closes[i].setOnClickListener(v -> {
                try { players[idx].stop(); players[idx].clearMediaItems(); } catch (Exception ignored) {}
                urls[idx] = null;
                types[idx] = null;
                MultiviewPickHolder.clearSlot(idx);
                titles[idx].setText("");
                badges[idx].setVisibility(View.GONE);
                images[idx].setVisibility(View.GONE);
                views[idx].setVisibility(View.VISIBLE);
                try { images[idx].setImageDrawable(null); } catch (Exception ignored) {}
                replayBtns[idx].setVisibility(View.GONE);
            });
        }

        for (int i = 0; i < 4; i++) {
            if (MultiviewPickHolder.slotUrls[i] != null) {
                loadSlot(i, MultiviewPickHolder.slotUrls[i], MultiviewPickHolder.slotTypes[i]);
            }
        }
        setActiveAudio(0);
        updateBadges();

        int[] pickIds = {R.id.btnPickSlot1, R.id.btnPickSlot2, R.id.btnPickSlot3, R.id.btnPickSlot4};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            findViewById(pickIds[i]).setOnClickListener(v -> pickVideoForSlot(idx));
        }

        findViewById(R.id.btnLayoutToggle).setOnClickListener(v -> toggleLayout());
        findViewById(R.id.btnCloseMulti).setOnClickListener(v -> finish());
    }

    private void toggleControlsFor(int idx) {
        if (controlsVisible[idx]) {
            hideControlsFor(idx);
        } else {
            for (int i = 0; i < 4; i++) hideControlsFor(i);
            if (types[idx] != null && "video".equals(types[idx])) {
                playPauseBtns[idx].setVisibility(View.VISIBLE);
                replayBtns[idx].setVisibility(View.GONE);
            }
            controlsVisible[idx] = true;
            scheduleHide(idx);
        }
    }

    private void hideControlsFor(int idx) {
        playPauseBtns[idx].setVisibility(View.GONE);
        replayBtns[idx].setVisibility(View.GONE);
        controlsVisible[idx] = false;
    }

    private void scheduleHide(int idx) {
        hideHandler.removeCallbacks(hideAll);
        hideHandler.postDelayed(hideAll, 3000);
    }

    private void openFullscreen(int idx) {
        if (urls[idx] == null) {
            Toast.makeText(this, "Slot kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("image".equals(types[idx])) {
            ArrayList<String> single = new ArrayList<>();
            single.add(urls[idx]);
            PlaylistHolder.set(single);
            Intent intent = new Intent(this, PreviewImageActivity.class);
            intent.putExtra("index", 0);
            startActivity(intent);
        } else {
            ArrayList<String> single = new ArrayList<>();
            single.add(urls[idx]);
            PlaylistHolder.set(single);
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("index", 0);
            startActivity(intent);
        }
    }

    private void swapSlots(int a, int b) {
        String ua = urls[a], ub = urls[b];
        String ta = types[a], tb = types[b];

        try { players[a].stop(); players[a].clearMediaItems(); } catch (Exception ignored) {}
        try { players[b].stop(); players[b].clearMediaItems(); } catch (Exception ignored) {}

        urls[a] = null; urls[b] = null; types[a] = null; types[b] = null;

        if (ub != null) loadSlot(a, ub, tb);
        else { MultiviewPickHolder.clearSlot(a); titles[a].setText(""); images[a].setImageDrawable(null); images[a].setVisibility(View.GONE); views[a].setVisibility(View.VISIBLE); }
        if (ua != null) loadSlot(b, ua, ta);
        else { MultiviewPickHolder.clearSlot(b); titles[b].setText(""); images[b].setImageDrawable(null); images[b].setVisibility(View.GONE); views[b].setVisibility(View.VISIBLE); }

        Toast.makeText(this, "Slot " + (a+1) + " ↔ Slot " + (b+1), Toast.LENGTH_SHORT).show();
    }

    private void loadSlot(int idx, String url, String type) {
        try {
            urls[idx] = url;
            types[idx] = type;
            MultiviewPickHolder.saveSlot(idx, url, type);

            String name = url.substring(url.lastIndexOf('/') + 1);
            if (name.length() > 30) name = name.substring(0, 27) + "...";
            titles[idx].setText(name);

            boolean isImage = "image".equals(type);
            if (!isImage) {
                String lower = url.toLowerCase();
                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".gif") ||
                    lower.endsWith(".webp") || lower.endsWith(".bmp")) {
                    isImage = true;
                }
            }

            if (isImage) {
                views[idx].setVisibility(View.GONE);
                images[idx].setVisibility(View.VISIBLE);
                try { images[idx].setImageURI(Uri.parse(url)); }
                catch (Exception e) { images[idx].setImageResource(android.R.drawable.ic_menu_gallery); }
                try { players[idx].stop(); } catch (Exception ignored) {}
                playPauseBtns[idx].setVisibility(View.GONE);
                replayBtns[idx].setVisibility(View.GONE);
            } else {
                images[idx].setVisibility(View.GONE);
                views[idx].setVisibility(View.VISIBLE);
                players[idx].setMediaItem(MediaItem.fromUri(Uri.parse(url)));
                players[idx].prepare();
                players[idx].setPlayWhenReady(true);
                replayBtns[idx].setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal load slot " + (idx + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void setActiveAudio(int idx) {
        // Mode fokus: mute semua, unmute slot yang ditap
        for (int i = 0; i < 4; i++) {
            muted[i] = (i != idx);
        }
        activeAudioSlot = idx;
        applyAudioFocus();
        updateBadges();
        updateMuteButtons();
    }

    private void updateMuteButtons() {
        for (int i = 0; i < 4; i++) {
            muteBtns[i].setImageResource(muted[i] ? R.drawable.ic_mute_w : R.drawable.ic_unmute_w);
        }
    }

    private void applyAudioFocus() {
        // Volume berdasarkan state muted saja (bisa multi-audio kalau user unmute beberapa)
        for (int i = 0; i < 4; i++) {
            try {
                float vol = muted[i] ? 0f : 1f;
                players[i].setVolume(vol);
            } catch (Exception ignored) {}
        }
    }

    private void updateBadges() {
        for (int i = 0; i < 4; i++) {
            badges[i].setVisibility(i == activeAudioSlot ? View.VISIBLE : View.GONE);
        }
    }

    private void toggleLayout() {
        isBigLayout = !isBigLayout;
        LinearLayout row1 = findViewById(R.id.row1);
        LinearLayout row2 = findViewById(R.id.row2);
        View slot2 = findViewById(R.id.slot2);
        if (isBigLayout) {
            if (slot2.getParent() == row1) {
                ((android.view.ViewGroup) slot2.getParent()).removeView(slot2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                lp.setMargins(2, 2, 2, 2);
                row2.addView(slot2, 0, lp);
            }
            Toast.makeText(this, "Layout: 1 Besar + 3 Kecil", Toast.LENGTH_SHORT).show();
        } else {
            if (slot2.getParent() == row2) {
                ((android.view.ViewGroup) slot2.getParent()).removeView(slot2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                lp.setMargins(2, 2, 2, 2);
                row1.addView(slot2, lp);
            }
            Toast.makeText(this, "Layout: 2x2 Grid", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickVideoForSlot(int idx) {
        MultiviewPickHolder.startPick(idx);
        Toast.makeText(this, "Buka Beranda, tap media untuk Slot " + (idx + 1), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MultiviewPickHolder.deliveredUrl != null && MultiviewPickHolder.deliveredSlot >= 0) {
            int slot = MultiviewPickHolder.deliveredSlot;
            String url = MultiviewPickHolder.deliveredUrl;
            String type = MultiviewPickHolder.deliveredType;
            MultiviewPickHolder.consume();
            loadSlot(slot, url, type);
            setActiveAudio(slot);
        }
        for (int i = 0; i < 4; i++) {
            if (MultiviewPickHolder.slotUrls[i] != null && urls[i] == null) {
                loadSlot(i, MultiviewPickHolder.slotUrls[i], MultiviewPickHolder.slotTypes[i]);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (int i = 0; i < 4; i++) {
            try { players[i].pause(); } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (int i = 0; i < 4; i++) {
            if (urls[i] != null && !"image".equals(types[i])) {
                try { players[i].play(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideHandler.removeCallbacks(hideAll);
        for (int i = 0; i < 4; i++) {
            try { players[i].release(); players[i] = null; } catch (Exception ignored) {}
        }
    }
}
