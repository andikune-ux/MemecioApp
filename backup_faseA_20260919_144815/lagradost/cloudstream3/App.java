package com.lagradost.cloudstream3;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * STUB CLOUDSTREAM — App (C-1)
 * ============================================================
 *
 * Wrapper HTTP yang dipakai plugin untuk fetch data.
 * Di CloudStream asli, `app` adalah object global di package utils.
 * Di stub ini, kita bikin class App + static field `app` (lihat CloudstreamAppKt).
 */
public class App {

    public static final int DEFAULT_TIMEOUT = 15000;

    public App() {}

    /** HTTP GET — return Response */
    public Response get(String url) {
        return get(url, null, null);
    }

    public Response get(String url, Map<String, String> headers, Map<String, String> params) {
        HttpURLConnection conn = null;
        try {
            String fullUrl = url;
            if (params != null && !params.isEmpty()) {
                StringBuilder sb = new StringBuilder(url);
                sb.append(url.contains("?") ? "&" : "?");
                boolean first = true;
                for (Map.Entry<String, String> e : params.entrySet()) {
                    if (!first) sb.append("&");
                    first = false;
                    sb.append(e.getKey()).append("=").append(e.getValue());
                }
                fullUrl = sb.toString();
            }

            URL u = new URL(fullUrl);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(DEFAULT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue());
                }
            }

            int code = conn.getResponseCode();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            br.close();

            Response r = new Response();
            r.code = code;
            r.text = sb.toString();
            r.url = fullUrl;
            return r;

        } catch (Exception e) {
            Response r = new Response();
            r.code = -1;
            r.text = "";
            r.url = url;
            return r;
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Exception ignored) {}
        }
    }

    /** HTTP POST — return Response */
    public Response post(String url, Map<String, String> data, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(DEFAULT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36");

            if (data != null) {
                StringBuilder body = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> e : data.entrySet()) {
                    if (!first) body.append("&");
                    first = false;
                    body.append(e.getKey()).append("=").append(e.getValue());
                }
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();
            }

            int code = conn.getResponseCode();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            br.close();

            Response r = new Response();
            r.code = code;
            r.text = sb.toString();
            r.url = url;
            return r;

        } catch (Exception e) {
            Response r = new Response();
            r.code = -1;
            r.text = "";
            r.url = url;
            return r;
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Exception ignored) {}
        }
    }

    // ============================================================
    // RESPONSE WRAPPER
    // ============================================================
    public static class Response {
        public int code;
        public String text;
        public String url;

        /** Parse HTML jadi Document (Jsoup) */
        public Document getDocument() {
            try {
                return Jsoup.parse(text == null ? "" : text);
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isSuccessful() {
            return code >= 200 && code < 300;
        }
    }
}
