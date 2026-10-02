package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class M3uCacheStore {

    private static final String PREF_NAME = "memecio_m3u_cache";
    private static final String KEY_PREFIX = "m3u_";
    private static final String KEY_TIME = "t_";
    private static final long EXPIRE_MS = 30 * 60 * 1000; // 30 menit
    private static final String SEP_ENTRY = "\u001C";
    private static final String SEP_FIELD = "\u001D";

    public static void save(Context context, String url, List<M3uParser.MediaEntry> entries) {
        if (url == null || entries == null || entries.isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (M3uParser.MediaEntry e : entries) {
            if (sb.length() > 0) sb.append(SEP_ENTRY);
            sb.append(e.url == null ? "" : e.url).append(SEP_FIELD)
              .append(e.title == null ? "" : e.title).append(SEP_FIELD)
              .append(e.thumbUrl == null ? "" : e.thumbUrl);
        }
        // Batasi ukuran cache 500 KB per entry
        String data = sb.toString();
        if (data.length() > 500000) {
            data = data.substring(0, 500000);
        }
        prefs.edit()
            .putString(KEY_PREFIX + url, data)
            .putLong(KEY_TIME + url, System.currentTimeMillis())
            .apply();
    }

    public static List<M3uParser.MediaEntry> load(Context context, String url) {
        if (url == null) return null;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long time = prefs.getLong(KEY_TIME + url, 0);
        if (time == 0) return null;
        if (System.currentTimeMillis() - time > EXPIRE_MS) return null;

        String data = prefs.getString(KEY_PREFIX + url, "");
        if (data.isEmpty()) return null;

        List<M3uParser.MediaEntry> result = new ArrayList<>();
        String[] entries = data.split(SEP_ENTRY);
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String[] parts = entry.split(SEP_FIELD, -1);
            if (parts.length < 1) continue;
            String u = parts[0];
            String title = parts.length > 1 ? parts[1] : null;
            String thumb = parts.length > 2 ? parts[2] : null;
            if (u.isEmpty()) continue;
            result.add(new M3uParser.MediaEntry(u,
                title == null || title.isEmpty() ? null : title,
                thumb == null || thumb.isEmpty() ? null : thumb));
        }
        return result.isEmpty() ? null : result;
    }

    public static void clearAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
