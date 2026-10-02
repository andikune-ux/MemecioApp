package com.memecio.app

object RoadmapStore {

    class RoadmapItem(
        @JvmField var name: String,
        @JvmField var note: String
    )

    @JvmStatic
    fun getRoadmap(): List<RoadmapItem> {
        val list = ArrayList<RoadmapItem>()
        list.add(RoadmapItem("Widget channel favorit", ""))
        list.add(RoadmapItem("Multi-audio track (pilih audio track film)", ""))
        list.add(RoadmapItem("Dual subtitle (2 bahasa sekaligus)", ""))
        list.add(RoadmapItem("Subtitle gesture control", ""))
        list.add(RoadmapItem("Audio equalizer + preset", ""))
        list.add(RoadmapItem("Video frame capture (screenshot resolusi asli)", ""))
        list.add(RoadmapItem("Kids lock (kunci layar anak)", ""))
        list.add(RoadmapItem("Headset control", ""))
        list.add(RoadmapItem("Mode Gelap/Terang", ""))
        list.add(RoadmapItem("Sleep timer audio", ""))
        list.add(RoadmapItem("Dynamic theme (tema ikut warna poster)", ""))
        list.add(RoadmapItem("Widget audio control di home screen", ""))
        list.add(RoadmapItem("Update aplikasi otomatis", ""))
        return list
    }
}
