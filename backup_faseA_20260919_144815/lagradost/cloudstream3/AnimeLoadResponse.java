package com.lagradost.cloudstream3;

/** Stub CloudStream - AnimeLoadResponse (C-2a). */
public class AnimeLoadResponse extends LoadResponse {
    public DubStatus dubStatus = DubStatus.Subbed;
    public ShowStatus showStatus = ShowStatus.Ongoing;
    public String japName = null;

    public AnimeLoadResponse(String name, String url, LoadType type) {
        super(name, url, type);
    }
}
