package com.memecio.app

import java.util.ArrayList

object ChangelogStore {

    data class BuildEntry(
        val version: String,
        val timestamp: String,
        val changes: ArrayList<String> = ArrayList()
    ) {
        fun add(change: String) {
            changes.add(change)
        }
    }

    data class FeatureStatus(
        val name: String,
        val status: String, // "Selesai", "Sebagian", "Belum"
        val dateAdded: String
    )

    fun getBuilds(): List<BuildEntry> {
        val list = ArrayList<BuildEntry>()

        val bLatest = BuildEntry("V.1.14.65", "16 Sep 2026, 16:31:47").apply {
            add("Integrasi lengkap Kalkulator & Kode Rahasia (000 - 999)")
            add("Decoy Calculator stealth gate (kode 140399)")
            add("Multiview 4 video/streaming sekaligus (2x2 grid)")
            add("Auto Exit Manager & Background Privacy protection")
            add("Restorasi 100% fungsionalitas aplikasi")
        }
        list.add(bLatest)

        val b4 = BuildEntry("V.1.14.60", "16 Sep 2026, 08:54:19").apply {
            add("Fix: SessionManager internal transition — tidak redirect ke kalkulator saat pilih file")
            add("Batch Privacy: Redirect ke Kalkulator saat keluar / layar mati")
            add("Batch Privacy: Pengecualian PiP, rotasi, CalculatorActivity")
            add("Media Scan by Folder (khusus offline)")
        }
        list.add(b4)

        val b3 = BuildEntry("V.1.14.0", "15 Sep 2026, 21:00:00").apply {
            add("Multiview 4 video/foto sekaligus (2x2 grid)")
            add("Audio focus per slot Multiview")
            add("Multiview pick dari Beranda (semua sumber)")
            add("Multiview Play/Pause + Replay + Mute per slot")
        }
        list.add(b3)

        val b2 = BuildEntry("V.1.13.0", "14 Sep 2026, 12:00:00").apply {
            add("Video Zoom & Pan (pinch 2 jari)")
            add("Android TV Support (banner + LEANBACK launcher)")
            add("Smart Playlist grouping: Folder/Date/Size/Duration")
            add("Download offline (MP4, Drive, HLS)")
        }
        list.add(b2)

        val b1 = BuildEntry("V.1.12.0", "12 Sep 2026, 17:55:00").apply {
            add("Batch 4: Halaman Kode Rahasia (000), Crash History (111), Changelog (222)")
            add("System Info (333), Network Info (555), Permission Info (666)")
            add("Storage Analyzer (777), Download List (808), Statistik (888)")
            add("Export Panduan Proyek (999)")
            add("Reset Cache (123), Repair Database (456), Factory Reset (789)")
        }
        list.add(b1)

        val b0 = BuildEntry("V.1.0.0", "10 Sep 2026, 20:00:00").apply {
            add("Rilis awal Memec.io")
            add("Video Player, PreviewImage, Audio Player")
            add("Streaming / Drive / Server Source")
            add("Statistik, Riwayat, Crash Log")
            add("PIN Dialog, Kalkulator akses (140399)")
        }
        list.add(b0)

        return list
    }

    fun getFeatureStatus(): List<FeatureStatus> {
        val list = ArrayList<FeatureStatus>()

        // Selesai
        list.add(FeatureStatus("Fitur Kalkulator Pintu Masuk (kode 140399)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Kalkulator Scientific (Landscape & TV mode)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Kode rahasia kalkulator (000 - 999)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Daftar Kode Rahasia (000)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Crash History (111)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Changelog & Status Fitur (222)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("System Info (333)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Network Info (555)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Permission Info (666)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Storage Analyzer (777)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Daftar Download (808)", "Selesai", "13 Sep 2026"))
        list.add(FeatureStatus("Statistik Media (888)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Export Panduan Proyek (999)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Toggle Developer Mode (101)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Force Crash simulation (102)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Panduan Gestur (103)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Pilihan Mode Tampilan (104)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Reset Cache (123)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Test Media Player (444)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Repair Database (456)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Factory Reset (789)", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Video Player: gesture volume, brightness, 3x speed", "Selesai", "12 Sep 2026"))
        list.add(FeatureStatus("Multiview 4 video/foto sekaligus (2x2 grid)", "Selesai", "15 Sep 2026"))
        list.add(FeatureStatus("Auto Exit Timer (5, 15, 30, 60, 120 menit)", "Selesai", "15 Sep 2026"))
        list.add(FeatureStatus("MultiSource: Simpan Drive/Streaming/Server terpisah", "Selesai", "16 Sep 2026"))
        list.add(FeatureStatus("Daftar Riwayat link dengan PIN 808080", "Selesai", "12 Sep 2026"))

        // Sebagian / Belum
        list.add(FeatureStatus("D-pad TV remote focus navigation", "Sebagian", "14 Sep 2026"))
        list.add(FeatureStatus("Multi-audio track selector", "Belum", "16 Sep 2026"))
        list.add(FeatureStatus("Equalizer audio + preset", "Belum", "16 Sep 2026"))

        return list
    }
}
