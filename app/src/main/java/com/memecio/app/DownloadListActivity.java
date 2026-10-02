package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DownloadListActivity extends Activity {

    static class Item {
        File file;
        String nama;
        String info;
    }

    private List<Item> items = new ArrayList<>();
    private BaseAdapter adapter;
    private ListView listView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);

        findViewById(R.id.btnBackDownload).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefreshDownload).setOnClickListener(v -> loadFiles());

        listView = findViewById(R.id.listDownload);
        tvEmpty = findViewById(R.id.tvDownloadEmpty);

        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public Object getItem(int p) { return items.get(p); }
            @Override public long getItemId(int p) { return p; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = LayoutInflater.from(DownloadListActivity.this)
                        .inflate(R.layout.item_download, parent, false);
                }
                Item it = items.get(position);
                ((TextView) v.findViewById(R.id.tvDownloadName)).setText(it.nama);
                ((TextView) v.findViewById(R.id.tvDownloadInfo)).setText(it.info);

                TextView btnHapus = v.findViewById(R.id.btnHapusDownload);
                btnHapus.setOnClickListener(view -> konfirmasiHapus(it));

                return v;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Item it = items.get(position);
            bukaFile(it.file);
        });

        loadFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFiles();
    }

    private void loadFiles() {
        items.clear();
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), "Termux/Download");
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    for (File f : files) {
                        if (!f.isFile()) continue;
                        Item it = new Item();
                        it.file = f;
                        it.nama = f.getName();
                        it.info = formatSize(f.length()) + " • " + sdf.format(new Date(f.lastModified()));
                        items.add(it);
                    }
                }
            }
        } catch (Exception ignored) {}

        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void bukaFile(File f) {
        try {
            String name = f.getName().toLowerCase();
            String mime = "*/*";
            if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".mov")) mime = "video/*";
            else if (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav")) mime = "audio/*";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) mime = "image/*";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), mime);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka file ini", Toast.LENGTH_LONG).show();
        }
    }

    private void konfirmasiHapus(final Item it) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus File")
            .setMessage("Hapus \"" + it.nama + "\"?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus", (d, w) -> {
                try {
                    if (it.file.delete()) {
                        Toast.makeText(this, "Dihapus", Toast.LENGTH_SHORT).show();
                        loadFiles();
                    } else {
                        Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private String formatSize(long b) {
        if (b < 1024) return b + " B";
        if (b < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", b / 1024.0);
        if (b < 1024L * 1024 * 1024) return String.format(Locale.getDefault(), "%.1f MB", b / (1024.0 * 1024));
        return String.format(Locale.getDefault(), "%.2f GB", b / (1024.0 * 1024 * 1024));
    }
}
