package com.memecio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaType
import com.memecio.app.ui.components.MediaItemCard
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

@Composable
fun HomeScreen(
    mediaList: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleVault: (String) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onDisguiseClick: () -> Unit,
    onAddStreamClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Live HLS", "Film", "Musik", "Favorit")

    // Filtered media excluding vault items
    val publicMedia = mediaList.filter { !it.isVault }
    val featuredItem = publicMedia.firstOrNull()

    val filteredList = publicMedia.filter { item ->
        val matchesCategory = when (selectedCategory) {
            "Semua" -> true
            "Live HLS" -> item.type == MediaType.HLS
            "Film" -> item.type == MediaType.VIDEO
            "Musik" -> item.type == MediaType.AUDIO
            "Favorit" -> item.isFavorite
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesSearch
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MemecioTopBar(
                title = "Beranda",
                onDisguiseClick = onDisguiseClick,
                onSearchClick = { isSearchActive = !isSearchActive }
            )

            // Search Bar toggle
            if (isSearchActive) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_field"),
                        placeholder = { Text("Cari judul atau saluran...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberPrimaryBright,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Featured Hero Banner
                if (featuredItem != null && !isSearchActive) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .clickable { onMediaClick(featuredItem) }
                        ) {
                            if (!featuredItem.thumbnailUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = featuredItem.thumbnailUri,
                                    contentDescription = featuredItem.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Gradient shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xBB090812),
                                                Color(0xF5090812)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyberAccentPink)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "UNGGULAN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = featuredItem.category,
                                        color = CyberPrimaryBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = featuredItem.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onMediaClick(featuredItem) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Putar Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = onAddStreamClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = CyberSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tambah Stream", fontSize = 12.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in categories) {
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CyberPrimary else DarkSurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) CyberPrimaryBright else GlassBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Koleksi Media (${filteredList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (filteredList.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val randomItem = filteredList.random()
                                        onMediaClick(randomItem)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Putar Acak",
                                    tint = CyberSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Acak",
                                    color = CyberSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Media list items
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Tidak ada media ditemukan",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onAddStreamClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                                ) {
                                    Text("Tambah Aliran / Video Baru")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        MediaItemCard(
                            item = item,
                            onClick = { onMediaClick(item) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onToggleVault = { onToggleVault(item.id) },
                            onDelete = { onDeleteMedia(item.id) }
                        )
                    }
                }
            }
        }
    }
}
