package com.lagradost.cloudstream3;

/** Stub CloudStream - AnimeSearchResponse (C-2a). */
public class AnimeSearchResponse extends SearchResponse {
    public DubStatus dubStatus = DubStatus.Subbed;

    public AnimeSearchResponse(String name, String url, String apiName) {
        super(name, url, apiName);
    }

    public AnimeSearchResponse(String name, String url, String apiName, DubStatus dubStatus) {
        super(name, url, apiName);
        this.dubStatus = dubStatus;
    }
}
