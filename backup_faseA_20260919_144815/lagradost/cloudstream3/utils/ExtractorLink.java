package com.lagradost.cloudstream3.utils;

import com.lagradost.cloudstream3.ExtractorLinkType;

/** Stub CloudStream - utils.ExtractorLink (C-2a). */
public class ExtractorLink extends com.lagradost.cloudstream3.ExtractorLink {

    public ExtractorLink(String source, String name, String url, String referer,
                          ExtractorLinkType type, int quality) {
        super(source, name, url, referer, convertType(type), quality);
    }

    private static com.lagradost.cloudstream3.ExtractorLinkType convertType(ExtractorLinkType t) {
        if (t == null) return com.lagradost.cloudstream3.ExtractorLinkType.VIDEO;
        try {
            return com.lagradost.cloudstream3.ExtractorLinkType.valueOf(t.name());
        } catch (Exception e) {
            return com.lagradost.cloudstream3.ExtractorLinkType.VIDEO;
        }
    }
}
