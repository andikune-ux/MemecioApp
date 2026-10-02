package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HiddenMediaListActivity extends Activity {

    private List<MediaItem> items = new ArrayList<>();
    private Set<String> selected = new HashSet<>();
    private boolean selectionMode = false;
    private BaseAdapter adapter;
    private GridView gridView;
    private TextView tvEmpty, tvInfo, tvTitle;
    private View bottomBar;
    private View btnPulihkanTop;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private java.util.concurrent.ExecutorService thumbExecutor = java.util.concurrent.Executors.newFixedThreadPool(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_media);

        findViewById(R.id.btnBackHidden).setOnClickListener(v -> {
            if (selectionMode) exitSelectionMode();
            else finish();
        });

        findViewById(R.id.btnToggleSelect).setOnClickListener(v -> toggleSelectionMode());
        btnPulihkanTop = findViewById(R.id.btnPulihkanTerpilih);
        if (btnPulihkanTop != null) btnPulihkanTop.setOnClickListener(v -> pulihkanTerpilih());

        findViewById(R.id.btnPulihkanBottom).setOnClickListener(v -> pulihkanTerpilih());
        findViewById(R.id.btnHapusTerpilih).setOnClickListener(v -> confirmHapusTerpilih());

        gridView = findViewById(R.id.gridHidden);
        tvEmpty = findViewById(R.id.tvHiddenEmpty);
        tvInfo = findViewById(R.id.tvHiddenInfo);
        tvTitle = findViewById(R.id.tvHiddenTitle);
        bottomBar = findViewById(R.id.bottomBar);

        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public Object getItem(int p) { return items.get(p); }
            @Override public long getItemId(int p) { return p; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) v = LayoutInflater.from(HiddenMediaListActivity.this)
                    .inflate(R.layout.item_hidden_grid, parent, false);

                MediaItem item = items.get(position);
                ImageView thumb = v.findViewById(R.id.ivHiddenThumb);
                ImageView playIcon = v.findViewById(R.id.ivHiddenPlay);
                ImageView checkmark = v.findViewById(R.id.ivCheckmark);
                TextView name = v.findViewById(R.id.tvHiddenName);

                name.setText(item.title != null ? item.title : "media");

                if (item.type == MediaItem.TYPE_VIDEO) {
                    playIcon.setVisibility(View.VISIBLE);
                } else {
                    playIcon.setVisibility(View.GONE);
                }

                checkmark.setVisibility(selected.contains(item.uri.toString()) ? View.VISIBLE : View.GONE);

                // Load thumbnail
                thumb.setImageBitmap(null);
                final String cacheKey = item.uri.toString();
                thumb.setTag(cacheKey);
                loadThumb(item, thumb, cacheKey);

                return v;
            }
        };
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((p, v, pos, id) -> {
            MediaItem item = items.get(pos);
            if (selectionMode) {
                toggleItemSelection(item);
            } else {
                openPreview(item);
            }
        });

        gridView.setOnItemLongClickListener((p, v, pos, id) -> {
            MediaItem item = items.get(pos);
            if (!selectionMode) enterSelectionMode();
            toggleItemSelection(item);
            return true;
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        items.clear();
        selected.clear();
        selectionMode = false;
        items.addAll(HiddenMediaStore.scanHidden(this));
        if (bottomBar != null) bottomBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        updateInfo();
    }

    private void toggleSelectionMode() {
        if (selectionMode) exitSelectionMode();
        else enterSelectionMode();
    }

    private void enterSelectionMode() {
        selectionMode = true;
        if (bottomBar != null) bottomBar.setVisibility(View.VISIBLE);
        updateInfo();
        adapter.notifyDataSetChanged();
    }

    private void exitSelectionMode() {
        selectionMode = false;
        selected.clear();
        if (bottomBar != null) bottomBar.setVisibility(View.GONE);
        updateInfo();
        adapter.notifyDataSetChanged();
    }

    private void toggleItemSelection(MediaItem item) {
        String key = item.uri.toString();
        if (selected.contains(key)) selected.remove(key);
        else selected.add(key);
        adapter.notifyDataSetChanged();
        updateInfo();
    }

    private void updateInfo() {
        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
        if (selectionMode) {
            tvInfo.setText(items.size() + " item \u2022 " + selected.size() + " dipilih");
        } else {
            tvInfo.setText(items.size() + " item tersembunyi");
        }
    }

    private void openPreview(MediaItem item) {
        try {
            // Clear MixedPlaylistHolder biar tidak pakai galeri utama
            try { MixedPlaylistHolder.clear(); } catch (Exception ignored) {}

            if (item.type == MediaItem.TYPE_IMAGE) {
                ArrayList<String> list = new ArrayList<>();
                list.add(item.uri.toString());
                Intent i = new Intent(this, PreviewImageActivity.class);
                i.putStringArrayListExtra("image_list", list);
                i.putExtra("index", 0);
                startActivity(i);
            } else if (item.type == MediaItem.TYPE_VIDEO) {
                ArrayList<String> list = new ArrayList<>();
                list.add(item.uri.toString());
                PlaylistHolder.set(list);
                Intent i = new Intent(this, VideoPlayerActivity.class);
                i.putExtra("index", 0);
                startActivity(i);
            } else if (item.type == MediaItem.TYPE_AUDIO) {
                Intent i = new Intent(this, AudioPlayerActivity.class);
                i.putExtra("audio_uri", item.uri.toString());
                i.putExtra("audio_title", item.title != null ? item.title : "Audio");
                startActivity(i);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal buka: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pulihkanTerpilih() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 item", Toast.LENGTH_SHORT).show();
            return;
        }
        int sukses = 0;
        List<MediaItem> toRestore = new ArrayList<>();
        for (MediaItem item : items) {
            if (selected.contains(item.uri.toString())) toRestore.add(item);
        }
        for (MediaItem item : toRestore) {
            if (HideHelper.kembalikan(this, item)) sukses++;
        }
        Toast.makeText(this, sukses + " item dipulihkan", Toast.LENGTH_SHORT).show();
        loadData();
    }

    private void confirmHapusTerpilih() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 item", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Hapus Permanen")
            .setMessage("Hapus " + selected.size() + " item permanen? Tidak bisa dikembalikan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus", (d, w) -> {
                int sukses = 0;
                for (MediaItem item : new ArrayList<>(items)) {
                    if (selected.contains(item.uri.toString())) {
                        try {
                            if (new File(item.uri.getPath()).delete()) sukses++;
                        } catch (Exception ignored) {}
                    }
                }
                Toast.makeText(this, sukses + " item dihapus", Toast.LENGTH_SHORT).show();
                loadData();
            })
            .show();
    }

    private void loadThumb(final MediaItem item, final ImageView thumb, final String cacheKey) {
        thumbExecutor.execute(() -> {
            Bitmap bmp = null;
            try {
                File f = new File(item.uri.getPath());
                if (item.type == MediaItem.TYPE_IMAGE) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 4;
                    bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
                } else if (item.type == MediaItem.TYPE_VIDEO) {
                    android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(f.getAbsolutePath());
                        bmp = mmr.getFrameAtTime(1_000_000L, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    } finally {
                        try { mmr.release(); } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}

            final Bitmap fb = bmp;
            if (fb != null) {
                mainHandler.post(() -> {
                    try {
                        if (cacheKey.equals(thumb.getTag())) {
                            thumb.setImageBitmap(fb);
                        }
                    } catch (Exception ignored) {}
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        try { thumbExecutor.shutdownNow(); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
