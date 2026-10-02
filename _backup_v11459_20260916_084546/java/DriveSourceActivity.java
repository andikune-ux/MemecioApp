package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriveSourceActivity extends Activity {

    private EditText etUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoundHelper.init(this);
        setContentView(R.layout.activity_drive_source);

        etUrl = findViewById(R.id.etDriveUrl);
        TextView btnBack = findViewById(R.id.btnBackDrive);
        Button btnTampilkan = findViewById(R.id.btnTampilkanDrive);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        btnTampilkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUrl.getText().toString().trim();
                if (url.isEmpty()) {
                    Toast.makeText(DriveSourceActivity.this, "Masukkan link terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }

                String folderId = extractFolderId(url);
                String fileId = extractFileId(url);

                RiwayatStore.tambah(DriveSourceActivity.this, url);

                if (folderId != null) {
                    String namaDefault = "Drive Folder " + folderId.substring(0, Math.min(6, folderId.length()));
                    tampilkanDialogNama(namaDefault, url);
                } else if (fileId != null) {
                    // Endpoint baru yang mengembalikan video langsung (confirm=t)
                    String directUrl = "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&confirm=t";
                    String thumbUrl = "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w400";
                    String namaFile = "Drive File " + fileId.substring(0, Math.min(6, fileId.length()));

                    int type = MediaItem.TYPE_VIDEO;
                    String lower = url.toLowerCase();
                    if (lower.contains(".jpg") || lower.contains(".jpeg")
                            || lower.contains(".png") || lower.contains(".webp")
                            || lower.contains(".gif")) {
                        type = MediaItem.TYPE_IMAGE;
                    } else if (lower.contains(".mp3") || lower.contains(".wav") || lower.contains(".m4a")) {
                        type = MediaItem.TYPE_AUDIO;
                    }

                    MediaItem mi = new MediaItem(Uri.parse(directUrl), type);
                    mi.isLocal = false;
                    mi.title = namaFile;
                    mi.thumbUrl = thumbUrl;

                    List<MediaItem> entries = new ArrayList<>();
                    entries.add(mi);
                    ExternalMediaStore.gantiSemua(DriveSourceActivity.this, entries, "drive");
                    SoundHelper.success();

                    Intent intent = new Intent(DriveSourceActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("focus_source", true);
                    startActivity(intent);
                    Toast.makeText(DriveSourceActivity.this, "File ditampilkan di Beranda", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    SoundHelper.error();
                    Toast.makeText(DriveSourceActivity.this,
                            "Link Drive tidak valid. Pastikan mengandung /d/FILE_ID/ atau /drive/folders/FOLDER_ID",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void tampilkanDialogNama(final String namaDefault, final String url) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_judul);

        final EditText etJudul = dialog.findViewById(R.id.etJudulPopup);
        TextView btnTampilkan = dialog.findViewById(R.id.btnTampilkanPopup);
        TextView btnBatal = dialog.findViewById(R.id.btnBatalPopup);

        if (etJudul != null) {
            etJudul.setText(namaDefault);
            etJudul.setSelection(namaDefault.length());
        }

        if (btnTampilkan != null) {
            btnTampilkan.setText("Simpan");
            btnTampilkan.setOnClickListener(v -> {
                String namaBaru = etJudul != null ? etJudul.getText().toString().trim() : namaDefault;
                if (namaBaru.isEmpty()) namaBaru = namaDefault;

                SavedLinksStore.tambah(DriveSourceActivity.this, namaBaru, url);
                SoundHelper.success();
                dialog.dismiss();

                Intent intent = new Intent(DriveSourceActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("focus_source", true);
                intent.putExtra("open_drive_folder_url", url);
                startActivity(intent);
                Toast.makeText(DriveSourceActivity.this, "Memuat folder Drive...", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (btnBatal != null) {
            btnBatal.setOnClickListener(v -> dialog.dismiss());
        } else {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }

        dialog.show();
    }

    private String extractFolderId(String url) {
        Pattern p = Pattern.compile("/drive/folders/([a-zA-Z0-9_-]+)");
        Matcher m = p.matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }

    private String extractFileId(String url) {
        Pattern p1 = Pattern.compile("/d/([a-zA-Z0-9_-]+)");
        Matcher m1 = p1.matcher(url);
        if (m1.find()) return m1.group(1);

        Pattern p2 = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)");
        Matcher m2 = p2.matcher(url);
        if (m2.find()) return m2.group(1);

        return null;
    }
}
