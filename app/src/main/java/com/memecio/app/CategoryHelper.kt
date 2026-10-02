package com.memecio.app

import java.util.Locale

object CategoryHelper {

    // Kategori TV Channel (untuk M3U/streaming)
    private val TV_KEYWORDS: Array<Array<String>> = arrayOf(
        // TV Nasional Indonesia
        arrayOf(
            "TV Nasional", "tvri nasional", "tvri", "rcti", "sctv",
            "indosiar", "antv", "trans tv", "trans7", "mnc tv", "mnctv", "gtv", "global tv",
            "inews", "net tv", "net.", "kompas tv", "metro tv", "tvone", "tv one", "mnc",
            "sindo", "cnn indonesia", "cnbc indonesia", "idntv", "idn tv", "daai tv",
            "daai", "tvri nasional"
        ),
        // TV Daerah / Lokal
        arrayOf(
            "TV Daerah", "tvri aceh", "tvri sumut", "tvri sumbar", "tvri sumsel", "tvri riau", "tvri jambi", "tvri babel",
            "tvri bengkulu", "tvri lampung", "tvri dki", "tvri jakarta", "tvri jabar", "tvri bandung",
            "tvri jateng", "tvri semarang", "tvri jatim", "tvri surabaya", "tvri yogyakarta", "tvri jogja",
            "tvri bali", "tvri ntb", "tvri ntt", "tvri kalbar", "tvri kalteng", "tvri kaltim", "tvri kalsel",
            "tvri sulsel", "tvri sulbar", "tvri sultra", "tvri sulteng", "tvri sulut", "tvri gorontalo",
            "tvri maluku", "tvri papua", "bali tv", "jogja tv", "jtv", "jatim tv", "batam tv", "padang tv",
            "balikpapan tv", "riau tv", "jambi tv", "palembang tv", "plp tv", "simpang 5",
            "malang tv", "batu tv", "fajar tv", "banten tv", "cakra tv", "semarang tv",
            "tegartv", "tegar tv", "radar tv"
        ),
        // TV Religi
        arrayOf(
            "TV Religi", "makkah", "madinah", "rodja", "rodja tv", "daai", "insan", "tv9",
            "al-bahjah", "albahjah", "al bahjah", "iqra", "hijrah", "islam", "channel islam",
            "tv islam", "tv muslim", "amin", "tvmu", "muhammadiyah", "salman", "sunnah",
            "tbn", "kristen", "katolik", "ewtn", "daystar", "cbn", "god tv", "gospel"
        ),
        // TV Internasional
        arrayOf(
            "TV Internasional", "nhk", "kbs", "cctv", "cgtn", "france 24", "trt",
            "al jazeera", "rt", "russia today", "dw", "deutsche welle", "cnn international",
            "bbc", "abc australia", "rai italia", "rtp", "fox", "sky news", "arirang",
            "tvn", "mbc", "jtbc", "kbs world", "fuji tv", "tbs", "tokyo mx", "tv asahi",
            "abc", "nbc", "cbs", "cctv-4", "phoenix", "cgtn", "arirang", "ariran"
        ),
        // TV Berita
        arrayOf(
            "TV Berita", "cnn", "cnbc", "metro tv", "tvone", "inews", "kompas tv",
            "berita", "news", "idn", "sindo", "antara", "antara tv", "tv berita",
            "jakarta globe", "bloomberg", "reuters", "cnn indonesia"
        ),
        // TV Anak
        arrayOf(
            "TV Anak", "kids", "kidz", "cartoon", "toon", "nickelodeon", "nick",
            "disney", "boomerang", "baby", "cbeebies", "cartoon network", "boomerang",
            "mychild", "my kidz", "dreamworks"
        ),
        // TV Olahraga
        arrayOf(
            "TV Olahraga", "sport", "bola", "espn", "fox sport", "fox sports",
            "beinsport", "beIN", "mola tv", "mola", "tvri sport", "tvri olahraga",
            "liga", "soccer", "football", "nba", "formula", "motogp", "f1", "wwe"
        ),
        // TV Hiburan & Film
        arrayOf(
            "TV Hiburan", "hbo", "cinemax", "fox movie", "trans tv", "trans7", "mnc tv",
            "gtv", "sctv", "rcti", "indosiar", "antv", "aniplus", "ani plus", "thrill",
            "zee", "galaxy", "celestial", "kix", "axn", "warner", "sony", "paramount",
            "studio universal", "diva", "e! entertainment", "rock action",
            "rock entertainment", "dens play", "dens life", "my cinema", "my family",
            "kplus", "k+", "one"
        ),
        // Radio
        arrayOf(
            "Radio", "radio", "fm", "am", "swara", "rri", "radio 51"
        )
    )

    // Kategori konten (film/anime/dll) — untuk video on-demand
    private val CONTENT_KEYWORDS: Array<Array<String>> = arrayOf(
        arrayOf("Korea", "korea", "drakor", "kdrama", "k-drama"),
        arrayOf("Netflix", "netflix"),
        arrayOf("Dub Indo", "dub indo", "sub indo", "dubbing indo"),
        arrayOf("Film", "film", "movie"),
        arrayOf("Anime", "anime", "jepang", "japan"),
        arrayOf("Arab", "arab", "arabic"),
        arrayOf("China", "china", "mandarin", "chinese"),
        arrayOf("Thailand", "thai", "thailand"),
        arrayOf("Turki", "turki", "turkish"),
        arrayOf("India", "india", "bollywood", "hindi"),
        arrayOf("Barat", "barat", "western", "hollywood")
    )

    @JvmStatic
    fun deteksiKategori(judul: String?): List<String> {
        val hasil = mutableListOf<String>()
        if (judul.isNullOrEmpty()) return hasil
        val lower = judul.lowercase(Locale.getDefault())

        // Cek TV keywords dulu (prioritas karena channel TV biasanya punya nama jelas)
        for (entry in TV_KEYWORDS) {
            val catName = entry[0]
            for (i in 1 until entry.size) {
                if (lower.contains(entry[i])) {
                    hasil.add(catName)
                    break
                }
            }
        }

        // Kalau tidak ada TV match, cek content keywords
        if (hasil.isEmpty()) {
            for (entry in CONTENT_KEYWORDS) {
                val catName = entry[0]
                for (i in 1 until entry.size) {
                    if (lower.contains(entry[i])) {
                        hasil.add(catName)
                        break
                    }
                }
            }
        }
        return hasil
    }

    /**
     * Deteksi kategori utama (1 saja) untuk grouping cepat.
     */
    @JvmStatic
    fun deteksiKategoriUtama(judul: String?): String {
        val list = deteksiKategori(judul)
        if (list.isEmpty()) return "Lainnya"
        return list[0]
    }
}
