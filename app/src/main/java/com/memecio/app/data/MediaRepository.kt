package com.memecio.app.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MediaRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("memecio_prefs", Context.MODE_PRIVATE)

    private val _mediaList = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaList: StateFlow<List<MediaItem>> = _mediaList.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _calculatorPin = MutableStateFlow(prefs.getString("calc_pin", "140399") ?: "140399")
    val calculatorPin: StateFlow<String> = _calculatorPin.asStateFlow()

    private val _devMode = MutableStateFlow(prefs.getBoolean("dev_mode", false))
    val devMode: StateFlow<Boolean> = _devMode.asStateFlow()

    private val _totalWatchMinutes = MutableStateFlow(prefs.getLong("total_watch_minutes", 42L))
    val totalWatchMinutes: StateFlow<Long> = _totalWatchMinutes.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val savedMediaJson = prefs.getString("saved_media", null)
        val items = mutableListOf<MediaItem>()

        if (savedMediaJson.isNullOrEmpty()) {
            items.addAll(getDefaultMediaItems())
        } else {
            try {
                val array = JSONArray(savedMediaJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(
                        MediaItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            uri = obj.getString("uri"),
                            type = MediaType.valueOf(obj.optString("type", MediaType.VIDEO.name)),
                            description = obj.optString("description", ""),
                            durationMs = obj.optLong("durationMs", 0L),
                            thumbnailUri = obj.optString("thumbnailUri", null).takeIf { !it.isNullOrBlank() },
                            category = obj.optString("category", "Umum"),
                            isVault = obj.optBoolean("isVault", false),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            isWatchLater = obj.optBoolean("isWatchLater", false),
                            lastPositionMs = obj.optLong("lastPositionMs", 0L),
                            lastPlayedTimestamp = obj.optLong("lastPlayedTimestamp", 0L),
                            dateAdded = obj.optLong("dateAdded", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                items.addAll(getDefaultMediaItems())
            }
        }
        _mediaList.value = items

        val savedPlaylistsJson = prefs.getString("saved_playlists", null)
        val pls = mutableListOf<Playlist>()
        if (savedPlaylistsJson.isNullOrEmpty()) {
            pls.add(
                Playlist(
                    id = "pl_default_1",
                    name = "Pilihan Terbaik",
                    description = "Kompilasi stream dan video pilihan berkualitas tinggi",
                    itemIds = items.filter { !it.isVault }.take(3).map { it.id }
                )
            )
            pls.add(
                Playlist(
                    id = "pl_default_2",
                    name = "Musik & Suasana",
                    description = "Alunan musik lofi dan relaksasi",
                    itemIds = items.filter { it.type == MediaType.AUDIO }.map { it.id }
                )
            )
        } else {
            try {
                val array = JSONArray(savedPlaylistsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val idArray = obj.getJSONArray("itemIds")
                    val idList = mutableListOf<String>()
                    for (j in 0 until idArray.length()) {
                        idList.add(idArray.getString(j))
                    }
                    pls.add(
                        Playlist(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            itemIds = idList,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
        _playlists.value = pls
    }

    private fun persistMedia() {
        val array = JSONArray()
        for (item in _mediaList.value) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("uri", item.uri)
                put("type", item.type.name)
                put("description", item.description)
                put("durationMs", item.durationMs)
                put("thumbnailUri", item.thumbnailUri ?: "")
                put("category", item.category)
                put("isVault", item.isVault)
                put("isFavorite", item.isFavorite)
                put("isWatchLater", item.isWatchLater)
                put("lastPositionMs", item.lastPositionMs)
                put("lastPlayedTimestamp", item.lastPlayedTimestamp)
                put("dateAdded", item.dateAdded)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_media", array.toString()).apply()
    }

    private fun persistPlaylists() {
        val array = JSONArray()
        for (pl in _playlists.value) {
            val obj = JSONObject().apply {
                put("id", pl.id)
                put("name", pl.name)
                put("description", pl.description)
                val idArr = JSONArray()
                pl.itemIds.forEach { idArr.put(it) }
                put("itemIds", idArr)
                put("createdAt", pl.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_playlists", array.toString()).apply()
    }

    fun addMediaItem(item: MediaItem) {
        _mediaList.value = listOf(item) + _mediaList.value
        persistMedia()
    }

    fun removeMediaItem(id: String) {
        _mediaList.value = _mediaList.value.filter { it.id != id }
        persistMedia()
    }

    fun toggleFavorite(id: String) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        persistMedia()
    }

    fun toggleWatchLater(id: String) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(isWatchLater = !it.isWatchLater) else it
        }
        persistMedia()
    }

    fun toggleVault(id: String) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) it.copy(isVault = !it.isVault) else it
        }
        persistMedia()
    }

    fun updateProgress(id: String, positionMs: Long) {
        _mediaList.value = _mediaList.value.map {
            if (it.id == id) {
                it.copy(
                    lastPositionMs = positionMs,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            } else it
        }
        persistMedia()
    }

    fun incrementWatchTime(minutes: Long) {
        val updated = _totalWatchMinutes.value + minutes
        _totalWatchMinutes.value = updated
        prefs.edit().putLong("total_watch_minutes", updated).apply()
    }

    fun setPin(newPin: String) {
        _calculatorPin.value = newPin
        prefs.edit().putString("calc_pin", newPin).apply()
    }

    fun toggleDevMode(): Boolean {
        val newValue = !_devMode.value
        _devMode.value = newValue
        prefs.edit().putBoolean("dev_mode", newValue).apply()
        return newValue
    }

    fun createPlaylist(name: String, description: String = "", initialItemIds: List<String> = emptyList()) {
        val newPl = Playlist(
            id = "pl_" + UUID.randomUUID().toString().take(8),
            name = name,
            description = description,
            itemIds = initialItemIds
        )
        _playlists.value = _playlists.value + newPl
        persistPlaylists()
    }

    fun addItemToPlaylist(playlistId: String, mediaId: String) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == playlistId && !pl.itemIds.contains(mediaId)) {
                pl.copy(itemIds = pl.itemIds + mediaId)
            } else pl
        }
        persistPlaylists()
    }

    fun removeItemFromPlaylist(playlistId: String, mediaId: String) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(itemIds = pl.itemIds.filter { it != mediaId })
            } else pl
        }
        persistPlaylists()
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filter { it.id != playlistId }
        persistPlaylists()
    }

    fun clearHistory() {
        _mediaList.value = _mediaList.value.map {
            it.copy(lastPositionMs = 0L, lastPlayedTimestamp = 0L)
        }
        persistMedia()
    }

    fun factoryReset() {
        prefs.edit().clear().apply()
        _calculatorPin.value = "140399"
        _devMode.value = false
        _totalWatchMinutes.value = 0L
        _mediaList.value = getDefaultMediaItems()
        loadData()
    }

    fun parseM3u(m3uContent: String): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val lines = m3uContent.lines()
        var currentTitle = ""
        var currentGroup = "IPTV"
        var currentLogo: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Parse attributes
                val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(trimmed)
                currentLogo = logoMatch?.groupValues?.get(1)

                val groupMatch = Regex("""group-title="([^"]+)"""").find(trimmed)
                currentGroup = groupMatch?.groupValues?.get(1) ?: "IPTV"

                val commaIndex = trimmed.lastIndexOf(',')
                currentTitle = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    "Saluran IPTV"
                }
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("rtmp://")) {
                    val title = if (currentTitle.isNotEmpty()) currentTitle else "Stream ${items.size + 1}"
                    items.add(
                        MediaItem(
                            id = "m3u_" + UUID.randomUUID().toString().take(8),
                            title = title,
                            uri = trimmed,
                            type = if (trimmed.contains(".m3u8")) MediaType.HLS else MediaType.VIDEO,
                            category = currentGroup,
                            thumbnailUri = currentLogo,
                            description = "Saluran dari M3U ($currentGroup)"
                        )
                    )
                    currentTitle = ""
                    currentLogo = null
                }
            }
        }
        return items
    }

    fun getDiagnostics(): AppDiagnostic {
        val rt = Runtime.getRuntime()
        val totalMem = rt.totalMemory() / (1024 * 1024)
        val freeMem = rt.freeMemory() / (1024 * 1024)

        var freeStorage = 500L
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            freeStorage = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        } catch (_: Exception) {}

        return AppDiagnostic(
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = "Memecio v1.03.0 (Build 103)",
            totalMemoryMb = totalMem,
            availableMemoryMb = freeMem,
            freeStorageMb = freeStorage,
            networkStatus = "Terhubung (Wi-Fi / Seluler Aktif)",
            totalWatchTimeMinutes = _totalWatchMinutes.value,
            activeMediaCount = _mediaList.value.count { !it.isVault },
            vaultItemCount = _mediaList.value.count { it.isVault }
        )
    }

    fun getSecretCodes(): List<SecretCode> = listOf(
        SecretCode("000", "Daftar Kode Rahasia", "Menampilkan panduan semua kode keypad kalkulator", "Bantuan"),
        SecretCode("111", "Diagnostik & Log Sesi", "Pemeriksaan kesehatan sistem dan status aplikasi", "Sistem"),
        SecretCode("222", "Catatan Rilis & Roadmap", "Fitur terbaru dan perkembangan versi Memecio", "Info"),
        SecretCode("333", "Spesifikasi Perangkat", "Info detail hardware, memori, dan OS Android", "Sistem"),
        SecretCode("444", "Uji Coba Media Player", "Pemutar cepat untuk memverifikasi stream video", "Media"),
        SecretCode("555", "Status Jaringan & Koneksi", "Pengujian latensi dan konektivitas live stream", "Jaringan"),
        SecretCode("666", "Inspeksi Izin Aplikasi", "Detail perizinan media, jaringan, dan background", "Privasi"),
        SecretCode("777", "Analisis Ruang Penyimpanan", "Pembersihan cache dan visualisasi penggunaan storage", "Penyimpanan"),
        SecretCode("888", "Statistik Jam Tonton", "Data analitik durasi playback dan riwayat", "Statistik"),
        SecretCode("999", "Ekspor & Cadangan Data", "Cadangkan konfigurasi dan playlist dalam format JSON", "Alat"),
        SecretCode("101", "Mode Pengembang", "Aktifkan kontrol debug tingkat lanjut", "Pengembang"),
        SecretCode("103", "Panduan Gestur Pemutar", "Tutorial kontrol usap kecerahan, volume, & seek", "Bantuan"),
        SecretCode("104", "Pengaturan Tampilan Grid", "Beralih tata letak thumbnail rapat atau longgar", "Tampilan"),
        SecretCode("123", "Reset Cache Gambar", "Kosongkan thumbnail cache Coil untuk menyegarkan UI", "Pembersih"),
        SecretCode("456", "Sinkronisasi Sumber", "Muat ulang daftar saluran streaming default", "Sinkronisasi"),
        SecretCode("789", "Reset Pabrik (Kembali Awal)", "Hapus semua data kustom dan kembalikan ke default", "Pengaturan"),
        SecretCode("808", "Daftar Unduhan Offline", "Manajemen file media tersimpan di perangkat", "Media")
    )

    private fun getDefaultMediaItems(): List<MediaItem> = listOf(
        MediaItem(
            id = "m1",
            title = "Big Buck Bunny (HLS Live Stream)",
            uri = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            type = MediaType.HLS,
            description = "Film animasi open source legendaris Blender Foundation resolusi adaptif",
            durationMs = 596000L,
            thumbnailUri = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
            category = "Film & Animasi",
            isFavorite = true
        ),
        MediaItem(
            id = "m2",
            title = "Tears of Steel (Sci-Fi 4K Sample)",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            type = MediaType.VIDEO,
            description = "Film fiksi ilmiah visual efek berkecepatan tinggi",
            durationMs = 734000L,
            thumbnailUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            category = "Sci-Fi",
            isFavorite = true
        ),
        MediaItem(
            id = "m3",
            title = "Sintel (Fantasy Animation)",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            type = MediaType.VIDEO,
            description = "Petualangan gadis pengembara mencari naga peliharaannya",
            durationMs = 888000L,
            thumbnailUri = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            category = "Film & Animasi"
        ),
        MediaItem(
            id = "m4",
            title = "Elephant's Dream",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            type = MediaType.VIDEO,
            description = "Animasi surrealis mesin mekanik raksasa penuh misteri",
            durationMs = 653000L,
            thumbnailUri = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            category = "Sci-Fi"
        ),
        MediaItem(
            id = "m5",
            title = "NASA TV Public HD Stream",
            uri = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
            type = MediaType.HLS,
            description = "Siaran langsung eksplorasi luar angkasa, ISS, dan sains astronomi",
            durationMs = 0L,
            thumbnailUri = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            category = "Live Streaming"
        ),
        MediaItem(
            id = "m6",
            title = "Chill Lofi Beats Radio",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            type = MediaType.AUDIO,
            description = "Alunan musik lofi santai untuk menemani waktu fokus dan belajar",
            durationMs = 150000L,
            thumbnailUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            category = "Musik & Audio",
            isFavorite = true
        ),
        MediaItem(
            id = "m7",
            title = "Synthwave Night Ride (High-Fi)",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            type = MediaType.AUDIO,
            description = "Nuansa retro 80-an elektrik dengan synth mendalam",
            durationMs = 180000L,
            thumbnailUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            category = "Musik & Audio"
        ),
        MediaItem(
            id = "m8",
            title = "Private Vault Document / Video",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            type = MediaType.VIDEO,
            description = "Media rahasia tersimpan aman di dalam brankas terlindung PIN",
            durationMs = 90000L,
            thumbnailUri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
            category = "Rahasia",
            isVault = true
        )
    )
}
