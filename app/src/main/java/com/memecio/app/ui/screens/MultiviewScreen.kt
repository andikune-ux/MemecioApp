package com.memecio.app.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.memecio.app.data.MediaItem
import com.memecio.app.ui.theme.*

@OptIn(UnstableApi::class)
@Composable
fun MultiviewScreen(
    mediaList: List<MediaItem>,
    onBack: () -> Unit,
    onDisguise: () -> Unit
) {
    val context = LocalContext.current

    var selectedItem1 by remember {
        mutableStateOf(mediaList.getOrNull(0) ?: MediaItem(id = "none1", title = "Select Stream 1", uri = ""))
    }
    var selectedItem2 by remember {
        mutableStateOf(mediaList.getOrNull(1) ?: mediaList.getOrNull(0) ?: MediaItem(id = "none2", title = "Select Stream 2", uri = ""))
    }

    var isMuted1 by remember { mutableStateOf(false) }
    var isMuted2 by remember { mutableStateOf(true) }

    val player1 = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    val player2 = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            volume = 0f
        }
    }

    LaunchedEffect(selectedItem1.uri) {
        if (selectedItem1.uri.isNotEmpty()) {
            player1.setMediaItem(ExoMediaItem.fromUri(Uri.parse(selectedItem1.uri)))
            player1.prepare()
            player1.play()
        }
    }

    LaunchedEffect(selectedItem2.uri) {
        if (selectedItem2.uri.isNotEmpty()) {
            player2.setMediaItem(ExoMediaItem.fromUri(Uri.parse(selectedItem2.uri)))
            player2.prepare()
            player2.play()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player1.release()
            player2.release()
        }
    }

    var showPickerForSlot by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "Multiview Dual Player",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Swap button
                    IconButton(onClick = {
                        val temp = selectedItem1
                        selectedItem1 = selectedItem2
                        selectedItem2 = temp
                    }) {
                        Icon(Icons.Default.SwapVert, "Swap", tint = NeonCyan)
                    }

                    // Disguise to Calculator
                    IconButton(
                        onClick = onDisguise,
                        modifier = Modifier.testTag("multiview_disguise_btn")
                    ) {
                        Icon(Icons.Default.Calculate, "Disguise", tint = NeonViolet)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Player 1 Pane
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF100E1C))
            ) {
                if (selectedItem1.uri.isNotEmpty()) {
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
                }

                // Controls overlay 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedItem1.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showPickerForSlot = 1 }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.VideoLibrary, "Change video", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        isMuted1 = !isMuted1
                        player1.volume = if (isMuted1) 0f else 1f
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isMuted1) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            "Audio",
                            tint = if (!isMuted1) NeonPink else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Divider(color = NeonViolet.copy(alpha = 0.6f), thickness = 2.dp)

            // Player 2 Pane
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF100E1C))
            ) {
                if (selectedItem2.uri.isNotEmpty()) {
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
                }

                // Controls overlay 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedItem2.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showPickerForSlot = 2 }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.VideoLibrary, "Change video", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        isMuted2 = !isMuted2
                        player2.volume = if (isMuted2) 0f else 1f
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isMuted2) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            "Audio",
                            tint = if (!isMuted2) NeonPink else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Slot selection dialog
        if (showPickerForSlot != null) {
            AlertDialog(
                onDismissRequest = { showPickerForSlot = null },
                title = { Text("Choose Media for Player $showPickerForSlot") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        mediaList.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (showPickerForSlot == 1) selectedItem1 = m
                                        else selectedItem2 = m
                                        showPickerForSlot = null
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Text(m.title, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPickerForSlot = null }) { Text("Cancel") }
                },
                containerColor = DarkSurfaceVariant
            )
        }
    }
}
