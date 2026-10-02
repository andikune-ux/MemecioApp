package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CrashHistoryActivity extends Activity {

    public static class CrashEntry {
        public String timestamp;
        public String type; // "FORCE CLOSE" atau "BALIK KE KALKULATOR"
        public String summary;
        public String fullBody;
    }

    private List<CrashEntry> entries = new ArrayList<>();
    private BaseAdapter adapter;
    private TextView tvEmptyRef;
    private ListView listRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_history);

        findViewById(R.id.btnBackCrashHistory).setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listCrashHistory);
        final TextView tvEmpty = findViewById(R.id.tvCrashHistoryEmpty);
        tvEmptyRef = tvEmpty;
        listRef = list;

        adapter = new BaseAdapter() {
            @Override public int getCount() { return entries.size(); }
            @Override public Object getItem(int pos) { return entries.get(pos); }
            @Override public long getItemId(int pos) { return pos; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = LayoutInflater.from(CrashHistoryActivity.this)
                        .inflate(R.layout.item_crash_history, parent, false);
                }
                CrashEntry e = entries.get(position);
                ((TextView) v.findViewById(R.id.tvCrashHistoryTime)).setText(e.timestamp);
                ((TextView) v.findViewById(R.id.tvCrashHistoryType)).setText(e.type);
                ((TextView) v.findViewById(R.id.tvCrashHistorySummary)).setText(e.summary);
                return v;
            }
        };
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            CrashEntry e = entries.get(position);
            Intent i = new Intent(CrashHistoryActivity.this, CrashLogDetailActivity.class);
            i.putExtra("crash_title", e.timestamp + " • " + e.type);
            i.putExtra("crash_body", e.fullBody);
            startActivity(i);
        });

        findViewById(R.id.btnHapusCrashHistory).setOnClickListener(v -> {
            new AlertDialog.Builder(CrashHistoryActivity.this)
                .setTitle("Hapus Riwayat")
                .setMessage("Hapus semua riwayat crash?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (d, w) -> {
                    File f = new File(getFilesDir(), "memecio_crash.txt");
                    if (f.exists()) f.delete();
                    loadEntries(tvEmptyRef, listRef);
                    Toast.makeText(CrashHistoryActivity.this, "Riwayat dihapus", Toast.LENGTH_SHORT).show();
                }).show();
        });

        loadEntries(tvEmpty, list);
    }

    private void loadEntries(TextView tvEmpty, ListView list) {
        entries.clear();
        File file = new File(getFilesDir(), "memecio_crash.txt");
        if (file.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                reader.close();
                parseContent(sb.toString());
            } catch (Exception ignored) {}
        }
        adapter.notifyDataSetChanged();
        if (entries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
    }

    private void parseContent(String content) {
        String[] blocks = content.split("=== CRASH at ");
        for (int i = 1; i < blocks.length; i++) {
            String block = blocks[i];
            int end = block.indexOf(" ===");
            if (end < 0) continue;
            CrashEntry e = new CrashEntry();
            e.timestamp = block.substring(0, end).trim();
            e.fullBody = block.substring(end + 3).trim();
            // Klasifikasi tipe
            if (e.fullBody.contains("Unable to start activity") || e.fullBody.contains("FATAL EXCEPTION")) {
                e.type = "FORCE CLOSE";
            } else {
                e.type = "CRASH";
            }
            // Ambil baris pertama exception sebagai summary
            String[] lines = e.fullBody.split("\n");
            for (String ln : lines) {
                String t = ln.trim();
                if (t.startsWith("java.") || t.startsWith("android.") || t.startsWith("at ")) {
                    e.summary = t;
                    break;
                }
            }
            if (e.summary == null) e.summary = "Detail tidak tersedia";
            entries.add(0, e);
        }
    }
}
