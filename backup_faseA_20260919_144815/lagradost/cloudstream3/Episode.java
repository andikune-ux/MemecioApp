package com.lagradost.cloudstream3;

/** Stub CloudStream - Episode (C-2a). */
public class Episode {
    public String name;
    public String url;
    public Integer season = null;
    public Integer episode = null;
    public String posterUrl = null;
    public String description = null;

    public Episode(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
