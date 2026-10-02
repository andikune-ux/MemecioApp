package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class StreamingSourceActivity extends Activity {

    private EditText etUrl;
    private List<MediaItem> savedLinks = new ArrayList<>();
    private ArrayAdapter<MediaItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_source);

        etUrl = findViewById(R.id.etStreamingUrl);
        TextView btnBack = findViewById(R.id.btnBackStreaming);
        Button btnUpload = findViewById(R.id.btnUploadM3u);
        final Button btnTampilkan = findViewById(R.id.btnTampilkanStreaming);
        final ListView listSavedLinks = findViewById(R.id.listSavedLinks);

        setupSavedList(listSavedLinks);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                // Suppress lock saat buka file picker
                try { SessionState.suppressLock(); } catch (Exception ignored) {}
                startActivityForResult(intent, 200);
            }
        });

        btnTampilkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = etUrl.getText().toString().trim();
                if (url.isEmpty()) {
                    Toast.makeText(StreamingSourceActivity.this, "Masukkan link terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidStreamingUrl(url)) {
                    Toast.makeText(StreamingSourceActivity.this, "Format link tidak dikenali", Toast.LENGTH_SHORT).show();
                    return;
                }
                deteksiDanProses(url);
            }
        });
    }

    private void setupSavedList(final ListView listSavedLinks) {
        if (listSavedLinks == null) return;
        savedLinks = SavedLinksStore.getAll(this);

        adapter = new ArrayAdapter<MediaItem>(this, 0, savedLinks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_playlist_row, parent, false);
                }
                final MediaItem entry = getItem(position);
                final TextView tv = view.findViewById(R.id.tvPlaylistTitle);
                String judul = (entry.title == null || entry.title.trim().isEmpty())
                        ? entry.uri.toString() : entry.title;
                tv.setText(judul);
                tv.setSelected(true);

                final boolean[] longFired = new boolean[1];
                final Handler longHandler = new Handler(Looper.getMainLooper());
                final Runnable longRunnable = new Runnable() {
                    @Override
                    public void run() {
                        longFired[0] = true;
                        confirmDelete(entry);
                    }
                };

                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                longFired[0] = false;
                                longHandler.postDelayed(longRunnable, 4000);
                                return true;
                            case MotionEvent.ACTION_UP:
                                longHandler.removeCallbacks(longRunnable);
                                if (!longFired[0]) {
                                    openInBeranda(entry);
                                }
                                return true;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_MOVE:
                                longHandler.removeCallbacks(longRunnable);
                                return true;
                        }
                        return false;
                    }
                });

                TextView btnHapus = view.findViewById(R.id.btnHapusPlaylist);
                if (btnHapus != null) {
                    btnHapus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDelete(entry);
                        }
                    });
                }

                return view;
            }
        };
        listSavedLinks.setAdapter(adapter);
    }

    private void confirmDelete(final MediaItem entry) {
        String judul = (entry.title == null || entry.title.trim().isEmpty()) ? "playlist ini" : entry.title;
        new AlertDialog.Builder(this)
                .setTitle("Hapus Playlist")
                .setMessage("Apakah anda ingin menghapus \"" + judul + "\"?")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                })
                .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SavedLinksStore.hapus(StreamingSourceActivity.this, entry.uri.toString());
                        savedLinks.clear();
                        savedLinks.addAll(SavedLinksStore.getAll(StreamingSourceActivity.this));
                        adapter.notifyDataSetChanged();
                        Toast.makeText(StreamingSourceActivity.this, "Playlist dihapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void openInBeranda(MediaItem entry) {
        Intent intent = new Intent(StreamingSourceActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("open_playlist_url", entry.uri.toString());
        intent.putExtra("open_playlist_title", entry.title);
        startActivity(intent);
        finish();
    }

    private void deteksiDanProses(final String url) {
        Toast.makeText(StreamingSourceActivity.this, "Memeriksa link...", Toast.LENGTH_SHORT).show();
        M3uParser.parseAsync(StreamingSourceActivity.this, url, new M3uParser.Callback() {
            @Override
            public void onSuccess(List<M3uParser.MediaEntry> entries) {
                if (entries != null && entries.size() > 1) {
                    showJudulDialog(url);
                } else {

                    String judulVideo;
                    String thumbVideo = null;
                    if (entries != null && entries.size() == 1) {
                        M3uParser.MediaEntry e = entries.get(0);
                        judulVideo = (e.title != null && !e.title.trim().isEmpty()) ? e.title : deriveTitleFromUrl(url);
                        thumbVideo = e.thumbUrl;
                    } else {
                        judulVideo = deriveTitleFromUrl(url);
                    }
                    prosesVideoTunggal(url, judulVideo, thumbVideo);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(StreamingSourceActivity.this, "Deteksi: " + message, Toast.LENGTH_LONG).show();
                prosesVideoTunggal(url, deriveTitleFromUrl(url), null);
            }
        });
    }

    private void prosesVideoTunggal(final String url, final String judul, final String thumbUrl) {
        RiwayatStore.tambah(StreamingSourceActivity.this, url);
        SavedLinksStore.tambah(StreamingSourceActivity.this, judul, url);

        List<MediaItem> mediaItems = new ArrayList<>();
        MediaItem mi = new MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO);
        mi.isLocal = false;
        mi.title = judul;
        mi.thumbUrl = thumbUrl;
        mediaItems.add(mi);

        ExternalMediaStore.gantiSemua(StreamingSourceActivity.this, mediaItems, judul);
        gotoBeranda();
    }

    private String deriveTitleFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String last = uri.getLastPathSegment();
            if (last == null || last.trim().isEmpty()) return "Video";
            int dot = last.lastIndexOf('.');
            if (dot > 0) last = last.substring(0, dot);
            last = last.replace('_', ' ').replace('-', ' ').trim();
            return last.isEmpty() ? "Video" : last;
        } catch (Exception e) {
            return "Video";
        }
    }

    private void showJudulDialog(final String url) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_judul);

        final EditText etJudul = dialog.findViewById(R.id.etJudulPopup);
        TextView btnTampilkanPopup = dialog.findViewById(R.id.btnTampilkanPopup);

        btnTampilkanPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String judul = etJudul.getText().toString().trim();
                if (judul.isEmpty()) judul = "Tanpa Judul";

                SavedLinksStore.tambah(StreamingSourceActivity.this, judul, url);
                dialog.dismiss();
                prosesTampilkan(url, judul);
            }
        });

        dialog.show();
    }

    private void prosesTampilkan(final String url, final String judul) {
        RiwayatStore.tambah(StreamingSourceActivity.this, url);
        Toast.makeText(StreamingSourceActivity.this, "Memuat...", Toast.LENGTH_SHORT).show();

        M3uParser.parseAsync(StreamingSourceActivity.this, url, new M3uParser.Callback() {
            @Override
            public void onSuccess(List<M3uParser.MediaEntry> entries) {
                List<MediaItem> mediaItems = new ArrayList<>();
                if (entries.isEmpty()) {
                    MediaItem mi = new MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO);
                    mi.isLocal = false;
                    mediaItems.add(mi);
                } else {
                    for (M3uParser.MediaEntry e : entries) {
                        MediaItem mi = new MediaItem(Uri.parse(e.url), MediaItem.TYPE_VIDEO);
                        mi.isLocal = false;
                        mi.title = e.title;
                        mi.thumbUrl = e.thumbUrl;
                        mi.isM3u = true;
                        mediaItems.add(mi);
                    }
                }
                ExternalMediaStore.gantiSemua(StreamingSourceActivity.this, mediaItems, judul);
                gotoBeranda();
            }

            @Override
            public void onError(String message) {
                List<MediaItem> mediaItems = new ArrayList<>();
                MediaItem mi = new MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO);
                mi.isLocal = false;
                mediaItems.add(mi);
                ExternalMediaStore.gantiSemua(StreamingSourceActivity.this, mediaItems, judul);
                Toast.makeText(StreamingSourceActivity.this, "Gagal parsing: " + message, Toast.LENGTH_LONG).show();
                gotoBeranda();
            }
        });
    }

    private void gotoBeranda() {
        try { SessionState.markInternalTransition(); } catch (Exception ignored) {}
        Intent intent = new Intent(StreamingSourceActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("focus_source", true);
        startActivity(intent);
        finish();
    }

    private boolean isValidStreamingUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String lower = url.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")
            || lower.startsWith("content://") || lower.startsWith("file://")) return true;
        // Path absolut (dari cache copy)
        if (url.startsWith("/")) return true;
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                // Ambil persistent permission
                try {
                    getContentResolver().takePersistableUriPermission(fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}

                // Copy ke cache lokal (jaminan baca)
                String localPath = copyM3uToCache(fileUri);
                if (localPath != null) {
                    etUrl.setText(localPath);
                    Toast.makeText(this, "File siap, tekan Tampilkan", Toast.LENGTH_SHORT).show();
                } else {
                    etUrl.setText(fileUri.toString());
                    Toast.makeText(this, "File dipilih, tekan Tampilkan", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String copyM3uToCache(Uri uri) {
        java.io.InputStream in = null;
        java.io.FileOutputStream out = null;
        try {
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            File cacheFile = new File(getCacheDir(), "picked_m3u_" + System.currentTimeMillis() + ".m3u");
            out = new FileOutputStream(cacheFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            out.flush();
            out.close();
            in.close();
            return cacheFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        } finally {
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
    }
}

