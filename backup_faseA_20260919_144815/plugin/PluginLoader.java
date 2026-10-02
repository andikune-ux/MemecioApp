package com.memecio.app.plugin;

import android.content.Context;
import android.util.Log;

import dalvik.system.DexClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Load plugin dari file .jar pakai DexClassLoader.
 *
 * Jar plugin harus punya:
 *   - manifest.json (metadata)
 *   - classes.dex (bytecode)
 */
public class PluginLoader {

    private static final String TAG = "PluginLoader";

    /** Load plugin dari file jar */
    public static PluginLoadResult loadFromJar(Context ctx, File jarFile) {
        try {
            PluginDescriptor d = PluginManifest.fromJar(jarFile);
            if (d == null) return PluginLoadResult.fail("unknown", "manifest.json tidak valid");
            if (!d.isValid()) return PluginLoadResult.fail(d.name, "manifest tidak lengkap");

            File optDir = new File(ctx.getFilesDir(), "plugin_opt");
            if (!optDir.exists()) optDir.mkdirs();

            DexClassLoader loader = new DexClassLoader(
                jarFile.getAbsolutePath(),
                optDir.getAbsolutePath(),
                null,
                ctx.getClassLoader());

            Class<?> clazz = loader.loadClass(d.className);
            Object obj = clazz.newInstance();

            // Jalur 1: implementasi MemecioPlugin langsung
            if (obj instanceof MemecioPlugin) {
                MemecioPlugin plugin = (MemecioPlugin) obj;
                MemecioProvider provider = plugin.getProvider();
                if (provider != null) provider.onInit(ctx);
                PluginManager.register(plugin);
                PluginStorage.addPlugin(ctx, d);
                PluginLogger.log(d.name, "Sukses diload dari " + jarFile.getName());
                return PluginLoadResult.ok(d.name, d);
            }

            // Jalur 2: extends CloudStream Plugin (C-2b-1)
            if (obj instanceof com.lagradost.cloudstream3.plugins.Plugin) {
                com.lagradost.cloudstream3.plugins.Plugin csPlugin =
                    (com.lagradost.cloudstream3.plugins.Plugin) obj;
                CloudStreamPluginAdapter adapter = new CloudStreamPluginAdapter(csPlugin, d);
                MemecioProvider provider = adapter.getProvider();
                if (provider != null) provider.onInit(ctx);
                PluginManager.register(adapter);
                PluginStorage.addPlugin(ctx, d);
                PluginLogger.log(d.name, "CloudStream plugin berhasil diload");
                return PluginLoadResult.ok(d.name, d);
            }

            // Kalau bukan keduanya
            return PluginLoadResult.fail(d.name,
                "Class bukan MemecioPlugin atau CloudStream Plugin");

        } catch (Throwable t) {
            Log.e(TAG, "Gagal load plugin: " + t.getMessage(), t);
            return PluginLoadResult.fail(jarFile.getName(), t.getMessage());
        }
    }

    /** Scan folder, load semua plugin .jar yang ada */
    public static List<PluginLoadResult> loadAllFromFolder(Context ctx, File folder) {
        List<PluginLoadResult> results = new ArrayList<>();
        if (folder == null || !folder.exists()) return results;
        File[] files = folder.listFiles();
        if (files == null) return results;
        for (File f : files) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
                results.add(loadFromJar(ctx, f));
            }
        }
        return results;
    }

    /** Load plugin yang sudah terdaftar di storage */
    public static List<PluginLoadResult> loadAllFromStorage(Context ctx) {
        List<PluginLoadResult> results = new ArrayList<>();
        List<PluginDescriptor> list = PluginStorage.getInstalledPlugins(ctx);
        for (PluginDescriptor d : list) {
            if (d.jarPath == null) continue;
            File f = new File(d.jarPath);
            if (!f.exists()) {
                results.add(PluginLoadResult.fail(d.name, "file tidak ada"));
                continue;
            }
            results.add(loadFromJar(ctx, f));
        }
        return results;
    }
}
