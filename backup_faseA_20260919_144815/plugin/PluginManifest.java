package com.memecio.app.plugin;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Baca manifest.json dari dalam file .jar plugin.
 * Format manifest.json:
 * {
 *   "name": "Sample",
 *   "version": "1.0.0",
 *   "author": "Dev",
 *   "description": "...",
 *   "className": "com.example.SamplePlugin",
 *   "supportedTypes": ["movie", "tv"]
 * }
 */
public class PluginManifest {

    private static final String TAG = "PluginManifest";

    /** Parse JSON string jadi PluginDescriptor */
    public static PluginDescriptor parse(String json, String jarPath) {
        try {
            JSONObject obj = new JSONObject(json);
            PluginDescriptor d = new PluginDescriptor();
            d.name = obj.optString("name", "");
            d.version = obj.optString("version", "0.0.0");
            d.author = obj.optString("author", "unknown");
            d.description = obj.optString("description", "");
            // Support 2 format: Memec.io (className) & CloudStream (pluginClassName)
            d.className = obj.optString("className", "");
            d.pluginClassName = obj.optString("pluginClassName", "");
            if (d.className.isEmpty() && !d.pluginClassName.isEmpty()) {
                d.className = d.pluginClassName;
            }
            d.jarPath = jarPath;
            JSONArray arr = obj.optJSONArray("supportedTypes");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    d.supportedTypes.add(arr.optString(i));
                }
            }
            return d;
        } catch (Exception e) {
            Log.e(TAG, "Gagal parse manifest: " + e.getMessage(), e);
            return null;
        }
    }

    /** Baca manifest.json dari dalam file .jar */
    public static PluginDescriptor fromJar(File jarFile) {
        if (jarFile == null || !jarFile.exists()) return null;
        try {
            // Deteksi CloudStream dari ekstensi
            boolean cs3 = jarFile.getName().toLowerCase().endsWith(".cs3");
            ZipFile zip = new ZipFile(jarFile);
            try {
                ZipEntry entry = zip.getEntry("manifest.json");
                if (entry == null) {
                    Log.e(TAG, "manifest.json tidak ditemukan di dalam jar");
                    return null;
                }
                InputStream in = zip.getInputStream(entry);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                in.close();
                String json = out.toString("UTF-8");
                PluginDescriptor parsed = parse(json, jarFile.getAbsolutePath());
                if (parsed != null) {
                    // Deteksi CloudStream: dari ekstensi ATAU dari field pluginClassName
                    if (cs3 || (parsed.pluginClassName != null && !parsed.pluginClassName.isEmpty())) {
                        parsed.isCloudStream = true;
                    }
                }
                return parsed;
            } finally {
                zip.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Gagal baca jar: " + e.getMessage(), e);
            return null;
        }
    }
}
