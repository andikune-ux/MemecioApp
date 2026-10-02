package com.lagradost.cloudstream3;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub CloudStream - ExtractorLink (C-1).
 * Link video yang di-extract dari plugin.
 */
public class ExtractorLink {
    public String source;
    public String name;
    public String url;
    public String referer;
    public ExtractorLinkType type = ExtractorLinkType.VIDEO;
    public int quality = 0;
    public Map<String, String> headers = new HashMap<>();

    public ExtractorLink(String source, String name, String url, String referer, ExtractorLinkType type, int quality) {
        this.source = source;
        this.name = name;
        this.url = url;
        this.referer = referer;
        this.type = type;
        this.quality = quality;
    }
}
