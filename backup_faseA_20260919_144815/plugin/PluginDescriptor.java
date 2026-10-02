package com.memecio.app.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Info ringkas plugin — hasil parse manifest.json.
 */
public class PluginDescriptor {
    public String name;
    public String version;
    public String author;
    public String description;
    public String className;       // class utama plugin (extends MemecioPlugin)
    public String jarPath;         // path file .jar
    public List<String> supportedTypes = new ArrayList<>();

    // Field CloudStream (C-2b-1)
    public boolean isCloudStream = false;
    public String pluginClassName = "";   // field manifest CloudStream

    public PluginDescriptor() {}

    public boolean isValid() {
        return name != null && !name.isEmpty()
            && className != null && !className.isEmpty()
            && jarPath != null && !jarPath.isEmpty();
    }

    @Override
    public String toString() {
        return name + " v" + version + " (" + author + ")";
    }
}
