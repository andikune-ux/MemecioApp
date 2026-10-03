package com.memecio.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaType
import com.memecio.app.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    onAddMedia: (MediaItem) -> Unit,
    onImportM3u: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddUrlDialog by remember { mutableStateOf(false) }
    var showM3uDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    // Android 13+ Photo/Video picker
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val newItem = MediaItem(
                id = UUID.randomUUID().toString(),
                title = "Local Media (${System.currentTimeMillis() % 10000})",
                uri = uri.toString(),
                type = MediaType.VIDEO,
                category = "Local Device"
            )
            onAddMedia(newItem)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Media Sources & Streams", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Add External Media",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Option 1: Direct URL
            item {
                SourceActionCard(
                    title = "Add Streaming URL",
                    description = "Direct HLS (.m3u8), MP4, MKV, or Audio stream link",
                    icon = Icons.Default.Link,
                    iconTint = NeonViolet,
                    onClick = { showAddUrlDialog = true }
                )
            }

            // Option 2: M3U Playlist Parser
            item {
                SourceActionCard(
                    title = "Import M3U / IPTV Playlist",
                    description = "Parse channel lists, groups, and logos from M3U text",
                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    iconTint = NeonPink,
                    onClick = { showM3uDialog = true }
                )
            }

            // Option 3: Local Storage Picker
            item {
                SourceActionCard(
                    title = "Select Local Storage Media",
                    description = "Import video, photo, or audio using Android Media Picker",
                    icon = Icons.Default.FolderOpen,
                    iconTint = NeonCyan,
                    onClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "One-Click Demo & Live Streams",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Presets
            val presets = listOf(
                MediaItem(
                    id = "preset_nasa",
                    title = "NASA TV Live Public Stream",
                    uri = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                    type = MediaType.STREAM_HLS,
                    category = "Live TV",
                    thumbnailUri = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500"
                ),
                MediaItem(
                    id = "preset_lofi",
                    title = "Lo-Fi Beats 24/7 Radio",
                    uri = "https://streams.ilovemusic.de/iloveradio17.mp3",
                    type = MediaType.AUDIO,
                    category = "Music",
                    thumbnailUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
                ),
                MediaItem(
                    id = "preset_bbb",
                    title = "Big Buck Bunny Full HD",
                    uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    type = MediaType.VIDEO,
                    category = "Movies",
                    durationMs = 596000L
                )
            )

            items(presets) { preset ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(preset.category, color = NeonCyan, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onAddMedia(preset) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Add", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Add Direct URL Dialog
        if (showAddUrlDialog) {
            var inputTitle by remember { mutableStateOf("") }
            var inputUrl by remember { mutableStateOf("") }
            var inputCategory by remember { mutableStateOf("Streams") }

            AlertDialog(
                onDismissRequest = { showAddUrlDialog = false },
                title = { Text("Add Stream URL") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("Stream Title") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_url_title_field")
                        )
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("URL (HLS, MP4, MP3)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_url_field")
                        )
                        OutlinedTextField(
                            value = inputCategory,
                            onValueChange = { inputCategory = it },
                            label = { Text("Category") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputUrl.isNotEmpty()) {
                                val isHls = inputUrl.contains(".m3u8")
                                onAddMedia(
                                    MediaItem(
                                        id = UUID.randomUUID().toString(),
                                        title = if (inputTitle.isNotEmpty()) inputTitle else "Stream Channel",
                                        uri = inputUrl.trim(),
                                        type = if (isHls) MediaType.STREAM_HLS else MediaType.STREAM_MP4,
                                        category = if (inputCategory.isNotEmpty()) inputCategory else "Streams"
                                    )
                                )
                                showAddUrlDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        modifier = Modifier.testTag("submit_add_url_btn")
                    ) {
                        Text("Add Stream")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddUrlDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // M3U Playlist Dialog
        if (showM3uDialog) {
            var m3uContent by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showM3uDialog = false },
                title = { Text("Import M3U Playlist") },
                text = {
                    Column {
                        Text(
                            text = "Paste M3U content below:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = m3uContent,
                            onValueChange = { m3uContent = it },
                            placeholder = { Text("#EXTM3U\n#EXTINF:-1,Channel 1\nhttp://...") },
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (m3uContent.isNotEmpty()) {
                                onImportM3u(m3uContent)
                                showM3uDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Parse & Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showM3uDialog = false }) { Text("Cancel") }
                },
                containerColor = DarkSurfaceVariant
            )
        }
    }
}

@Composable
private fun SourceActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
