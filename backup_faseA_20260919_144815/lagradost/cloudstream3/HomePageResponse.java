package com.lagradost.cloudstream3;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub CloudStream - HomePageResponse (C-1).
 * Response utama dari getMainPage().
 */
public class HomePageResponse {
    public List<HomePageList> items = new ArrayList<>();
    public boolean hasNext;

    public HomePageResponse() {}

    public HomePageResponse(List<HomePageList> items, boolean hasNext) {
        this.items = items;
        this.hasNext = hasNext;
    }

    /** Satu baris (row) di home page */
    public static class HomePageList {
        public String name;
        public List<SearchResponse> list;

        public HomePageList(String name, List<SearchResponse> list) {
            this.name = name;
            this.list = list;
        }
    }
}
