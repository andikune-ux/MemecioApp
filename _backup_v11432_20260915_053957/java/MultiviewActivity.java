package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;

public class MultiviewActivity extends Activity {

    private ExoPlayer[] players = new ExoPlayer[4];
    private PlayerView[] views = new PlayerView[4];
    private FrameLayout[] slots = new FrameLayout[4];
    private TextView[] titles = new TextView[4];
    private ImageView[] badges = new ImageView[4];
    private ImageButton[] closes = new ImageButton[4];
    private String[] urls = new String[4];
    private int activeAudioSlot = 0;
    private boolean isBigLayout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiview);

        // Bind views
        views[0] = findViewById(R.id.player1); views[1] = findViewById(R.id.player2);
        views[2] = findViewById(R.id.player3); views[3] = findViewById(R.id.player4);
        slots[0] = findViewById(R.id.slot1); slots[1] = findViewById(R.id.slot2);
        slots[2] = findViewById(R.id.slot3); slots[3] = findViewById(R.id.slot4);
        titles[0] = findViewById(R.id.title1); titles[1] = findViewById(R.id.title2);
        titles[2] = findViewById(R.id.title3); titles[3] = findViewById(R.id.title4);
        badges[0] = findViewById(R.id.audioBadge1); badges[1] = findViewById(R.id.audioBadge2);
        badges[2] = findViewById(R.id.audioBadge3); badges[3] = findViewById(R.id.audioBadge4);
        closes[0] = findViewById(R.id.close1); closes[1] = findViewById(R.id.close2);
        closes[2] = findViewById(R.id.close3); closes[3] = findViewById(R.id.close4);

        // Init 4 players
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            players[i] = new ExoPlayer.Builder(this).build();
            views[i].setPlayer(players[i]);
            players[i].setVolume(i == 0 ? 1f : 0f);
            players[i].setPlayWhenReady(true);

            slots[i].setOnClickListener(v -> setActiveAudio(idx));

            closes[i].setOnClickListener(v -> {
                try { players[idx].stop(); players[idx].clearMediaItems(); } catch (Exception ignored) {}
                urls[idx] = null;
                titles[idx].setText("");
                badges[idx].setVisibility(View.GONE);
            });
        }

        // Auto-load dari playlist (max 4)
        ArrayList<String> playlist = PlaylistHolder.get();
        if (playlist != null && !playlist.isEmpty()) {
            for (int i = 0; i < 4 && i < playlist.size(); i++) {
                loadSlot(i, playlist.get(i));
            }
        }
        setActiveAudio(0);
        updateBadges();

        // Tombol pick per slot
        int[] pickIds = {R.id.btnPickSlot1, R.id.btnPickSlot2, R.id.btnPickSlot3, R.id.btnPickSlot4};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            findViewById(pickIds[i]).setOnClickListener(v -> pickVideoForSlot(idx));
        }

        // Layout toggle
        findViewById(R.id.btnLayoutToggle).setOnClickListener(v -> toggleLayout());

        // Close
        findViewById(R.id.btnCloseMulti).setOnClickListener(v -> finish());
    }

    private void loadSlot(int idx, String url) {
        try {
            urls[idx] = url;
            players[idx].setMediaItem(MediaItem.fromUri(Uri.parse(url)));
            players[idx].prepare();
            String name = url.substring(url.lastIndexOf('/') + 1);
            if (name.length() > 30) name = name.substring(0, 27) + "...";
            titles[idx].setText(name);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal load slot " + (idx + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void setActiveAudio(int idx) {
        activeAudioSlot = idx;
        for (int i = 0; i < 4; i++) {
            try { players[i].setVolume(i == idx ? 1f : 0f); } catch (Exception ignored) {}
        }
        updateBadges();
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
            // 1 Besar + 3 Kecil: slot1 di row1, slot2/3/4 di row2
            if (slot2.getParent() == row1) {
                ((android.view.ViewGroup) slot2.getParent()).removeView(slot2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                lp.setMargins(2, 2, 2, 2);
                row2.addView(slot2, 0, lp);
            }
            Toast.makeText(this, "Layout: 1 Besar + 3 Kecil", Toast.LENGTH_SHORT).show();
        } else {
            // 2x2: slot2 kembali ke row1
            if (slot2.getParent() == row2) {
                ((android.view.ViewGroup) slot2.getParent()).removeView(slot2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                lp.setMargins(2, 2, 2, 2);
                row1.addView(slot2, lp);
            }
            Toast.makeText(this, "Layout: 2x2 Grid", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int REQ_PICK = 100;
    private int pendingSlot = 0;

    private void pickVideoForSlot(int idx) {
        MultiviewPickHolder.startPick(idx);
        Toast.makeText(this, "Buka Beranda, tap media untuk Slot " + (idx + 1), Toast.LENGTH_LONG).show();
        finish(); // balik ke MainActivity (Beranda)
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_PICK && res == RESULT_OK && data != null && data.getData() != null) {
            loadSlot(pendingSlot, data.getData().toString());
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
    protected void onResume() {
        super.onResume();
        // Cek apakah ada media yang baru di-pick dari Beranda
        if (MultiviewPickHolder.deliveredUrl != null && MultiviewPickHolder.deliveredSlot >= 0) {
            int slot = MultiviewPickHolder.deliveredSlot;
            String url = MultiviewPickHolder.deliveredUrl;
            MultiviewPickHolder.consume();
            loadSlot(slot, url);
            setActiveAudio(slot);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (int i = 0; i < 4; i++) {
            if (urls[i] != null) {
                try { players[i].play(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < 4; i++) {
            try { players[i].release(); players[i] = null; } catch (Exception ignored) {}
        }
    }
}
