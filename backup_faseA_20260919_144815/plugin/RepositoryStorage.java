package com.memecio.app.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Simpan daftar URL repo yang ditambahkan user.
 */
public class RepositoryStorage {

    private static final String PREF = "memecio_repo_storage";
    private static final String KEY_REPOS = "repo_urls";

    public static class RepoEntry {
        public String url;
        public String name;
        public RepoEntry(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    /** Tambah repo URL */
    public static void addRepo(Context ctx, String url, String name) {
        if (url == null || url.isEmpty()) return;
        try {
            List<RepoEntry> list = getRepos(ctx);
            for (RepoEntry e : list) {
                if (url.equals(e.url)) {
                    e.name = name;  // update nama
                    saveAll(ctx, list);
                    return;
                }
            }
            list.add(new RepoEntry(url, name));
            saveAll(ctx, list);
        } catch (Exception ignored) {}
    }

    /** Hapus repo URL */
    public static void removeRepo(Context ctx, String url) {
        if (url == null) return;
        try {
            List<RepoEntry> list = getRepos(ctx);
            for (int i = list.size() - 1; i >= 0; i--) {
                if (url.equals(list.get(i).url)) list.remove(i);
            }
            saveAll(ctx, list);
        } catch (Exception ignored) {}
    }

    /** Ambil semua repo URL */
    public static List<RepoEntry> getRepos(Context ctx) {
        List<RepoEntry> result = new ArrayList<>();
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            String json = sp.getString(KEY_REPOS, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                result.add(new RepoEntry(o.optString("url"), o.optString("name")));
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static void saveAll(Context ctx, List<RepoEntry> list) {
        try {
            JSONArray arr = new JSONArray();
            for (RepoEntry e : list) {
                JSONObject o = new JSONObject();
                o.put("url", e.url);
                o.put("name", e.name);
                arr.put(o);
            }
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
               .edit().putString(KEY_REPOS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }
}
