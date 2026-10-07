package com.memecio.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaType
import com.memecio.app.ui.components.MediaItemCard
import com.memecio.app.ui.components.MemecioTopBar
import com.memecio.app.ui.theme.CyberAccentPink
import com.memecio.app.ui.theme.CyberPrimary
import com.memecio.app.ui.theme.CyberPrimaryBright
import com.memecio.app.ui.theme.CyberSecondary
import com.memecio.app.ui.theme.CyberTertiary
import com.memecio.app.ui.theme.DarkBackground
import com.memecio.app.ui.theme.DarkSurfaceCard
import com.memecio.app.ui.theme.DarkSurfaceVariant
import com.memecio.app.ui.theme.GlassBorder
import com.memecio.app.ui.theme.TextMuted
import com.memecio.app.ui.theme.TextPrimary
import com.memecio.app.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun SourcesScreen(
    mediaList: List<MediaItem>,
    onAddMedia: (MediaItem) -> Unit,
    onImportM3u: (String) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleVault: (String) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onDisguiseClick: () -> Unit
) {
    var showAddUrlDialog by remember { mutableStateOf(false) }
    var showM3uDialog by remember { mutableStateOf(false) }

    // Picker for local device video / audio files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val title = it.lastPathSegment ?: "Media Perangkat"
            val item = MediaItem(
                id = "local_" + UUID.randomUUID().toString().take(8),
                title = title.substringAfterLast('/'),
                uri = it.toString(),
                type = if (it.toString().contains("audio")) MediaType.AUDIO else MediaType.VIDEO,
                category = "Penyimpanan Lokal",
                description = "File media dari penyimpanan internal perangkat"
            )
            onAddMedia(item)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sources_screen"),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MemecioTopBar(
                title = "Sumber Streaming",
                onDisguiseClick = onDisguiseClick
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick actions cards
                item {
                    Text(
                        text = "Tambah Sumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SourceActionCard(
                            title = "URL Stream",
                            subtitle = "M3U8 / MP4 / HLS",
                            icon = Icons.Default.AddLink,
                            tint = CyberSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { showAddUrlDialog = true }
                        )

                        SourceActionCard(
                            title = "Daftar M3U",
                            subtitle = "Saluran IPTV Lengkap",
                            icon = Icons.Default.ListAlt,
                            tint = CyberPrimaryBright,
                            modifier = Modifier.weight(1f),
                            onClick = { showM3uDialog = true }
                        )

                        SourceActionCard(
                            title = "Penyimpanan",
                            subtitle = "Berkas Lokal",
                            icon = Icons.Default.FolderOpen,
                            tint = CyberTertiary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                filePickerLauncher.launch(arrayOf("video/*", "audio/*"))
                            }
                        )
                    }
                }

                // Curated IPTV & Stream List
                item {
                    Text(
                        text = "Saluran & Sumber Aktif (${mediaList.count { !it.isVault }})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                items(mediaList.filter { !it.isVault }, key = { it.id }) { item ->
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

    // Add Direct Stream Dialog
    if (showAddUrlDialog) {
        var inputTitle by remember { mutableStateOf("") }
        var inputUrl by remember { mutableStateOf("") }
        var inputCategory by remember { mutableStateOf("Streaming") }

        AlertDialog(
            onDismissRequest = { showAddUrlDialog = false },
            title = { Text("Tambah Aliran Stream", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Nama Saluran / Judul") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberPrimaryBright,
                            unfocusedBorderColor = GlassBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("URL Stream (.m3u8, .mp4, dll)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberPrimaryBright,
                            unfocusedBorderColor = GlassBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("Kategori") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberPrimaryBright,
                            unfocusedBorderColor = GlassBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            val newItem = MediaItem(
                                id = "custom_" + UUID.randomUUID().toString().take(8),
                                title = if (inputTitle.isNotBlank()) inputTitle else "Aliran Kustom",
                                uri = inputUrl.trim(),
                                type = if (inputUrl.contains(".m3u8")) MediaType.HLS else MediaType.VIDEO,
                                category = if (inputCategory.isNotBlank()) inputCategory else "Streaming",
                                description = "Aliran langsung ditambahkan manual"
                            )
                            onAddMedia(newItem)
                            showAddUrlDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUrlDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = DarkBackground
        )
    }

    // Import M3U Dialog
    if (showM3uDialog) {
        var m3uContent by remember {
            mutableStateOf(
                """
#EXTM3U
#EXTINF:-1 tvg-logo="https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400" group-title="Cinema",Blender Tears of Steel 4K
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4
#EXTINF:-1 tvg-logo="https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400" group-title="Animasi",Big Buck Bunny HLS Multi-bitrate
https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8
                """.trimIndent()
            )
        }

        AlertDialog(
            onDismissRequest = { showM3uDialog = false },
            title = { Text("Impor Playlist M3U", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Tempel teks playlist format #EXTM3U di bawah ini:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = m3uContent,
                        onValueChange = { m3uContent = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberPrimaryBright,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (m3uContent.isNotBlank()) {
                            onImportM3u(m3uContent)
                            showM3uDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                ) {
                    Text("Impor Saluran")
                }
            },
            dismissButton = {
                TextButton(onClick = { showM3uDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = DarkBackground
        )
    }
}

@Composable
fun SourceActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = DarkSurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
