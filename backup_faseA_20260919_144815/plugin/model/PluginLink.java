package com.memecio.app.plugin.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Link video dari plugin — bisa multiple kualitas.
 */
public class PluginLink {
    public String url;           // URL video (.mp4 / .m3u8)
    public String quality;       // "1080p", "720p", "480p", "auto"
    public String subtitleUrl;   // URL subtitle (opsional)
    public Map<String, String> headers = new HashMap<>();  // header HTTP (opsional)

    public PluginLink() {}

    public PluginLink(String url, String quality) {
        this.url = url;
        this.quality = quality;
    }
}
