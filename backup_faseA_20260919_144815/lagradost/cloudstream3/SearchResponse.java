package com.lagradost.cloudstream3;

/**
 * Stub CloudStream - SearchResponse (C-1).
 * Hasil pencarian dari plugin.
 */
public class SearchResponse {
    public String name;
    public String url;
    public String apiName;
    public TvType type;
    public String posterUrl;
    public Integer year;

    public SearchResponse(String name, String url, String apiName, TvType type, String posterUrl, Integer year) {
        this.name = name;
        this.url = url;
        this.apiName = apiName;
        this.type = type;
        this.posterUrl = posterUrl;
        this.year = year;
    }

    // Constructor minimal (yang paling sering dipakai plugin)
    public SearchResponse(String name, String url, String apiName) {
        this(name, url, apiName, null, null, null);
    }
}
