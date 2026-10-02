package com.memecio.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryHelper {

    // Kategori TV Channel (untuk M3U/streaming)
    private static final String[][] TV_KEYWORDS = {
        // TV Nasional Indonesia
        {"TV Nasional", "tvri nasional", "tvri", "rcti", "sctv", "indosiar", "antv", "trans tv", "trans7",
                       "mnc tv", "mnctv", "gtv", "global tv", "inews", "net tv", "net.", "kompas tv",
                       "metro tv", "tvone", "tv one", "mnc", "sindo", "cnn indonesia", "cnbc indonesia",
                       "idntv", "idn tv", "daai tv", "daai", "tvri nasional"},

        // TV Daerah / Lokal
        {"TV Daerah", "tvri aceh", "tvri sumut", "tvri sumbar", "tvri sumsel", "tvri riau", "tvri jambi",
                     "tvri babel", "tvri bengkulu", "tvri lampung", "tvri dki", "tvri jakarta",
                     "tvri jabar", "tvri bandung", "tvri jateng", "tvri semarang", "tvri jatim",
                     "tvri surabaya", "tvri yogyakarta", "tvri jogja", "tvri bali", "tvri ntb",
                     "tvri ntt", "tvri kalbar", "tvri kalteng", "tvri kaltim", "tvri kalsel",
                     "tvri sulsel", "tvri sulbar", "tvri sultra", "tvri sulteng", "tvri sulut",
                     "tvri gorontalo", "tvri maluku", "tvri papua", "bali tv", "jogja tv", "jtv",
                     "jatim tv", "batam tv", "padang tv", "balikpapan tv", "riau tv", "jambi tv",
                     "palembang tv", "plp tv", "simpang 5", "malang tv", "batu tv", "fajar tv",
                     "banten tv", "cakra tv", "semarang tv", "tegartv", "tegar tv", "radar tv"},

        // TV Religi
        {"TV Religi", "makkah", "madinah", "rodja", "rodja tv", "daai", "insan", "tv9",
                     "al-bahjah", "albahjah", "al bahjah", "iqra", "hijrah", "islam",
                     "channel islam", "tv islam", "tv muslim", "amin", "tvmu", "muhammadiyah",
                     "salman", "sunnah", "tbn", "kristen", "katolik", "ewtn", "daystar",
                     "cbn", "god tv", "gospel"},

        // TV Internasional
        {"TV Internasional", "nhk", "kbs", "cctv", "cgtn", "france 24", "trt", "al jazeera",
                            "rt", "russia today", "dw", "deutsche welle", "cnn international",
                            "bbc", "abc australia", "rai italia", "rtp", "fox", "sky news",
                            "arirang", "tvn", "mbc", "jtbc", "kbs world", "fuji tv", "tbs",
                            "tokyo mx", "tv asahi", "abc", "nbc", "cbs", "cctv-4", "phoenix",
                            "cgtn", "arirang", "ariran"},

        // TV Berita
        {"TV Berita", "cnn", "cnbc", "metro tv", "tvone", "inews", "kompas tv", "berita",
                     "news", "idn", "sindo", "antara", "antara tv", "tv berita", "jakarta globe",
                     "bloomberg", "reuters", "cnn indonesia"},

        // TV Anak
        {"TV Anak", "kids", "kidz", "cartoon", "toon", "nickelodeon", "nick", "disney",
                   "boomerang", "baby", "cbeebies", "cartoon network", "boomerang",
                   "mychild", "my kidz", "dreamworks"},

        // TV Olahraga
        {"TV Olahraga", "sport", "bola", "espn", "fox sport", "fox sports", "beinsport",
                       "beIN", "mola tv", "mola", "tvri sport", "tvri olahraga", "liga",
                       "soccer", "football", "nba", "formula", "motogp", "f1", "wwe"},

        // TV Hiburan & Film
        {"TV Hiburan", "hbo", "cinemax", "fox movie", "trans tv", "trans7", "mnc tv",
                      "gtv", "sctv", "rcti", "indosiar", "antv", "aniplus", "ani plus",
                      "thrill", "zee", "galaxy", "celestial", "kix", "axn", "warner",
                      "sony", "paramount", "studio universal", "diva", "e! entertainment",
                      "rock action", "rock entertainment", "dens play", "dens life",
                      "my cinema", "my family", "kplus", "k+", "one"},

        // Radio
        {"Radio", "radio", "fm", "am", "swara", "rri", "radio 51"}
    };

    // Kategori konten (film/anime/dll) — untuk video on-demand
    private static final String[][] CONTENT_KEYWORDS = {
        {"Korea", "korea", "drakor", "kdrama", "k-drama"},
        {"Netflix", "netflix"},
        {"Dub Indo", "dub indo", "sub indo", "dubbing indo"},
        {"Film", "film", "movie"},
        {"Anime", "anime", "jepang", "japan"},
        {"Arab", "arab", "arabic"},
        {"China", "china", "mandarin", "chinese"},
        {"Thailand", "thai", "thailand"},
        {"Turki", "turki", "turkish"},
        {"India", "india", "bollywood", "hindi"},
        {"Barat", "barat", "western", "hollywood"}
    };

    public static List<String> deteksiKategori(String judul) {
        List<String> hasil = new ArrayList<>();
        if (judul == null || judul.isEmpty()) return hasil;
        String lower = judul.toLowerCase(Locale.getDefault());

        // Cek TV keywords dulu (prioritas karena channel TV biasanya punya nama jelas)
        for (String[] entry : TV_KEYWORDS) {
            String catName = entry[0];
            for (int i = 1; i < entry.length; i++) {
                if (lower.contains(entry[i])) {
                    hasil.add(catName);
                    break;
                }
            }
        }

        // Kalau tidak ada TV match, cek content keywords
        if (hasil.isEmpty()) {
            for (String[] entry : CONTENT_KEYWORDS) {
                String catName = entry[0];
                for (int i = 1; i < entry.length; i++) {
                    if (lower.contains(entry[i])) {
                        hasil.add(catName);
                        break;
                    }
                }
            }
        }

        return hasil;
    }

    /**
     * Deteksi kategori utama (1 saja) untuk grouping cepat.
     */
    public static String deteksiKategoriUtama(String judul) {
        List<String> list = deteksiKategori(judul);
        if (list.isEmpty()) return "Lainnya";
        return list.get(0);
    }
}
