package com.lagradost.cloudstream3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ============================================================
 * STUB CLOUDSTREAM — MainAPI (C-1)
 * ============================================================
 *
 * Abstract base class yang di-extends oleh setiap plugin CloudStream.
 *
 * Di CloudStream asli, method wajib pakai `suspend` (Kotlin coroutines).
 * Di stub Java ini, method wajib tidak pakai suspend — untuk kompatibilitas
 * dasar. Support `suspend` sebenarnya akan ditambah di C-2+.
 */
public abstract class MainAPI {

    // Field publik yang bisa di-override plugin
    public String mainUrl = "";
    public String name = "";
    public boolean hasMainPage = false;
    public boolean hasQuickSearch = false;
    public boolean hasDownloadSupport = false;
    public boolean hasChromecastSupport = false;
    public boolean usesCloudflare = false;
    public Set<TvType> supportedTypes = new HashSet<>();
    public String description = "";

    public MainAPI() {}

    /** Dipanggil sekali saat plugin di-load */
    public void init() {}

    // ============================================================
    // METHOD YANG DI-OVERRIDE PLUGIN (return null/false default)
    // ============================================================

    public HomePageResponse getMainPage(int page, MainPageRequest request) {
        return null;
    }

    public List<SearchResponse> search(String query) {
        return new ArrayList<>();
    }

    public LoadResponse load(String url) {
        return null;
    }

    public boolean loadLinks(String data, boolean isCasting,
                              SubtitleCallback subtitleCallback,
                              ExtractorCallback callback) {
        return false;
    }

    // ============================================================
    // REQUEST DATA CLASS
    // ============================================================

    public static class MainPageRequest {
        public String name;
        public String data;
        public boolean horizontalImages;

        public MainPageRequest(String name, String data) {
            this.name = name;
            this.data = data;
            this.horizontalImages = false;
        }

        public MainPageRequest(String name, String data, boolean horizontalImages) {
            this.name = name;
            this.data = data;
            this.horizontalImages = horizontalImages;
        }
    }

    /** Callback extractor — plugin panggil untuk kirim link video */
    public interface ExtractorCallback {
        void invoke(ExtractorLink link);
    }
}
