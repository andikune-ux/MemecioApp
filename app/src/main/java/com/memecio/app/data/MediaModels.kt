package com.memecio.app.data

enum class MediaType {
    VIDEO,
    HLS,
    AUDIO,
    IMAGE
}

data class MediaItem(
    val id: String,
    val title: String,
    val uri: String,
    val type: MediaType = MediaType.VIDEO,
    val description: String = "",
    val durationMs: Long = 0L,
    val thumbnailUri: String? = null,
    val category: String = "Umum",
    val isVault: Boolean = false,
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val itemIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class SecretCode(
    val code: String,
    val title: String,
    val description: String,
    val category: String
)

data class AppDiagnostic(
    val deviceModel: String,
    val androidVersion: String,
    val appVersion: String,
    val totalMemoryMb: Long,
    val availableMemoryMb: Long,
    val freeStorageMb: Long,
    val networkStatus: String,
    val totalWatchTimeMinutes: Long,
    val activeMediaCount: Int,
    val vaultItemCount: Int
)
