package com.memecio.app

object SmartPlaylistHelper {

    const val MODE_RESET = ""
    const val MODE_KATEGORI = "kategori"
    const val MODE_FOLDER = "folder"
    const val MODE_DATE = "date"
    const val MODE_SIZE = "size"
    const val MODE_DURATION = "duration"

    // ----- FOLDER -----
    @JvmStatic
    fun filterByFolder(input: List<MediaItem>, folder: String): List<MediaItem> {
        val out = mutableListOf<MediaItem>()
        for (m in input) {
            val f = m.folderPath ?: "(tanpa folder)"
            if (f == folder) out.add(m)
        }
        return out
    }

    @JvmStatic
    fun getFolders(input: List<MediaItem>): List<String> {
        val list = mutableListOf<String>()
        for (m in input) {
            val f = m.folderPath ?: "(tanpa folder)"
            if (!list.contains(f)) list.add(f)
        }
        return list
    }

    @JvmStatic
    fun countInFolder(input: List<MediaItem>, folder: String): Int {
        var c = 0
        for (m in input) {
            val f = m.folderPath ?: "(tanpa folder)"
            if (f == folder) c++
        }
        return c
    }

    // ----- DATE -----
    @JvmStatic
    fun dateGroupLabel(dateAddedSec: Long): String {
        if (dateAddedSec <= 0) return "Tidak diketahui"
        val ms = dateAddedSec * 1000L
        val now = System.currentTimeMillis()
        val diff = now - ms
        val day = 24L * 60 * 60 * 1000
        if (diff < day) return "Hari ini"
        if (diff < 7 * day) return "Minggu ini"
        if (diff < 30 * day) return "Bulan ini"
        if (diff < 365 * day) return "Tahun ini"
        return "Lebih lama"
    }

    @JvmStatic
    fun getDateGroups(): List<String> {
        return listOf(
            "Hari ini",
            "Minggu ini",
            "Bulan ini",
            "Tahun ini",
            "Lebih lama",
            "Tidak diketahui"
        )
    }

    @JvmStatic
    fun filterByDate(input: List<MediaItem>, group: String): List<MediaItem> {
        val out = mutableListOf<MediaItem>()
        for (m in input) {
            if (dateGroupLabel(m.dateAdded) == group) out.add(m)
        }
        return out
    }

    // ----- SIZE -----
    @JvmStatic
    fun sizeGroupLabel(bytes: Long): String {
        val mb = bytes / (1024L * 1024L)
        if (mb < 100) return "Kecil (<100 MB)"
        if (mb < 1024) return "Sedang (100 MB - 1 GB)"
        return "Besar (>1 GB)"
    }

    @JvmStatic
    fun getSizeGroups(): List<String> {
        return listOf(
            "Kecil (<100 MB)",
            "Sedang (100 MB - 1 GB)",
            "Besar (>1 GB)"
        )
    }

    @JvmStatic
    fun filterBySize(input: List<MediaItem>, group: String): List<MediaItem> {
        val out = mutableListOf<MediaItem>()
        for (m in input) {
            if (sizeGroupLabel(m.size) == group) out.add(m)
        }
        return out
    }

    // ----- DURATION -----
    @JvmStatic
    fun durationGroupLabel(ms: Long): String {
        val min = ms / 60000L
        if (min < 5) return "Pendek (<5 menit)"
        if (min < 30) return "Sedang (5-30 menit)"
        return "Panjang (>30 menit)"
    }

    @JvmStatic
    fun getDurationGroups(): List<String> {
        return listOf(
            "Pendek (<5 menit)",
            "Sedang (5-30 menit)",
            "Panjang (>30 menit)"
        )
    }

    @JvmStatic
    fun filterByDuration(input: List<MediaItem>, group: String): List<MediaItem> {
        val out = mutableListOf<MediaItem>()
        for (m in input) {
            if (durationGroupLabel(m.duration) == group) out.add(m)
        }
        return out
    }

    // ----- DATE RANGE -----
    @JvmStatic
    fun filterByDateRange(input: List<MediaItem>, daysBack: Int): List<MediaItem> {
        val out = mutableListOf<MediaItem>()
        val now = System.currentTimeMillis()
        val cutoff = now - (daysBack * 24L * 60 * 60 * 1000)
        for (m in input) {
            val ms = m.dateAdded * 1000L
            if (ms >= cutoff) out.add(m)
        }
        return out
    }

    // ----- DURATION RANGE -----
    @JvmStatic
    fun filterByDurationRange(input: List<MediaItem>, minMs: Long, maxMs: Long): List<MediaItem> {
        val out = mutableListOf<MediaItem>()
        for (m in input) {
            if (m.duration in minMs..maxMs) out.add(m)
        }
        return out
    }
}
