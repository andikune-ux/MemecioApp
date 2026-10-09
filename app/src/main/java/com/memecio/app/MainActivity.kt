package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    companion object {
        private const val REQ_STORAGE_PERMISSION = 100
    }

    private lateinit var container: FrameLayout
    private lateinit var btnBeranda: LinearLayout
    private lateinit var btnSumber: LinearLayout
    private lateinit var btnProfil: LinearLayout

    private var tvIconBeranda: ImageView? = null
    private var tvLabelBeranda: TextView? = null
    private var tvIconSumber: ImageView? = null
    private var tvLabelSumber: TextView? = null
    private var tvIconProfil: ImageView? = null
    private var tvLabelProfil: TextView? = null

    private var tvHariTanggal: TextView? = null
    private var tvJamRealtime: TextView? = null
    private var tvKoneksiIndicator: TextView? = null

    private val localMedia = ArrayList<MediaItem>()
    private val combinedMedia = ArrayList<MediaItem>()
    private var currentFilter = 0 // 0: Semua, 1: Foto, 2: Video
    private var sortAscending = true
    private var searchQuery = ""
    private var activeSource = "offline"

    // Sidebar
    private var sidebarContainer: LinearLayout? = null
    private var sidebarList: ListView? = null
    private var sidebarAdapter: ArrayAdapter<MediaItem>? = null
    private val sidebarSavedItems = ArrayList<MediaItem>()
    private var sidebarOpen = false

    // Real-time Clock & Network
    private val clockHandler = Handler(Looper.getMainLooper())
    private var clockRunnable: Runnable? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundHelper.init(this)

        val effectiveMode = DisplayModeStore.getEffectiveMode(this)
        if (effectiveMode == "tv") {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        btnBeranda = findViewById(R.id.btnBeranda)
        btnSumber = findViewById(R.id.btnSumber)
        btnProfil = findViewById(R.id.btnProfil)

        tvIconBeranda = findViewById(R.id.tvIconBeranda)
        tvLabelBeranda = findViewById(R.id.tvLabelBeranda)
        tvIconSumber = findViewById(R.id.tvIconSumber)
        tvLabelSumber = findViewById(R.id.tvLabelSumber)
        tvIconProfil = findViewById(R.id.tvIconProfil)
        tvLabelProfil = findViewById(R.id.tvLabelProfil)

        tvHariTanggal = findViewById(R.id.tvHariTanggal)
        tvJamRealtime = findViewById(R.id.tvJamRealtime)
        tvKoneksiIndicator = findViewById(R.id.tvKoneksiIndicator)

        setupKoneksiIndicator()
        startClock()

        btnBeranda.setOnClickListener {
            switchToTab(0)
            resolveActiveSourceAndLoad()
        }

        btnSumber.setOnClickListener {
            switchToTab(1)
        }

        btnProfil.setOnClickListener {
            switchToTab(2)
        }

        switchToTab(0)
        handleIntentExtras(intent)
    }

    override fun onResume() {
        super.onResume()
        if (currentTab == 0) {
            refreshSidebar()
            resolveActiveSourceAndLoad()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        switchToTab(0)
        handleIntentExtras(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        clockRunnable?.let { clockHandler.removeCallbacks(it) }
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (ignored: Exception) {}
    }

    private fun handleIntentExtras(intent: Intent?) {
        if (intent == null) return
        val playlistUrl = intent.getStringExtra("open_playlist_url")
        val playlistTitle = intent.getStringExtra("open_playlist_title")

        if (playlistUrl != null) {
            val item = MediaItem(Uri.parse(playlistUrl), MediaItem.TYPE_VIDEO).apply {
                title = playlistTitle
            }
            loadPlaylist(item)
        } else if (intent.getBooleanExtra("focus_source", false)) {
            activeSource = ExternalMediaStore.getSourceLabel(this)
            renderFromExternalOnly()
        } else {
            activeSource = "offline"
            checkPermissionAndLoad()
        }
    }

    private fun startClock() {
        clockRunnable = object : Runnable {
            override fun run() {
                val now = Date()
                val fmtHari = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID"))
                val fmtJam = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                tvHariTanggal?.text = fmtHari.format(now)
                tvJamRealtime?.text = fmtJam.format(now)
                clockHandler.postDelayed(this, 1000)
            }
        }
        clockHandler.post(clockRunnable!!)
    }

    private fun setupKoneksiIndicator() {
        try {
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            tvKoneksiIndicator?.setTextColor(0xFFFFC107.toInt()) // Kuning

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread { tvKoneksiIndicator?.setTextColor(0xFF4CAF50.toInt()) } // Hijau
                }

                override fun onLost(network: Network) {
                    runOnUiThread { tvKoneksiIndicator?.setTextColor(0xFFF44336.toInt()) } // Merah
                }

                override fun onUnavailable() {
                    runOnUiThread { tvKoneksiIndicator?.setTextColor(0xFF000000.toInt()) }
                }
            }

            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(req, networkCallback!!)
        } catch (ignored: Exception) {}
    }

    private fun switchToTab(index: Int) {
        currentTab = index
        selectTab(index)
        when (index) {
            0 -> {
                showContent(R.layout.content_beranda)
                setupFilterTabs()
                setupSidebar()
                renderBeranda()
            }
            1 -> {
                showContent(R.layout.content_sumber)
                setupSumberButtons()
            }
            2 -> {
                showContent(R.layout.content_profil)
                setupProfilButtons()
            }
        }
    }

    private fun selectTab(index: Int) {
        val selectedColor = 0xFFFFFFFF.toInt()
        val unselectedColor = 0xFF9E9E9E.toInt()

        tvIconBeranda?.setColorFilter(if (index == 0) selectedColor else unselectedColor)
        tvLabelBeranda?.setTextColor(if (index == 0) selectedColor else unselectedColor)
        btnBeranda.setBackgroundResource(if (index == 0) R.drawable.bg_glass_button_selected else 0)

        tvIconSumber?.setColorFilter(if (index == 1) selectedColor else unselectedColor)
        tvLabelSumber?.setTextColor(if (index == 1) selectedColor else unselectedColor)
        btnSumber.setBackgroundResource(if (index == 1) R.drawable.bg_glass_button_selected else 0)

        tvIconProfil?.setColorFilter(if (index == 2) selectedColor else unselectedColor)
        tvLabelProfil?.setTextColor(if (index == 2) selectedColor else unselectedColor)
        btnProfil.setBackgroundResource(if (index == 2) R.drawable.bg_glass_button_selected else 0)
    }

    private fun showContent(layoutRes: Int) {
        container.removeAllViews()
        LayoutInflater.from(this).inflate(layoutRes, container, true)
    }

    // ==========================================
    // BERANDA (TAB 0)
    // ==========================================

    private fun setupFilterTabs() {
        val btnSemua = container.findViewById<TextView>(R.id.btnFilterSemua)
        val btnFoto = container.findViewById<TextView>(R.id.btnFilterFoto)
        val btnVideo = container.findViewById<TextView>(R.id.btnFilterVideo)
        val btnSort = container.findViewById<TextView>(R.id.btnSort)
        val btnToggleSidebar = container.findViewById<ImageButton>(R.id.btnToggleSidebar)
        val etSearch = container.findViewById<EditText>(R.id.etSearch)
        val btnSearch = container.findViewById<ImageButton>(R.id.btnSearch)

        btnSemua?.setOnClickListener {
            currentFilter = 0
            updateFilterTabsUI()
            renderBeranda()
        }

        btnFoto?.setOnClickListener {
            currentFilter = 1
            updateFilterTabsUI()
            renderBeranda()
        }

        btnVideo?.setOnClickListener {
            currentFilter = 2
            updateFilterTabsUI()
            renderBeranda()
        }

        btnSort?.setOnClickListener {
            sortAscending = !sortAscending
            btnSort.text = if (sortAscending) "A-Z" else "Z-A"
            renderBeranda()
        }

        btnToggleSidebar?.setOnClickListener {
            toggleSidebar()
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                renderBeranda()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSearch?.setOnClickListener {
            searchQuery = etSearch?.text?.toString()?.trim() ?: ""
            renderBeranda()
        }

        updateFilterTabsUI()
    }

    private fun updateFilterTabsUI() {
        val btnSemua = container.findViewById<TextView>(R.id.btnFilterSemua) ?: return
        val btnFoto = container.findViewById<TextView>(R.id.btnFilterFoto) ?: return
        val btnVideo = container.findViewById<TextView>(R.id.btnFilterVideo) ?: return

        btnSemua.setBackgroundResource(if (currentFilter == 0) R.drawable.bg_glass_button_selected else R.drawable.bg_filter_transparent)
        btnSemua.setTextColor(if (currentFilter == 0) 0xFFFFFFFF.toInt() else 0xFF1C1C1E.toInt())

        btnFoto.setBackgroundResource(if (currentFilter == 1) R.drawable.bg_glass_button_selected else R.drawable.bg_filter_transparent)
        btnFoto.setTextColor(if (currentFilter == 1) 0xFFFFFFFF.toInt() else 0xFF1C1C1E.toInt())

        btnVideo.setBackgroundResource(if (currentFilter == 2) R.drawable.bg_glass_button_selected else R.drawable.bg_filter_transparent)
        btnVideo.setTextColor(if (currentFilter == 2) 0xFFFFFFFF.toInt() else 0xFF1C1C1E.toInt())
    }

    private fun setupSidebar() {
        sidebarContainer = container.findViewById(R.id.sidebarContainer)
        sidebarList = container.findViewById(R.id.sidebarSavedLinks)
        if (sidebarList == null) return

        sidebarAdapter = object : ArrayAdapter<MediaItem>(this, 0, sidebarSavedItems) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
                val item = getItem(position)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val text2 = view.findViewById<TextView>(android.R.id.text2)

                text1?.text = item?.title ?: item?.uri?.toString() ?: "Playlist"
                text1?.setTextColor(0xFF1C1C1E.toInt())
                text2?.text = item?.uri?.toString() ?: ""
                text2?.setTextColor(0xFF8E8E93.toInt())

                view.setOnClickListener {
                    item?.let {
                        loadPlaylist(it)
                        toggleSidebar()
                    }
                }

                view.setOnLongClickListener {
                    item?.let { confirmDeletePlaylist(it) }
                    true
                }

                return view
            }
        }

        sidebarList?.adapter = sidebarAdapter
        refreshSidebar()
    }

    private fun toggleSidebar() {
        sidebarContainer?.let { sb ->
            sidebarOpen = !sidebarOpen
            sb.visibility = if (sidebarOpen) View.VISIBLE else View.GONE
        }
    }

    private fun refreshSidebar() {
        sidebarSavedItems.clear()
        sidebarSavedItems.addAll(SavedLinksStore.getAll(this))
        sidebarAdapter?.notifyDataSetChanged()
    }

    private fun confirmDeletePlaylist(item: MediaItem) {
        val title = item.title ?: "Playlist ini"
        AlertDialog.Builder(this)
            .setTitle("Hapus Playlist")
            .setMessage("Apakah Anda yakin ingin menghapus '$title'?")
            .setPositiveButton("Hapus") { _, _ ->
                SavedLinksStore.hapus(this, item.uri.toString())
                refreshSidebar()
                Toast.makeText(this, "Playlist dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadPlaylist(playlistItem: MediaItem) {
        val url = playlistItem.uri.toString()
        Toast.makeText(this, "Memuat playlist...", Toast.LENGTH_SHORT).show()

        M3uParser.parseAsync(this, url, object : M3uParser.Callback {
            override fun onSuccess(entries: List<M3uParser.MediaEntry>) {
                val mediaItems = ArrayList<MediaItem>()
                if (entries.isEmpty()) {
                    val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
                        isLocal = false
                        title = playlistItem.title
                    }
                    mediaItems.add(mi)
                } else {
                    for (e in entries) {
                        val mi = MediaItem(Uri.parse(e.url), MediaItem.TYPE_VIDEO).apply {
                            isLocal = false
                            title = e.title
                            thumbUrl = e.thumbUrl
                            isM3u = true
                        }
                        mediaItems.add(mi)
                    }
                }
                ExternalMediaStore.gantiSemua(this@MainActivity, mediaItems, playlistItem.title ?: "Playlist")
                activeSource = ExternalMediaStore.getSourceLabel(this@MainActivity)
                renderFromExternalOnly()
            }

            override fun onError(message: String) {
                val mediaItems = ArrayList<MediaItem>()
                val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO).apply {
                    isLocal = false
                    title = playlistItem.title
                }
                mediaItems.add(mi)
                ExternalMediaStore.gantiSemua(this@MainActivity, mediaItems, playlistItem.title ?: "Playlist")
                activeSource = ExternalMediaStore.getSourceLabel(this@MainActivity)
                Toast.makeText(this@MainActivity, "Gagal parsing: $message", Toast.LENGTH_LONG).show()
                renderFromExternalOnly()
            }
        })
    }

    private fun resolveActiveSourceAndLoad() {
        if ("offline" == activeSource) {
            checkPermissionAndLoad()
        } else {
            renderFromExternalOnly()
        }
    }

    private fun renderFromExternalOnly() {
        combinedMedia.clear()
        combinedMedia.addAll(ExternalMediaStore.getAll(this))
        renderBeranda()
    }

    private fun checkPermissionAndLoad() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        var allGranted = true
        for (p in perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }

        if (!allGranted) {
            requestPermissions(perms, REQ_STORAGE_PERMISSION)
        } else {
            loadOfflineMedia()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_STORAGE_PERMISSION) {
            var granted = grantResults.isNotEmpty()
            for (r in grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) granted = false
            }
            if (granted) {
                loadOfflineMedia()
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show()
                renderBeranda()
            }
        }
    }

    private fun loadOfflineMedia() {
        localMedia.clear()

        try {
            val imgCursor: Cursor? = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                null, null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
            )
            imgCursor?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                    val item = MediaItem(uri, MediaItem.TYPE_IMAGE).apply {
                        isLocal = true
                    }
                    localMedia.add(item)
                }
            }
        } catch (ignored: Exception) {}

        try {
            val vidCursor: Cursor? = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID),
                null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC"
            )
            vidCursor?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                    val item = MediaItem(uri, MediaItem.TYPE_VIDEO).apply {
                        isLocal = true
                    }
                    localMedia.add(item)
                }
            }
        } catch (ignored: Exception) {}

        combinedMedia.clear()
        combinedMedia.addAll(localMedia)
        renderBeranda()
    }

    private fun renderBeranda() {
        val emptyState = container.findViewById<View>(R.id.emptyState)
        val grid = container.findViewById<GridView>(R.id.gridBeranda) ?: return

        val filtered = ArrayList<MediaItem>()
        for (m in combinedMedia) {
            var matchType = false
            if (currentFilter == 0) matchType = true
            else if (currentFilter == 1 && m.type == MediaItem.TYPE_IMAGE) matchType = true
            else if (currentFilter == 2 && m.type == MediaItem.TYPE_VIDEO) matchType = true

            var matchSearch = true
            if (searchQuery.isNotEmpty()) {
                val title = m.title ?: m.uri.lastPathSegment ?: ""
                matchSearch = title.lowercase().contains(searchQuery.lowercase())
            }

            if (matchType && matchSearch) {
                filtered.add(m)
            }
        }

        if (sortAscending) {
            filtered.sortBy { it.title ?: it.uri.lastPathSegment ?: "" }
        } else {
            filtered.sortByDescending { it.title ?: it.uri.lastPathSegment ?: "" }
        }

        if (filtered.isEmpty()) {
            emptyState?.visibility = View.VISIBLE
            grid.visibility = View.GONE
        } else {
            emptyState?.visibility = View.GONE
            grid.visibility = View.VISIBLE
            grid.adapter = ThumbnailAdapter(this, filtered, object : ThumbnailAdapter.OnThumbnailClickListener {
                override fun onThumbnailClick(mediaItem: MediaItem) {
                    if (mediaItem.type == MediaItem.TYPE_VIDEO) {
                        val videoPlaylist = ArrayList<String>()
                        var clickedIndex = 0
                        var counter = 0
                        for (m in filtered) {
                            if (m.type == MediaItem.TYPE_VIDEO) {
                                videoPlaylist.add(m.uri.toString())
                                if (m.uri == mediaItem.uri) {
                                    clickedIndex = counter
                                }
                                counter++
                            }
                        }
                        PlaylistHolder.set(videoPlaylist)
                        val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java).apply {
                            putStringArrayListExtra("playlist", videoPlaylist)
                            putExtra("index", clickedIndex)
                        }
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@MainActivity, PreviewImageActivity::class.java).apply {
                            putExtra("uri", mediaItem.uri.toString())
                        }
                        startActivity(intent)
                    }
                }
            })
        }
    }

    // ==========================================
    // SUMBER (TAB 1)
    // ==========================================

    private fun setupSumberButtons() {
        val switchSmart = container.findViewById<Switch>(R.id.switchSmart)
        val switchOffline = container.findViewById<Switch>(R.id.switchOffline)
        val switchMediaScan = container.findViewById<Switch>(R.id.switchMediaScan)

        val btnKembalikan = container.findViewById<View>(R.id.btnKembalikan)
        val btnDrive = container.findViewById<View>(R.id.btnSumberDrive)
        val btnStreaming = container.findViewById<View>(R.id.btnSumberStreaming)
        val btnPlaylistManual = container.findViewById<View>(R.id.btnPlaylistManual)
        val btnServer = container.findViewById<View>(R.id.btnSumberServer)
        val btnRiwayat = container.findViewById<View>(R.id.btnRiwayat)

        switchOffline?.isChecked = (activeSource == "offline")
        switchOffline?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activeSource = "offline"
                Toast.makeText(this, "Sumber Offline diaktifkan", Toast.LENGTH_SHORT).show()
            }
        }

        switchSmart?.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Smart Playlist Aktif" else "Smart Playlist Nonaktif", Toast.LENGTH_SHORT).show()
        }

        switchMediaScan?.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Media Scan Folder Aktif" else "Media Scan Folder Nonaktif", Toast.LENGTH_SHORT).show()
        }

        btnKembalikan?.setOnClickListener {
            Toast.makeText(this, "Semua media telah dipulihkan", Toast.LENGTH_SHORT).show()
        }

        btnDrive?.setOnClickListener {
            startActivity(Intent(this, DriveSourceActivity::class.java))
        }

        btnStreaming?.setOnClickListener {
            startActivity(Intent(this, StreamingSourceActivity::class.java))
        }

        btnPlaylistManual?.setOnClickListener {
            showAddManualPlaylistDialog()
        }

        btnServer?.setOnClickListener {
            val intent = Intent(this, PinDialogActivity::class.java).apply {
                putExtra(PinDialogActivity.EXTRA_TARGET, PinDialogActivity.TARGET_SERVER)
            }
            startActivity(intent)
        }

        btnRiwayat?.setOnClickListener {
            val intent = Intent(this, PinDialogActivity::class.java).apply {
                putExtra(PinDialogActivity.EXTRA_TARGET, PinDialogActivity.TARGET_RIWAYAT)
            }
            startActivity(intent)
        }
    }

    private fun showAddManualPlaylistDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }
        val etTitle = EditText(this).apply {
            hint = "Nama Playlist"
        }
        val etUrl = EditText(this).apply {
            hint = "URL Playlist / M3U8"
        }
        layout.addView(etTitle)
        layout.addView(etUrl)

        AlertDialog.Builder(this)
            .setTitle("Tambah Playlist Manual")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                val url = etUrl.text.toString().trim()
                if (url.isNotEmpty()) {
                    SavedLinksStore.tambah(this, if (title.isEmpty()) "Playlist" else title, url)
                    Toast.makeText(this, "Playlist berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "URL tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ==========================================
    // PROFIL (TAB 2)
    // ==========================================

    private var devClickCount = 0

    private fun setupProfilButtons() {
        val btnMode = container.findViewById<View>(R.id.btnModeTampilan)
        val btnZoom = container.findViewById<View>(R.id.btnZoomUI)
        val btnCariOnline = container.findViewById<View>(R.id.btnPencarianOnline)
        val btnDataSaver = container.findViewById<View>(R.id.btnDataSaver)
        val btnBackup = container.findViewById<View>(R.id.btnBackupLengkap)
        val btnStatistik = container.findViewById<View>(R.id.btnStatistik)
        val btnAutoExit = container.findViewById<View>(R.id.btnAutoExit)
        val btnLogCrash = container.findViewById<View>(R.id.btnLogCrash)
        val btnDevTools = container.findViewById<View>(R.id.btnDevTools)

        val tvVersiApp = container.findViewById<TextView>(R.id.tvVersiApp)
        val tvVersiBuild = container.findViewById<TextView>(R.id.tvVersiBuild)

        tvVersiApp?.text = "Memec.io V.1.14.65"
        tvVersiBuild?.text = "Build: Kotlin Engine 1.14"

        // 7 clicks to toggle Dev Mode
        tvVersiApp?.setOnClickListener {
            devClickCount++
            if (devClickCount >= 7) {
                devClickCount = 0
                val enabled = DeveloperModeStore.toggle(this)
                btnDevTools?.visibility = if (enabled) View.VISIBLE else View.GONE
            } else if (devClickCount >= 4) {
                Toast.makeText(this, "${7 - devClickCount} ketukan lagi untuk Developer Mode", Toast.LENGTH_SHORT).show()
            }
        }

        btnDevTools?.visibility = if (DeveloperModeStore.isEnabled(this)) View.VISIBLE else View.GONE

        btnMode?.setOnClickListener {
            TestModesHelper.showChoice(this)
        }

        btnZoom?.setOnClickListener {
            Toast.makeText(this, "Ukuran UI: Skala 100% (Default)", Toast.LENGTH_SHORT).show()
        }

        btnCariOnline?.setOnClickListener {
            switchToTab(1)
        }

        btnDataSaver?.setOnClickListener {
            Toast.makeText(this, "Mode Hemat Data: Aktif (Kualitas adaptif)", Toast.LENGTH_SHORT).show()
        }

        btnBackup?.setOnClickListener {
            showBackupDialog()
        }

        btnStatistik?.setOnClickListener {
            startActivity(Intent(this, StatistikActivity::class.java))
        }

        btnAutoExit?.setOnClickListener {
            showAutoExitDialog()
        }

        btnLogCrash?.setOnClickListener {
            startActivity(Intent(this, CrashHistoryActivity::class.java))
        }

        btnDevTools?.setOnClickListener {
            showDevToolsDialog()
        }
    }

    private fun showBackupDialog() {
        val options = arrayOf(
            "Ekspor Panduan Proyek (999)",
            "Reset Cache Aplikasi (123)",
            "Perbaiki Database (456)"
        )
        AlertDialog.Builder(this)
            .setTitle("Cadangan & Pemeliharaan")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ProjectExportHelper.export(this)
                    1 -> CacheResetter.confirmAndReset(this)
                    2 -> RepairDatabaseHelper.confirmAndRepair(this)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDevToolsDialog() {
        val all = SecretCodeRegistry.getAll()
        val items = all.map { "${it.code} - ${it.title}${if (it.hidden) " [Hidden]" else ""}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Developer Tools")
            .setItems(items) { _, which ->
                handleDevCode(all[which].code)
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun handleDevCode(code: String) {
        try {
            when (code) {
                "000" -> startActivity(Intent(this, SecretCodesActivity::class.java))
                "111" -> startActivity(Intent(this, CrashHistoryActivity::class.java))
                "222" -> startActivity(Intent(this, ChangelogActivity::class.java))
                "333" -> startActivity(Intent(this, SystemInfoActivity::class.java))
                "555" -> startActivity(Intent(this, NetworkInfoActivity::class.java))
                "666" -> startActivity(Intent(this, PermissionInfoActivity::class.java))
                "777" -> startActivity(Intent(this, StorageAnalyzerActivity::class.java))
                "808" -> startActivity(Intent(this, DownloadListActivity::class.java))
                "888" -> startActivity(Intent(this, StatistikActivity::class.java))
                "999" -> ProjectExportHelper.export(this)
                "101" -> {
                    DeveloperModeStore.toggle(this)
                    recreate()
                }
                "102" -> throw RuntimeException("Force Crash via Dev Tools")
                "103" -> TestGestureHelper.showGuide(this)
                "104" -> TestModesHelper.showChoice(this)
                "123" -> CacheResetter.confirmAndReset(this)
                "444" -> TestMediaHelper.showChoice(this)
                "456" -> RepairDatabaseHelper.confirmAndRepair(this)
                "789" -> FactoryResetHelper.confirmAndReset(this)
                "200" -> {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
                "201" -> {
                    try {
                        startActivity(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Tidak didukung di HP ini", Toast.LENGTH_SHORT).show()
                    }
                }
                "202" -> {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Tidak didukung di HP ini", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Aksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAutoExitDialog() {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_auto_exit)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<View>(R.id.btnExit5)?.setOnClickListener { AutoExitManager.instance.start(this, 5); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit15)?.setOnClickListener { AutoExitManager.instance.start(this, 15); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit30)?.setOnClickListener { AutoExitManager.instance.start(this, 30); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit60)?.setOnClickListener { AutoExitManager.instance.start(this, 60); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit120)?.setOnClickListener { AutoExitManager.instance.start(this, 120); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExitCancel)?.setOnClickListener {
            AutoExitManager.instance.cancel(this)
            Toast.makeText(this, "Auto Exit dimatikan", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btnExitBatal)?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
