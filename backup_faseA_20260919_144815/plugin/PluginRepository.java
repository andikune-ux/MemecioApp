package com.memecio.app.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Model repository plugin — hasil parse repo.json.
 */
public class PluginRepository {
    public String name;
    public String author;
    public String url;              // URL sumber repo.json
    public List<RepositoryPlugin> plugins = new ArrayList<>();

    public boolean isValid() {
        return plugins != null && !plugins.isEmpty();
    }

    public int pluginCount() {
        return plugins == null ? 0 : plugins.size();
    }
}
