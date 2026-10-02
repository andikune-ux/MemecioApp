package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class CustomPlaylistStore {

    private static final String PREF_NAME = "memecio_custom_playlists";
    private static final String KEY_DATA = "playlists_data";
    private static final String SEP_PLAYLIST = "\u0004";
    private static final String SEP_LINE = "\u0005";
    private static final String SEP_FIELD = "\u0006";

    public static class Playlist {
        public String name;
        public List<MediaItem> items = new ArrayList<>();
    }

    public static List<Playlist> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(KEY_DATA, "");
        List<Playlist> result = new ArrayList<>();
        if (data.isEmpty()) return result;

        String[] blocks = data.split(SEP_PLAYLIST);
        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;
            String[] lines = block.split(SEP_LINE);
            if (lines.length < 1) continue;

            Playlist pl = new Playlist();
            pl.name = lines[0];
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEP_FIELD, -1);
                if (parts.length < 2) continue;
                try {
                    int type = Integer.parseInt(parts[0]);
                    String uriStr = parts[1];
                    String title = parts.length > 2 ? parts[2] : "";
                    MediaItem item = new MediaItem(Uri.parse(uriStr), type);
                    item.isLocal = false;
                    item.title = title.isEmpty() ? null : title;
                    pl.items.add(item);
                } catch (Exception ignored) {}
            }
            result.add(pl);
        }
        return result;
    }

    public static void saveAll(Context context, List<Playlist> playlists) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < playlists.size(); i++) {
            Playlist pl = playlists.get(i);
            if (i > 0) sb.append(SEP_PLAYLIST);
            sb.append(pl.name);
            for (MediaItem item : pl.items) {
                sb.append(SEP_LINE)
                  .append(item.type).append(SEP_FIELD)
                  .append(item.uri.toString()).append(SEP_FIELD)
                  .append(item.title == null ? "" : item.title);
            }
        }
        prefs.edit().putString(KEY_DATA, sb.toString()).apply();
    }

    public static void tambahPlaylist(Context context, String nama) {
        List<Playlist> pls = getAll(context);
        Playlist pl = new Playlist();
        pl.name = nama;
        pls.add(pl);
        saveAll(context, pls);
    }

    public static void hapusPlaylist(Context context, int index) {
        List<Playlist> pls = getAll(context);
        if (index >= 0 && index < pls.size()) {
            pls.remove(index);
            saveAll(context, pls);
        }
    }

    public static void tambahItem(Context context, int index, MediaItem item) {
        List<Playlist> pls = getAll(context);
        if (index >= 0 && index < pls.size()) {
            pls.get(index).items.add(item);
            saveAll(context, pls);
        }
    }
}
