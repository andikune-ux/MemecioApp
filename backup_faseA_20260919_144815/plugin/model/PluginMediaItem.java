package com.memecio.app.plugin.model;

/**
 * Item hasil plugin (film/series/anime).
 * Ini representasi ringan — nanti dikonversi ke MediaItem saat diputar.
 */
public class PluginMediaItem {
    public static final int TYPE_MOVIE = 1;
    public static final int TYPE_TV = 2;
    public static final int TYPE_ANIME = 3;

    public String id;            // ID unik di plugin
    public String title;         // Judul film
    public String posterUrl;     // URL poster
    public String description;   // Sinopsis
    public String url;           // URL internal plugin (untuk load detail)
    public int type = TYPE_MOVIE;
    public int year = 0;

    // Field detail (C-2b-4)
    public String duration = "";
    public java.util.List<String> tags = new java.util.ArrayList<>();
    public int episodeCount = 0;

    public PluginMediaItem() {}

    public PluginMediaItem(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }
}
