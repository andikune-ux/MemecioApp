package com.memecio.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.StringReader
import java.util.UUID

class MediaRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("memecio_prefs", Context.MODE_PRIVATE)

    private val _mediaList = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaList: StateFlow<List<MediaItem>> = _mediaList.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _pinCode = MutableStateFlow(prefs.getString("pin_code", "140399") ?: "140399")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _displayMode = MutableStateFlow(
        try {
            DisplayMode.valueOf(prefs.getString("display_mode", DisplayMode.AUTO.name) ?: DisplayMode.AUTO.name)
        } catch (_: Exception) {
            DisplayMode.AUTO
        }
    )
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _gridColumns = MutableStateFlow(prefs.getInt("grid_columns", 2))
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _developerMode = MutableStateFlow(prefs.getBoolean("dev_mode", false))
    val developerMode: StateFlow<Boolean> = _developerMode.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val mediaJson = prefs.getString("media_items", null)
        if (mediaJson != null && mediaJson.isNotEmpty()) {
            _mediaList.value = parseMediaJson(mediaJson)
        } else {
            // Seed initial high quality media & live streams
            _mediaList.value = getInitialMediaSeeds()
            saveMedia()
        }

        val playlistsJson = prefs.getString("playlists", null)
        if (playlistsJson != null && playlistsJson.isNotEmpty()) {
            _playlists.value = parsePlaylistsJson(playlistsJson)
        } else {
            _playlists.value = listOf(
                Playlist(
                    id = "pl_default_1",
                    title = "Demo Highlights",
                    description = "Open source media & cinematic demos",
                    mediaIds = _mediaList.value.take(3).map { it.id }
                )
            )
            savePlaylists()
        }

        val historyJson = prefs.getString("history", null)
        if (historyJson != null) {
            _history.value = parseHistoryJson(historyJson)
        }
    }

    fun addMedia(item: MediaItem) {
        val current = _mediaList.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(0, item)
        }
        _mediaList.value = current
        saveMedia()
    }

    fun removeMedia(id: String) {
        _mediaList.value = _mediaList.value.filter { it.id != id }
        saveMedia()
    }

    fun toggleFavorite(id: String) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        saveMedia()
    }

    fun toggleWatchLater(id: String) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(isWatchLater = !it.isWatchLater) else it
        }
        saveMedia()
    }

    fun toggleHidden(id: String) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(isHidden = !it.isHidden) else it
        }
        saveMedia()
    }

    fun recordView(id: String, positionMs: Long = 0L, durationMs: Long = 0L) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(viewCount = it.viewCount + 1) else it
        }
        saveMedia()

        if (durationMs > 0L) {
            val hist = _history.value.filter { it.mediaId != id }.toMutableList()
            hist.add(0, HistoryEntry(mediaId = id, positionMs = positionMs, durationMs = durationMs))
            _history.value = hist.take(50)
            saveHistory()
        }
    }

    fun createPlaylist(title: String, description: String = "", initialMediaIds: List<String> = emptyList()): Playlist {
        val newPl = Playlist(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            mediaIds = initialMediaIds
        )
        val current = _playlists.value.toMutableList()
        current.add(newPl)
        _playlists.value = current
        savePlaylists()
        return newPl
    }

    fun addToPlaylist(playlistId: String, mediaId: String) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == playlistId && !pl.mediaIds.contains(mediaId)) {
                pl.copy(mediaIds = pl.mediaIds + mediaId)
            } else pl
        }
        savePlaylists()
    }

    fun removeFromPlaylist(playlistId: String, mediaId: String) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(mediaIds = pl.mediaIds.filter { it != mediaId })
            } else pl
        }
        savePlaylists()
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filter { it.id != playlistId }
        savePlaylists()
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistory()
    }

    fun setPinCode(newPin: String) {
        _pinCode.value = newPin
        prefs.edit().putString("pin_code", newPin).apply()
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
        prefs.edit().putString("display_mode", mode.name).apply()
    }

    fun setGridColumns(cols: Int) {
        _gridColumns.value = cols
        prefs.edit().putInt("grid_columns", cols).apply()
    }

    fun toggleDeveloperMode() {
        val next = !_developerMode.value
        _developerMode.value = next
        prefs.edit().putBoolean("dev_mode", next).apply()
    }

    fun resetCache() {
        // Clear temp preferences or cache
        prefs.edit().remove("temp_cache").apply()
    }

    fun factoryReset() {
        prefs.edit().clear().apply()
        _mediaList.value = getInitialMediaSeeds()
        _playlists.value = emptyList()
        _history.value = emptyList()
        _pinCode.value = "140399"
        _displayMode.value = DisplayMode.AUTO
        _gridColumns.value = 2
        _developerMode.value = false
        saveMedia()
    }

    fun parseM3uPlaylist(content: String, defaultCategory: String = "IPTV"): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val reader = BufferedReader(StringReader(content))
        var currentTitle = ""
        var currentLogo: String? = null
        var currentGroup = defaultCategory

        var line = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Parse attributes
                val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(trimmed)
                if (logoMatch != null) currentLogo = logoMatch.groupValues[1]

                val groupMatch = Regex("""group-title="([^"]+)"""").find(trimmed)
                if (groupMatch != null) currentGroup = groupMatch.groupValues[1]

                val commaIndex = trimmed.lastIndexOf(',')
                currentTitle = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else "Stream Channel"
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                // This is the URL
                val isHls = trimmed.contains(".m3u8") || trimmed.contains("hls")
                val item = MediaItem(
                    id = UUID.randomUUID().toString(),
                    title = if (currentTitle.isNotEmpty()) currentTitle else "Live Stream",
                    uri = trimmed,
                    type = if (isHls) MediaType.STREAM_HLS else MediaType.STREAM_MP4,
                    thumbnailUri = currentLogo,
                    category = currentGroup
                )
                items.add(item)
                currentTitle = ""
                currentLogo = null
                currentGroup = defaultCategory
            }
            line = reader.readLine()
        }
        return items
    }

    fun exportToJson(): String {
        val root = JSONObject()
        val mediaArr = JSONArray()
        _mediaList.value.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("title", m.title)
            obj.put("uri", m.uri)
            obj.put("type", m.type.name)
            obj.put("thumbnailUri", m.thumbnailUri ?: "")
            obj.put("category", m.category)
            obj.put("isFavorite", m.isFavorite)
            obj.put("isHidden", m.isHidden)
            obj.put("isWatchLater", m.isWatchLater)
            mediaArr.put(obj)
        }
        root.put("media", mediaArr)
        root.put("pin", _pinCode.value)
        return root.toString(2)
    }

    private fun saveMedia() {
        val arr = JSONArray()
        _mediaList.value.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("title", m.title)
            obj.put("uri", m.uri)
            obj.put("type", m.type.name)
            obj.put("thumbnailUri", m.thumbnailUri ?: "")
            obj.put("durationMs", m.durationMs)
            obj.put("category", m.category)
            obj.put("isFavorite", m.isFavorite)
            obj.put("isHidden", m.isHidden)
            obj.put("isWatchLater", m.isWatchLater)
            obj.put("addedDate", m.addedDate)
            obj.put("viewCount", m.viewCount)
            obj.put("sizeBytes", m.sizeBytes)
            obj.put("description", m.description)
            arr.put(obj)
        }
        prefs.edit().putString("media_items", arr.toString()).apply()
    }

    private fun savePlaylists() {
        val arr = JSONArray()
        _playlists.value.forEach { pl ->
            val obj = JSONObject()
            obj.put("id", pl.id)
            obj.put("title", pl.title)
            obj.put("description", pl.description)
            val ids = JSONArray()
            pl.mediaIds.forEach { ids.put(it) }
            obj.put("mediaIds", ids)
            obj.put("createdAt", pl.createdAt)
            arr.put(obj)
        }
        prefs.edit().putString("playlists", arr.toString()).apply()
    }

    private fun saveHistory() {
        val arr = JSONArray()
        _history.value.forEach { h ->
            val obj = JSONObject()
            obj.put("mediaId", h.mediaId)
            obj.put("positionMs", h.positionMs)
            obj.put("durationMs", h.durationMs)
            obj.put("timestamp", h.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }

    private fun parseMediaJson(jsonStr: String): List<MediaItem> {
        val list = mutableListOf<MediaItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("type", MediaType.VIDEO.name)
                val type = try { MediaType.valueOf(typeStr) } catch (_: Exception) { MediaType.VIDEO }
                list.add(
                    MediaItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        uri = obj.getString("uri"),
                        type = type,
                        thumbnailUri = obj.optString("thumbnailUri").takeIf { it.isNotEmpty() },
                        durationMs = obj.optLong("durationMs", 0L),
                        category = obj.optString("category", "General"),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isHidden = obj.optBoolean("isHidden", false),
                        isWatchLater = obj.optBoolean("isWatchLater", false),
                        addedDate = obj.optLong("addedDate", System.currentTimeMillis()),
                        viewCount = obj.optInt("viewCount", 0),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        description = obj.optString("description", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parsePlaylistsJson(jsonStr: String): List<Playlist> {
        val list = mutableListOf<Playlist>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val idsArr = obj.optJSONArray("mediaIds")
                val mediaIds = mutableListOf<String>()
                if (idsArr != null) {
                    for (j in 0 until idsArr.length()) {
                        mediaIds.add(idsArr.getString(j))
                    }
                }
                list.add(
                    Playlist(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        mediaIds = mediaIds,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseHistoryJson(jsonStr: String): List<HistoryEntry> {
        val list = mutableListOf<HistoryEntry>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    HistoryEntry(
                        mediaId = obj.getString("mediaId"),
                        positionMs = obj.optLong("positionMs", 0L),
                        durationMs = obj.optLong("durationMs", 0L),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun getInitialMediaSeeds(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "seed_1",
                title = "Big Buck Bunny (4K Ultra HD Demo)",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                type = MediaType.VIDEO,
                thumbnailUri = "https://images.unsplash.com/photo-1574063413132-355dbfd83e25?w=500",
                durationMs = 596000L,
                category = "Movies",
                isFavorite = true,
                description = "Classic open source test animation with high dynamic range audio and video."
            ),
            MediaItem(
                id = "seed_2",
                title = "NASA TV Live Stream (HLS)",
                uri = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                type = MediaType.STREAM_HLS,
                thumbnailUri = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                category = "Live TV",
                isFavorite = true,
                description = "Official NASA TV Live HLS stream with spacewalks, rocket launches, and ISS views."
            ),
            MediaItem(
                id = "seed_3",
                title = "Tears of Steel (Sci-Fi Short)",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                type = MediaType.VIDEO,
                thumbnailUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
                durationMs = 734000L,
                category = "Sci-Fi",
                description = "Dystopian VFX visual showcase set in Amsterdam."
            ),
            MediaItem(
                id = "seed_4",
                title = "Sintel (Fantasy Animation)",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                type = MediaType.VIDEO,
                thumbnailUri = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500",
                durationMs = 888000L,
                category = "Movies",
                description = "Heartwarming dragon fantasy adventure."
            ),
            MediaItem(
                id = "seed_5",
                title = "Lo-Fi Beats 24/7 (Audio Stream)",
                uri = "https://streams.ilovemusic.de/iloveradio17.mp3",
                type = MediaType.AUDIO,
                thumbnailUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                category = "Music",
                description = "Chill relaxation and study stream."
            ),
            MediaItem(
                id = "seed_6",
                title = "Cyberpunk Neon Cityscape",
                uri = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1200",
                type = MediaType.IMAGE,
                thumbnailUri = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500",
                category = "Wallpaper",
                description = "Stunning ultra HD futuristic wallpaper."
            )
        )
    }

    companion object {
        val secretCodes: List<SecretCode> = listOf(
            SecretCode("000", "Daftar Kode Rahasia", "Menampilkan daftar semua kode rahasia"),
            SecretCode("111", "Riwayat Crash Log", "Riwayat crash aplikasi & log sesi"),
            SecretCode("222", "Changelog & Status Fitur", "Daftar pembaruan versi dan status fitur terkini"),
            SecretCode("333", "System Info", "Info perangkat: Android, RAM, storage, spesifikasi layar"),
            SecretCode("444", "Test Media Player", "Buka player uji coba dengan sample video HLS / MP4"),
            SecretCode("555", "Network Info", "Status koneksi, IP address, kecepatan & tipe jaringan"),
            SecretCode("666", "Daftar Permission", "Status izin aplikasi & akses penyimpanan"),
            SecretCode("777", "Storage Analyzer", "Ukuran cache, memori, media tersimpan"),
            SecretCode("888", "Statistik Penggunaan", "Total pemutaran, favorit, durasi tonton"),
            SecretCode("999", "Export Data JSON", "Cadangkan semua daftar putar & riwayat"),
            SecretCode("123", "Reset Cache", "Bersihkan cache thumbnail & preview player"),
            SecretCode("456", "Repair Database", "Sinkronisasi & perbaikan struktur data lokal"),
            SecretCode("789", "Factory Reset", "Kembalikan aplikasi ke pengaturan awal"),
            SecretCode("101", "Toggle Developer Mode", "Aktifkan menu debug & inspeksi teknis"),
            SecretCode("103", "Panduan Gestur Player", "Pelajari gestur kontrol media player"),
            SecretCode("104", "Ganti Mode Tampilan", "Pilih tata letak HP, Tablet, atau Android TV"),
            SecretCode("808", "Daftar Unduhan", "Kelola media offline yang tersimpan")
        )
    }
}
