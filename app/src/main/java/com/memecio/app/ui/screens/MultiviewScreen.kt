package com.memecio.app.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.memecio.app.data.MediaItem
import com.memecio.app.ui.components.MemecioTopBar
import com.memecio.app.ui.theme.CyberAccentPink
import com.memecio.app.ui.theme.CyberPrimary
import com.memecio.app.ui.theme.CyberPrimaryBright
import com.memecio.app.ui.theme.CyberSecondary
import com.memecio.app.ui.theme.DarkBackground
import com.memecio.app.ui.theme.DarkSurfaceCard
import com.memecio.app.ui.theme.DarkSurfaceVariant
import com.memecio.app.ui.theme.GlassBorder
import com.memecio.app.ui.theme.TextMuted
import com.memecio.app.ui.theme.TextPrimary
import com.memecio.app.ui.theme.TextSecondary

@OptIn(UnstableApi::class)
@Composable
fun MultiviewScreen(
    availableMedia: List<MediaItem>,
    onDisguiseClick: () -> Unit
) {
    val context = LocalContext.current
    val playableMedia = availableMedia.filter { !it.isVault }

    var item1 by remember { mutableStateOf(playableMedia.getOrNull(0)) }
    var item2 by remember { mutableStateOf(playableMedia.getOrNull(1) ?: playableMedia.getOrNull(0)) }

    var isPlaying1 by remember { mutableStateOf(true) }
    var isPlaying2 by remember { mutableStateOf(true) }
    var isMuted1 by remember { mutableStateOf(false) }
    var isMuted2 by remember { mutableStateOf(true) } // Stream 2 muted by default to prevent audio clash

    var selectingSlot by remember { mutableStateOf<Int?>(null) }

    // ExoPlayer 1
    val player1 = remember {
        ExoPlayer.Builder(context).build().apply {
            item1?.let { setMediaItem(ExoMediaItem.fromUri(it.uri)) }
            prepare()
            playWhenReady = true
        }
    }

    // ExoPlayer 2
    val player2 = remember {
        ExoPlayer.Builder(context).build().apply {
            item2?.let { setMediaItem(ExoMediaItem.fromUri(it.uri)) }
            prepare()
            volume = 0f
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player1.release()
            player2.release()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("multiview_screen"),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MemecioTopBar(
                title = "Multiview Ganda",
                onDisguiseClick = onDisguiseClick
            )

            // Swap streams header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Streaming Ganda Simultan",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .clickable {
                            // Swap streams
                            val temp = item1
                            item1 = item2
                            item2 = temp

                            item1?.let {
                                player1.setMediaItem(ExoMediaItem.fromUri(it.uri))
                                player1.prepare()
                            }
                            item2?.let {
                                player2.setMediaItem(ExoMediaItem.fromUri(it.uri))
                                player2.prepare()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Tukar Posisi",
                        tint = CyberPrimaryBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tukar Aliran",
                        color = CyberPrimaryBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Split View: Stream 1 (Top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = player1
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Stream 1 overlay controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectingSlot = 1 },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SLOT 1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item1?.title ?: "Pilih Stream",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (player1.isPlaying) {
                                    player1.pause()
                                    isPlaying1 = false
                                } else {
                                    player1.play()
                                    isPlaying1 = true
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying1) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isMuted1 = !isMuted1
                                player1.volume = if (isMuted1) 0f else 1f
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted1) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (isMuted1) CyberAccentPink else CyberSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Split View: Stream 2 (Bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = player2
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Stream 2 overlay controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectingSlot = 2 },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberSecondary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SLOT 2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item2?.title ?: "Pilih Stream",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (player2.isPlaying) {
                                    player2.pause()
                                    isPlaying2 = false
                                } else {
                                    player2.play()
                                    isPlaying2 = true
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying2) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isMuted2 = !isMuted2
                                player2.volume = if (isMuted2) 0f else 1f
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted2) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (isMuted2) CyberAccentPink else CyberSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal to choose stream for slot
    if (selectingSlot != null) {
        AlertDialog(
            onDismissRequest = { selectingSlot = null },
            title = { Text("Pilih Saluran untuk Slot $selectingSlot", color = TextPrimary) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(playableMedia) { media ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceCard)
                                .clickable {
                                    if (selectingSlot == 1) {
                                        item1 = media
                                        player1.setMediaItem(ExoMediaItem.fromUri(media.uri))
                                        player1.prepare()
                                        player1.play()
                                        isPlaying1 = true
                                    } else {
                                        item2 = media
                                        player2.setMediaItem(ExoMediaItem.fromUri(media.uri))
                                        player2.prepare()
                                        player2.play()
                                        isPlaying2 = true
                                    }
                                    selectingSlot = null
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = media.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectingSlot = null }) {
                    Text("Tutup", color = CyberPrimaryBright)
                }
            },
            containerColor = DarkBackground
        )
    }
}
