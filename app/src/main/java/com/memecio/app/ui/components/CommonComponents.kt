package com.memecio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaType
import com.memecio.app.ui.theme.*

@Composable
fun MediaThumbnailCard(
    item: MediaItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onWatchLaterToggle: () -> Unit,
    onHideToggle: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("media_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF1E1A33))
            ) {
                if (!item.thumbnailUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.thumbnailUri,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.type) {
                                MediaType.AUDIO -> Icons.Default.MusicNote
                                MediaType.IMAGE -> Icons.Default.Image
                                MediaType.STREAM_HLS, MediaType.STREAM_MP4 -> Icons.Default.LiveTv
                                else -> Icons.Default.PlayCircle
                            },
                            contentDescription = null,
                            tint = NeonViolet.copy(alpha = 0.8f),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Type Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = when (item.type) {
                            MediaType.STREAM_HLS -> "LIVE HLS"
                            MediaType.STREAM_MP4 -> "STREAM"
                            MediaType.AUDIO -> "AUDIO"
                            MediaType.IMAGE -> "IMAGE"
                            MediaType.VIDEO -> "VIDEO"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (item.type) {
                            MediaType.STREAM_HLS -> NeonPink
                            MediaType.AUDIO -> NeonCyan
                            else -> Color.White
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Duration badge if > 0
                if (item.durationMs > 0) {
                    val totalSec = item.durationMs / 1000
                    val min = totalSec / 60
                    val sec = totalSec % 60
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Favorite heart quick icon
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .testTag("fav_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) NeonPink else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                        if (item.viewCount > 0) {
                            Text(
                                text = " • ${item.viewCount} views",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Menu button
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (item.isWatchLater) "Remove from Watch Later" else "Watch Later") },
                            leadingIcon = { Icon(Icons.Default.WatchLater, null) },
                            onClick = {
                                menuExpanded = false
                                onWatchLaterToggle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Playlist") },
                            leadingIcon = { Icon(Icons.Default.PlaylistAdd, null) },
                            onClick = {
                                menuExpanded = false
                                onAddToPlaylist()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (item.isHidden) "Unhide from Vault" else "Hide to Vault") },
                            leadingIcon = { Icon(if (item.isHidden) Icons.Default.LockOpen else Icons.Default.Lock, null) },
                            onClick = {
                                menuExpanded = false
                                onHideToggle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = AccentError) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = AccentError) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
