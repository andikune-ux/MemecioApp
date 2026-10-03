package com.memecio.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaType
import com.memecio.app.ui.components.MediaThumbnailCard
import com.memecio.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mediaList: List<MediaItem>,
    gridColumns: Int,
    onMediaClick: (MediaItem) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onWatchLaterToggle: (String) -> Unit,
    onHideToggle: (String) -> Unit,
    onAddToPlaylist: (MediaItem) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onDisguise: () -> Unit,
    onOpenMultiview: () -> Unit,
    onOpenSources: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Newest") }
    var showSortMenu by remember { mutableStateOf(false) }

    // Categories extracted from media
    val categories = remember(mediaList) {
        listOf("All") + mediaList.map { it.category }.distinct().filter { it.isNotEmpty() }
    }

    // Filter media items (exclude hidden items - they belong in Vault!)
    val filteredMedia = remember(mediaList, searchQuery, selectedFilter, selectedCategory, sortBy) {
        var items = mediaList.filter { !it.isHidden }

        // Filter by Type/List
        items = when (selectedFilter) {
            "Videos" -> items.filter { it.type == MediaType.VIDEO }
            "Live Streams" -> items.filter { it.type == MediaType.STREAM_HLS || it.type == MediaType.STREAM_MP4 }
            "Audio" -> items.filter { it.type == MediaType.AUDIO }
            "Images" -> items.filter { it.type == MediaType.IMAGE }
            "Favorites" -> items.filter { it.isFavorite }
            "Watch Later" -> items.filter { it.isWatchLater }
            else -> items
        }

        // Category filter
        if (selectedCategory != "All") {
            items = items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        // Search query
        if (searchQuery.isNotEmpty()) {
            items = items.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        // Sorting
        when (sortBy) {
            "Newest" -> items.sortedByDescending { it.addedDate }
            "Oldest" -> items.sortedBy { it.addedDate }
            "Title A-Z" -> items.sortedBy { it.title.lowercase() }
            "Title Z-A" -> items.sortedByDescending { it.title.lowercase() }
            "Views" -> items.sortedByDescending { it.viewCount }
            else -> items
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .statusBarsPadding()
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Memec.io",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Multiview dual player
                        IconButton(
                            onClick = onOpenMultiview,
                            modifier = Modifier.testTag("nav_multiview_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerticalSplit,
                                contentDescription = "Dual View",
                                tint = NeonCyan
                            )
                        }

                        // Add / Manage Sources
                        IconButton(
                            onClick = onOpenSources,
                            modifier = Modifier.testTag("nav_sources_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLink,
                                contentDescription = "Sources",
                                tint = NeonViolet
                            )
                        }

                        // Discreet Calculator Disguise Button
                        FilledTonalIconButton(
                            onClick = onDisguise,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFF2B2244)
                            ),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("nav_disguise_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Disguise as Calculator",
                                tint = NeonPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Search Bar Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search media, streams, tags...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("search_field")
                )

                // Filter Types Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Videos", "Live Streams", "Audio", "Images", "Favorites", "Watch Later")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = NeonViolet,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Sub-category and Sort Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category selector row
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) NeonCyan else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Sort menu
                    Box {
                        TextButton(
                            onClick = { showSortMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Sort, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(sortBy, color = TextSecondary, fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            listOf("Newest", "Oldest", "Title A-Z", "Title Z-A", "Views").forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text(sort) },
                                    onClick = {
                                        sortBy = sort
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (filteredMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No media matches '$searchQuery'" else "No media found in this filter",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenSources,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Media or Streams")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns.coerceIn(1, 4)),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("media_grid"),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredMedia, key = { it.id }) { item ->
                    MediaThumbnailCard(
                        item = item,
                        onClick = { onMediaClick(item) },
                        onFavoriteToggle = { onFavoriteToggle(item.id) },
                        onWatchLaterToggle = { onWatchLaterToggle(item.id) },
                        onHideToggle = { onHideToggle(item.id) },
                        onAddToPlaylist = { onAddToPlaylist(item) },
                        onDelete = { onDeleteMedia(item.id) }
                    )
                }
            }
        }
    }
}
