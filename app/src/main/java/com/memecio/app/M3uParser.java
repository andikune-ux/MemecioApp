package com.memecio.app;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3uParser {

    public static class MediaEntry {
        public String url;
        public String title;
        public String thumbUrl;
        public String groupTitle;  // dari M3U #EXTINF group-title="..."
        public MediaEntry(String url, String title, String thumbUrl) {
            this.url = url;
            this.title = title;
            this.thumbUrl = thumbUrl;
        }
    }

    public interface Callback {
        void onSuccess(List<MediaEntry> entries);
        void onError(String message);
    }

    private static final Pattern LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"");
    private static final Pattern GROUP_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"");

    public static void parseAsync(final Context context, final String source, final Callback callback) {
        // Cek cache dulu (hanya untuk HTTP URL)
        if (source != null && source.startsWith("http")) {
            List<MediaEntry> cached = M3uCacheStore.load(context, source);
            if (cached != null && !cached.isEmpty()) {
                postResult(context, callback, cached, null);
                return;
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    InputStream inputStream;

                    // Kalau sumber bukan http, langsung buka dari content resolver (file lokal)
                    if (!source.startsWith("http://") && !source.startsWith("https://")) {
                        if (source.startsWith("/")) {
                            java.io.File f = new java.io.File(source);
                            if (!f.exists()) {
                                postResult(context, callback, null, "File tidak ditemukan");
                                return;
                            }
                            inputStream = new java.io.FileInputStream(f);
                        } else {
                            Uri uri = Uri.parse(source);
                            inputStream = context.getContentResolver().openInputStream(uri);
                        }
                        if (inputStream == null) {
                            postResult(context, callback, null, "Tidak bisa membuka sumber");
                            return;
                        }
                        List<MediaEntry> entries = bacaStream(inputStream, source);
                        postResult(context, callback, entries, null);
                        return;
                    }

                    // Kalau http, ambil dulu isi file
                    conn = openWithRedirects(source, 5);
                    inputStream = conn.getInputStream();

                    if (inputStream == null) {
                        postResult(context, callback, null, "Tidak bisa membuka sumber");
                        return;
                    }

                    // Baca mentah dulu
                    byte[] rawBytes = readAllBytes(inputStream);
                    inputStream.close();
                    if (conn != null) conn.disconnect();

                    String text = new String(rawBytes, "UTF-8");

                    // Deteksi: apakah ini playlist M3U?
                    boolean isPlaylist = text.contains("#EXTM3U") || text.contains("#EXTINF");

                    if (!isPlaylist) {
                        // Bukan M3U → single video
                        List<MediaEntry> single = new ArrayList<>();
                        single.add(new MediaEntry(source, null, null));
                        postResult(context, callback, single, null);
                        return;
                    }

                    // Parse sebagai playlist
                    List<MediaEntry> entries = parseText(text);

                    // Simpan ke cache kalau ini HTTP
                    if (source.startsWith("http") && entries != null && !entries.isEmpty()) {
                        M3uCacheStore.save(context, source, entries);
                    }
                    postResult(context, callback, entries, null);

                } catch (Exception e) {
                    if (conn != null) {
                        try {
                            InputStream err = conn.getErrorStream();
                            if (err != null) err.close();
                        } catch (Exception ignored) {}
                        conn.disconnect();
                    }
                    Log.e("M3uParser", "Parse error: " + e.getMessage(), e);
                    postResult(context, callback, null, e.getMessage());
                }
            }
        }).start();
    }

    private static List<MediaEntry> bacaStream(InputStream inputStream, String source) throws Exception {
        byte[] rawBytes = readAllBytes(inputStream);
        inputStream.close();
        String text = new String(rawBytes, "UTF-8");
        if (text.contains("#EXTM3U") || text.contains("#EXTINF")) {
            return parseText(text);
        }
        List<MediaEntry> single = new ArrayList<>();
        single.add(new MediaEntry(source, null, null));
        return single;
    }

    private static List<MediaEntry> parseText(String text) {
        List<MediaEntry> entries = new ArrayList<>();
        String[] lines = text.split("\n");
        String pendingTitle = null;
        String pendingGroup = null;
        String pendingThumb = null;
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#EXTM3U")) continue;
            if (line.startsWith("#EXTINF")) {
                pendingTitle = extractTitle(line);
                pendingThumb = extractThumb(line);
                continue;
            }
            if (line.startsWith("#")) continue;
            entries.add(new MediaEntry(line, pendingTitle, pendingThumb));
            pendingTitle = null;
            pendingThumb = null;
        }
        return entries;
    }

    private static String extractTitle(String extinfLine) {
        int lastComma = extinfLine.lastIndexOf(',');
        if (lastComma == -1 || lastComma == extinfLine.length() - 1) return null;
        String title = extinfLine.substring(lastComma + 1).trim();
        return title.isEmpty() ? null : title;
    }

    private static String extractGroup(String extinfLine) {
        try {
            Matcher m = GROUP_PATTERN.matcher(extinfLine);
            if (m.find()) {
                String g = m.group(1);
                return (g == null || g.isEmpty()) ? null : g;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String extractThumb(String extinfLine) {
        Matcher m = LOGO_PATTERN.matcher(extinfLine);
        if (m.find()) {
            String logo = m.group(1);
            return (logo == null || logo.isEmpty()) ? null : logo;
        }
        return null;
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private static HttpURLConnection openWithRedirects(String urlStr, int maxRedirects) throws Exception {
        String currentUrl = urlStr;
        for (int i = 0; i < maxRedirects; i++) {
            URL url = new URL(currentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "VLC/3.0.16 LibVLC/3.0.16");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "close");
            try {
                String host = url.getProtocol() + "://" + url.getHost() + "/";
                conn.setRequestProperty("Referer", host);
                conn.setRequestProperty("Origin", host);
            } catch (Exception ignoredRef) {}
            conn.connect();

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_PERM
                    || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER
                    || code == 307 || code == 308) {
                String newUrl = conn.getHeaderField("Location");
                conn.disconnect();
                if (newUrl == null) {
                    throw new Exception("Redirect tanpa Location header (kode " + code + ")");
                }
                currentUrl = newUrl.startsWith("http") ? newUrl : new URL(new URL(currentUrl), newUrl).toString();
                continue;
            }

            if (code >= 200 && code < 300) {
                return conn;
            } else {
                String msg = "Server menolak dengan kode HTTP " + code;
                conn.disconnect();
                throw new Exception(msg);
            }
        }
        throw new Exception("Terlalu banyak redirect (>" + maxRedirects + ")");
    }

    private static void postResult(Context context, final Callback callback, final List<MediaEntry> entries, final String error) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error != null) {
                        callback.onError(error);
                    } else {
                        callback.onSuccess(entries);
                    }
                }
            });
        }
    }
}
