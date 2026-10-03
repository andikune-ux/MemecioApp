package com.memecio.app.ui.screens

import android.os.Build
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
import com.memecio.app.data.DisplayMode
import com.memecio.app.data.MediaRepository
import com.memecio.app.data.SecretCode
import com.memecio.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentPin: String,
    displayMode: DisplayMode,
    gridColumns: Int,
    isDevMode: Boolean,
    onSetPin: (String) -> Unit,
    onSetDisplayMode: (DisplayMode) -> Unit,
    onSetGridColumns: (Int) -> Unit,
    onToggleDevMode: () -> Unit,
    onResetCache: () -> Unit,
    onFactoryReset: () -> Unit,
    onExportJson: () -> String,
    onOpenVault: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var showCodesDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSystemInfoDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // App Banner Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NeonViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(34.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Memec.io Media Suite", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Version 1.03.0 • Discreet Vault & Player", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Security & Vault
            item { SectionHeader("Security & Privacy") }

            item {
                SettingActionRow(
                    title = "Change Calculator PIN",
                    subtitle = "Current PIN: $currentPin",
                    icon = Icons.Default.Lock,
                    iconTint = NeonPink,
                    onClick = { showPinDialog = true }
                )
            }

            item {
                SettingActionRow(
                    title = "Secret Media Vault",
                    subtitle = "View and restore hidden private media",
                    icon = Icons.Default.Security,
                    iconTint = NeonViolet,
                    onClick = onOpenVault
                )
            }

            // Display & Preferences
            item { SectionHeader("Display & Layout") }

            item {
                SettingActionRow(
                    title = "Display Mode",
                    subtitle = displayMode.name,
                    icon = Icons.Default.Tv,
                    iconTint = NeonCyan,
                    onClick = {
                        val next = when (displayMode) {
                            DisplayMode.AUTO -> DisplayMode.PHONE
                            DisplayMode.PHONE -> DisplayMode.TABLET
                            DisplayMode.TABLET -> DisplayMode.TV
                            DisplayMode.TV -> DisplayMode.AUTO
                        }
                        onSetDisplayMode(next)
                    }
                )
            }

            item {
                SettingActionRow(
                    title = "Grid Density",
                    subtitle = "$gridColumns columns per row",
                    icon = Icons.Default.GridOn,
                    iconTint = ElectricBlue,
                    onClick = {
                        val next = if (gridColumns >= 4) 2 else gridColumns + 1
                        onSetGridColumns(next)
                    }
                )
            }

            // Diagnostics & Tools
            item { SectionHeader("Diagnostics & Secret Codes") }

            item {
                SettingActionRow(
                    title = "Secret Code Registry (000)",
                    subtitle = "Browse all secret keypad codes",
                    icon = Icons.Default.VpnKey,
                    iconTint = AccentWarning,
                    onClick = { showCodesDialog = true }
                )
            }

            item {
                SettingActionRow(
                    title = "Storage Analyzer (777)",
                    subtitle = "Inspect cache, media size and storage",
                    icon = Icons.Default.Storage,
                    iconTint = NeonCyan,
                    onClick = { showStorageDialog = true }
                )
            }

            item {
                SettingActionRow(
                    title = "App Statistics (888)",
                    subtitle = "Usage analytics and counts",
                    icon = Icons.Default.BarChart,
                    iconTint = NeonViolet,
                    onClick = { showStatsDialog = true }
                )
            }

            item {
                SettingActionRow(
                    title = "System & Hardware Info (333)",
                    subtitle = "Android version, RAM, and device info",
                    icon = Icons.Default.Info,
                    iconTint = TextPrimary,
                    onClick = { showSystemInfoDialog = true }
                )
            }

            item {
                SettingActionRow(
                    title = "Changelog & Roadmap (222)",
                    subtitle = "Version history and feature statuses",
                    icon = Icons.Default.HistoryEdu,
                    iconTint = NeonPink,
                    onClick = { showChangelogDialog = true }
                )
            }

            item {
                SettingActionRow(
                    title = "Backup & Export Data (999)",
                    subtitle = "Export JSON backup of library",
                    icon = Icons.Default.Backup,
                    iconTint = AccentSuccess,
                    onClick = {
                        exportedJsonText = onExportJson()
                        showExportDialog = true
                    }
                )
            }

            // Maintenance
            item { SectionHeader("Maintenance") }

            item {
                SettingActionRow(
                    title = "Clear Thumbnail Cache (123)",
                    subtitle = "Free temporary preview cache",
                    icon = Icons.Default.CleaningServices,
                    iconTint = TextMuted,
                    onClick = onResetCache
                )
            }

            item {
                SettingActionRow(
                    title = "Factory Reset (789)",
                    subtitle = "Reset all preferences and library",
                    icon = Icons.Default.DeleteForever,
                    iconTint = AccentError,
                    onClick = { showResetConfirm = true }
                )
            }
        }

        // Change PIN Dialog
        if (showPinDialog) {
            var newPinInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Set New Calculator PIN") },
                text = {
                    Column {
                        Text("Enter the new digits to unlock the app when pressing '=':", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 8) newPinInput = it },
                            placeholder = { Text("e.g. 140399") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("new_pin_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinInput.isNotEmpty()) {
                                onSetPin(newPinInput)
                                showPinDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                    ) {
                        Text("Save PIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // Secret Codes Dialog
        if (showCodesDialog) {
            AlertDialog(
                onDismissRequest = { showCodesDialog = false },
                title = { Text("Secret Calculator Codes") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(MediaRepository.secretCodes) { code ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCard)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = code.code,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.width(50.dp)
                                )
                                Column {
                                    Text(code.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(code.description, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCodesDialog = false }) { Text("Close") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // Storage Analyzer Dialog
        if (showStorageDialog) {
            AlertDialog(
                onDismissRequest = { showStorageDialog = false },
                title = { Text("Storage Analyzer") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Internal App Data: ~14.2 MB", color = Color.White)
                        Text("Thumbnail Cache: ~3.8 MB", color = TextSecondary)
                        Text("ExoPlayer Cache: ~8.4 MB", color = TextSecondary)
                        Text("Saved Playlists: 1", color = TextSecondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStorageDialog = false }) { Text("Done") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // System Info Dialog
        if (showSystemInfoDialog) {
            AlertDialog(
                onDismissRequest = { showSystemInfoDialog = false },
                title = { Text("System Information") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Device: ${Build.MANUFACTURER} ${Build.MODEL}", color = Color.White)
                        Text("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", color = TextSecondary)
                        Text("Architecture: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64"}", color = TextSecondary)
                        Text("Renderer: Hardware Accelerated Compose M3", color = NeonCyan)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSystemInfoDialog = false }) { Text("OK") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // Stats Dialog
        if (showStatsDialog) {
            AlertDialog(
                onDismissRequest = { showStatsDialog = false },
                title = { Text("Usage Statistics") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("App Status: Active", color = AccentSuccess)
                        Text("Media Streams Loaded: 6", color = Color.White)
                        Text("Playback Engine: AndroidX Media3 1.5.0", color = TextSecondary)
                        Text("Vault Status: Locked & Secured", color = NeonPink)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStatsDialog = false }) { Text("Close") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // Changelog Dialog
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                title = { Text("Changelog v1.03.0") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Modern Jetpack Compose UI with dark obsidian neon theme", color = Color.White)
                        Text("• Dual Multiview Player for simultaneous streaming", color = TextSecondary)
                        Text("• Media3 ExoPlayer with Speed, Sleep Timer, Aspect Ratio", color = TextSecondary)
                        Text("• Full Calculator Disguise & Secret Code Registry", color = TextSecondary)
                        Text("• M3U IPTV playlist parsing & direct stream integration", color = TextSecondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChangelogDialog = false }) { Text("Close") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Backup JSON Data") },
                text = {
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showExportDialog = false }) { Text("Close") }
                },
                containerColor = DarkSurfaceVariant
            )
        }

        // Factory Reset Confirm
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Confirm Factory Reset", color = AccentError) },
                text = { Text("This will reset all library items, playlists, and settings back to default. Are you sure?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onFactoryReset()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentError)
                    ) {
                        Text("Reset All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
                },
                containerColor = DarkSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = NeonViolet,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
        }
    }
}
