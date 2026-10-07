package com.memecio.app.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaType
import com.memecio.app.ui.components.formatDuration
import com.memecio.app.ui.theme.CyberAccentPink
import com.memecio.app.ui.theme.CyberPrimary
import com.memecio.app.ui.theme.CyberPrimaryBright
import com.memecio.app.ui.theme.CyberSecondary
import com.memecio.app.ui.theme.CyberTertiary
import com.memecio.app.ui.theme.DarkBackground
import com.memecio.app.ui.theme.DarkSurfaceCard
import com.memecio.app.ui.theme.GlassBorder
import com.memecio.app.ui.theme.TextMuted
import com.memecio.app.ui.theme.TextPrimary
import com.memecio.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaItem: MediaItem,
    onBackClick: () -> Unit,
    onDisguiseClick: () -> Unit,
    onUpdateProgress: (Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(mediaItem.lastPositionMs) }
    var totalDuration by remember { mutableLongStateOf(mediaItem.durationMs) }
    var isBuffering by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var sleepTimerRemainingSec by remember { mutableStateOf<Int?>(null) }

    // Create ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val exoItem = ExoMediaItem.fromUri(mediaItem.uri)
            setMediaItem(exoItem)
            prepare()
            if (mediaItem.lastPositionMs > 0) {
                seekTo(mediaItem.lastPositionMs)
            }
            playWhenReady = true
        }
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > 0) totalDuration = dur
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            onUpdateProgress(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    // Periodic position updater
    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            if (exoPlayer.isPlaying) {
                currentPosition = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (dur > 0) totalDuration = dur
                onUpdateProgress(currentPosition)
            }
            delay(1000)
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4500)
            showControls = false
        }
    }

    // Sleep timer countdown
    LaunchedEffect(sleepTimerRemainingSec) {
        if (sleepTimerRemainingSec != null && sleepTimerRemainingSec!! > 0) {
            delay(1000)
            sleepTimerRemainingSec = sleepTimerRemainingSec!! - 1
            if (sleepTimerRemainingSec == 0) {
                exoPlayer.pause()
                sleepTimerRemainingSec = null
                onDisguiseClick()
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("player_screen"),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
        ) {
            // Player View
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        this.resizeMode = resizeMode
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.resizeMode = resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Indicator
            if (isBuffering) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = CyberPrimaryBright,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Controls Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mediaItem.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mediaItem.category + if (mediaItem.type == MediaType.HLS) " • LIVE STREAM" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberPrimaryBright
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Picture-in-Picture Button
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val params = PictureInPictureParams.Builder()
                                                .setAspectRatio(Rational(16, 9))
                                                .build()
                                            activity.enterPictureInPictureMode(params)
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPicture,
                                        contentDescription = "PiP",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Quick Disguise
                            IconButton(onClick = onDisguiseClick) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Kunci ke Kalkulator",
                                    tint = CyberPrimaryBright
                                )
                            }
                        }
                    }

                    // Center Playback Buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(36.dp)
                    ) {
                        // Rewind -10s
                        IconButton(
                            onClick = {
                                val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                exoPlayer.seekTo(target)
                                currentPosition = target
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "-10 Detik",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Play / Pause
                        IconButton(
                            onClick = {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CyberPrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Jeda" else "Putar",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Fast Forward +10s
                        IconButton(
                            onClick = {
                                val target = exoPlayer.currentPosition + 10000
                                exoPlayer.seekTo(target)
                                currentPosition = target
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "+10 Detik",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bottom Bar Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Sleep timer remaining chip
                        if (sleepTimerRemainingSec != null) {
                            val mins = sleepTimerRemainingSec!! / 60
                            val secs = sleepTimerRemainingSec!! % 60
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberAccentPink.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = CyberAccentPink,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Tidur otomatis: %02d:%02d".format(mins, secs),
                                    color = CyberAccentPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Timeline Slider & Timestamps
                        if (mediaItem.type != MediaType.HLS || totalDuration > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatDuration(currentPosition),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatDuration(totalDuration),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Slider(
                                value = if (totalDuration > 0) (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f,
                                onValueChange = { frac ->
                                    val newPos = (frac * totalDuration).toLong()
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimaryBright,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CyberAccentPink)
                                )
                                Text(
                                    text = "SIARAN LANGSUNG (LIVE)",
                                    color = CyberAccentPink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Bottom Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left group: Aspect Ratio & Speed
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Aspect Ratio Toggle
                                IconButton(
                                    onClick = {
                                        resizeMode = when (resizeMode) {
                                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Rasio Layar",
                                        tint = if (resizeMode != AspectRatioFrameLayout.RESIZE_MODE_FIT) CyberSecondary else Color.White
                                    )
                                }

                                // Playback Speed
                                IconButton(onClick = { showSpeedDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Kecepatan Playback",
                                        tint = if (playbackSpeed != 1.0f) CyberSecondary else Color.White
                                    )
                                }
                            }

                            // Right group: Sleep Timer, Mute & Info
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Sleep Timer
                                IconButton(onClick = { showTimerDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Pengatur Waktu Tidur",
                                        tint = if (sleepTimerRemainingSec != null) CyberAccentPink else Color.White
                                    )
                                }

                                // Mute toggle
                                IconButton(
                                    onClick = {
                                        isMuted = !isMuted
                                        exoPlayer.volume = if (isMuted) 0f else 1f
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                        contentDescription = "Suara",
                                        tint = if (isMuted) CyberAccentPink else Color.White
                                    )
                                }

                                // Stream Info
                                IconButton(onClick = { showInfoDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info Stream",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Kecepatan Pemutaran", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (spd in speeds) {
                        val isSel = spd == playbackSpeed
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyberPrimary else DarkSurfaceCard)
                                .clickable {
                                    playbackSpeed = spd
                                    exoPlayer.playbackParameters = PlaybackParameters(spd)
                                    showSpeedDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${spd}x" + if (spd == 1.0f) " (Normal)" else "",
                                color = if (isSel) Color.White else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Tutup", color = CyberPrimaryBright)
                }
            },
            containerColor = DarkBackground
        )
    }

    // Sleep Timer Dialog
    if (showTimerDialog) {
        val options = listOf(
            Pair("Matikan Timer", 0),
            Pair("15 Menit", 15 * 60),
            Pair("30 Menit", 30 * 60),
            Pair("45 Menit", 45 * 60),
            Pair("60 Menit", 60 * 60)
        )
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Pengatur Waktu Tidur", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((label, sec) in options) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceCard)
                                .clickable {
                                    sleepTimerRemainingSec = if (sec > 0) sec else null
                                    showTimerDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Text(text = label, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Batal", color = CyberPrimaryBright)
                }
            },
            containerColor = DarkBackground
        )
    }

    // Stream Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Detail Media", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Judul: ${mediaItem.title}", color = TextSecondary, fontSize = 13.sp)
                    Text("Format: ${mediaItem.type.name}", color = TextSecondary, fontSize = 13.sp)
                    Text("Kategori: ${mediaItem.category}", color = TextSecondary, fontSize = 13.sp)
                    Text("Durasi: ${formatDuration(totalDuration)}", color = TextSecondary, fontSize = 13.sp)
                    Text("Sumber URL:", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        text = mediaItem.uri,
                        color = CyberSecondary,
                        fontSize = 11.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                ) {
                    Text("Tutup")
                }
            },
            containerColor = DarkBackground
        )
    }
}
