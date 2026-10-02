package com.lagradost.cloudstream3;

/**
 * Stub CloudStream - SearchResponseKt (C-2b-4 fix).
 * Extension function toSearchResponse.
 */
public class SearchResponseKt {

    public SearchResponseKt() {}

    /** Extension: SearchResponse.toSearchResponse(...) - return as-is */
    public static SearchResponse toSearchResponse(SearchResponse base) {
        return base;
    }

    /** Overload dengan varargs supaya fleksibel */
    public static SearchResponse toSearchResponse(SearchResponse base, Object... args) {
        return base;
    }

    /** Extension pada List<SearchResponse> */
    public static java.util.List<SearchResponse> toSearchResponse(
            java.util.List<SearchResponse> base) {
        return base;
    }
}
