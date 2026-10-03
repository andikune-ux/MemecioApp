package com.memecio.app.data

enum class MediaType {
    VIDEO,
    AUDIO,
    IMAGE,
    STREAM_HLS,
    STREAM_MP4
}

data class MediaItem(
    val id: String,
    val title: String,
    val uri: String,
    val type: MediaType = MediaType.VIDEO,
    val thumbnailUri: String? = null,
    val durationMs: Long = 0L,
    val category: String = "General",
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isWatchLater: Boolean = false,
    val addedDate: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val sizeBytes: Long = 0L,
    val description: String = ""
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val mediaIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class HistoryEntry(
    val mediaId: String,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class SecretCode(
    val code: String,
    val title: String,
    val description: String,
    val isHidden: Boolean = false
)

enum class DisplayMode {
    AUTO,
    PHONE,
    TABLET,
    TV
}
