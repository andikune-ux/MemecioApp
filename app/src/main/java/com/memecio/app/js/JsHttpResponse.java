package com.memecio.app.js;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Response HTTP yang bisa diakses dari JS.
 */
public class JsHttpResponse {
    public int code = 0;
    public String text = "";
    public String url = "";
    public String error = null;

    /** Parse HTML jadi Jsoup Document - JS panggil: resp.document() */
    public Document document() {
        try {
            return Jsoup.parse(text == null ? "" : text, url);
        } catch (Throwable t) {
            return Jsoup.parse("");
        }
    }

    /** Cek status sukses - JS panggil: resp.ok() */
    public boolean ok() {
        return code >= 200 && code < 300;
    }
}
