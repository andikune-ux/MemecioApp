package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiwayatActivity extends Activity {

    private List<String> allRiwayat = new ArrayList<>();
    private List<String> filteredRiwayat = new ArrayList<>();
    private RiwayatAdapter adapter;
    private ListView listView;
    private TextView tvEmpty;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        TextView btnBack = findViewById(R.id.btnBackRiwayat);
        TextView btnExport = findViewById(R.id.btnExportRiwayat);
        TextView btnHapusSemua = findViewById(R.id.btnHapusSemuaRiwayat);
        tvEmpty = findViewById(R.id.tvRiwayatEmpty);
        listView = findViewById(R.id.listRiwayat);
        etSearch = findViewById(R.id.etSearchRiwayat);

        btnBack.setOnClickListener(v -> finish());
        btnExport.setOnClickListener(v -> exportRiwayat());
        btnHapusSemua.setOnClickListener(v -> confirmHapusSemua());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(s.toString()); }
        });

        loadData();
    }

    private void loadData() {
        allRiwayat = RiwayatStore.getAll(this);
        adapter = new RiwayatAdapter(this, filteredRiwayat, url -> {
            RiwayatStore.hapus(RiwayatActivity.this, url);
            loadData();
            Toast.makeText(RiwayatActivity.this, "URL dihapus", Toast.LENGTH_SHORT).show();
        });
        listView.setAdapter(adapter);
        applyFilter(etSearch.getText().toString());
    }

    private void applyFilter(String query) {
        filteredRiwayat.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredRiwayat.addAll(allRiwayat);
        } else {
            String q = query.toLowerCase();
            for (String url : allRiwayat) {
                if (url.toLowerCase().contains(q)) filteredRiwayat.add(url);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredRiwayat.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            tvEmpty.setText(allRiwayat.isEmpty()
                ? "Belum ada riwayat link"
                : "Tidak ada hasil untuk \"" + query + "\"");
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void exportRiwayat() {
        if (allRiwayat.isEmpty()) {
            Toast.makeText(this, "Riwayat kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), "Termux/Riwayat");
            if (!dir.exists()) dir.mkdirs();
            String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
            File file = new File(dir, "riwayat_" + ts + ".txt");

            StringBuilder sb = new StringBuilder();
            sb.append("Riwayat Link Memec.io\n");
            sb.append("Total: ").append(allRiwayat.size()).append("\n");
            sb.append("Export: ").append(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");
            for (int i = 0; i < allRiwayat.size(); i++) {
                sb.append(i + 1).append(". ").append(allRiwayat.get(i)).append("\n");
            }

            FileOutputStream out = new FileOutputStream(file);
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.close();

            Toast.makeText(this, "Tersimpan:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal export: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmHapusSemua() {
        if (allRiwayat.isEmpty()) {
            Toast.makeText(this, "Riwayat sudah kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Hapus Semua Riwayat")
            .setMessage("Hapus " + allRiwayat.size() + " URL dari riwayat?\n\nTindakan ini tidak bisa dibatalkan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus Semua", (d, w) -> {
                RiwayatStore.kosongkan(this);
                loadData();
                Toast.makeText(this, "Semua riwayat dihapus", Toast.LENGTH_SHORT).show();
            })
            .show();
    }
}
