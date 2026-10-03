package com.memecio.app.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memecio.app.data.HistoryEntry
import com.memecio.app.data.MediaItem
import com.memecio.app.data.Playlist
import com.memecio.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    history: List<HistoryEntry>,
    allMedia: List<MediaItem>,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onPlayMedia: (MediaItem) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Playlists", "Watch Later", "Riwayat (History)")
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = { Text("Library & Playlists", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = { showCreateDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "New Playlist", tint = NeonCyan)
                            }
                        } else if (selectedTab == 2) {
                            IconButton(onClick = onClearHistory) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = AccentError)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
                    contentColor = NeonViolet,
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) NeonCyan else TextMuted,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Playlists
                    if (playlists.isEmpty()) {
                        EmptyState("No playlists yet", "Tap + above to create a custom playlist.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(playlists) { pl ->
                                val mediaInPl = pl.mediaIds.mapNotNull { id -> allMedia.find { it.id == id } }
                                PlaylistRowCard(
                                    playlist = pl,
                                    itemCount = mediaInPl.size,
                                    onPlayAll = {
                                        mediaInPl.firstOrNull()?.let { onPlayMedia(it) }
                                    },
                                    onDelete = { onDeletePlaylist(pl.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Watch Later
                    val watchLaterItems = allMedia.filter { it.isWatchLater }
                    if (watchLaterItems.isEmpty()) {
                        EmptyState("Watch Later is empty", "Add media to Watch Later from the options menu on any item.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(watchLaterItems) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPlayMedia(item) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PlayCircle, null, tint = NeonViolet, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                            Text(item.category, color = NeonCyan, fontSize = 12.sp)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // History
                    if (history.isEmpty()) {
                        EmptyState("No watch history", "Media you play will show up here.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(history) { entry ->
                                val media = allMedia.find { it.id == entry.mediaId }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            media?.let { onPlayMedia(it) }
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(media?.title ?: "Unknown Media", color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (entry.durationMs > 0) {
                                            val progress = (entry.positionMs.toFloat() / entry.durationMs.toFloat()).coerceIn(0f, 1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth(),
                                                color = NeonViolet,
                                                trackColor = DarkSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            var newTitle by remember { mutableStateOf("") }
            var newDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New Playlist") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Playlist Title") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("Description (Optional)") },
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
                            if (newTitle.isNotEmpty()) {
                                onCreatePlaylist(newTitle, newDesc)
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                },
                containerColor = DarkSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaylistRowCard(
    playlist: Playlist,
    itemCount: Int,
    onPlayAll: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonViolet.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = NeonViolet, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$itemCount items • ${playlist.description}", color = TextSecondary, fontSize = 12.sp)
            }
            IconButton(onClick = onPlayAll) {
                Icon(Icons.Default.PlayArrow, "Play", tint = NeonCyan)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = AccentError.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.FolderOpen, null, tint = TextMuted, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
    }
}
