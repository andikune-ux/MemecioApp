package com.memecio.app.plugin;

/**
 * Hasil load plugin (sukses / gagal).
 */
public class PluginLoadResult {
    public boolean success;
    public String pluginName;
    public String errorMessage;
    public PluginDescriptor descriptor;

    public static PluginLoadResult ok(String name, PluginDescriptor d) {
        PluginLoadResult r = new PluginLoadResult();
        r.success = true;
        r.pluginName = name;
        r.descriptor = d;
        return r;
    }

    public static PluginLoadResult fail(String name, String error) {
        PluginLoadResult r = new PluginLoadResult();
        r.success = false;
        r.pluginName = name;
        r.errorMessage = error;
        return r;
    }
}
