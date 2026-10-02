package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class SavedLinksStore {

    private static final String PREF_NAME = "memecio_saved_links";
    private static final String KEY_LIST = "saved_links_list";
    private static final String SEPARATOR = "\u0002";
    private static final String FIELD_SEP = "\u0003";

    public static void tambah(Context context, String title, String url) {
        List<MediaItem> list = getAll(context);
        MediaItem item = new MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO);
        item.isLocal = false;
        item.title = title;
        list.add(0, item);
        simpanSemua(context, list);
    }

    public static void hapus(Context context, String url) {
        List<MediaItem> list = getAll(context);
        List<MediaItem> hasil = new ArrayList<>();
        for (MediaItem m : list) {
            if (!m.uri.toString().equals(url)) hasil.add(m);
        }
        simpanSemua(context, hasil);
    }

    public static void updateTitle(Context context, String url, String newTitle) {
        List<MediaItem> list = getAll(context);
        for (MediaItem m : list) {
            if (m.uri.toString().equals(url)) {
                m.title = newTitle;
                break;
            }
        }
        simpanSemua(context, list);
    }

    public static void simpanSemua(Context context, List<MediaItem> items) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (MediaItem item : items) {
            if (sb.length() > 0) sb.append(SEPARATOR);
            sb.append(item.type).append(FIELD_SEP)
                    .append(item.uri.toString()).append(FIELD_SEP)
                    .append(item.title == null ? "" : item.title);
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply();
    }

    public static List<MediaItem> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_LIST, "");
        List<MediaItem> result = new ArrayList<>();
        if (existing.isEmpty()) return result;

        String[] entries = existing.split(SEPARATOR);
        for (String entry : entries) {
            String[] parts = entry.split(FIELD_SEP, -1);
            if (parts.length < 2) continue;
            try {
                int type = Integer.parseInt(parts[0]);
                String uriStr = parts[1];
                String title = parts.length > 2 ? parts[2] : "";
                MediaItem item = new MediaItem(Uri.parse(uriStr), type);
                item.isLocal = false;
                item.title = title.isEmpty() ? null : title;
                result.add(item);
            } catch (Exception ignored) {}
        }
        return result;
    }
}

