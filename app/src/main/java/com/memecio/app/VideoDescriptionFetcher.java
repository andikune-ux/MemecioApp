package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoDescriptionFetcher {

    public interface Callback {
        void onResult(String description);
    }

    private static final String PREF_NAME = "memecio_video_desc";
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000L; // 24 jam
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final Pattern META_DESC = Pattern.compile(
        "<meta[^>]+(?:name|property)=[\"'](?:description|og:description)[\"'][^>]+content=[\"']([^\"']+)[\"']",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern META_DESC2 = Pattern.compile(
        "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+(?:name|property)=[\"'](?:description|og:description)[\"']",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_TAG = Pattern.compile(
        "<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE);

    public static void fetch(final Context context, final String videoUrl, final String videoTitle, final Callback callback) {
        if (videoUrl == null) {
            if (callback != null) callback.onResult(defaultDescription(videoTitle, null));
            return;
        }

        // Cek cache
        String cached = loadCache(context, videoUrl);
        if (cached != null && !cached.isEmpty()) {
            if (callback != null) callback.onResult(cached);
            return;
        }

        // Kalau video lokal (content:// atau file://) → langsung default
        if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
            String desc = defaultDescription(videoTitle, videoUrl);
            saveCache(context, videoUrl, desc);
            if (callback != null) callback.onResult(desc);
            return;
        }

        // Kalau bukan http, langsung default
        if (!videoUrl.startsWith("http")) {
            String desc = defaultDescription(videoTitle, videoUrl);
            saveCache(context, videoUrl, desc);
            if (callback != null) callback.onResult(desc);
            return;
        }

        // Fetch dari internet
        executor.execute(() -> {
            String description = null;
            try {
                // Coba fetch metadata halaman (kalau URL-nya bukan video langsung)
                if (!isDirectVideoUrl(videoUrl)) {
                    description = fetchMetaDescription(videoUrl);
                }

                boolean isDefault = false;

                // Kalau masih kosong, coba dari judul
                if (description == null || description.trim().isEmpty()) {
                    description = defaultDescription(videoTitle, videoUrl);
                    isDefault = true;
                }

                // Translate hanya kalau bukan default (URL/teknis tidak perlu translate)
                if (!isDefault && description != null) {
                    String translated = tryTranslate(description);
                    if (translated != null && !translated.isEmpty() && isValidDescription(translated)) {
                        description = translated;
                    }
                }

                saveCache(context, videoUrl, description);
            } catch (Exception e) {
                description = defaultDescription(videoTitle, videoUrl);
            }

            final String result = description;
            mainHandler.post(() -> {
                if (callback != null) callback.onResult(result);
            });
        });
    }

    private static boolean isDirectVideoUrl(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm")
            || lower.endsWith(".m3u8") || lower.endsWith(".mp3") || lower.endsWith(".m4a")
            || lower.endsWith(".mov") || lower.endsWith(".avi")
            || lower.contains("googlevideo.com")
            || lower.contains("usercontent.google.com")
            || lower.contains(".m3u8?");
    }

    private static String fetchMetaDescription(String pageUrl) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(pageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setInstanceFollowRedirects(true);

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return null;

            is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            int total = 0;
            while ((line = reader.readLine()) != null && total < 50000) {
                sb.append(line).append("\n");
                total += line.length();
            }
            reader.close();
            is.close();
            conn.disconnect();

            String html = sb.toString();

            // Coba meta description
            Matcher m = META_DESC.matcher(html);
            if (m.find()) {
                String d = unescapeHtml(m.group(1).trim());
                if (isValidDescription(d)) return d;
            }
            m = META_DESC2.matcher(html);
            if (m.find()) {
                String d = unescapeHtml(m.group(1).trim());
                if (isValidDescription(d)) return d;
            }

            // Fallback ke title
            Matcher t = TITLE_TAG.matcher(html);
            if (t.find()) {
                String title = unescapeHtml(t.group(1).trim());
                if (title.length() > 5 && title.length() < 200) return title;
            }
        } catch (Exception ignored) {
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.disconnect(); } catch (Exception ignored) {}
        }
        return null;
    }

    private static String unescapeHtml(String s) {
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
    }

    private static String tryTranslate(String text) {
        if (text == null || text.length() < 3) return null;
        // Kalau text sangat pendek atau tidak ada huruf, skip
        if (text.length() > 3000) text = text.substring(0, 3000);
        try {
            String url = "https://translate.googleapis.com/translate_a/single"
                + "?client=gtx&dt=t&sl=auto&tl=id&q=" + URLEncoder.encode(text, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) { conn.disconnect(); return null; }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            is.close();
            conn.disconnect();

            String response = sb.toString();
            // Format: [[["halo","hello",null,null,10]],null,"en",...]
            int start = response.indexOf("[[[\"");
            if (start < 0) return null;

            StringBuilder result = new StringBuilder();
            int i = start + 4;
            while (i < response.length()) {
                // Cari awal string ("...")
                int q1 = response.indexOf("\"", i);
                if (q1 < 0) break;
                int q2 = q1 + 1;
                StringBuilder piece = new StringBuilder();
                while (q2 < response.length()) {
                    char c = response.charAt(q2);
                    if (c == '\\' && q2 + 1 < response.length()) {
                        char next = response.charAt(q2 + 1);
                        if (next == 'n') piece.append('\n');
                        else if (next == 't') piece.append('\t');
                        else if (next == '"') piece.append('"');
                        else if (next == '\\') piece.append('\\');
                        else if (next == 'u' && q2 + 5 < response.length()) {
                            try {
                                String hex = response.substring(q2 + 2, q2 + 6);
                                piece.append((char) Integer.parseInt(hex, 16));
                                q2 += 4;
                            } catch (Exception e) {
                                piece.append(next);
                            }
                        } else {
                            piece.append(next);
                        }
                        q2 += 2;
                    } else if (c == '"') {
                        break;
                    } else {
                        piece.append(c);
                        q2++;
                    }
                }
                if (piece.length() > 0) result.append(piece);
                // Cari koma pemisah berikutnya
                int comma = response.indexOf(",", q2);
                if (comma < 0) break;
                // Cek apakah ini akhir blok (]]])
                if (response.substring(q2, Math.min(q2 + 3, response.length())).contains("]")) {
                    // Coba potong di antara ] untuk next piece
                }
                int nextStart = response.indexOf("[\"", q2 + 1);
                if (nextStart < 0) break;
                i = nextStart;
            }

            String out = result.toString().trim();
            return out.isEmpty() ? null : out;
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean isValidDescription(String desc) {
        if (desc == null) return false;
        String d = desc.trim();
        if (d.length() < 15) return false;
        if (d.length() > 2000) return false;

        // Harus ada minimal 3 spasi (deskripsi normal biasanya multi-kata)
        int spaces = 0;
        for (char c : d.toCharArray()) if (c == ' ') spaces++;
        if (spaces < 3) return false;

        // Cek rasio karakter valid
        int validChars = 0;
        int totalChars = 0;
        int hexLike = 0;
        for (char c : d.toCharArray()) {
            totalChars++;
            if (Character.isLetterOrDigit(c) || c == ' ' || c == ',' || c == '.' || c == '!'
                || c == '?' || c == '-' || c == '\'' || c == '"' || c == ':' || c == ';'
                || c == '(' || c == ')' || c == '&') {
                validChars++;
            }
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                hexLike++;
            }
        }

        if (totalChars == 0) return false;
        double validRatio = (double) validChars / totalChars;
        if (validRatio < 0.85) return false;

        // Cek rasio spasi (deskripsi asli biasanya > 5% spasi)
        double spaceRatio = (double) spaces / totalChars;
        if (spaceRatio < 0.05) return false;

        // Tolak kalau terlalu banyak karakter hex (kemungkinan hash/random string)
        // Deskripsi asli biasanya punya banyak huruf di luar a-f
        int letters = 0;
        for (char c : d.toCharArray()) if (Character.isLetter(c)) letters++;
        if (letters < totalChars * 0.5) return false;

        return true;
    }

    private static String defaultDescription(String title, String url) {
        StringBuilder sb = new StringBuilder();
        String judulBersih = title != null ? title.trim() : "Video";
        if (judulBersih.isEmpty()) judulBersih = "Video";

        sb.append("Judul: ").append(judulBersih).append("\n\n");

        if (url != null) {
            // Info tambahan dari URL
            try {
                URL u = new URL(url);
                String host = u.getHost();
                String path = u.getPath();
                if (host != null && !host.isEmpty()) {
                    sb.append("Sumber: ").append(host).append("\n");
                }
                if (path != null && !path.isEmpty()) {
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                        String filename = path.substring(lastSlash + 1);
                        int q = filename.indexOf('?');
                        if (q > 0) filename = filename.substring(0, q);
                        if (filename.length() > 0 && filename.length() < 80) {
                            sb.append("File: ").append(filename).append("\n");
                        }
                    }
                }
            } catch (Exception ignored) {}

            // Deteksi format
            String lower = url.toLowerCase();
            if (lower.contains(".m3u8")) sb.append("Format: HLS Streaming\n");
            else if (lower.contains(".mp4")) sb.append("Format: MP4\n");
            else if (lower.contains(".mp3")) sb.append("Format: Audio MP3\n");
            else if (lower.contains("drive.google.com")) sb.append("Sumber: Google Drive\n");
        }

        return sb.toString();
    }

    private static String loadCache(Context context, String key) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            long time = prefs.getLong("t_" + key.hashCode(), 0);
            if (time == 0) return null;
            if (System.currentTimeMillis() - time > EXPIRE_MS) return null;
            return prefs.getString("d_" + key.hashCode(), null);
        } catch (Exception e) {
            return null;
        }
    }

    private static void saveCache(Context context, String key, String value) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                .putString("d_" + key.hashCode(), value)
                .putLong("t_" + key.hashCode(), System.currentTimeMillis())
                .apply();
        } catch (Exception ignored) {}
    }
}
