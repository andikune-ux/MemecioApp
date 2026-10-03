package com.memecio.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memecio.app.data.MediaItem
import com.memecio.app.data.MediaRepository
import com.memecio.app.ui.screens.*
import com.memecio.app.ui.theme.DarkSurface
import com.memecio.app.ui.theme.MemecioTheme
import com.memecio.app.ui.theme.NeonCyan
import com.memecio.app.ui.theme.NeonViolet
import com.memecio.app.ui.theme.TextMuted

class MainActivity : ComponentActivity() {

    private lateinit var repository: MediaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = MediaRepository(applicationContext)

        setContent {
            MemecioTheme {
                MainAppNav(repository = repository)
            }
        }
    }
}

@Composable
fun MainAppNav(repository: MediaRepository) {
    val context = LocalContext.current

    val mediaList by repository.mediaList.collectAsStateWithLifecycle()
    val playlists by repository.playlists.collectAsStateWithLifecycle()
    val history by repository.history.collectAsStateWithLifecycle()
    val pinCode by repository.pinCode.collectAsStateWithLifecycle()
    val displayMode by repository.displayMode.collectAsStateWithLifecycle()
    val gridColumns by repository.gridColumns.collectAsStateWithLifecycle()
    val isDevMode by repository.developerMode.collectAsStateWithLifecycle()

    // Navigation and state
    var isUnlocked by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) }
    var activePlayerMedia by remember { mutableStateOf<MediaItem?>(null) }
    var isMultiviewActive by remember { mutableStateOf(false) }
    var isVaultActive by remember { mutableStateOf(false) }

    // Dialogs triggered from secret codes
    var activeSecretDialog by remember { mutableStateOf<String?>(null) }

    // Back handling
    BackHandler {
        when {
            activePlayerMedia != null -> activePlayerMedia = null
            isMultiviewActive -> isMultiviewActive = false
            isVaultActive -> isVaultActive = false
            currentTab != 0 -> currentTab = 0
            isUnlocked -> isUnlocked = false // Disguise back to calculator
        }
    }

    if (!isUnlocked) {
        // Calculator Disguise
        CalculatorScreen(
            currentPin = pinCode,
            onUnlock = {
                isUnlocked = true
                Toast.makeText(context, "Welcome to Memec.io", Toast.LENGTH_SHORT).show()
            },
            onSecretCode = { code ->
                when (code) {
                    "000", "111", "222", "333", "555", "666", "777", "888" -> {
                        activeSecretDialog = code
                    }
                    "444" -> {
                        // Launch test video
                        isUnlocked = true
                        activePlayerMedia = mediaList.firstOrNull()
                    }
                    "999" -> {
                        activeSecretDialog = "999"
                    }
                    "123" -> {
                        repository.resetCache()
                        Toast.makeText(context, "Thumbnail cache cleared", Toast.LENGTH_SHORT).show()
                    }
                    "456" -> {
                        Toast.makeText(context, "Database integrity verified OK", Toast.LENGTH_SHORT).show()
                    }
                    "789" -> {
                        repository.factoryReset()
                        Toast.makeText(context, "App reset to factory defaults", Toast.LENGTH_SHORT).show()
                    }
                    "101" -> {
                        repository.toggleDeveloperMode()
                        val state = if (!isDevMode) "ENABLED" else "DISABLED"
                        Toast.makeText(context, "Developer Mode $state", Toast.LENGTH_SHORT).show()
                    }
                    "103" -> {
                        Toast.makeText(context, "Double-tap sides to seek ±10s. Tap screen for controls.", Toast.LENGTH_LONG).show()
                    }
                    "104" -> {
                        val next = if (gridColumns >= 4) 2 else gridColumns + 1
                        repository.setGridColumns(next)
                        Toast.makeText(context, "Grid set to $next columns", Toast.LENGTH_SHORT).show()
                    }
                    "808" -> {
                        isUnlocked = true
                        currentTab = 2
                    }
                }
            }
        )

        // Secret code info dialogs while in Calculator mode
        if (activeSecretDialog != null) {
            val codeEntry = MediaRepository.secretCodes.find { it.code == activeSecretDialog }
            AlertDialog(
                onDismissRequest = { activeSecretDialog = null },
                title = { Text(codeEntry?.title ?: "Secret Code $activeSecretDialog") },
                text = {
                    Column {
                        Text(codeEntry?.description ?: "Diagnostics information", color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        when (activeSecretDialog) {
                            "000" -> {
                                Text("All Available Codes:\n" + MediaRepository.secretCodes.joinToString("\n") { "${it.code}: ${it.title}" }, fontSize = 12.sp, color = NeonCyan)
                            }
                            "333" -> {
                                Text("Device Model: ${android.os.Build.MODEL}\nAndroid SDK: ${android.os.Build.VERSION.SDK_INT}\nArchitecture: ${android.os.Build.SUPPORTED_ABIS.firstOrNull()}", color = NeonCyan)
                            }
                            "555" -> {
                                Text("Network: Active Connected\nProtocols: HTTP/2, HLS, TLS 1.3\nStreaming Status: Ready", color = NeonCyan)
                            }
                            "999" -> {
                                Text("Data exported successfully to memory.", color = NeonCyan)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeSecretDialog = null }) { Text("Close") }
                },
                containerColor = DarkSurface
            )
        }
        return
    }

    // When unlocked: Full Player, Multiview, Vault, or Main Tabbed App
    if (activePlayerMedia != null) {
        val playingMedia = activePlayerMedia!!
        val playlist = mediaList.filter { !it.isHidden }
        PlayerScreen(
            item = playingMedia,
            playlist = playlist,
            onBack = { activePlayerMedia = null },
            onDisguise = {
                activePlayerMedia = null
                isUnlocked = false
            },
            onRecordProgress = { pos, dur ->
                repository.recordView(playingMedia.id, pos, dur)
            }
        )
        return
    }

    if (isMultiviewActive) {
        MultiviewScreen(
            mediaList = mediaList.filter { !it.isHidden },
            onBack = { isMultiviewActive = false },
            onDisguise = {
                isMultiviewActive = false
                isUnlocked = false
            }
        )
        return
    }

    if (isVaultActive) {
        VaultScreen(
            hiddenMediaList = mediaList.filter { it.isHidden },
            onMediaClick = { activePlayerMedia = it },
            onUnhide = { repository.toggleHidden(it) },
            onDelete = { repository.removeMedia(it) },
            onBack = { isVaultActive = false }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = NeonViolet
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Beranda") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = NeonViolet.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("tab_beranda")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = "Sources") },
                    label = { Text("Sumber") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = NeonViolet.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("tab_sumber")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                    label = { Text("Playlists") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = NeonViolet.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("tab_playlists")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Profile") },
                    label = { Text("Profil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = NeonViolet.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("tab_profil")
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    mediaList = mediaList,
                    gridColumns = gridColumns,
                    onMediaClick = { activePlayerMedia = it },
                    onFavoriteToggle = { repository.toggleFavorite(it) },
                    onWatchLaterToggle = { repository.toggleWatchLater(it) },
                    onHideToggle = {
                        repository.toggleHidden(it)
                        Toast.makeText(context, "Item moved to Secret Vault", Toast.LENGTH_SHORT).show()
                    },
                    onAddToPlaylist = { item ->
                        val defaultPl = playlists.firstOrNull()
                        if (defaultPl != null) {
                            repository.addToPlaylist(defaultPl.id, item.id)
                            Toast.makeText(context, "Added to '${defaultPl.title}'", Toast.LENGTH_SHORT).show()
                        } else {
                            repository.createPlaylist("My Favorites", initialMediaIds = listOf(item.id))
                            Toast.makeText(context, "Added to new playlist", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteMedia = { repository.removeMedia(it) },
                    onDisguise = { isUnlocked = false },
                    onOpenMultiview = { isMultiviewActive = true },
                    onOpenSources = { currentTab = 1 }
                )
                1 -> SourcesScreen(
                    onAddMedia = {
                        repository.addMedia(it)
                        Toast.makeText(context, "Added '${it.title}'", Toast.LENGTH_SHORT).show()
                        currentTab = 0
                    },
                    onImportM3u = { m3uText ->
                        val parsed = repository.parseM3uPlaylist(m3uText)
                        parsed.forEach { repository.addMedia(it) }
                        Toast.makeText(context, "Imported ${parsed.size} channels", Toast.LENGTH_SHORT).show()
                        currentTab = 0
                    },
                    onBack = { currentTab = 0 }
                )
                2 -> PlaylistsScreen(
                    playlists = playlists,
                    history = history,
                    allMedia = mediaList,
                    onCreatePlaylist = { title, desc ->
                        repository.createPlaylist(title, desc)
                        Toast.makeText(context, "Playlist '$title' created", Toast.LENGTH_SHORT).show()
                    },
                    onDeletePlaylist = { repository.deletePlaylist(it) },
                    onPlayMedia = { activePlayerMedia = it },
                    onClearHistory = {
                        repository.clearHistory()
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    },
                    onBack = { currentTab = 0 }
                )
                3 -> ProfileScreen(
                    currentPin = pinCode,
                    displayMode = displayMode,
                    gridColumns = gridColumns,
                    isDevMode = isDevMode,
                    onSetPin = {
                        repository.setPinCode(it)
                        Toast.makeText(context, "PIN updated to '$it'", Toast.LENGTH_SHORT).show()
                    },
                    onSetDisplayMode = { repository.setDisplayMode(it) },
                    onSetGridColumns = { repository.setGridColumns(it) },
                    onToggleDevMode = { repository.toggleDeveloperMode() },
                    onResetCache = {
                        repository.resetCache()
                        Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                    },
                    onFactoryReset = {
                        repository.factoryReset()
                        Toast.makeText(context, "Reset complete", Toast.LENGTH_SHORT).show()
                    },
                    onExportJson = { repository.exportToJson() },
                    onOpenVault = { isVaultActive = true },
                    onBack = { currentTab = 0 }
                )
            }
        }
    }
}
