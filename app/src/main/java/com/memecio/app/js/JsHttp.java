package com.memecio.app.js;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * HTTP client untuk JS plugin.
 * JS panggil: http.get(url), http.post(url, body)
 */
public class JsHttp {

    private static final int TIMEOUT_MS = 20000;

    public JsHttp() {}

    public JsHttpResponse get(String url) {
        return request("GET", url, null, null);
    }

    public JsHttpResponse get(String url, Map<String, String> headers) {
        return request("GET", url, headers, null);
    }

    public JsHttpResponse post(String url, String body) {
        return request("POST", url, null, body);
    }

    public JsHttpResponse post(String url, Map<String, String> headers, String body) {
        return request("POST", url, headers, body);
    }

    private JsHttpResponse request(String method, String url,
                                    Map<String, String> headers, String body) {
        JsHttpResponse r = new JsHttpResponse();
        r.url = url;
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/110.0.0.0");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue());
                }
            }
            if ("POST".equals(method) && body != null) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.close();
            }

            r.code = conn.getResponseCode();
            java.io.InputStream is = (r.code >= 400)
                ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) {
                r.text = "";
                return r;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            br.close();
            r.text = sb.toString();
        } catch (Throwable t) {
            r.code = -1;
            r.text = "";
            r.error = t.getClass().getSimpleName() + ": " + t.getMessage();
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Throwable ignored) {}
        }
        return r;
    }
}
