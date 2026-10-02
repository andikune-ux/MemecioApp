package com.memecio.app;

import java.util.ArrayList;
import java.util.List;

public class MixedPlaylistHolder {
    private static List<MediaItem> items = new ArrayList<>();
    private static int currentIndex = 0;

    public static void set(List<MediaItem> list, int index) {
        items = new ArrayList<>(list);
        currentIndex = index;
    }

    public static List<MediaItem> getItems() {
        return items;
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public static void setCurrentIndex(int i) {
        currentIndex = i;
    }

    public static MediaItem getCurrent() {
        if (items.isEmpty() || currentIndex < 0 || currentIndex >= items.size()) return null;
        return items.get(currentIndex);
    }

    public static boolean isActive() {
        return !items.isEmpty();
    }

    public static void clear() {
        items = new ArrayList<>();
        currentIndex = 0;
    }
}
