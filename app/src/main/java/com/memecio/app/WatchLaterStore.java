package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class WatchLaterStore {

    private static final String PREF_NAME = "memecio_watch_later";
    private static final String KEY_LIST = "watch_later_list";
    private static final String SEP_LINE = "\u0007";
    private static final String SEP_FIELD = "\u0008";

    public static void add(Context context, String uri, String title) {
        List<MediaItem> items = getAll(context);
        for (MediaItem item : items) {
            if (item.uri.toString().equals(uri)) return;
        }
        MediaItem item = new MediaItem(Uri.parse(uri), MediaItem.TYPE_VIDEO);
        item.isLocal = false;
        item.title = title;
        items.add(item);
        save(context, items);
    }

    public static void remove(Context context, String uri) {
        List<MediaItem> items = getAll(context);
        List<MediaItem> result = new ArrayList<>();
        for (MediaItem item : items) {
            if (!item.uri.toString().equals(uri)) result.add(item);
        }
        save(context, result);
    }

    public static List<MediaItem> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(KEY_LIST, "");
        List<MediaItem> result = new ArrayList<>();
        if (data.isEmpty()) return result;
        String[] lines = data.split(SEP_LINE);
        for (String line : lines) {
            if (line.isEmpty()) continue;
            String[] parts = line.split(SEP_FIELD, -1);
            if (parts.length < 2) continue;
            try {
                String uri = parts[0];
                String title = parts.length > 1 ? parts[1] : "";
                MediaItem item = new MediaItem(Uri.parse(uri), MediaItem.TYPE_VIDEO);
                item.isLocal = false;
                item.title = title.isEmpty() ? null : title;
                result.add(item);
            } catch (Exception ignored) {}
        }
        return result;
    }

    private static void save(Context context, List<MediaItem> items) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (MediaItem item : items) {
            if (sb.length() > 0) sb.append(SEP_LINE);
            sb.append(item.uri.toString()).append(SEP_FIELD)
              .append(item.title == null ? "" : item.title);
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply();
    }
}
