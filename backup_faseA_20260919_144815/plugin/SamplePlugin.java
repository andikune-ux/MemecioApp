package com.memecio.app.plugin;

import android.content.Context;
import com.memecio.app.plugin.model.PluginLink;
import com.memecio.app.plugin.model.PluginMediaItem;
import com.memecio.app.plugin.model.PluginSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * SAMPLE PLUGIN — CONTOH & TESTING
 * ============================================================
 *
 * Plugin dummy untuk menguji fondasi PluginManager.
 * Tidak scraping apapun — hanya mengembalikan data contoh.
 */
public class SamplePlugin implements MemecioPlugin {

    @Override public String getName()        { return "Sample"; }
    @Override public String getVersion()     { return "1.0.0"; }
    @Override public String getAuthor()      { return "Memec.io"; }
    @Override public String getDescription() { return "Plugin contoh untuk testing fondasi."; }
    @Override public String[] getSupportedTypes() { return new String[]{"movie", "tv", "anime"}; }
    @Override public MemecioProvider getProvider() { return new SampleProvider(); }

    // ============================================================
    // PROVIDER
    // ============================================================
    public static class SampleProvider extends MemecioProvider {

        @Override
        public void onInit(Context ctx) {
            super.onInit(ctx);
            PluginLogger.log("Sample", "onInit dipanggil");
        }

        @Override
        public List<PluginSearchResult> search(String query) {
            List<PluginSearchResult> list = new ArrayList<>();
            PluginSearchResult r = new PluginSearchResult();
            r.id = "sample_1";
            r.title = "Hasil untuk: " + (query == null ? "?" : query);
            r.posterUrl = "";
            r.url = "sample://item/1";
            r.type = PluginSearchResult.TYPE_MOVIE;
            r.year = 2026;
            list.add(r);
            return list;
        }

        @Override
        public List<PluginMediaItem> getMainPage() {
            List<PluginMediaItem> list = new ArrayList<>();
            PluginMediaItem m = new PluginMediaItem(
                "sample_home_1", "Contoh Film", "sample://item/home_1");
            m.posterUrl = "";
            m.description = "Film contoh dari Sample Plugin.";
            m.year = 2026;
            list.add(m);
            return list;
        }

        @Override
        public List<PluginLink> loadLinks(PluginMediaItem item) {
            List<PluginLink> list = new ArrayList<>();
            list.add(new PluginLink(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "720p"));
            return list;
        }

        @Override public List<String> getGenres() {
            List<String> g = new ArrayList<>();
            g.add("Action"); g.add("Comedy"); g.add("Drama");
            return g;
        }
    }
}
