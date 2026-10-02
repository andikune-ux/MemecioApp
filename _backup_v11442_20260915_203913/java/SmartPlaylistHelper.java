package com.memecio.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SmartPlaylistHelper — grouping media berdasarkan mode.
 * Mode: folder, date, size, duration (kategori pakai CategoryHelper existing).
 */
public class SmartPlaylistHelper {

    public static final String MODE_RESET = "";
    public static final String MODE_KATEGORI = "kategori";
    public static final String MODE_FOLDER = "folder";
    public static final String MODE_DATE = "date";
    public static final String MODE_SIZE = "size";
    public static final String MODE_DURATION = "duration";

    // ----- FOLDER -----
    public static List<MediaItem> filterByFolder(List<MediaItem> input, String folder) {
        List<MediaItem> out = new ArrayList<>();
        for (MediaItem m : input) {
            String f = m.folderPath != null ? m.folderPath : "(tanpa folder)";
            if (f.equals(folder)) out.add(m);
        }
        return out;
    }

    public static List<String> getFolders(List<MediaItem> input) {
        List<String> list = new ArrayList<>();
        for (MediaItem m : input) {
            String f = m.folderPath != null ? m.folderPath : "(tanpa folder)";
            if (!list.contains(f)) list.add(f);
        }
        return list;
    }

    public static int countInFolder(List<MediaItem> input, String folder) {
        int c = 0;
        for (MediaItem m : input) {
            String f = m.folderPath != null ? m.folderPath : "(tanpa folder)";
            if (f.equals(folder)) c++;
        }
        return c;
    }

    // ----- DATE -----
    public static String dateGroupLabel(long dateAddedSec) {
        if (dateAddedSec <= 0) return "Tidak diketahui";
        long ms = dateAddedSec * 1000L;
        long now = System.currentTimeMillis();
        long diff = now - ms;
        long day = 24L * 60 * 60 * 1000;
        if (diff < day) return "Hari ini";
        if (diff < 7 * day) return "Minggu ini";
        if (diff < 30 * day) return "Bulan ini";
        if (diff < 365 * day) return "Tahun ini";
        return "Lebih lama";
    }

    public static List<String> getDateGroups() {
        List<String> l = new ArrayList<>();
        l.add("Hari ini");
        l.add("Minggu ini");
        l.add("Bulan ini");
        l.add("Tahun ini");
        l.add("Lebih lama");
        l.add("Tidak diketahui");
        return l;
    }

    public static List<MediaItem> filterByDate(List<MediaItem> input, String group) {
        List<MediaItem> out = new ArrayList<>();
        for (MediaItem m : input) {
            if (dateGroupLabel(m.dateAdded).equals(group)) out.add(m);
        }
        return out;
    }

    // ----- SIZE -----
    public static String sizeGroupLabel(long bytes) {
        long mb = bytes / (1024L * 1024L);
        if (mb < 100) return "Kecil (<100 MB)";
        if (mb < 1024) return "Sedang (100 MB - 1 GB)";
        return "Besar (>1 GB)";
    }

    public static List<String> getSizeGroups() {
        List<String> l = new ArrayList<>();
        l.add("Kecil (<100 MB)");
        l.add("Sedang (100 MB - 1 GB)");
        l.add("Besar (>1 GB)");
        return l;
    }

    public static List<MediaItem> filterBySize(List<MediaItem> input, String group) {
        List<MediaItem> out = new ArrayList<>();
        for (MediaItem m : input) {
            if (sizeGroupLabel(m.size).equals(group)) out.add(m);
        }
        return out;
    }

    // ----- DURATION -----
    public static String durationGroupLabel(long ms) {
        long min = ms / 60000L;
        if (min < 5) return "Pendek (<5 menit)";
        if (min < 30) return "Sedang (5-30 menit)";
        return "Panjang (>30 menit)";
    }

    public static List<String> getDurationGroups() {
        List<String> l = new ArrayList<>();
        l.add("Pendek (<5 menit)");
        l.add("Sedang (5-30 menit)");
        l.add("Panjang (>30 menit)");
        return l;
    }

    public static List<MediaItem> filterByDuration(List<MediaItem> input, String group) {
        List<MediaItem> out = new ArrayList<>();
        for (MediaItem m : input) {
            if (durationGroupLabel(m.duration).equals(group)) out.add(m);
        }
        return out;
    }
}
