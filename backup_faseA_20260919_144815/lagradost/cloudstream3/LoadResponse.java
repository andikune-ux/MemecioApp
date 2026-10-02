package com.lagradost.cloudstream3;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub CloudStream - LoadResponse (C-1).
 * Detail film/series dari plugin.
 */
public class LoadResponse {
    public String name;
    public String url;
    public String apiName;
    public LoadType type = LoadType.Movie;
    public String posterUrl;
    public String plot;
    public Integer year;
    public List<String> tags = new ArrayList<>();
    public List<Episode> episodes = new ArrayList<>();
    public String duration;
    public String trailerUrl;
    public List<Actor> actors = new ArrayList<>();
    public String backgroundPosterUrl;
    public String comingSoon = null;
    public String recommendations = null;

    public LoadResponse(String name, String url, LoadType type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    /** Episode (untuk series/anime) */
    public static class Episode {
        public String name;
        public String url;
        public Integer season;
        public Integer episode;
        public String posterUrl;
        public String description;

        public Episode(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    /** Actor */
    public static class Actor {
        public String name;
        public String image;
        public Actor(String name, String image) {
            this.name = name;
            this.image = image;
        }
    }
}
