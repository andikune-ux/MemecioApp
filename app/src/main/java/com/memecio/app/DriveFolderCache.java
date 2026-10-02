package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class DriveFolderCache {

    private static final String PREF_NAME = "memecio_drive_cache";
    private static final String KEY_PREFIX = "folder_";
    private static final String KEY_TIME = "time_";
    private static final long EXPIRE_MS = 30 * 60 * 1000; // 30 menit
    private static final String SEP_ITEM = "\u001A";
    private static final String SEP_FIELD = "\u001B";

    public static void save(Context context, String folderId, List<MediaItem> items) {
        if (folderId == null || items == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (MediaItem m : items) {
            if (sb.length() > 0) sb.append(SEP_ITEM);
            sb.append(m.type).append(SEP_FIELD)
              .append(m.uri.toString()).append(SEP_FIELD)
              .append(m.title == null ? "" : m.title).append(SEP_FIELD)
              .append(m.thumbUrl == null ? "" : m.thumbUrl);
        }
        prefs.edit()
            .putString(KEY_PREFIX + folderId, sb.toString())
            .putLong(KEY_TIME + folderId, System.currentTimeMillis())
            .apply();
    }

    public static List<MediaItem> load(Context context, String folderId) {
        if (folderId == null) return null;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long time = prefs.getLong(KEY_TIME + folderId, 0);
        if (time == 0) return null;
        if (System.currentTimeMillis() - time > EXPIRE_MS) return null;

        String data = prefs.getString(KEY_PREFIX + folderId, "");
        if (data.isEmpty()) return null;

        List<MediaItem> result = new ArrayList<>();
        String[] entries = data.split(SEP_ITEM);
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String[] parts = entry.split(SEP_FIELD, -1);
            if (parts.length < 3) continue;
            try {
                int type = Integer.parseInt(parts[0]);
                String uriStr = parts[1];
                String title = parts[2];
                String thumb = parts.length > 3 ? parts[3] : "";
                MediaItem mi = new MediaItem(Uri.parse(uriStr), type);
                mi.isLocal = false;
                mi.title = title.isEmpty() ? null : title;
                mi.thumbUrl = thumb.isEmpty() ? null : thumb;
                result.add(mi);
            } catch (Exception ignored) {}
        }
        return result.isEmpty() ? null : result;
    }

    public static void clearAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
