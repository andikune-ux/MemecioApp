package com.memecio.app.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * PLUGIN MANAGER — REGISTRY PLUGIN AKTIF
 * ============================================================
 *
 * Menyimpan semua plugin yang terdaftar. Akses via static.
 *
 * Tahap 1: registrasi manual di kode (dipanggil SamplePlugin).
 * Tahap 2: load dari file eksternal (.jar/.cs3).
 * Tahap 3: fetch dari repository GitHub.
 */
public class PluginManager {

    private static final Map<String, MemecioPlugin> plugins = new LinkedHashMap<>();

    /** Daftar plugin — dipanggil manual di kode */
    public static void register(MemecioPlugin plugin) {
        if (plugin == null || plugin.getName() == null) return;
        plugins.put(plugin.getName(), plugin);
        PluginLogger.log(plugin.getName(), "Plugin terdaftar v" + plugin.getVersion());
    }

    /** Hapus plugin */
    public static void unregister(String pluginName) {
        if (pluginName == null) return;
        MemecioPlugin removed = plugins.remove(pluginName);
        if (removed != null) {
            try { removed.getProvider().onDestroy(); } catch (Exception ignored) {}
            PluginLogger.log(pluginName, "Plugin dihapus");
        }
    }

    /** Daftar plugin aktif */
    public static List<MemecioPlugin> getActive() {
        return new ArrayList<>(plugins.values());
    }

    /** Semua provider dari plugin aktif */
    public static List<MemecioProvider> getProviders() {
        List<MemecioProvider> list = new ArrayList<>();
        for (MemecioPlugin p : plugins.values()) {
            if (p.getProvider() != null) list.add(p.getProvider());
        }
        return list;
    }

    /** Cek status plugin */
    public static boolean isRegistered(String pluginName) {
        return pluginName != null && plugins.containsKey(pluginName);
    }

    /** Ambil plugin by name */
    public static MemecioPlugin get(String pluginName) {
        return plugins.get(pluginName);
    }

    /** Jumlah plugin aktif */
    public static int count() {
        return plugins.size();
    }
}
