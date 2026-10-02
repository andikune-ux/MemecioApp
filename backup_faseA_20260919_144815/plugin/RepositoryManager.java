package com.memecio.app.plugin;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch + parse repo.json dari URL.
 *
 * Support 2 format:
 * 1. Memec.io:
 *    { "name": "...", "plugins": [{ name, version, downloadUrl, ... }] }
 *
 * 2. CloudStream:
 *    { "name": "...", "manifestVersion": 1, "pluginLists": ["url1", "url2"] }
 *    atau array plugin langsung: [{ name, internalName, url, version, ... }]
 */
public class RepositoryManager {

    private static final String TAG = "RepositoryManager";
    private static final int TIMEOUT_MS = 15000;

    /** Normalisasi URL: convert skema cloudstreamrepo:// ke https:// */
    public static String normalizeUrl(String url) {
        if (url == null) return null;
        String u = url.trim();
        // CloudStream scheme
        if (u.startsWith("cloudstreamrepo://")) {
            u = "https://" + u.substring("cloudstreamrepo://".length());
        }
        // Tambahkan https:// kalau tidak ada skema
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }
        return u;
    }

    /** Fetch repo.json dari URL + parse jadi PluginRepository */
    public static PluginRepository fetch(String rawUrl) {
        String url = normalizeUrl(rawUrl);
        if (url == null || url.isEmpty()) return null;

        PluginRepository repo = new PluginRepository();
        repo.url = rawUrl;
        repo.name = "Repository";

        // Fetch URL utama
        String json = httpGet(url);
        if (json == null) {
            Log.e(TAG, "Fetch gagal: " + url);
            return null;
        }

        // Parse berdasarkan format
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("[")) {
                // Format array plugin (CloudStream plugins.json)
                JSONArray arr = new JSONArray(trimmed);
                parsePluginArray(arr, repo, false);
            } else {
                JSONObject obj = new JSONObject(trimmed);

                // Nama repo (kalau ada)
                repo.name = obj.optString("name", "Repository");
                repo.author = obj.optString("author", obj.optString("description", ""));

                // Cek format CloudStream — ada field "pluginLists"
                JSONArray pluginLists = obj.optJSONArray("pluginLists");
                if (pluginLists != null) {
                    Log.d(TAG, "Format CloudStream terdeteksi (pluginLists)");
                    int mv = obj.optInt("manifestVersion", 0);
                    for (int i = 0; i < pluginLists.length(); i++) {
                        String listUrl = pluginLists.optString(i);
                        if (listUrl == null || listUrl.isEmpty()) continue;
                        String listJson = httpGet(normalizeUrl(listUrl));
                        if (listJson == null) continue;
                        try {
                            String lt = listJson.trim();
                            if (lt.startsWith("[")) {
                                JSONArray arr = new JSONArray(lt);
                                parsePluginArray(arr, repo, true);
                                // Set manifestVersion ke semua plugin
                                for (RepositoryPlugin p : repo.plugins) {
                                    if (p.manifestVersion == 0) p.manifestVersion = mv;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Parse plugin list gagal: " + e.getMessage());
                        }
                    }
                } else {
                    // Format Memec.io — punya field "plugins"
                    JSONArray arr = obj.optJSONArray("plugins");
                    if (arr != null) {
                        parsePluginArray(arr, repo, false);
                    }
                }
            }
            Log.d(TAG, "Repo parsed: " + repo.pluginCount() + " plugin");
            return repo;
        } catch (Exception e) {
            Log.e(TAG, "Parse gagal: " + e.getMessage(), e);
            return null;
        }
    }

    /** Parse array JSON jadi daftar RepositoryPlugin */
    private static void parsePluginArray(JSONArray arr, PluginRepository repo, boolean cloudStreamDefault) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject po = arr.optJSONObject(i);
                if (po == null) continue;

                RepositoryPlugin p = new RepositoryPlugin();

                // Name
                p.name = po.optString("name", "");
                if (p.name.isEmpty()) p.name = po.optString("internalName", "");
                p.internalName = po.optString("internalName", p.name);

                // Version
                p.version = po.optString("version", "1.0.0");
                if (p.version.isEmpty()) p.version = "1.0.0";

                // Author
                p.author = po.optString("author", "");
                if (p.author.isEmpty()) {
                    JSONArray authors = po.optJSONArray("authors");
                    if (authors != null && authors.length() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < authors.length(); j++) {
                            if (j > 0) sb.append(", ");
                            sb.append(authors.optString(j));
                        }
                        p.author = sb.toString();
                    }
                }
                if (p.author.isEmpty()) p.author = "unknown";

                // Description
                p.description = po.optString("description", "");

                // Download URL — coba beberapa field
                String dl = po.optString("downloadUrl", "");
                if (dl.isEmpty()) dl = po.optString("url", "");
                if (dl.isEmpty()) dl = po.optString("fileUrl", "");
                p.downloadUrl = dl;

                // Icon
                p.iconUrl = po.optString("iconUrl", "");

                // ManifestVersion
                p.manifestVersion = po.optInt("manifestVersion", 0);

                // isCloudStream — deteksi otomatis
                if (cloudStreamDefault || p.downloadUrl.toLowerCase().endsWith(".cs3")) {
                    p.isCloudStream = true;
                }

                // SupportedTypes
                JSONArray ta = po.optJSONArray("supportedTypes");
                if (ta != null) {
                    for (int j = 0; j < ta.length(); j++) p.supportedTypes.add(ta.optString(j));
                }

                if (p.isValid()) repo.plugins.add(p);
            } catch (Exception e) {
                Log.e(TAG, "Parse plugin item gagal: " + e.getMessage());
            }
        }
    }

    /** HTTP GET sederhana */
    private static String httpGet(String url) {
        if (url == null || url.isEmpty()) return null;
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MemecioApp/1.0");
            conn.setInstanceFollowRedirects(true);

            int code = conn.getResponseCode();
            if (code != 200) {
                Log.e(TAG, "HTTP error: " + code + " untuk " + url);
                return null;
            }

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            br.close();
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "httpGet gagal: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Exception ignored) {}
        }
    }
}
