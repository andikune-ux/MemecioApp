package com.memecio.app;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupRestoreHelper {

    private static File getBackupDir() {
        File dir = new File(Environment.getExternalStorageDirectory(), "Termux/Ekspor File");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void exportPlaylists(Context context) {
        try {
            JSONObject root = new JSONObject();

            // 1. Playlist manual
            JSONArray manuals = new JSONArray();
            List<CustomPlaylistStore.Playlist> playlists = CustomPlaylistStore.getAll(context);
            for (CustomPlaylistStore.Playlist pl : playlists) {
                JSONObject obj = new JSONObject();
                obj.put("name", pl.name);
                JSONArray itemsArr = new JSONArray();
                for (MediaItem item : pl.items) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("uri", item.uri.toString());
                    itemObj.put("type", item.type);
                    itemObj.put("title", item.title == null ? "" : item.title);
                    itemsArr.put(itemObj);
                }
                obj.put("items", itemsArr);
                manuals.put(obj);
            }
            root.put("playlist_manual", manuals);

            // 2. Playlist URL / streaming (SavedLinksStore)
            JSONArray savedLinks = new JSONArray();
            try {
                List<MediaItem> saved = SavedLinksStore.getAll(context);
                for (MediaItem m : saved) {
                    JSONObject obj = new JSONObject();
                    obj.put("uri", m.uri.toString());
                    obj.put("title", m.title == null ? "" : m.title);
                    savedLinks.put(obj);
                }
            } catch (Exception ignored) {}
            root.put("playlist_url", savedLinks);

            // 3. Riwayat link
            JSONArray riwayat = new JSONArray();
            try {
                List<String> hist = RiwayatStore.getAll(context);
                for (String h : hist) {
                    riwayat.put(h);
                }
            } catch (Exception ignored) {}
            root.put("riwayat_link", riwayat);

            // 4. Media eksternal aktif
            JSONArray eksternal = new JSONArray();
            try {
                List<MediaItem> ext = ExternalMediaStore.getAll(context);
                for (MediaItem m : ext) {
                    JSONObject obj = new JSONObject();
                    obj.put("uri", m.uri.toString());
                    obj.put("type", m.type);
                    obj.put("title", m.title == null ? "" : m.title);
                    eksternal.put(obj);
                }
            } catch (Exception ignored) {}
            root.put("media_eksternal", eksternal);

            // 5. Info header
            root.put("export_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            // Simpan file dengan timestamp
            String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
            File file = new File(getBackupDir(), "data_memecio_" + ts + ".json");
            FileOutputStream out = new FileOutputStream(file);
            out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            out.close();

            Toast.makeText(context,
                "Ekspor berhasil:\n" + file.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Gagal ekspor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void importPlaylists(Context context) {
        try {
            // Cari file JSON terbaru di folder
            File dir = getBackupDir();
            File[] files = dir.listFiles((d, name) -> name.startsWith("data_memecio_") && name.endsWith(".json"));
            if (files == null || files.length == 0) {
                Toast.makeText(context, "Tidak ada file backup di " + dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return;
            }
            // Ambil file terbaru
            File latest = files[0];
            for (File f : files) {
                if (f.lastModified() > latest.lastModified()) latest = f;
            }

            FileInputStream in = new FileInputStream(latest);
            byte[] buf = new byte[(int) latest.length()];
            in.read(buf);
            in.close();
            String json = new String(buf, StandardCharsets.UTF_8);

            JSONObject root = new JSONObject(json);
            int imported = 0;

            // 1. Import playlist manual (tambah ke existing)
            if (root.has("playlist_manual")) {
                JSONArray manuals = root.getJSONArray("playlist_manual");
                List<CustomPlaylistStore.Playlist> existing = CustomPlaylistStore.getAll(context);
                for (int i = 0; i < manuals.length(); i++) {
                    JSONObject obj = manuals.getJSONObject(i);
                    String namaBaru = obj.getString("name");
                    // Skip kalau nama sudah ada
                    boolean skip = false;
                    for (CustomPlaylistStore.Playlist ex : existing) {
                        if (ex.name.equals(namaBaru)) { skip = true; break; }
                    }
                    if (skip) continue;

                    CustomPlaylistStore.Playlist pl = new CustomPlaylistStore.Playlist();
                    pl.name = namaBaru;
                    JSONArray itemsArr = obj.getJSONArray("items");
                    for (int j = 0; j < itemsArr.length(); j++) {
                        JSONObject itemObj = itemsArr.getJSONObject(j);
                        MediaItem item = new MediaItem(Uri.parse(itemObj.getString("uri")), itemObj.getInt("type"));
                        item.isLocal = false;
                        String t = itemObj.optString("title", "");
                        item.title = t.isEmpty() ? null : t;
                        pl.items.add(item);
                    }
                    existing.add(pl);
                    imported++;
                }
                CustomPlaylistStore.saveAll(context, existing);
            }

            // 2. Import playlist URL (tambah ke existing)
            if (root.has("playlist_url")) {
                JSONArray savedArr = root.getJSONArray("playlist_url");
                List<MediaItem> existingSaved = SavedLinksStore.getAll(context);
                for (int i = 0; i < savedArr.length(); i++) {
                    JSONObject obj = savedArr.getJSONObject(i);
                    String uri = obj.getString("uri");
                    // Skip duplikat
                    boolean skip = false;
                    for (MediaItem ex : existingSaved) {
                        if (ex.uri.toString().equals(uri)) { skip = true; break; }
                    }
                    if (skip) continue;

                    String t = obj.optString("title", "");
                    SavedLinksStore.tambah(context, t.isEmpty() ? uri : t, uri);
                    imported++;
                }
            }

            // 3. Import riwayat link
            if (root.has("riwayat_link")) {
                JSONArray riwArr = root.getJSONArray("riwayat_link");
                List<String> existingRiw = RiwayatStore.getAll(context);
                for (int i = 0; i < riwArr.length(); i++) {
                    String url = riwArr.getString(i);
                    if (!existingRiw.contains(url)) {
                        RiwayatStore.tambah(context, url);
                        imported++;
                    }
                }
            }

            Toast.makeText(context,
                "Impor dari: " + latest.getName() + "\n" + imported + " item ditambahkan",
                Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Gagal impor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
