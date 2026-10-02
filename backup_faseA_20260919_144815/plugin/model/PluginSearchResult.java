package com.memecio.app.plugin.model;

/**
 * Hasil pencarian dari plugin (bisa berupa film, series, atau folder kategori).
 */
public class PluginSearchResult {
    public static final int TYPE_MOVIE = 1;
    public static final int TYPE_TV = 2;
    public static final int TYPE_ANIME = 3;

    public String id;
    public String title;
    public String posterUrl;
    public String url;           // URL internal plugin (untuk load detail)
    public int type = TYPE_MOVIE;
    public int year = 0;

    public PluginSearchResult() {}
}
