package com.memecio.app.plugin;

import android.content.Context;
import android.util.Log;

import com.lagradost.cloudstream3.MainAPI;
import com.memecio.app.plugin.model.PluginLink;
import com.memecio.app.plugin.model.PluginMediaItem;
import com.memecio.app.plugin.model.PluginSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter: MainAPI CloudStream -> MemecioProvider.
 * C-2b-2: panggil search() via CoroutineHelper.
 */
public class CloudStreamProviderAdapter extends MemecioProvider {

    private static final String TAG = "CSProviderAdapter";
    private static String lastError = null;

    public static String getLastError() { return lastError; }
    public static void clearLastError() { lastError = null; }

    private final com.lagradost.cloudstream3.plugins.Plugin csPlugin;
    private final PluginDescriptor descriptor;
    private MainAPI mainApi;

    public CloudStreamProviderAdapter(com.lagradost.cloudstream3.plugins.Plugin csPlugin,
                                       PluginDescriptor descriptor) {
        this.csPlugin = csPlugin;
        this.descriptor = descriptor;
        try {
            this.mainApi = csPlugin.getMainPlugin();
            Log.d(TAG, "MainAPI: " + (mainApi != null ? mainApi.getClass().getName() : "null"));
        } catch (Throwable t) {
            Log.e(TAG, "GetMainPlugin error", t);
            lastError = "GetMainPlugin: " + t;
            this.mainApi = null;
        }
    }

    @Override
    public void onInit(Context context) {
        super.onInit(context);

        StringBuilder err = new StringBuilder();

        // Step 1: panggil load() - jangan overwrite lastError kalau sudah ada
        try {
            if (csPlugin != null) {
                csPlugin.load(context);
                Log.d(TAG, "Plugin.load() selesai");
            }
        } catch (Throwable t) {
            Log.e(TAG, "Plugin.load error", t);
            err.append("load: ").append(t.getClass().getSimpleName());
            if (t.getMessage() != null) err.append(" - ").append(t.getMessage());
            err.append("; ");
        }

        // Step 2: resolve mainApi pakai semua cara
        if (mainApi == null) {
            mainApi = resolveMainApi();
            Log.d(TAG, "MainAPI resolved: " + (mainApi != null ? mainApi.getClass().getName() : "null"));
        }

        // Step 3: kalau tetap null, coba instantiate langsung
        if (mainApi == null) {
            mainApi = tryInstantiateDirectApi(context);
            Log.d(TAG, "MainAPI via instantiate: " + (mainApi != null ? mainApi.getClass().getName() : "null"));
        }

        if (mainApi == null && err.length() > 0) {
            lastError = err.toString();
        }
    }

    /** Coba semua method + field yang umum */
    private MainAPI resolveMainApi() {
        if (csPlugin == null) return null;

        // Coba method getter
        String[] methods = {"getMainPlugin", "getApi", "getMainApi"};
        for (String m : methods) {
            try {
                java.lang.reflect.Method method = csPlugin.getClass().getMethod(m);
                method.setAccessible(true);
                Object r = method.invoke(csPlugin);
                if (r instanceof MainAPI) return (MainAPI) r;
            } catch (Throwable ignored) {}
        }

        // Coba field langsung (naik ke parent)
        String[] fields = {"api", "mainApi", "mainPlugin", "plugin"};
        Class<?> clazz = csPlugin.getClass();
        while (clazz != null) {
            for (String f : fields) {
                try {
                    java.lang.reflect.Field field = clazz.getDeclaredField(f);
                    field.setAccessible(true);
                    Object r = field.get(csPlugin);
                    if (r instanceof MainAPI) return (MainAPI) r;
                } catch (Throwable ignored) {}
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

    /** Instantiate MainAPI langsung dari nama class (AnoboyPlugin -> Anoboy) */
    private MainAPI tryInstantiateDirectApi(Context context) {
        try {
            String cn = descriptor.pluginClassName;
            if (cn == null || cn.isEmpty()) return null;

            // Coba beberapa variasi nama
            String[] candidates = new String[]{
                cn.replace("Plugin", ""),   // com.anoboy.AnoboyPlugin -> com.anoboy.Anoboy
                cn + "Provider"
            };

            ClassLoader cl = csPlugin != null ? csPlugin.getClass().getClassLoader()
                                              : context.getClassLoader();

            for (String c : candidates) {
                try {
                    Class<?> k = Class.forName(c, true, cl);
                    Object obj = k.newInstance();
                    if (obj instanceof MainAPI) {
                        Log.d(TAG, "Instantiate direct OK: " + c);
                        // Set ke csPlugin supaya getMainPlugin() juga return value
                        try {
                            csPlugin.setApi((MainAPI) obj);
                            Log.d(TAG, "setApi() ke csPlugin OK");
                        } catch (Throwable ignored) {}
                        return (MainAPI) obj;
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Instantiate " + c + " fail: " + t.getMessage());
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @Override
    public List<PluginSearchResult> search(String query) {
        lastError = null;
        List<PluginSearchResult> out = new ArrayList<>();

        if (mainApi == null) {
            if (lastError == null || lastError.isEmpty()) {
                lastError = "MainAPI null - plugin tidak punya provider";
            }
            return out;
        }

        Log.d(TAG, "search(): " + query);
        CoroutineHelper.Result r = CoroutineHelper.callSuspend(mainApi, "search", query);

        if (!r.success) {
            lastError = formatError(r.error);
            Log.e(TAG, "search failed: " + lastError, r.error);
            return out;
        }

        Object data = r.data;
        Log.d(TAG, "search data class: " + (data == null ? "null" : data.getClass().getName()));

        if (data == null) { lastError = "Hasil null dari plugin"; return out; }
        if (!(data instanceof List)) {
            lastError = "Hasil bukan List: " + data.getClass().getName();
            return out;
        }

        for (Object o : (List<?>) data) {
            try {
                if (o instanceof com.lagradost.cloudstream3.SearchResponse) {
                    com.lagradost.cloudstream3.SearchResponse sr =
                        (com.lagradost.cloudstream3.SearchResponse) o;
                    PluginSearchResult psr = new PluginSearchResult();
                    psr.title = sr.name;
                    psr.url = sr.url;
                    psr.id = sr.url;
                    psr.posterUrl = sr.posterUrl;
                    psr.year = sr.year != null ? sr.year : 0;
                    out.add(psr);
                } else {
                    Log.w(TAG, "Item bukan SearchResponse: "
                        + (o == null ? "null" : o.getClass().getName()));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Convert error", t);
            }
        }

        Log.d(TAG, "search done: " + out.size() + " hasil");
        return out;
    }

    @Override
    public List<PluginMediaItem> getMainPage() {
        lastError = null;
        List<PluginMediaItem> out = new ArrayList<>();

        if (mainApi == null) {
            lastError = "MainAPI null";
            return out;
        }

        CoroutineHelper.Result r = CoroutineHelper.callSuspend(mainApi, "getMainPage", 1, null);
        if (!r.success) {
            lastError = formatError(r.error);
            Log.e(TAG, "getMainPage failed: " + lastError, r.error);
            return out;
        }

        Object data = r.data;
        if (data == null) return out;

        if (data instanceof com.lagradost.cloudstream3.HomePageResponse) {
            com.lagradost.cloudstream3.HomePageResponse hp =
                (com.lagradost.cloudstream3.HomePageResponse) data;
            if (hp.items != null) {
                for (com.lagradost.cloudstream3.HomePageResponse.HomePageList row : hp.items) {
                    if (row == null || row.list == null) continue;
                    for (Object obj : row.list) {
                        if (!(obj instanceof com.lagradost.cloudstream3.SearchResponse)) continue;
                        com.lagradost.cloudstream3.SearchResponse sr =
                            (com.lagradost.cloudstream3.SearchResponse) obj;
                        PluginMediaItem pi = new PluginMediaItem();
                        pi.title = sr.name;
                        pi.url = sr.url;
                        pi.id = sr.url;
                        pi.posterUrl = sr.posterUrl;
                        pi.year = sr.year != null ? sr.year : 0;
                        out.add(pi);
                    }
                }
            }
        }

        Log.d(TAG, "getMainPage done: " + out.size() + " item");
        return out;
    }

    @Override
    public PluginMediaItem loadDetail(PluginMediaItem item) {
        lastError = null;
        if (mainApi == null) {
            if (lastError == null || lastError.isEmpty()) {
                lastError = "MainAPI null";
            }
            return item;
        }
        if (item == null || item.url == null || item.url.isEmpty()) {
            return item;
        }

        Log.d(TAG, "loadDetail(): " + item.url);
        CoroutineHelper.Result r = CoroutineHelper.callSuspend(mainApi, "load", item.url);

        if (!r.success) {
            lastError = formatError(r.error);
            Log.e(TAG, "loadDetail failed: " + lastError, r.error);
            return item;
        }

        Object data = r.data;
        if (data == null) {
            lastError = "Hasil load() null";
            return item;
        }
        if (!(data instanceof com.lagradost.cloudstream3.LoadResponse)) {
            lastError = "Hasil bukan LoadResponse: " + data.getClass().getName();
            return item;
        }

        try {
            com.lagradost.cloudstream3.LoadResponse lr =
                (com.lagradost.cloudstream3.LoadResponse) data;

            // Enrich item
            if (lr.plot != null && !lr.plot.isEmpty()) item.description = lr.plot;
            if (lr.posterUrl != null && !lr.posterUrl.isEmpty()) item.posterUrl = lr.posterUrl;
            if (lr.duration != null && !lr.duration.isEmpty()) item.duration = lr.duration;
            if (lr.year != null && lr.year > 0) item.year = lr.year;
            if (lr.tags != null && !lr.tags.isEmpty()) {
                item.tags = new java.util.ArrayList<>(lr.tags);
            }
            if (lr.episodes != null && !lr.episodes.isEmpty()) {
                item.episodeCount = lr.episodes.size();
            }

            Log.d(TAG, "loadDetail ok: ep=" + item.episodeCount + " dur=" + item.duration);
        } catch (Throwable t) {
            lastError = formatError(t);
            Log.e(TAG, "Convert LoadResponse error: " + lastError, t);
        }

        return item;
    }

    @Override
    public List<PluginLink> loadLinks(PluginMediaItem item) {
        lastError = null;
        final List<PluginLink> out = new ArrayList<>();

        if (mainApi == null) {
            if (lastError == null || lastError.isEmpty()) {
                lastError = "MainAPI null - plugin tidak punya provider";
            }
            return out;
        }

        if (item == null || item.url == null || item.url.isEmpty()) {
            lastError = "Item tidak valid (URL kosong)";
            return out;
        }

        Log.d(TAG, "loadLinks(): " + item.url);

        try {
            // Bikin callback untuk subtitle + link
            kotlin.jvm.functions.Function1<com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> subCb =
                LoadLinksCallback.subtitle(s -> {
                    Log.d(TAG, "Subtitle: " + (s != null ? s.url : "null"));
                });

            final java.util.concurrent.CountDownLatch linkLatch =
                new java.util.concurrent.CountDownLatch(1);

            kotlin.jvm.functions.Function1<com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> linkCb =
                LoadLinksCallback.link(l -> {
                    try {
                        if (l == null) return;
                        PluginLink pl = new PluginLink();
                        pl.url = l.url;
                        pl.quality = (l.quality > 0 ? (l.quality + "p") : "auto");
                        out.add(pl);
                        Log.d(TAG, "Link ditambahkan: " + l.url);
                    } catch (Throwable t) {
                        Log.e(TAG, "Link convert error", t);
                    } finally {
                        linkLatch.countDown();
                    }
                });

            // Coba panggil method loadLinks yang signature-nya 4 argumen
            java.lang.reflect.Method target = null;
            for (java.lang.reflect.Method m : mainApi.getClass().getMethods()) {
                if (!m.getName().equals("loadLinks")) continue;
                Class<?>[] ptypes = m.getParameterTypes();
                if (ptypes.length != 4) continue;
                target = m; break;
            }

            if (target == null) {
                lastError = "Method loadLinks(4 args) tidak ditemukan di plugin";
                return out;
            }

            Object ret = target.invoke(mainApi, item.url, false, subCb, linkCb);

            // Kalau return-nya COROUTINE_SUSPENDED -> tunggu latch
            boolean suspended = false;
            try {
                Class<?> intrinsics = Class.forName("kotlin.coroutines.intrinsics.IntrinsicsKt");
                java.lang.reflect.Method getSuspended = intrinsics.getMethod("getCOROUTINE_SUSPENDED");
                Object suspendedVal = getSuspended.invoke(null);
                suspended = (ret == suspendedVal);
            } catch (Throwable ignored) {}

            if (suspended) {
                // Tunggu sampai callback link dipanggil (maks 30s)
                try { linkLatch.await(30, java.util.concurrent.TimeUnit.SECONDS); }
                catch (Throwable ignored) {}
            }

            Log.d(TAG, "loadLinks done: " + out.size() + " link");

        } catch (Throwable t) {
            lastError = formatError(t);
            Log.e(TAG, "loadLinks error: " + lastError, t);
        }

        return out;
    }

    /** Dump info lengkap plugin untuk debugging */
    public String debugDump() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PLUGIN DEBUG ===\n");
        sb.append("Descriptor name: ").append(descriptor.name).append("\n");
        sb.append("Descriptor className: ").append(descriptor.className).append("\n");
        sb.append("Descriptor pluginClassName: ").append(descriptor.pluginClassName).append("\n");
        sb.append("isCloudStream: ").append(descriptor.isCloudStream).append("\n");

        if (csPlugin == null) {
            sb.append("csPlugin: NULL\n");
            return sb.toString();
        }

        sb.append("csPlugin class: ").append(csPlugin.getClass().getName()).append("\n");
        sb.append("csPlugin superclass: ").append(csPlugin.getClass().getSuperclass() != null ?
                csPlugin.getClass().getSuperclass().getName() : "null").append("\n");
        sb.append("mainApi: ").append(mainApi != null ? mainApi.getClass().getName() : "null").append("\n");
        sb.append("lastError: ").append(lastError).append("\n");

        // Dump semua method
        sb.append("\n--- METHODS ---\n");
        try {
            java.lang.reflect.Method[] methods = csPlugin.getClass().getMethods();
            for (java.lang.reflect.Method m : methods) {
                String mn = m.getName();
                if (mn.startsWith("__") || mn.contains("synthetic")) continue;
                sb.append(mn).append("(");
                Class<?>[] ps = m.getParameterTypes();
                for (int i = 0; i < ps.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(ps[i].getSimpleName());
                }
                sb.append(") -> ").append(m.getReturnType().getSimpleName()).append("\n");
            }
        } catch (Throwable t) {
            sb.append("Method dump error: ").append(t).append("\n");
        }

        // Dump semua field
        sb.append("\n--- FIELDS ---\n");
        try {
            Class<?> clazz = csPlugin.getClass();
            while (clazz != null && !clazz.getName().equals("java.lang.Object")) {
                java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
                for (java.lang.reflect.Field f : fields) {
                    f.setAccessible(true);
                    String val;
                    try {
                        Object v = f.get(csPlugin);
                        val = (v == null) ? "null" : v.getClass().getSimpleName() + "@" + Integer.toHexString(v.hashCode());
                    } catch (Throwable ignored) {
                        val = "?";
                    }
                    sb.append(clazz.getSimpleName()).append(".").append(f.getName())
                      .append(" : ").append(f.getType().getSimpleName())
                      .append(" = ").append(val).append("\n");
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable t) {
            sb.append("Field dump error: ").append(t).append("\n");
        }

        // Coba instantiate Anoboy langsung
        sb.append("\n--- TRY INSTANTIATE ---\n");
        String[] candidates = {"com.anoboy.Anoboy", "com.anoboy.AnoboyPlugin"};
        ClassLoader cl = csPlugin.getClass().getClassLoader();
        for (String cn : candidates) {
            try {
                Class<?> k = Class.forName(cn, true, cl);
                sb.append("Class ").append(cn).append(" OK (super=").append(k.getSuperclass() != null ? k.getSuperclass().getName() : "?").append(")\n");
                try {
                    Object obj = k.newInstance();
                    sb.append("  newInstance OK: ").append(obj.getClass().getName()).append("\n");
                } catch (Throwable t) {
                    sb.append("  newInstance fail: ").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
                }
            } catch (Throwable t) {
                sb.append("Class ").append(cn).append(" NOT FOUND\n");
            }
        }

        return sb.toString();
    }

    private static String formatError(Throwable t) {
        if (t == null) return "unknown error";
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getSimpleName());
        if (t.getMessage() != null) sb.append(": ").append(t.getMessage());
        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            sb.append(" \u2192 ").append(cause.getClass().getSimpleName());
            if (cause.getMessage() != null) sb.append(": ").append(cause.getMessage());
        }
        return sb.toString();
    }
}
