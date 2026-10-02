package com.memecio.app.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simpan daftar plugin yang terinstall (path file + metadata).
 * Pakai SharedPreferences — format JSON.
 */
public class PluginStorage {

    private static final String PREF = "memecio_plugin_storage";
    private static final String KEY_PLUGINS = "installed_plugins";

    /** Folder utama plugin: /sdcard/Termux/MemecioPlugins/ */
    public static File getPluginFolder(Context ctx) {
        File base = new File(android.os.Environment.getExternalStorageDirectory(), "Termux");
        File folder = new File(base, "MemecioPlugins");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    /** Folder internal (backup, tidak bisa dihapus user) */
    public static File getInternalPluginFolder(Context ctx) {
        File folder = new File(ctx.getFilesDir(), "plugins");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    /** Simpan 1 plugin ke storage */
    public static void addPlugin(Context ctx, PluginDescriptor d) {
        try {
            List<PluginDescriptor> list = getInstalledPlugins(ctx);
            // Cek duplikat by name
            for (int i = 0; i < list.size(); i++) {
                if (d.name.equals(list.get(i).name)) {
                    list.set(i, d);  // update
                    saveAll(ctx, list);
                    return;
                }
            }
            list.add(d);
            saveAll(ctx, list);
        } catch (Exception ignored) {}
    }

    /** Hapus plugin dari storage */
    public static void removePlugin(Context ctx, String name) {
        try {
            List<PluginDescriptor> list = getInstalledPlugins(ctx);
            for (int i = list.size() - 1; i >= 0; i--) {
                if (name.equals(list.get(i).name)) list.remove(i);
            }
            saveAll(ctx, list);
        } catch (Exception ignored) {}
    }

    /** Ambil semua plugin terinstall */
    public static List<PluginDescriptor> getInstalledPlugins(Context ctx) {
        List<PluginDescriptor> result = new ArrayList<>();
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            String json = sp.getString(KEY_PLUGINS, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                PluginDescriptor d = new PluginDescriptor();
                d.name = o.optString("name");
                d.version = o.optString("version");
                d.author = o.optString("author");
                d.description = o.optString("description");
                d.className = o.optString("className");
                d.jarPath = o.optString("jarPath");
                result.add(d);
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static void saveAll(Context ctx, List<PluginDescriptor> list) {
        try {
            JSONArray arr = new JSONArray();
            for (PluginDescriptor d : list) {
                JSONObject o = new JSONObject();
                o.put("name", d.name);
                o.put("version", d.version);
                o.put("author", d.author);
                o.put("description", d.description);
                o.put("className", d.className);
                o.put("jarPath", d.jarPath);
                arr.put(o);
            }
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
               .edit().putString(KEY_PLUGINS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }
}
