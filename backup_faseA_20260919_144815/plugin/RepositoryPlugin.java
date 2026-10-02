package com.memecio.app.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Model plugin yang ada di repo.json.
 * Support 2 format: Memec.io + CloudStream.
 */
public class RepositoryPlugin {
    public String name;
    public String version;
    public String author;
    public String description;
    public String downloadUrl;
    public String iconUrl;
    public List<String> supportedTypes = new ArrayList<>();

    // Field khusus CloudStream
    public String internalName = "";
    public int manifestVersion = 0;
    public boolean isCloudStream = false;

    public boolean isValid() {
        return name != null && !name.isEmpty()
            && downloadUrl != null && !downloadUrl.isEmpty();
    }

    public boolean isCloudStreamPlugin() {
        if (isCloudStream) return true;
        if (downloadUrl != null && downloadUrl.toLowerCase().endsWith(".cs3")) return true;
        return false;
    }
}
