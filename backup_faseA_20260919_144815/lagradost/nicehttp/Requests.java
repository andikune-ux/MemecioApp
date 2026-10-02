package com.lagradost.nicehttp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Stub nicehttp - Requests (C-2b-4 fix).
 * HTTP client wrapper kompatibel plugin CloudStream.
 */
public class Requests {

    private static final String TAG = "NiceHttp";
    private static final int TIMEOUT_MS = 20000;
    public String baseUrl = "";

    public Requests() {}

    public Response get(String url) {
        return get(url, null, null);
    }

    public Response get(String url, Map<String, String> headers) {
        return get(url, headers, null);
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
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/110.0.0.0");
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

            return new Response(fullUrl, code, sb.toString());

        } catch (Throwable t) {
            Log.e(TAG, "get error: " + t.getMessage());
            return new Response(url, -1, "");
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Throwable ignored) {}
        }
    }

    public Response post(String url, Map<String, String> data) {
        return post(url, data, null);
    }

    public Response post(String url, Map<String, String> data, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/110.0.0.0");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue());
                }
            }

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

            return new Response(url, code, sb.toString());

        } catch (Throwable t) {
            Log.e(TAG, "post error: " + t.getMessage());
            return new Response(url, -1, "");
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Throwable ignored) {}
        }
    }
}
