package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExternalMediaStore {

    private static final String PREF_NAME = "memecio_external_media";
    private static final String KEY_LIST = "external_list";
    private static final String KEY_SOURCE_LABEL = "external_source_label";
    private static final String SEPARATOR = "\u0002";
    private static final String FIELD_SEP = "\u0003";
    private static final int MAX_PREF_LENGTH = 200_000; // 200 KB limit for prefs
    private static final String BIG_FILE_NAME = "external_media_big.txt";
    private static final String BIG_LABEL_NAME = "external_media_big_label.txt";

    private static File getBigFile(Context context) {
        return new File(context.getFilesDir(), BIG_FILE_NAME);
    }

    private static File getBigLabelFile(Context context) {
        return new File(context.getFilesDir(), BIG_LABEL_NAME);
    }

    public static void gantiSemua(Context context, List<MediaItem> items, String sourceLabel) {
        StringBuilder sb = new StringBuilder();
        for (MediaItem item : items) {
            if (sb.length() > 0) sb.append(SEPARATOR);
            sb.append(item.type).append(FIELD_SEP)
              .append(item.uri.toString()).append(FIELD_SEP)
              .append(item.title == null ? "" : item.title).append(FIELD_SEP)
              .append(item.thumbUrl == null ? "" : item.thumbUrl);
        }
        String data = sb.toString();

        try {
            if (data.length() > MAX_PREF_LENGTH) {
                // Simpan ke file, hapus pref list
                File f = getBigFile(context);
                FileOutputStream out = new FileOutputStream(f);
                out.write(data.getBytes(StandardCharsets.UTF_8));
                out.close();

                File lf = getBigLabelFile(context);
                FileOutputStream lout = new FileOutputStream(lf);
                lout.write((sourceLabel == null ? "" : sourceLabel).getBytes(StandardCharsets.UTF_8));
                lout.close();

                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                    .remove(KEY_LIST)
                    .putString(KEY_SOURCE_LABEL, sourceLabel)
                    .apply();
            } else {
                // Hapus file besar kalau ada
                try { File f = getBigFile(context); if (f.exists()) f.delete(); } catch (Exception ignored) {}
                try { File lf = getBigLabelFile(context); if (lf.exists()) lf.delete(); } catch (Exception ignored) {}

                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                    .putString(KEY_LIST, data)
                    .putString(KEY_SOURCE_LABEL, sourceLabel)
                    .apply();
            }
        } catch (Exception ignored) {}
        // Auto-save ke MultiSourceStore (untuk multi-source support)
        try {
            if (sourceLabel != null && !sourceLabel.isEmpty()) {
                String sid = "src_" + Math.abs(sourceLabel.hashCode());
                MultiSourceStore.save(context, sid, sourceLabel, "auto", items);
            }
        } catch (Exception ignored) {}
    }

    public static void tambahSatu(Context context, String uri, int type, String sourceLabel) {
        List<MediaItem> list = getAll(context);
        list.add(new MediaItem(Uri.parse(uri), type));
        gantiSemua(context, list, sourceLabel);
    }

    public static List<MediaItem> getAll(Context context) {
        List<MediaItem> result = new ArrayList<>();
        String data = null;

        // Cek file besar dulu
        try {
            File f = getBigFile(context);
            if (f.exists() && f.length() > 0) {
                FileInputStream in = new FileInputStream(f);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                data = sb.toString();
            }
        } catch (Exception ignored) {}

        // Kalau tidak ada, ambil dari prefs
        if (data == null || data.isEmpty()) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            data = prefs.getString(KEY_LIST, "");
        }

        if (data == null || data.isEmpty()) return result;

        String[] entries = data.split(SEPARATOR);
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String[] parts = entry.split(FIELD_SEP, -1);
            if (parts.length < 2) continue;
            try {
                int type = Integer.parseInt(parts[0]);
                String uriStr = parts[1];
                String title = parts.length > 2 ? parts[2] : "";
                String thumb = parts.length > 3 ? parts[3] : "";
                MediaItem item = new MediaItem(Uri.parse(uriStr), type);
                item.isLocal = false;
                item.title = title.isEmpty() ? null : title;
                item.thumbUrl = thumb.isEmpty() ? null : thumb;
                result.add(item);
            } catch (Exception ignored) {}
        }
        return result;
    }

    public static String getSourceLabel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Cek file label besar
        try {
            File lf = getBigLabelFile(context);
            if (lf.exists() && lf.length() > 0) {
                FileInputStream in = new FileInputStream(lf);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                String label = sb.toString().trim();
                if (!label.isEmpty()) return label;
            }
        } catch (Exception ignored) {}
        return prefs.getString(KEY_SOURCE_LABEL, "");
    }

    public static void kosongkan(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, "").putString(KEY_SOURCE_LABEL, "").apply();
        try { File f = getBigFile(context); if (f.exists()) f.delete(); } catch (Exception ignored) {}
        try { File lf = getBigLabelFile(context); if (lf.exists()) lf.delete(); } catch (Exception ignored) {}
    }
}
