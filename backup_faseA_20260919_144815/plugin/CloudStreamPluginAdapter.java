package com.memecio.app.plugin;

/**
 * Adapter: membungkus plugin CloudStream (extends com.lagradost.cloudstream3.plugins.Plugin)
 * supaya bisa dipakai sebagai MemecioPlugin.
 *
 * C-2b-1: hanya bungkus + register. Panggilan method (search/load) di C-2b-2.
 */
public class CloudStreamPluginAdapter implements MemecioPlugin {

    private final com.lagradost.cloudstream3.plugins.Plugin csPlugin;
    private final PluginDescriptor descriptor;
    private final CloudStreamProviderAdapter provider;

    public CloudStreamPluginAdapter(com.lagradost.cloudstream3.plugins.Plugin csPlugin,
                                     PluginDescriptor descriptor) {
        this.csPlugin = csPlugin;
        this.descriptor = descriptor;
        this.provider = new CloudStreamProviderAdapter(csPlugin, descriptor);
    }

    @Override
    public String getName() {
        return descriptor.name != null ? descriptor.name : "CloudStream Plugin";
    }

    @Override
    public String getVersion() {
        return descriptor.version != null ? descriptor.version : "1.0.0";
    }

    @Override
    public String getAuthor() {
        return descriptor.author != null ? descriptor.author : "unknown";
    }

    @Override
    public String getDescription() {
        return "CloudStream plugin: " + getName();
    }

    @Override
    public MemecioProvider getProvider() {
        return provider;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{"movie", "tv", "anime"};
    }

    public com.lagradost.cloudstream3.plugins.Plugin getCsPlugin() {
        return csPlugin;
    }
}
