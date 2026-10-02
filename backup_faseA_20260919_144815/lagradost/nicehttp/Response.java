package com.lagradost.nicehttp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Stub nicehttp - Response (C-2b-4 fix).
 * Response wrapper hasil HTTP request.
 */
public class Response {
    private final String url;
    private final int code;
    private final String text;

    public Response(String url, int code, String text) {
        this.url = url;
        this.code = code;
        this.text = text;
    }

    public String getUrl() { return url; }
    public int getCode() { return code; }
    public String getText() { return text; }

    public Document getDocument() {
        try { return Jsoup.parse(text == null ? "" : text); }
        catch (Throwable t) { return null; }
    }

    public boolean isSuccessful() { return code >= 200 && code < 300; }
}
