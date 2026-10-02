package com.memecio.app;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * ROADMAP STORE — RENCANA FITUR SELANJUTNYA
 * ============================================================
 *
 * Auto-pull oleh ProjectExportHelper Section 9.
 * Setiap fitur baru yang belum dikerjakan -> tambah di getRoadmap().
 * Setiap fitur selesai -> HAPUS dari sini, tambah di
 * ChangelogStore.getFeatureStatus() dengan status "Selesai".
 */
public class RoadmapStore {

    public static class RoadmapItem {
        public String name;
        public String note;

        public RoadmapItem(String name, String note) {
            this.name = name;
            this.note = note;
        }
    }

    public static List<RoadmapItem> getRoadmap() {
        List<RoadmapItem> list = new ArrayList<>();
        list.add(new RoadmapItem("Widget channel favorit", ""));
        list.add(new RoadmapItem("Multi-audio track (pilih audio track film)", ""));
        list.add(new RoadmapItem("Dual subtitle (2 bahasa sekaligus)", ""));
        list.add(new RoadmapItem("Subtitle gesture control", ""));
        list.add(new RoadmapItem("Audio equalizer + preset", ""));
        list.add(new RoadmapItem("Video frame capture (screenshot resolusi asli)", ""));
        list.add(new RoadmapItem("Kids lock (kunci layar anak)", ""));
        list.add(new RoadmapItem("Headset control", ""));
        list.add(new RoadmapItem("Mode Gelap/Terang", ""));
        list.add(new RoadmapItem("Sleep timer audio", ""));
        list.add(new RoadmapItem("Dynamic theme (tema ikut warna poster)", ""));
        list.add(new RoadmapItem("Widget audio control di home screen", ""));
        list.add(new RoadmapItem("Update aplikasi otomatis", ""));
        return list;
    }
}
