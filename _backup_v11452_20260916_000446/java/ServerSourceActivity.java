package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ServerSourceActivity extends Activity {

    private Uri selectedFileUri;
    private String uploadedUrl;
    private ProgressBar progressBar;
    private TextView tvPercent, tvSelectedFile;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_source);

        TextView btnBack = findViewById(R.id.btnBackServer);
        Button btnUpload = findViewById(R.id.btnUploadServer);
        Button btnTampilkan = findViewById(R.id.btnTampilkanServer);
        progressBar = findViewById(R.id.progressUpload);
        tvPercent = findViewById(R.id.tvUploadPercent);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 300);
            }
        });

        btnTampilkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadedUrl == null) {
                    Toast.makeText(ServerSourceActivity.this, "Upload file terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }
                RiwayatStore.tambah(ServerSourceActivity.this, uploadedUrl);

                String lower = uploadedUrl.toLowerCase();
                int type = MediaItem.TYPE_VIDEO;
                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                    type = MediaItem.TYPE_IMAGE;
                }

                List<MediaItem> entries = new ArrayList<>();
                MediaItem __mi = new MediaItem(android.net.Uri.parse(uploadedUrl), type); __mi.isLocal = false; entries.add(__mi);
                ExternalMediaStore.gantiSemua(ServerSourceActivity.this, entries, "server");

                Toast.makeText(ServerSourceActivity.this, "Ditampilkan di Beranda", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ServerSourceActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("focus_source", true);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                tvSelectedFile.setText(selectedFileUri.getLastPathSegment());
                simulateUpload();
            }
        }
    }

    private void simulateUpload() {
        progressBar.setVisibility(View.VISIBLE);
        tvPercent.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        final int[] progress = {0};
        Runnable uploadRunnable = new Runnable() {
            @Override
            public void run() {
                progress[0] += 5;
                if (progress[0] > 100) progress[0] = 100;
                progressBar.setProgress(progress[0]);
                tvPercent.setText(progress[0] + "%");

                if (progress[0] < 100) {
                    handler.postDelayed(this, 100);
                } else {
                    uploadedUrl = selectedFileUri.toString();
                    Toast.makeText(ServerSourceActivity.this, "Upload selesai", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handler.post(uploadRunnable);
    }
}

