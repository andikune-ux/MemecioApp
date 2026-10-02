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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MultiSourceStore — simpan semua sumber online (Drive/Streaming/Server) terpisah.
 * Tidak lagi timpa-menimpa. Setiap sumber punya sourceId unik.
 */
public class MultiSourceStore {
    private static final String PREF_NAME = "memecio_multi_source";
    private static final String KEY_SOURCES = "sources_list";     // daftar sourceId
    private static final String KEY_PREFIX = "src_";              // prefix data per source
    private static final String SEP_FIELD = "\u0003";             // antar field
    private static final String SEP_ITEM = "\u0002";              // antar item
    private static final String SEP_SOURCE = "\u0001";            // antar sourceId di list

    // Format data per source: label|type|item1\u0002item2\u0002...
    // Format item: type\u0003uri\u0003title\u0003thumb\u0003sourceTitle

    public static void save(Context ctx, String sourceId, String sourceLabel, String sourceType, List<MediaItem> items) {
        if (sourceId == null || sourceId.isEmpty() || items == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append(sourceLabel == null ? "" : sourceLabel).append(SEP_FIELD);
        sb.append(sourceType == null ? "" : sourceType);
        for (MediaItem m : items) {
            if (m == null) continue;
            sb.append(SEP_ITEM);  // selalu sebelum tiap item (fix: item pertama hilang)
            sb.append(m.type).append(SEP_FIELD)
              .append(m.uri.toString()).append(SEP_FIELD)
              .append(m.title == null ? "" : m.title).append(SEP_FIELD)
              .append(m.thumbUrl == null ? "" : m.thumbUrl).append(SEP_FIELD)
              .append(m.sourceTitle == null ? "" : m.sourceTitle);
        }

        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> sources = new LinkedHashSet<>(prefs.getStringSet(KEY_SOURCES, new LinkedHashSet<String>()));
        sources.add(sourceId);
        prefs.edit()
            .putStringSet(KEY_SOURCES, sources)
            .putString(KEY_PREFIX + sourceId, sb.toString())
            .apply();
    }

    public static List<MediaItem> getAllMerged(Context ctx) {
        List<MediaItem> result = new ArrayList<>();
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> sources = prefs.getStringSet(KEY_SOURCES, new LinkedHashSet<String>());
        for (String sourceId : sources) {
            String data = prefs.getString(KEY_PREFIX + sourceId, "");
            if (data == null || data.isEmpty()) continue;
            result.addAll(parseData(data));
        }
        return result;
    }

    public static List<MediaItem> getBySource(Context ctx, String sourceId) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(KEY_PREFIX + sourceId, "");
        if (data == null || data.isEmpty()) return new ArrayList<>();
        return parseData(data);
    }

    public static List<String> listSourceIds(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> sources = prefs.getStringSet(KEY_SOURCES, new LinkedHashSet<String>());
        return new ArrayList<>(sources);
    }

    public static String getLabel(Context ctx, String sourceId) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(KEY_PREFIX + sourceId, "");
        if (data == null || data.isEmpty()) return "";
        String[] parts = data.split(SEP_FIELD);
        return parts.length > 0 ? parts[0] : "";
    }

    public static void removeSource(Context ctx, String sourceId) {
        if (sourceId == null) return;
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> sources = new LinkedHashSet<>(prefs.getStringSet(KEY_SOURCES, new LinkedHashSet<String>()));
        sources.remove(sourceId);
        prefs.edit()
            .putStringSet(KEY_SOURCES, sources)
            .remove(KEY_PREFIX + sourceId)
            .apply();
    }

    public static void kosongkan(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private static List<MediaItem> parseData(String data) {
        List<MediaItem> result = new ArrayList<>();
        // Format: "label\u0003type\u0002item1\u0002item2..."
        // topParts[0] = "label\u0003type" (header saja), topParts[1..] = items
        String[] topParts = data.split(SEP_ITEM);
        if (topParts.length < 2) return result;
        for (int i = 1; i < topParts.length; i++) {
            String itemStr = topParts[i];
            if (itemStr.isEmpty()) continue;
            String[] fields = itemStr.split(SEP_FIELD, -1);
            if (fields.length < 2) continue;
            try {
                int type = Integer.parseInt(fields[0]);
                String uriStr = fields[1];
                String title = fields.length > 2 ? fields[2] : "";
                String thumb = fields.length > 3 ? fields[3] : "";
                String sourceTitle = fields.length > 4 ? fields[4] : "";
                MediaItem m = new MediaItem(Uri.parse(uriStr), type);
                m.isLocal = false;
                m.title = title.isEmpty() ? null : title;
                m.thumbUrl = thumb.isEmpty() ? null : thumb;
                m.sourceTitle = sourceTitle.isEmpty() ? null : sourceTitle;
                result.add(m);
            } catch (Exception ignored) {}
        }
        return result;
    }

    /** Migrasi data dari ExternalMediaStore kalau ada. */
    public static void migrateFromExternal(Context ctx) {
        try {
            List<MediaItem> old = ExternalMediaStore.getAll(ctx);
            if (old == null || old.isEmpty()) return;
            String oldLabel = ExternalMediaStore.getSourceLabel(ctx);
            if (oldLabel == null || oldLabel.isEmpty()) oldLabel = "Sumber Lama";
            // Cek apakah sudah pernah migrasi
            SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            if (prefs.getBoolean("migrated", false)) return;
            // Migrasi pakai sourceId dari label
            String sourceId = "migrated_" + Math.abs(oldLabel.hashCode());
            save(ctx, sourceId, oldLabel, "migrated", old);
            prefs.edit().putBoolean("migrated", true).apply();
        } catch (Exception ignored) {}
    }
}
