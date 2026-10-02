package com.lagradost.cloudstream3;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * Stub CloudStream - MainAPIKt (C-2b-4 fix).
 * Top-level helper functions dari MainAPI.kt.
 */
public class MainAPIKt {

    public MainAPIKt() {}

    // ============================================================
    // mainPageOf(vararg Pair<String, List<SearchResponse>>): List
    // ============================================================
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List mainPageOf(kotlin.Pair[] pairs) {
        List result = new ArrayList();
        if (pairs == null) return result;
        for (kotlin.Pair p : pairs) {
            try {
                if (p == null) continue;
                Object first = p.getFirst();
                Object second = p.getSecond();
                String name = (first == null) ? "" : first.toString();
                List<SearchResponse> list = (second instanceof List)
                    ? (List<SearchResponse>) second
                    : new ArrayList<SearchResponse>();
                result.add(new HomePageResponse.HomePageList(name, list));
            } catch (Throwable ignored) {}
        }
        return result;
    }

    // ============================================================
    // newHomePageResponse - helper
    // ============================================================
    public static HomePageResponse newHomePageResponse(List<HomePageResponse.HomePageList> items) {
        return new HomePageResponse(items, false);
    }

    public static HomePageResponse newHomePageResponse(List<HomePageResponse.HomePageList> items, boolean hasNext) {
        return new HomePageResponse(items, hasNext);
    }

    /** Signature $default: (List, boolean, int, Object) */
    public static HomePageResponse newHomePageResponse$default(
            List<HomePageResponse.HomePageList> items, boolean hasNext, int mask, Object marker) {
        if (items == null) items = new ArrayList<>();
        return new HomePageResponse(items, hasNext);
    }

    // ============================================================
    // newEpisode - helper
    // ============================================================
    public static Episode newEpisode(String url) {
        return new Episode("", url);
    }

    public static Episode newEpisode(String url, String name) {
        return new Episode(name == null ? "" : name, url);
    }

    /** Signature $default: (String, String, int, Object) */
    public static Episode newEpisode$default(String url, String name, int mask, Object marker) {
        return new Episode(name == null ? "" : name, url);
    }

    // ============================================================
    // newAnimeSearchResponse - helper
    // ============================================================
    public static AnimeSearchResponse newAnimeSearchResponse(
            String name, String url, TvType type) {
        return new AnimeSearchResponse(name, url, "anime", DubStatus.Subbed);
    }

    public static AnimeSearchResponse newAnimeSearchResponse(
            String name, String url, TvType type, DubStatus dubStatus) {
        return new AnimeSearchResponse(name, url, "anime", dubStatus);
    }

    /** Signature $default: (String, String, TvType, DubStatus, Function1, int, Object) */
    public static AnimeSearchResponse newAnimeSearchResponse$default(
            String name, String url, TvType type, DubStatus dubStatus,
            Function1 initializer, int mask, Object marker) {
        AnimeSearchResponse resp = new AnimeSearchResponse(name, url, "anime",
            dubStatus == null ? DubStatus.Subbed : dubStatus);
        if (initializer != null) {
            try { initializer.invoke(resp); } catch (Throwable ignored) {}
        }
        return resp;
    }

    // ============================================================
    // newAnimeLoadResponse - helper
    // ============================================================
    public static AnimeLoadResponse newAnimeLoadResponse(
            String name, String url, LoadType type) {
        return new AnimeLoadResponse(name, url, type);
    }

    public static AnimeLoadResponse newAnimeLoadResponse(
            String name, String url, LoadType type, String japName) {
        AnimeLoadResponse r = new AnimeLoadResponse(name, url, type);
        r.japName = japName;
        return r;
    }

    /** Signature $default: (String, String, LoadType, Function1, int, Object) */
    public static AnimeLoadResponse newAnimeLoadResponse$default(
            String name, String url, LoadType type,
            Function1 initializer, int mask, Object marker) {
        AnimeLoadResponse r = new AnimeLoadResponse(name, url, type);
        if (initializer != null) {
            try { initializer.invoke(r); } catch (Throwable ignored) {}
        }
        return r;
    }
}
