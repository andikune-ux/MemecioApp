package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PreviewImageActivity extends Activity {

    private ImageView imgPreview;
    private TextView tvCounter;
    private ArrayList<String> imageList = new ArrayList<>();
    private int currentIndex = 0;

    private void debugLog(String msg) {
        try {
            java.io.File dir = new java.io.File("/sdcard/Download");
            if (!dir.exists()) dir = getExternalFilesDir(null);
            if (dir == null) return;
            java.io.File f = new java.io.File(dir, "memecio_preview_debug.txt");
            java.io.FileWriter fw = new java.io.FileWriter(f, true);
            fw.write(new java.util.Date() + " | " + msg + "\n");
            fw.close();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek MixedPlaylistHolder dulu
        if (MixedPlaylistHolder.isActive()) {
            int idx = MixedPlaylistHolder.getCurrentIndex();
            List<MediaItem> mixed = MixedPlaylistHolder.getItems();
            if (idx >= 0 && idx < mixed.size()) {
                MediaItem m = mixed.get(idx);
                if (m.type == MediaItem.TYPE_VIDEO) {
                    // Ini video, lempar ke VideoPlayerActivity
                    Intent vp = new Intent(this, VideoPlayerActivity.class);
                    vp.putExtra("index", idx);
                    startActivity(vp);
                    finish();
                    return;
                }
            }
        }

        debugLog("onCreate START");
        try {
            setContentView(R.layout.activity_preview_image);
        } catch (Throwable t) {
            debugLog("setContentView GAGAL: " + t);
            throw new RuntimeException(t);
        }
        debugLog("setContentView OK");

        imgPreview = findViewById(R.id.imgPreview);
        tvCounter = findViewById(R.id.tvPreviewCounter);
        View btnClose = findViewById(R.id.btnClosePreview);

        ArrayList<String> fromIntent = getIntent().getStringArrayListExtra("image_list");
        currentIndex = getIntent().getIntExtra("index", 0);

        if (fromIntent != null && !fromIntent.isEmpty()) {
            imageList = fromIntent;
        } else {
            String single = getIntent().getStringExtra("uri");
            if (single != null) {
                imageList.add(single);
                currentIndex = 0;
            }
        }

        debugLog("imageList.size=" + imageList.size() + " currentIndex=" + currentIndex);
        if (imageList.isEmpty() && !MixedPlaylistHolder.isActive()) {
            debugLog("imageList KOSONG & Mixed kosong -> finish()");
            finish();
            return;
        }

        if (currentIndex < 0 || currentIndex >= imageList.size()) currentIndex = 0;

        loadCurrent();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgPreview.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0;
            long startTime = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startTime = System.currentTimeMillis();
                        return true;
                    case MotionEvent.ACTION_UP:
                        float dx = event.getX() - startX;
                        long dt = System.currentTimeMillis() - startTime;
                        if (Math.abs(dx) > 100 && dt < 800) {
                            if (dx > 0) showPrevious();
                            else showNext();
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }
                return false;
            }
        });
    }

    private void showNext() {
        if (MixedPlaylistHolder.isActive()) {
            int idx = MixedPlaylistHolder.getCurrentIndex();
            List<MediaItem> mixed = MixedPlaylistHolder.getItems();
            int nextIdx = idx + 1;
            if (nextIdx >= mixed.size()) {
                Toast.makeText(this, "Sudah di akhir", Toast.LENGTH_SHORT).show();
                return;
            }
            MediaItem nextItem = mixed.get(nextIdx);
            MixedPlaylistHolder.setCurrentIndex(nextIdx);
            if (nextItem.type == MediaItem.TYPE_VIDEO) {
                Intent vp = new Intent(this, VideoPlayerActivity.class);
                vp.putExtra("index", nextIdx);
                startActivity(vp);
                finish();
                return;
            } else {
                loadCurrent();
                return;
            }
        }
        // Fallback mode lama
        if (currentIndex < imageList.size() - 1) {
            currentIndex++;
            loadCurrent();
        } else {
            Toast.makeText(this, "Ini foto terakhir", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPrevious() {
        if (MixedPlaylistHolder.isActive()) {
            int idx = MixedPlaylistHolder.getCurrentIndex();
            List<MediaItem> mixed = MixedPlaylistHolder.getItems();
            int prevIdx = idx - 1;
            if (prevIdx < 0) {
                Toast.makeText(this, "Sudah di awal", Toast.LENGTH_SHORT).show();
                return;
            }
            MediaItem prevItem = mixed.get(prevIdx);
            MixedPlaylistHolder.setCurrentIndex(prevIdx);
            if (prevItem.type == MediaItem.TYPE_VIDEO) {
                Intent vp = new Intent(this, VideoPlayerActivity.class);
                vp.putExtra("index", prevIdx);
                startActivity(vp);
                finish();
                return;
            } else {
                loadCurrent();
                return;
            }
        }
        // Fallback mode lama
        if (currentIndex > 0) {
            currentIndex--;
            loadCurrent();
        } else {
            Toast.makeText(this, "Ini foto pertama", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap currentBitmap = null;

    private void loadCurrent() {
        try {
            String uri;
            if (MixedPlaylistHolder.isActive()) {
                MediaItem cur = MixedPlaylistHolder.getCurrent();
                if (cur == null) { finish(); return; }
                if (cur.type != MediaItem.TYPE_IMAGE) {
                    // Lempar ke video player
                    Intent vp = new Intent(this, VideoPlayerActivity.class);
                    vp.putExtra("index", MixedPlaylistHolder.getCurrentIndex());
                    startActivity(vp);
                    finish();
                    return;
                }
                uri = cur.uri.toString();
            } else {
                uri = imageList.get(currentIndex);
            }
            debugLog("loadCurrent: uri=" + uri);
            recycleBitmap();
            imgPreview.setImageBitmap(null);
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                loadRemote(uri);
            } else {
                loadLocal(uri);
            }
            if (tvCounter != null) {
                if (MixedPlaylistHolder.isActive()) {
                    tvCounter.setText((MixedPlaylistHolder.getCurrentIndex() + 1) + " / " + MixedPlaylistHolder.getItems().size());
                } else {
                    tvCounter.setText((currentIndex + 1) + " / " + imageList.size());
                }
            }
        } catch (Exception e) {
            debugLog("loadCurrent EXCEPTION: " + e);
            Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void recycleBitmap() {
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
        }
        currentBitmap = null;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void loadLocal(final String uriStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                InputStream is = null;
                try {
                    // Kalau file:// langsung pakai FileInputStream
                    if (uriStr.startsWith("file://") || uriStr.startsWith("/")) {
                        String path = uriStr.startsWith("file://")
                            ? android.net.Uri.parse(uriStr).getPath()
                            : uriStr;
                        java.io.File file = new java.io.File(path);
                        if (!file.exists()) {
                            android.util.Log.e("MEMECIO_IMG", "File tidak ada: " + path);
                            postBitmap(null);
                            return;
                        }

                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path, opts);
                        opts.inSampleSize = calculateInSampleSize(opts, 1080, 1920);
                        opts.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(path, opts);
                    } else {
                        // content:// pakai ContentResolver
                        Uri uri = Uri.parse(uriStr);
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        is = getContentResolver().openInputStream(uri);
                        BitmapFactory.decodeStream(is, null, opts);
                        if (is != null) { is.close(); is = null; }

                        opts.inSampleSize = calculateInSampleSize(opts, 1080, 1920);
                        opts.inJustDecodeBounds = false;
                        is = getContentResolver().openInputStream(uri);
                        bitmap = BitmapFactory.decodeStream(is, null, opts);
                    }
                } catch (Exception e) {
                    android.util.Log.e("MEMECIO_IMG", "Gagal load local: " + uriStr, e);
                } finally {
                    if (is != null) try { is.close(); } catch (Exception ignored) {}
                }
                postBitmap(bitmap);
            }
        }).start();
    }

    private void postBitmap(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    recycleBitmap();
                    currentBitmap = bitmap;
                    imgPreview.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(PreviewImageActivity.this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadRemote(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                InputStream is = null;
                HttpURLConnection conn = null;
                try {
                    URL u = new URL(url);
                    conn = (HttpURLConnection) u.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setInstanceFollowRedirects(true);

                    // Pass 1: baca bounds
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    is = conn.getInputStream();
                    BitmapFactory.decodeStream(is, null, opts);
                    is.close();
                    is = null;

                    // Pass 2: decode dengan sampling
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    opts.inSampleSize = calculateInSampleSize(opts, 1080, 1920);
                    opts.inJustDecodeBounds = false;
                    is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is, null, opts);
                } catch (Exception e) {
                    android.util.Log.e("MEMECIO_IMG", "Gagal load remote: " + url, e);
                } finally {
                    if (is != null) try { is.close(); } catch (Exception ignored) {}
                    if (conn != null) try { conn.disconnect(); } catch (Exception ignored) {}
                }
                postBitmap(bitmap);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleBitmap();
    }
}
