package com.memecio.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.GridView
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.Locale
import java.util.Map
import java.util.regex.Pattern

class MainActivity : Activity() {

    companion object {
        private const val REQ_STORAGE_PERMISSION = 100
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_SORT_MODE = "sort_mode"
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private var container: FrameLayout? = null
    private var localMedia: MutableList<MediaItem> = ArrayList()
    private var combinedMedia: MutableList<MediaItem> = ArrayList()
    private var currentFilter = 0
    private var activeSource = "offline"

    private var btnBeranda: FrameLayout? = null
    private var btnSumber: FrameLayout? = null
    private var btnProfil: FrameLayout? = null
    private var tvIconBeranda: ImageView? = null
    private var tvLabelBeranda: TextView? = null
    private var tvIconSumber: ImageView? = null
    private var tvLabelSumber: TextView? = null
    private var tvIconProfil: ImageView? = null
    private var tvLabelProfil: TextView? = null

    private var sidebarList: ListView? = null
    private var sidebarAdapter: ArrayAdapter<MediaItem>? = null
    private var sidebarSavedItems: MutableList<MediaItem> = ArrayList()
    private var sidebarContainer: LinearLayout? = null
    private var sidebarOpen = false
    private var btnToggleSidebar: ImageButton? = null
    private var kategoriList: LinearLayout? = null
    private var scrollKategori: ScrollView? = null
    private var overlayExpand: FrameLayout? = null
    private var gridExpand: GridLayout? = null
    private var tvExpandTitle: TextView? = null
    private var btnShrink: ImageButton? = null
    private var gridBeranda: GridView? = null
    private var etSearch: EditText? = null
    private var historyChipsContainer: LinearLayout? = null
    private var btnSort: View? = null
    private var searchQuery = ""
    private var activeCategory = ""
    private var smartMode = ""
    private var smartTarget = ""
    private var folderModeEnabled = false
    private var selectedFolder: String? = null
    private var categoryChipsContainer: LinearLayout? = null
    private var categoryChipScroll: View? = null
    private val searchDebounceHandler = Handler(Looper.getMainLooper())
    private var searchDebounceRunnable: Runnable? = null

    private var tvKoneksiIndicator: TextView? = null
    private var currentTabIndex = 0
    private val cachedTabViews: MutableMap<Int, View> = HashMap()
    private val alreadySetup: MutableSet<Int> = HashSet()
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val clockHandler = Handler(Looper.getMainLooper())
    private var clockRunnable: Runnable? = null
    private fun debugLogMain(msg: String) {
    try {
        var dir: File? = File("/sdcard/Download")
        if (dir == null || !dir.exists()) dir = getExternalFilesDir(null)
        if (dir == null) return
        val f = File(dir, "memecio_preview_debug.txt")
        val fw = FileWriter(f, true)
        fw.write("${Date()} | [Main] $msg\n")
        fw.close()
    } catch (ignored: Exception) {}
}

override fun onCreate(savedInstanceState: Bundle?) {
    try { MultiSourceStore.migrateFromExternal(this) } catch (ignored: Exception) {}
    try { MultiSourceStore.deduplicate(this) } catch (ignored: Exception) {}
    super.onCreate(savedInstanceState)
    doOnCreate(savedInstanceState)
}

private fun doOnCreate(savedInstanceState: Bundle?) {
    try {
        doOnCreateInner(savedInstanceState)
    } catch (t: Throwable) {
        logCrashToFile(t)
        Log.e("MEMECIO_CRASH", "doOnCreate failed", t)
        throw RuntimeException(t)
    }
}

private fun doOnCreateInner(savedInstanceState: Bundle?) {
    SoundHelper.init(this)
    setContentView(R.layout.activity_main)
    tvKoneksiIndicator = findViewById(R.id.tvKoneksiIndicator)
    setupKoneksiIndicator()

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

    btnBeranda?.setOnClickListener {
        switchToTab(0)
        resolveActiveSourceAndLoad()
    }
    btnSumber?.setOnClickListener { switchToTab(1) }
    btnProfil?.setOnClickListener { switchToTab(2) }

    switchToTab(0)
    handleIntentExtras(intent)
    startClock()
}

override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (ev.action == MotionEvent.ACTION_DOWN) SoundHelper.click()
    return super.dispatchTouchEvent(ev)
}

override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN) {
        val code = event.keyCode
        if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN ||
            code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) {
            SoundHelper.nav()
        } else if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER ||
            code == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            SoundHelper.click()
        }
        if (DisplayModeStore.isTvMode(this)) {
            val focus = currentFocus
            val focusDiTab = (focus === btnBeranda || focus === btnSumber || focus === btnProfil)
            if (focusDiTab) {
                if (code == KeyEvent.KEYCODE_DPAD_LEFT && currentTabIndex > 0) {
                    val newIdx = currentTabIndex - 1
                    switchToTab(newIdx)
                    focusTabButton(newIdx)
                    return true
                }
                if (code == KeyEvent.KEYCODE_DPAD_RIGHT && currentTabIndex < 2) {
                    val newIdx = currentTabIndex + 1
                    switchToTab(newIdx)
                    focusTabButton(newIdx)
                    return true
                }
            }
        }
    }
    return super.dispatchKeyEvent(event)
}

private fun setupKoneksiIndicator() {
    val tv = tvKoneksiIndicator ?: return
    try {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        tv.setTextColor(0xFFFFC107.toInt())
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread { tv.setTextColor(0xFF4CAF50.toInt()) }
            }
            override fun onLost(network: Network) {
                runOnUiThread { tv.setTextColor(0xFFF44336.toInt()) }
            }
            override fun onUnavailable() {
                runOnUiThread { tv.setTextColor(0xFF000000.toInt()) }
            }
        }
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        connectivityManager?.registerNetworkCallback(req, networkCallback!!)
        cekStatusAwal()
    } catch (ignored: Exception) {}
}

private fun cekStatusAwal() {
    val tv = tvKoneksiIndicator ?: return
    try {
        val airplane = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0)
        if (airplane == 1) { tv.setTextColor(0xFF000000.toInt()); return }
        val activeNet = connectivityManager?.activeNetwork
        if (activeNet == null) { tv.setTextColor(0xFF000000.toInt()); return }
        val caps = connectivityManager?.getNetworkCapabilities(activeNet)
        if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            tv.setTextColor(0xFF4CAF50.toInt())
        } else {
            tv.setTextColor(0xFFF44336.toInt())
        }
    } catch (ignored: Exception) {}
}

private fun startClock() {
    val tvHari = findViewById<TextView>(R.id.tvHariTanggal) ?: return
    val tvJam = findViewById<TextView>(R.id.tvJamRealtime) ?: return
    clockRunnable = object : Runnable {
        override fun run() {
            val now = Date()
            val fmtHari = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID"))
            val fmtJam = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            tvHari.text = fmtHari.format(now)
            tvJam.text = fmtJam.format(now)
            clockHandler.postDelayed(this, 1000)
        }
    }
    clockHandler.post(clockRunnable!!)
}

override fun onDestroy() {
    super.onDestroy()
    clockRunnable?.let { clockHandler.removeCallbacks(it) }
}

override fun onResume() {
    super.onResume()
    applyDisplayModeSafe()
    try { applyImmersiveIfLandscape() } catch (ignored: Exception) {}
    try { AutoExitManager.getInstance().onActivityResumed(this) } catch (ignored: Exception) {}
    refreshSidebar()
    try {
        if (!MediaScanCache.isFresh(this) && activeSource == "offline") {
            loadOfflineMedia()
            renderBeranda()
        }
    } catch (ignored: Exception) {}
    if (MultiviewPickHolder.isPicking()) {
        try {
            Toast.makeText(this,
                "Pilih media untuk Slot ${MultiviewPickHolder.pickSlot + 1}",
                Toast.LENGTH_LONG).show()
            if (currentTabIndex != 0) switchToTab(0)
        } catch (ignored: Exception) {}
    }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    switchToTab(0)
    handleIntentExtras(intent)
}

override fun onBackPressed() {
    if (overlayExpand != null && overlayExpand!!.visibility == View.VISIBLE) {
        shrinkKategori()
    } else {
        super.onBackPressed()
    }
}
private fun handleIntentExtras(intent: Intent) {
    val customIndex = intent.getIntExtra("open_custom_playlist", -1)
    if (customIndex >= 0) {
        val pls = CustomPlaylistStore.getAll(this)
        if (customIndex < pls.size) {
            val pl = pls[customIndex]
            combinedMedia.clear()
            combinedMedia.addAll(pl.items)
            activeSource = "custom"
            switchToTab(0)
            return
        }
    }
    val driveFolderUrl = intent.getStringExtra("open_drive_folder_url")
    if (driveFolderUrl != null) { openDriveFolder(driveFolderUrl); return }
    val playlistUrl = intent.getStringExtra("open_playlist_url")
    val playlistTitle = intent.getStringExtra("open_playlist_title")
    if (playlistUrl != null) {
        val item = MediaItem(Uri.parse(playlistUrl), MediaItem.TYPE_VIDEO)
        item.title = playlistTitle
        loadPlaylist(item)
    } else if (intent.getBooleanExtra("focus_source", false)) {
        activeSource = ExternalMediaStore.getSourceLabel(this)
        renderFromExternalOnly()
    } else {
        val prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE)
        val offlineOn = prefs.getBoolean("offline_enabled", true)
        val lastSource = prefs.getString("last_source", "offline")
        if (offlineOn || lastSource == "offline") {
            activeSource = "offline"
            checkPermissionAndLoad()
        } else {
            activeSource = "external"
            renderFromExternalOnly()
        }
    }
}

private fun openDriveFolder(url: String) {
    val folderId = extractFolderId(url)
    if (folderId == null) {
        Toast.makeText(this, "ID Folder tidak ditemukan", Toast.LENGTH_SHORT).show()
        return
    }
    val cached = DriveFolderCache.load(this, folderId)
    if (cached != null && cached.isNotEmpty()) {
        ExternalMediaStore.gantiSemua(this, cached, "Drive Folder")
        activeSource = ExternalMediaStore.getSourceLabel(this)
        renderFromExternalOnly()
        Toast.makeText(this, "Dari cache (${cached.size} file)", Toast.LENGTH_SHORT).show()
        refreshDriveFolderInBackground(folderId, true)
        return
    }
    Toast.makeText(this, "Memuat folder Drive...", Toast.LENGTH_SHORT).show()
    refreshDriveFolderInBackground(folderId, false)
}

private fun refreshDriveFolderInBackground(folderId: String, silent: Boolean) {
    val folderPageUrl = "https://drive.google.com/drive/folders/$folderId"
    Thread {
        try {
            val doc: Document = Jsoup.connect(folderPageUrl)
                .timeout(20000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "en-US,en;q=0.9")
                .maxBodySize(0)
                .get()
            val html = doc.html()
            val isiFolder = ArrayList<MediaItem>()
            val p = Pattern.compile("_DRIVE_ivd\\s*=\\s*'([^']+)'")
            val m = p.matcher(html)
            if (m.find()) {
                val raw = m.group(1)
                val json = raw.replace("\\x", "\\u00").replace("\\/", "/")
                try {
                    val root = JSONArray(json)
                    var files: JSONArray? = null
                    for (i in 0 until root.length()) {
                        val o = root.get(i)
                        if (o is JSONArray) {
                            if (o.length() > 0 && o.opt(0) is JSONArray) {
                                val candidate = o.get(0) as JSONArray
                                if (candidate.length() >= 3) { files = o; break }
                            }
                        }
                    }
                    if (files != null) {
                        for (i in 0 until files.length()) {
                            val entry = files.getJSONArray(i)
                            if (entry.length() < 3) continue
                            val fileId = entry.getString(0)
                            var title: String? = entry.getString(2)
                            val mime = if (entry.length() > 3) entry.optString(3, "") else ""
                            if (fileId == null || fileId.isEmpty()) continue
                            if (title == null) title = "File"
                            var type = MediaItem.TYPE_VIDEO
                            if (mime.startsWith("image/")) type = MediaItem.TYPE_IMAGE
                            else if (mime.startsWith("audio/")) type = MediaItem.TYPE_AUDIO
                            else if (mime.startsWith("video/")) type = MediaItem.TYPE_VIDEO
                            else {
                                val lower = title.lowercase(Locale.getDefault())
                                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                                    lower.endsWith(".png") || lower.endsWith(".webp") ||
                                    lower.endsWith(".gif")) type = MediaItem.TYPE_IMAGE
                                else if (lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                                    lower.endsWith(".m4a")) type = MediaItem.TYPE_AUDIO
                            }
                            val directUrl = "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
                            val mi = MediaItem(Uri.parse(directUrl), type)
                            mi.isLocal = false
                            mi.title = title
                            mi.thumbUrl = "https://drive.google.com/thumbnail?id=$fileId&sz=w400"
                            isiFolder.add(mi)
                        }
                    }
                } catch (ignoredJSON: Exception) {}
            }
            if (isiFolder.isEmpty()) {
                try {
                    val linksMain = doc.select("a[href]")
                    val seenMain = HashSet<String>()
                    for (link in linksMain) {
                        val href = link.attr("href")
                        val title = link.text()
                        var fId: String? = null
                        if (href.contains("/file/d/")) {
                            try { fId = href.split("/file/d/")[1].split("/")[0] } catch (ignored: Exception) {}
                        }
                        if (fId == null || fId.isEmpty() || seenMain.contains(fId)) continue
                        seenMain.add(fId)
                        var type = MediaItem.TYPE_VIDEO
                        val lower = title.lowercase(Locale.getDefault())
                        if (lower.contains(".jpg") || lower.contains(".jpeg") ||
                            lower.contains(".png") || lower.contains(".webp") ||
                            lower.contains(".gif")) type = MediaItem.TYPE_IMAGE
                        else if (lower.contains(".mp3") || lower.contains(".wav") ||
                            lower.contains(".m4a")) type = MediaItem.TYPE_AUDIO
                        val directUrl = "https://drive.usercontent.google.com/download?id=$fId&export=download&confirm=t"
                        val mi = MediaItem(Uri.parse(directUrl), type)
                        mi.isLocal = false
                        mi.title = title
                        mi.sourceTitle = "Drive"
                        mi.thumbUrl = "https://drive.google.com/thumbnail?id=$fId&sz=w400"
                        isiFolder.add(mi)
                    }
                } catch (ignoredDom: Exception) {}
            }
            if (isiFolder.isEmpty()) {
                try {
                    val doc2 = Jsoup.connect("https://drive.google.com/embeddedfolderview?id=$folderId#list")
                        .timeout(20000).userAgent("Mozilla/5.0").get()
                    val links = doc2.select("a[href]")
                    val seen = HashSet<String>()
                    for (link in links) {
                        val href = link.attr("href")
                        val title = link.text()
                        var fileId: String? = null
                        if (href.contains("/file/d/")) fileId = href.split("/file/d/")[1].split("/")[0]
                        else if (href.contains("/open?id=")) fileId = href.split("/open\\?id=")[1].split("&")[0]
                        else if (href.contains("id=")) fileId = href.split("id=")[1].split("&")[0]
                        if (fileId == null || seen.contains(fileId)) continue
                        seen.add(fileId)
                        var type = MediaItem.TYPE_VIDEO
                        val lower = title.lowercase(Locale.getDefault())
                        if (lower.contains(".jpg") || lower.contains(".jpeg") ||
                            lower.contains(".png") || lower.contains(".webp") ||
                            lower.contains(".gif")) type = MediaItem.TYPE_IMAGE
                        else if (lower.contains(".mp3") || lower.contains(".wav") ||
                            lower.contains(".m4a")) type = MediaItem.TYPE_AUDIO
                        val directUrl = "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
                        val mi = MediaItem(Uri.parse(directUrl), type)
                        mi.isLocal = false
                        mi.title = title
                        mi.thumbUrl = "https://drive.google.com/thumbnail?id=$fileId&sz=w400"
                        isiFolder.add(mi)
                    }
                } catch (ignoredFB: Exception) {}
            }
            runOnUiThread {
                if (isiFolder.isEmpty()) {
                    if (!silent) {
                        SoundHelper.error()
                        Toast.makeText(this@MainActivity, "Folder kosong atau tidak dapat diakses", Toast.LENGTH_LONG).show()
                    }
                } else {
                    if (!silent) SoundHelper.success()
                    DriveFolderCache.save(this@MainActivity, folderId, isiFolder)
                    ExternalMediaStore.gantiSemua(this@MainActivity, isiFolder, "Drive Folder")
                    activeSource = ExternalMediaStore.getSourceLabel(this@MainActivity)
                    renderFromExternalOnly()
                    if (silent) Toast.makeText(this@MainActivity, "Diperbarui: ${isiFolder.size} file", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                SoundHelper.error()
                Toast.makeText(this@MainActivity, "Gagal memuat folder: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}

private fun extractFolderId(url: String): String? {
    try {
        val p = Pattern.compile("/drive/folders/([a-zA-Z0-9_-]+)")
        val m = p.matcher(url)
        if (m.find()) return m.group(1)
    } catch (ignored: Exception) {}
    return null
}

private fun selectTab(index: Int) {
    currentTabIndex = index
    tvLabelBeranda?.setTextColor(0xFF727272.toInt())
    tvLabelSumber?.setTextColor(0xFF727272.toInt())
    tvLabelProfil?.setTextColor(0xFF727272.toInt())
    val dens = resources.displayMetrics.density
    val yNaik = -20f * dens
    val yNormal = 0f
    val bulletBeranda = findViewById<View>(R.id.bulletBeranda)
    if (bulletBeranda != null) {
        bulletBeranda.setBackgroundResource(if (index == 0) R.drawable.bg_nav_bullet_blue else 0)
        bulletBeranda.translationY = if (index == 0) yNaik else yNormal
    }
    val bulletSumber = findViewById<View>(R.id.bulletSumber)
    if (bulletSumber != null) {
        bulletSumber.setBackgroundResource(if (index == 1) R.drawable.bg_nav_bullet_blue else 0)
        bulletSumber.translationY = if (index == 1) yNaik else yNormal
    }
    val bulletProfil = findViewById<View>(R.id.bulletProfil)
    if (bulletProfil != null) {
        bulletProfil.setBackgroundResource(if (index == 2) R.drawable.bg_nav_bullet_blue else 0)
        bulletProfil.translationY = if (index == 2) yNaik else yNormal
    }
    val iconNonaktif = 0xFF727272.toInt()
    val iconAktif = 0xFFFFFFFF.toInt()
    tvIconBeranda?.setColorFilter(if (index == 0) iconAktif else iconNonaktif)
    tvIconSumber?.setColorFilter(if (index == 1) iconAktif else iconNonaktif)
    tvIconProfil?.setColorFilter(if (index == 2) iconAktif else iconNonaktif)
}

private fun switchToTab(index: Int) {
    if (index < 0 || index > 2) return
    selectTab(index)
    if (index == 0) {
        showContent(R.layout.content_beranda)
        if (!alreadySetup.contains(0)) {
            setupFilterTabs()
            setupSidebar()
            alreadySetup.add(0)
        }
        container?.post { try { renderBeranda() } catch (ignored: Exception) {} }
    } else if (index == 1) {
        showContent(R.layout.content_sumber)
        if (!alreadySetup.contains(1)) { setupSumberButtons(); alreadySetup.add(1) }
    } else if (index == 2) {
        showContent(R.layout.content_profil)
        if (!alreadySetup.contains(2)) { setupProfilButtons(); alreadySetup.add(2) }
    }
}

private fun focusTabButton(index: Int) {
    val target: View? = if (index == 0) btnBeranda else if (index == 1) btnSumber else btnProfil
    target?.requestFocus()
}

private fun showContent(layoutRes: Int) {
    var cached = cachedTabViews[layoutRes]
    if (cached == null) {
        cached = layoutInflater.inflate(layoutRes, container, false)
        cachedTabViews[layoutRes] = cached
    }
    val parent = cached.parent
    if (parent != null) (parent as ViewGroup).removeView(cached)
    container?.removeAllViews()
    container?.addView(cached)
}
private fun setupSumberButtons() {
    val switchSmart = container?.findViewById<Switch>(R.id.switchSmart)
    val ignoreSmart = booleanArrayOf(false)
    val ignoreOffline = booleanArrayOf(false)
    val switchOffline = container?.findViewById<Switch>(R.id.switchOffline)

    if (switchSmart != null) {
        val savedMode = getSharedPreferences("memecio_settings", MODE_PRIVATE).getString("smart_mode", "")
        smartMode = savedMode ?: ""
        val smartOnlineOn = "otomatis" == smartMode || SmartPlaylistHelper.MODE_KATEGORI == smartMode
        switchSmart.isChecked = smartOnlineOn
        updateSmartLabel()
        switchSmart.setOnCheckedChangeListener { _, isChecked ->
            if (ignoreSmart[0]) return@setOnCheckedChangeListener
            if (isChecked) {
                getSharedPreferences("memecio_settings", MODE_PRIVATE).edit().putBoolean("offline_enabled", false).apply()
                activeSource = "external"
                if (switchOffline != null && switchOffline.isChecked) {
                    ignoreOffline[0] = true
                    switchOffline.isChecked = false
                    ignoreOffline[0] = false
                }
                showSmartPlaylistDialog()
            } else {
                smartMode = ""; smartTarget = ""; activeCategory = ""
                getSharedPreferences("memecio_settings", MODE_PRIVATE).edit().putString("smart_mode", "").apply()
                updateSmartLabel()
                renderBeranda()
            }
        }
    }

    val btnDrive = container?.findViewById<View>(R.id.btnSumberDrive)
    val btnStreaming = container?.findViewById<View>(R.id.btnSumberStreaming)
    val btnPlaylistManual = container?.findViewById<View>(R.id.btnPlaylistManual)
    val btnServer = container?.findViewById<View>(R.id.btnSumberServer)
    val btnRiwayat = container?.findViewById<View>(R.id.btnRiwayat)
    val btnKembalikan = container?.findViewById<View>(R.id.btnKembalikan)

    if (switchOffline != null) {
        val prefsSw = getSharedPreferences("memecio_settings", MODE_PRIVATE)
        val offlineOn = prefsSw.getBoolean("offline_enabled", true)
        switchOffline.isChecked = offlineOn
        switchOffline.setOnCheckedChangeListener { _, isChecked ->
            if (ignoreOffline[0]) return@setOnCheckedChangeListener
            val prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE)
            prefs.edit().putBoolean("offline_enabled", isChecked).apply()
            if (isChecked) {
                smartMode = ""; smartTarget = ""; activeCategory = ""
                prefs.edit().putString("smart_mode", "").apply()
                if (switchSmart != null && switchSmart.isChecked) {
                    ignoreSmart[0] = true
                    switchSmart.isChecked = false
                    ignoreSmart[0] = false
                }
                updateSmartLabel()
                activeSource = "offline"
                prefs.edit().putString("last_source", "offline").apply()
                switchToTab(0)
                resolveActiveSourceAndLoad()
                showMediaScanDialog()
            } else {
                activeSource = "external"
                var last = prefs.getString("last_source", "drive")
                if (last == "offline") last = "drive"
                prefs.edit().putString("last_source", last).apply()
                switchToTab(0)
                resolveActiveSourceAndLoad()
            }
        }
    }
    btnDrive?.setOnClickListener {
        getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
            .putString("last_source", "drive").putBoolean("offline_enabled", false).apply()
        startActivity(Intent(this, DriveSourceActivity::class.java))
    }
    btnStreaming?.setOnClickListener {
        getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
            .putString("last_source", "streaming").putBoolean("offline_enabled", false).apply()
        startActivity(Intent(this, StreamingSourceActivity::class.java))
    }
    btnPlaylistManual?.setOnClickListener {
        startActivity(Intent(this, CustomPlaylistActivity::class.java))
    }
    btnServer?.setOnClickListener {
        getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
            .putString("last_source", "server").putBoolean("offline_enabled", false).apply()
        val intent = Intent(this, PinDialogActivity::class.java)
        intent.putExtra(PinDialogActivity.EXTRA_TARGET, PinDialogActivity.TARGET_SERVER)
        startActivity(intent)
    }
    if (btnKembalikan != null) {
        val jumlah = HiddenMediaStore.getCount()
        setGroupText(btnKembalikan, "Kembalikan Media ($jumlah)")
        btnKembalikan.setOnClickListener {
            try { startActivity(Intent(this, HiddenMediaListActivity::class.java)) }
            catch (e: Exception) { Toast.makeText(this, "Gagal buka: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }
    btnRiwayat?.setOnClickListener {
        val intent = Intent(this, PinDialogActivity::class.java)
        intent.putExtra(PinDialogActivity.EXTRA_TARGET, PinDialogActivity.TARGET_RIWAYAT)
        startActivity(intent)
    }
}

private fun resolveActiveSourceAndLoad() {
    if ("offline" == activeSource) checkPermissionAndLoad()
    else if ("custom" == activeSource) renderBeranda()
    else renderFromExternalOnly()
}

private fun renderFromExternalOnly() {
    combinedMedia.clear()
    combinedMedia.addAll(ExternalMediaStore.getAll(this))
    renderBeranda()
    refreshSidebar()
}

private fun checkPermissionAndLoad() {
    val perms: Array<String> = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO")
    } else {
        arrayOf("android.permission.READ_EXTERNAL_STORAGE")
    }
    var allGranted = true
    for (p in perms) {
        if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) { allGranted = false; break }
    }
    if (!allGranted) requestPermissions(perms, REQ_STORAGE_PERMISSION)
    else loadOfflineMedia()
}

override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQ_STORAGE_PERMISSION) {
        var granted = grantResults.isNotEmpty()
        for (r in grantResults) { if (r != PackageManager.PERMISSION_GRANTED) granted = false }
        if (granted) loadOfflineMedia()
        else { Toast.makeText(this, "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show(); renderBeranda() }
    }
}

private fun extractFolderName(dataPath: String?): String? {
    try {
        if (dataPath.isNullOrEmpty()) return null
        val slash = dataPath.lastIndexOf('/')
        if (slash < 0) return null
        val parent = dataPath.substring(0, slash)
        val slash2 = parent.lastIndexOf('/')
        if (slash2 < 0) return parent
        val name = parent.substring(slash2 + 1)
        if (name.isEmpty()) return "Root"
        return name
    } catch (ignored: Exception) {}
    return null
}

private fun updatePrivacyLabel(btnPrivacy: View?) {
    try {
        if (btnPrivacy is ImageButton) {
            btnPrivacy.setImageResource(R.drawable.ic_lock_w)
            btnPrivacy.alpha = if (PrivacyStore.isEnabled()) 1.0f else 0.35f
        }
    } catch (ignored: Exception) {}
}

private fun updateSmartLabel() {
    try {
        val tv = container?.findViewById<TextView>(R.id.tvSmartMode) ?: return
        var label = "Semua"
        if (SmartPlaylistHelper.MODE_KATEGORI == smartMode) label = "Kategori"
        else if (SmartPlaylistHelper.MODE_FOLDER == smartMode) label = "Folder"
        else if (SmartPlaylistHelper.MODE_DATE == smartMode) label = "Tanggal"
        else if (SmartPlaylistHelper.MODE_SIZE == smartMode) label = "Ukuran"
        else if (SmartPlaylistHelper.MODE_DURATION == smartMode) label = "Durasi"
        if (smartTarget.isNotEmpty()) label = "$label: $smartTarget"
        tv.text = label
    } catch (ignored: Exception) {}
}

private fun loadOfflineMedia() {
    localMedia.clear()
    val imgCursor: Cursor? = contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATA),
        null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")
    var countImg = 0
    if (imgCursor != null) {
        while (imgCursor.moveToNext()) {
            val id = imgCursor.getLong(0)
            val date = imgCursor.getLong(1)
            val sz = imgCursor.getLong(2)
            val dataPath = imgCursor.getString(3)
            val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            val item = MediaItem(uri, MediaItem.TYPE_IMAGE)
            item.isLocal = true; item.dateAdded = date; item.size = sz
            item.folderPath = extractFolderName(dataPath)
            localMedia.add(item); countImg++
        }
        imgCursor.close()
    }
    debugLogMain("Scan IMAGE: $countImg")

    val vidCursor: Cursor? = contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATA),
        null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
    var countVid = 0
    if (vidCursor != null) {
        while (vidCursor.moveToNext()) {
            val id = vidCursor.getLong(0)
            val date = vidCursor.getLong(1)
            val sz = vidCursor.getLong(2)
            val dur = vidCursor.getLong(3)
            val dataPath = vidCursor.getString(4)
            val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
            val item = MediaItem(uri, MediaItem.TYPE_VIDEO)
            item.isLocal = true; item.dateAdded = date; item.size = sz; item.duration = dur
            item.folderPath = extractFolderName(dataPath)
            localMedia.add(item); countVid++
        }
        vidCursor.close()
    }
    debugLogMain("Scan VIDEO: $countVid")
    try { MediaScanCache.markScanned(this) } catch (ignored: Exception) {}

    try {
        val audCursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE),
            null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")
        if (audCursor != null) {
            while (audCursor.moveToNext()) {
                val id = audCursor.getLong(0)
                val title = audCursor.getString(1)
                val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                val item = MediaItem(uri, 4)
                item.isLocal = true; item.title = title
                localMedia.add(item)
            }
            audCursor.close()
        }
    } catch (ignored: Exception) {}

    try {
        val hidden = HiddenMediaStore.scanHidden(this)
        localMedia.addAll(hidden)
    } catch (ignored: Exception) {}

    combinedMedia.clear()
    combinedMedia.addAll(localMedia)
    var nImg = 0; var nVid = 0; var nAud = 0
    for (m in combinedMedia) {
        if (m.type == MediaItem.TYPE_IMAGE) nImg++
        else if (m.type == MediaItem.TYPE_VIDEO) nVid++
        else if (m.type == 4) nAud++
    }
    debugLogMain("Setelah combinedMedia: img=$nImg vid=$nVid aud=$nAud total=${combinedMedia.size}")
    renderBeranda()
    refreshSidebar()
}

private fun setupFilterTabs() {
    val btnSemua = container?.findViewById<View>(R.id.btnFilterSemua) ?: return
    val btnFoto = container?.findViewById<View>(R.id.btnFilterFoto)
    val btnVideo = container?.findViewById<View>(R.id.btnFilterVideo)
    categoryChipsContainer = container?.findViewById(R.id.categoryChipsContainer)
    categoryChipScroll = container?.findViewById(R.id.categoryChipScroll)
    btnSemua.setOnClickListener { currentFilter = 0; updateFilterTabsUI(); renderBeranda() }
    btnFoto?.setOnClickListener { currentFilter = 1; updateFilterTabsUI(); renderBeranda() }
    btnVideo?.setOnClickListener { currentFilter = 2; updateFilterTabsUI(); renderBeranda() }
    updateFilterTabsUI()
    setupMultiviewButton()
    setupSearch()
    kategoriList = container?.findViewById(R.id.kategoriList)
    scrollKategori = container?.findViewById(R.id.scrollKategori)
    overlayExpand = container?.findViewById(R.id.overlayExpand)
    gridExpand = container?.findViewById(R.id.gridExpand)
    tvExpandTitle = container?.findViewById(R.id.tvExpandTitle)
    btnShrink = container?.findViewById(R.id.btnShrink)
    btnShrink?.setOnClickListener { SoundHelper.click(); shrinkKategori() }
    gridBeranda = container?.findViewById(R.id.gridBeranda)
}

private fun setupMultiviewButton() {
    val btnMulti = container?.findViewById<View>(R.id.btnMultiview)
    btnMulti?.setOnClickListener { startActivity(Intent(this, MultiviewActivity::class.java)) }
    if (true) return
}

private fun setupSearch() {
    etSearch = container?.findViewById(R.id.etSearch)
    historyChipsContainer = container?.findViewById(R.id.historyChipsContainer)
    val et = etSearch ?: return
    loadSearchHistory()
    et.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            searchQuery = s?.toString()?.trim() ?: ""
            searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
            searchDebounceRunnable = Runnable { renderBeranda() }
            searchDebounceHandler.postDelayed(searchDebounceRunnable!!, 300)
        }
    })
    val btnSearch = container?.findViewById<ImageButton>(R.id.btnSearch)
    btnSearch?.setOnClickListener {
        searchQuery = etSearch?.text?.toString()?.trim() ?: ""
        if (searchQuery.isNotEmpty()) saveSearchHistory(searchQuery)
        renderBeranda()
    }
}

private fun saveSearchHistory(query: String) {
    val prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE)
    var history = prefs.getString(KEY_SEARCH_HISTORY, "") ?: ""
    if (!history.contains("$query|")) {
        history = "$query|$history"
        val items = history.split("\\|".toRegex()).toTypedArray()
        val sb = StringBuilder()
        var count = 0
        for (item in items) {
            if (item.isNotEmpty() && count < 10) {
                if (sb.isNotEmpty()) sb.append("|")
                sb.append(item); count++
            }
        }
        prefs.edit().putString(KEY_SEARCH_HISTORY, sb.toString()).apply()
        loadSearchHistory()
    }
}

private fun loadSearchHistory() {
    val hcc = historyChipsContainer ?: return
    hcc.removeAllViews()
    val prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE)
    val history = prefs.getString(KEY_SEARCH_HISTORY, "") ?: ""
    if (history.isEmpty()) return
    val items = history.split("\\|".toRegex()).toTypedArray()
    for (item in items) {
        if (item.isEmpty()) continue
        val keyword = item
        val chip = TextView(this)
        chip.text = item; chip.textSize = 12f
        chip.setPadding(24, 12, 24, 12)
        chip.setTextColor(0xFF1C1C1E.toInt())
        chip.setBackgroundResource(R.drawable.bg_glass_button)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 8, 0)
        hcc.addView(chip, params)
        chip.setOnClickListener { etSearch?.setText(keyword); etSearch?.setSelection(keyword.length) }
    }
}
    private fun applySort(list: MutableList<MediaItem>) {
        val prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE)
        val mode = prefs.getString(KEY_SORT_MODE, "tanggal_baru")
        val comp: Comparator<MediaItem> = when (mode) {
            "tanggal_baru" -> Comparator { a, b -> b.dateAdded.compareTo(a.dateAdded) }
            "nama_desc" -> Comparator { a, b -> (b.title ?: "").compareTo(a.title ?: "", ignoreCase = true) }
            "tanggal_desc" -> Comparator { a, b -> (b.uri?.toString() ?: "").compareTo(a.uri?.toString() ?: "") }
            else -> Comparator { a, b -> (a.title ?: "").compareTo(b.title ?: "", ignoreCase = true) }
        }
        Collections.sort(list, comp)
    }

    private fun updateFilterTabsUI() {
        val btnSemua = container?.findViewById<View>(R.id.btnFilterSemua) ?: return
        applyFilterStyle(btnSemua, currentFilter == 0)
        applyFilterStyle(container?.findViewById(R.id.btnFilterFoto), currentFilter == 1)
        applyFilterStyle(container?.findViewById(R.id.btnFilterVideo), currentFilter == 2)
    }

    private fun applyFilterStyle(btn: View?, isActive: Boolean) {
        if (btn == null) return
        try {
            val bgRes = if (isActive) R.drawable.bg_glass_button_selected else R.drawable.bg_glass_button
            btn.setBackgroundResource(bgRes)
        } catch (ignored: Exception) {}
        if (btn is TextView) btn.setTextColor(if (isActive) 0xFFFFFFFF.toInt() else resources.getColor(R.color.text_dark))
        else if (btn is ImageButton) try { btn.setColorFilter(if (isActive) 0xFFFFFFFF.toInt() else 0xFF1C1C1E.toInt()) } catch (ignored: Exception) {}
    }

    private fun setupSidebar() {
        sidebarContainer = container?.findViewById(R.id.sidebarContainer)
        sidebarList = container?.findViewById(R.id.sidebarSavedLinks)
        btnToggleSidebar = container?.findViewById(R.id.btnToggleSidebar)
        val rightContentContainer = container?.findViewById<View>(R.id.rightContentContainer)
        val sl = sidebarList ?: return
        val sc = sidebarContainer ?: return
        sidebarOpen = false
        sc.visibility = View.GONE
        sidebarAdapter = object : ArrayAdapter<MediaItem>(this, 0, sidebarSavedItems) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var view = convertView
                if (view == null) view = layoutInflater.inflate(R.layout.item_playlist_row, parent, false)
                val entry = getItem(position)
                val tv = view.findViewById<TextView>(R.id.tvPlaylistTitle)
                val judul = if (entry?.title.isNullOrEmpty()) entry?.uri?.toString() ?: "(tanpa judul)" else entry?.title ?: ""
                tv.text = judul
                tv.isSelected = true
                view.setOnClickListener { entry?.let { loadPlaylist(it) }; toggleSidebar(false) }
                val btnRename = view.findViewById<TextView>(R.id.btnRenamePlaylist)
                btnRename?.setOnClickListener { entry?.let { tampilkanDialogRename(it) } }
                val btnHapus = view.findViewById<TextView>(R.id.btnHapusPlaylist)
                btnHapus?.setOnClickListener { entry?.let { konfirmasiHapusPlaylist(it) } }
                return view
            }
        }
        sl.adapter = sidebarAdapter
        btnToggleSidebar?.setOnClickListener { toggleSidebar(!sidebarOpen) }
        rightContentContainer?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && sidebarOpen) toggleSidebar(false)
            false
        }
        container?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && sidebarOpen) toggleSidebar(false)
            false
        }
        refreshSidebar()
    }

    private fun toggleSidebar(show: Boolean) {
        val sc = sidebarContainer ?: return
        if (sidebarOpen == show) return
        sidebarOpen = show
        val targetX = if (show) 0f else -sc.width.toFloat()
        sc.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofFloat(sc, "translationX", sc.translationX, targetX)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { if (!sidebarOpen) sc.visibility = View.GONE }
        })
        animator.start()
    }

    private fun tampilkanDialogRename(entry: MediaItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_input_judul)
        val etJudul = dialog.findViewById<EditText>(R.id.etJudulPopup)
        val btnSimpan = dialog.findViewById<View>(R.id.btnTampilkanPopup)
        val btnBatal = dialog.findViewById<View>(R.id.btnBatalPopup)
        val judulLama = entry.title ?: ""
        etJudul?.setText(judulLama); etJudul?.setSelection(judulLama.length)
        btnSimpan?.let {
            if (it is TextView) it.text = "Simpan"
            it.setOnClickListener {
                val namaBaru = etJudul?.text?.toString()?.trim() ?: ""
                if (namaBaru.isEmpty()) { Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                SavedLinksStore.updateTitle(this, entry.uri.toString(), namaBaru)
                refreshSidebar()
                Toast.makeText(this, "Nama diubah", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        btnBatal?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun konfirmasiHapusPlaylist(entry: MediaItem) {
        val judul = if (entry.title.isNullOrEmpty()) "playlist ini" else entry.title
        AlertDialog.Builder(this)
            .setTitle("Hapus Playlist")
            .setMessage("Apakah anda ingin menghapus \"$judul\"?")
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Hapus") { _, _ ->
                SavedLinksStore.hapus(this, entry.uri.toString())
                refreshSidebar()
                Toast.makeText(this, "Playlist dihapus", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun loadPlaylist(playlistItem: MediaItem) {
        val url = playlistItem.uri.toString()
        if (url.contains("/drive/folders/")) { openDriveFolder(url); return }
        Toast.makeText(this, "Memuat playlist...", Toast.LENGTH_SHORT).show()
        M3uParser.parseAsync(this, url, object : M3uParser.Callback {
            override fun onSuccess(entries: MutableList<M3uParser.MediaEntry>?) {
                val mediaItems = ArrayList<MediaItem>()
                if (entries == null || entries.isEmpty()) {
                    val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO)
                    mi.isLocal = false; mediaItems.add(mi)
                } else {
                    for (e in entries) {
                        val mi = MediaItem(Uri.parse(e.url), MediaItem.TYPE_VIDEO)
                        mi.sourceTitle = e.groupTitle; mi.isLocal = false
                        mi.title = e.title; mi.thumbUrl = e.thumbUrl; mi.isM3u = true
                        mediaItems.add(mi)
                    }
                }
                ExternalMediaStore.gantiSemua(this@MainActivity, mediaItems, playlistItem.title)
                activeSource = ExternalMediaStore.getSourceLabel(this@MainActivity)
                SoundHelper.success()
                renderFromExternalOnly()
            }
            override fun onError(message: String?) {
                val mediaItems = ArrayList<MediaItem>()
                val mi = MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO)
                mi.isLocal = false; mediaItems.add(mi)
                ExternalMediaStore.gantiSemua(this@MainActivity, mediaItems, playlistItem.title)
                activeSource = ExternalMediaStore.getSourceLabel(this@MainActivity)
                SoundHelper.error()
                Toast.makeText(this@MainActivity, "Gagal parsing: $message", Toast.LENGTH_LONG).show()
                renderFromExternalOnly()
            }
        })
    }

    private fun refreshSidebar() {
        val sa = sidebarAdapter ?: return
        sidebarSavedItems.clear()
        sidebarSavedItems.addAll(SavedLinksStore.getAll(this))
        sa.notifyDataSetChanged()
    }

    private fun setGroupText(v: View?, text: String) {
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                val c = v.getChildAt(i)
                if (c is TextView) { c.text = text; return }
            }
        }
    }

    private fun logCrashToFile(t: Throwable) {
        try {
            var dir = getExternalFilesDir(null)
            if (dir == null) dir = filesDir
            val f = File(dir, "memecio_crash.txt")
            val fw = FileWriter(f, true)
            fw.write("\n=== ${Date()} ===\n")
            val sw = StringWriter()
            t.printStackTrace(PrintWriter(sw))
            fw.write(sw.toString())
            fw.close()
        } catch (ignored: Exception) {}
    }

    private fun applyTvFocusRingsIfTv() {}

    private fun applyDisplayModeSafe() {
        try { applyDisplayMode() } catch (t: Throwable) { logCrashToFile(t) }
    }

    private fun applyImmersiveIfLandscape() {
        try {
            val orient = resources.configuration.orientation
            if (orient == Configuration.ORIENTATION_LANDSCAPE) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val lp = window.attributes
                    lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    window.attributes = lp
                }
                if (Build.VERSION.SDK_INT >= 30) {
                    val c = window.insetsController
                    if (c != null) {
                        c.hide(WindowInsets.Type.statusBars())
                        c.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
                }
            } else {
                if (Build.VERSION.SDK_INT >= 30) window.insetsController?.show(WindowInsets.Type.statusBars())
                else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        } catch (ignored: Exception) {}
    }

    private fun applyDisplayMode() {
        val effectiveMode = DisplayModeStore.getEffectiveMode(this)
        val currentOrientation = resources.configuration.orientation
        val requested = requestedOrientation
        if (effectiveMode == "tv") {
            if (currentOrientation != Configuration.ORIENTATION_LANDSCAPE || requested != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            if (requested != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        applyTvFocusRingsIfTv()
    }

    private fun updateModeButton(btnMode: View?) {
        val mode = DisplayModeStore.getMode(this)
        val label = DisplayModeStore.getModeLabel(mode)
        var suffix = ""
        if (DisplayModeStore.MODE_AUTO == mode) {
            val effective = DisplayModeStore.getEffectiveMode(this)
            suffix = " (" + (if (DisplayModeStore.MODE_TV == effective) "TV" else "HP") + ")"
        }
        setGroupText(btnMode, "Mode Tampilan: $label$suffix")
    }

    private fun setupProfilButtons() {
        val btnMode = container?.findViewById<View>(R.id.btnModeTampilan)
        val btnPencarianOnline = container?.findViewById<View>(R.id.btnPencarianOnline)
        val btnStatistik = container?.findViewById<View>(R.id.btnStatistik)
        val btnInfoAplikasi = container?.findViewById<View>(R.id.btnInfoAplikasi)
        val tvVersiBuild = container?.findViewById<TextView>(R.id.tvVersiBuild)
        tvVersiBuild?.let {
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                it.text = "Build: ${fmt.format(Date(pInfo.lastUpdateTime))}"
            } catch (e: Exception) { it.text = "Build: -" }
        }
        val tvVersiApp = container?.findViewById<TextView>(R.id.tvVersiApp)
        tvVersiApp?.let {
            try {
                val pInfo2 = packageManager.getPackageInfo(packageName, 0)
                it.text = "Memec.io v${pInfo2.versionName}"
            } catch (e: Exception) { it.text = "Memec.io" }
        }
        val btnBackupLengkap = container?.findViewById<View>(R.id.btnBackupLengkap)
        val btnLogCrash = container?.findViewById<View>(R.id.btnLogCrash)
        val btnDevTools = container?.findViewById<View>(R.id.btnDevTools)
        val btnAutoExit = container?.findViewById<View>(R.id.btnAutoExit)
        btnMode?.let {
            updateModeButton(it)
            it.setOnClickListener {
                val current = DisplayModeStore.getMode(this)
                val next = DisplayModeStore.nextMode(current)
                DisplayModeStore.setMode(this, next)
                updateModeButton(it)
                applyDisplayMode()
            }
        }
        val btnPrivacy = container?.findViewById<View>(R.id.btnPrivacyToggle)
        btnPrivacy?.let {
            updatePrivacyLabel(it)
            it.setOnClickListener {
                val newState = PrivacyStore.toggle()
                updatePrivacyLabel(it)
                try {
                    if (newState) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } catch (ignored: Exception) {}
                Toast.makeText(this, "FLAG SECURE " + (if (newState) "ON" else "OFF"), Toast.LENGTH_SHORT).show()
            }
        }
        container?.findViewById<View>(R.id.btnDebugMulti)?.setOnClickListener { showMultiSourceDebug() }
        val btnGridColumns = container?.findViewById<View>(R.id.btnGridColumns)
        btnGridColumns?.let {
            setGroupText(it, "Kolom Grid: ${GridColumnsStore.label(this)}")
            it.setOnClickListener {
                val labels = arrayOf("Auto","1 Kolom","2 Kolom","3 Kolom","4 Kolom","5 Kolom","6 Kolom","7 Kolom","8 Kolom")
                AlertDialog.Builder(this).setTitle("Jumlah Kolom Grid").setItems(labels) { _, which ->
                    val v = if (which == 0) GridColumnsStore.AUTO else which
                    GridColumnsStore.set(this, v)
                    setGroupText(it, "Kolom Grid: ${GridColumnsStore.label(this)}")
                    if (gridBeranda != null) {
                        val cols = GridColumnsStore.get(this)
                        if (cols > 0) gridBeranda?.numColumns = cols
                    }
                    Toast.makeText(this, "Kolom Grid: ${GridColumnsStore.label(this)}", Toast.LENGTH_SHORT).show()
                }.show()
            }
        }
        btnPencarianOnline?.setOnClickListener {
            Toast.makeText(this, "Ketuk tab Beranda untuk mencari konten", Toast.LENGTH_LONG).show()
            switchToTab(0); renderFromExternalOnly()
        }
        btnStatistik?.setOnClickListener { startActivity(Intent(this, StatistikActivity::class.java)) }
        btnInfoAplikasi?.setOnClickListener {
            startActivity(Intent(this, AppInfoActivity::class.java))
        }
        btnBackupLengkap?.setOnClickListener { showBackupChoiceDialog() }
        btnLogCrash?.setOnClickListener { startActivity(Intent(this, CrashLogActivity::class.java)) }
        btnAutoExit?.setOnClickListener { showAutoExitDialog() }
        if (btnDevTools != null) {
            if (DeveloperModeStore.isEnabled(this)) {
                btnDevTools.visibility = View.VISIBLE
                btnDevTools.setOnClickListener { showDevToolsDialog() }
            } else btnDevTools.visibility = View.GONE
        }
    }

    private fun showBackupChoiceDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_backup_choice)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<View>(R.id.btnChoiceBackupAman).setOnClickListener {
            dialog.dismiss(); checkPermissionAndBackupAman()
        }
        dialog.findViewById<View>(R.id.btnChoiceEkspor).setOnClickListener {
            dialog.dismiss(); BackupRestoreHelper.exportPlaylists(this)
        }
        dialog.findViewById<View>(R.id.btnChoiceImpor).setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("Impor JSON")
                .setMessage("Impor data dari file backup terbaru?\nData lama tetap ada, hanya ditambahkan.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Impor") { _, _ -> BackupRestoreHelper.importPlaylists(this) }
                .show()
        }
        dialog.findViewById<View>(R.id.btnBatalBackupChoice).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun checkPermissionAndBackupAman() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                    Toast.makeText(this, "Izinkan akses semua file, lalu tekan Backup lagi", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
                return
            }
        }
        ProjectExportHelper.export(this)
    }

    private fun showDevToolsDialog() {
        val all = SecretCodeRegistry.getAll()
        val items = Array(all.size) { i ->
            val c = all[i]
            c.code + " - " + c.title + (if (c.hidden) " [hidden]" else "")
        }
        AlertDialog.Builder(this)
            .setTitle("Developer Tools")
            .setItems(items) { _, which -> handleDevCode(all[which].code) }
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
                "888" -> startActivity(Intent(this, StatistikActivity::class.java))
                "999" -> ProjectExportHelper.export(this)
                "123" -> CacheResetter.confirmAndReset(this)
                "456" -> RepairDatabaseHelper.confirmAndRepair(this)
                "789" -> FactoryResetHelper.confirmAndReset(this)
                "101" ->  { DeveloperModeStore.toggle(this); recreate() }
                "103" -> TestGestureHelper.showGuide(this)
                "104" -> TestModesHelper.showChoice(this)
                "102" -> throw RuntimeException("Force crash via Dev Tools")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAutoExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_auto_exit)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.findViewById<View>(R.id.btnExit5)?.setOnClickListener { AutoExitManager.getInstance().start(this, 5); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit15)?.setOnClickListener { AutoExitManager.getInstance().start(this, 15); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit30)?.setOnClickListener { AutoExitManager.getInstance().start(this, 30); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit60)?.setOnClickListener { AutoExitManager.getInstance().start(this, 60); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExit120)?.setOnClickListener { AutoExitManager.getInstance().start(this, 120); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnExitCancel)?.setOnClickListener {
            AutoExitManager.getInstance().cancel(this)
            Toast.makeText(this, "Auto Exit dimatikan", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btnExitBatal)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showMultiSourceDebug() {
        try {
            val sb = StringBuilder()
            val ids = MultiSourceStore.listSourceIds(this)
            sb.append("Total sources: ${ids.size}\n\n")
            if (ids.isEmpty()) sb.append("(Kosong - belum ada sumber tersimpan)\n")
            var idx = 1
            for (id in ids) {
                val label = MultiSourceStore.getLabel(this, id)
                val items = MultiSourceStore.getBySource(this, id)
                sb.append("[$idx] $id\n")
                sb.append("    Label: $label\n")
                sb.append("    Items: ${items.size}\n\n")
                idx++
            }
            val debugText = sb.toString()
            val view = layoutInflater.inflate(R.layout.dialog_debug_multi, null)
            view.findViewById<TextView>(R.id.tvDebugContent).text = debugText
            val dialog = AlertDialog.Builder(this).setView(view).setCancelable(true).create()
            view.findViewById<Button>(R.id.btnDebugSalin)?.setOnClickListener {
                try {
                    val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cb.setPrimaryClip(ClipData.newPlainText("Debug Multi", debugText))
                    Toast.makeText(this, "Tersalin", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {}
            }
            view.findViewById<Button>(R.id.btnDebugExit)?.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSmartPlaylistDialog() {
        val dens = resources.displayMetrics.density
        val pad = (16 * dens).toInt()
        val cont = LinearLayout(this)
        cont.orientation = LinearLayout.VERTICAL
        cont.setPadding(pad, pad, pad, pad)
        val desc = TextView(this)
        desc.text = "Smart Playlist otomatis mengelompokkan video berdasarkan Riwayat, Populer, Terbaru, dan Kategori."
        desc.textSize = 12f
        desc.setTextColor(0xFF8A8A8E.toInt())
        desc.setPadding(0, 0, 0, (16 * dens).toInt())
        cont.addView(desc)
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        val tvLabel = TextView(this)
        tvLabel.text = "Aktifkan Smart Playlist"
        tvLabel.textSize = 13f
        tvLabel.setTextColor(0xFF1C1C1E.toInt())
        tvLabel.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(tvLabel)
        val swSmart = Switch(this)
        swSmart.isChecked = "otomatis" == smartMode || SmartPlaylistHelper.MODE_KATEGORI == smartMode
        row.addView(swSmart)
        cont.addView(row)
        AlertDialog.Builder(this)
            .setTitle("Smart Playlist (Online)")
            .setView(cont)
            .setCancelable(true)
            .setPositiveButton("Terapkan") { _, _ ->
                if (swSmart.isChecked) {
                    smartMode = "otomatis"; activeSource = "external"
                    smartTarget = ""; activeCategory = ""; saveAndRender()
                } else {
                    smartMode = ""; smartTarget = ""; activeCategory = ""
                    getSharedPreferences("memecio_settings", MODE_PRIVATE).edit().putString("smart_mode", "").apply()
                    updateSmartLabel()
                    findViewById<Switch>(R.id.switchSmart)?.isChecked = false
                    renderBeranda()
                }
            }
            .setNegativeButton("Tutup") { _, _ ->
                if (smartMode.isEmpty()) findViewById<Switch>(R.id.switchSmart)?.isChecked = false
            }
            .show()
    }

    private fun saveAndRender() {
        getSharedPreferences("memecio_settings", MODE_PRIVATE).edit().putString("smart_mode", smartMode).apply()
        updateSmartLabel()
        renderBeranda()
    }

    private fun showMediaScanDialog() {
        val dens = resources.displayMetrics.density
        val pad = (16 * dens).toInt()
        val cont = LinearLayout(this)
        cont.orientation = LinearLayout.VERTICAL
        cont.setPadding(pad, pad, pad, pad)
        val swMediaScan = Switch(this)
        swMediaScan.isChecked = getSharedPreferences("memecio_settings", MODE_PRIVATE).getBoolean("folder_scan_enabled", false)
        val tvLbl = TextView(this)
        tvLbl.text = "Aktifkan Media Scan"; tvLbl.textSize = 13f; tvLbl.setTextColor(0xFF1C1C1E.toInt())
        cont.addView(tvLbl); cont.addView(swMediaScan)
        AlertDialog.Builder(this)
            .setTitle("Media Scan (Offline)")
            .setView(cont)
            .setPositiveButton("Terapkan") { _, _ ->
                if (swMediaScan.isChecked) {
                    folderModeEnabled = true; selectedFolder = null
                    getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
                        .putBoolean("folder_scan_enabled", true).putBoolean("offline_enabled", true).apply()
                    renderBeranda()
                } else {
                    folderModeEnabled = false
                    getSharedPreferences("memecio_settings", MODE_PRIVATE).edit().putBoolean("folder_scan_enabled", false).apply()
                    renderBeranda()
                }
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun buildCategoryChips() {
        val ccc = categoryChipsContainer ?: return
        val ccs = categoryChipScroll ?: return
        ccc.removeAllViews()
        val counts = LinkedHashMap<String, Int>()
        for (m in combinedMedia) {
            val judul = m.title ?: m.uri.toString()
            val cat = CategoryHelper.deteksiKategoriUtama(judul)
            if (cat == null || cat.isEmpty() || "Lainnya" == cat) continue
            val c = counts[cat]; counts[cat] = if (c == null) 1 else c + 1
        }
        if (counts.isEmpty()) { ccs.visibility = View.GONE; return }
        ccs.visibility = View.VISIBLE
        for ((cat, count) in counts) {
            val chip = TextView(this)
            chip.text = "$cat ($count)"; chip.textSize = 11f
            chip.setPadding(24, 12, 24, 12)
            chip.setTextColor(0xFF1C1C1E.toInt())
            chip.setBackgroundResource(R.drawable.bg_glass_button)
            chip.setOnClickListener {
                activeCategory = if (cat == activeCategory) "" else cat
                buildCategoryChips(); renderBeranda()
            }
            ccc.addView(chip)
        }
    }

    private fun renderFolderList() {
        val kl = kategoriList ?: return
        kl.removeAllViews()
        val counts = LinkedHashMap<String, Int>()
        for (m in combinedMedia) {
            val f = m.folderPath ?: "Root"
            val c = counts[f]; counts[f] = if (c == null) 1 else c + 1
        }
        if (counts.isEmpty()) return
        for ((folder, count) in counts) addFolderRow(folder, count)
    }

    private fun addFolderRow(folderName: String, count: Int) {
        val kl = kategoriList ?: return
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(16, 20, 16, 20)
        row.isClickable = true; row.isFocusable = true
        row.setBackgroundResource(R.drawable.bg_glass_card)
        val name = TextView(this)
        name.text = "$folderName ($count)"; name.textSize = 14f
        name.setTextColor(0xFF1C1C1E.toInt())
        row.addView(name)
        row.setOnClickListener { selectedFolder = folderName; renderBeranda() }
        kl.addView(row)
    }

    private fun renderKategori(items: List<MediaItem>) {
        val kl = kategoriList ?: return
        kl.removeAllViews()
        if (items.isEmpty()) return
        val groups = LinkedHashMap<String, MutableList<MediaItem>>()
        for (m in items) {
            val judul = m.title ?: m.uri.toString()
            val cats = CategoryHelper.deteksiKategori(judul) ?: arrayListOf("Lainnya")
            for (cat in cats) {
                if (!groups.containsKey(cat)) groups[cat] = ArrayList()
                groups[cat]?.add(m)
            }
        }
        for ((key, value) in groups) addKategoriRow(key, value)
    }

    private fun addKategoriRow(judul: String, items: List<MediaItem>) {
        val kl = kategoriList ?: return
        val header = TextView(this)
        header.text = "$judul (${items.size})"
        header.textSize = 14f
        header.setTypeface(null, Typeface.BOLD)
        header.setTextColor(0xFF1C1C1E.toInt())
        header.setPadding(16, 18, 16, 6)
        kl.addView(header)
        val hsv = HorizontalScrollView(this)
        val rowLayout = LinearLayout(this)
        rowLayout.orientation = LinearLayout.HORIZONTAL
        for (item in items.take(30)) {
            val tv = TextView(this)
            tv.text = item.title ?: "Video"
            tv.textSize = 11f
            tv.setPadding(16, 10, 16, 10)
            tv.setTextColor(0xFF1C1C1E.toInt())
            tv.setBackgroundResource(R.drawable.bg_glass_card)
            tv.setOnClickListener {
                val idx = combinedMedia.indexOfFirst { it.uri == item.uri }
                if (idx >= 0) {
                    MixedPlaylistHolder.set(combinedMedia, idx)
                    if (item.type == MediaItem.TYPE_VIDEO) {
                        val vp = ArrayList<String>()
                        combinedMedia.forEach { if (it.type == MediaItem.TYPE_VIDEO) vp.add(it.uri.toString()) }
                        PlaylistHolder.set(vp)
                        val intent = Intent(this, VideoPlayerActivity::class.java)
                        intent.putExtra("index", idx)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, PreviewImageActivity::class.java)
                        intent.putExtra("index", idx)
                        startActivity(intent)
                    }
                }
            }
            rowLayout.addView(tv)
        }
        hsv.addView(rowLayout)
        kl.addView(hsv)
    }

    private fun expandKategori(judul: String, items: List<MediaItem>) {
        val oe = overlayExpand ?: return
        tvExpandTitle?.text = judul
        oe.visibility = View.VISIBLE
    }

    private fun shrinkKategori() {
        overlayExpand?.visibility = View.GONE
    }

    private fun renderSmartGroups(items: List<MediaItem>) {
        renderKategori(items)
    }

    private fun renderBeranda() {
        val emptyState = container?.findViewById<View>(R.id.emptyState)
        val grid = container?.findViewById<GridView>(R.id.gridBeranda) ?: return
        val offlineOn = getSharedPreferences("memecio_settings", MODE_PRIVATE).getBoolean("offline_enabled", true)
        val smartOn = smartMode.isNotEmpty()
        val isOfflineSource = "offline" == activeSource
        if (isOfflineSource && !offlineOn && !smartOn) {
            emptyState?.visibility = View.VISIBLE
            grid.visibility = View.GONE
            return
        }
        val userCols = GridColumnsStore.get(this)
        if (userCols > 0) grid.numColumns = userCols
        val isOnline = "offline" != activeSource
        val smartValidForOnline = "otomatis" == smartMode || SmartPlaylistHelper.MODE_KATEGORI == smartMode
        val kategoriOn = smartMode.isNotEmpty() && isOnline && smartValidForOnline
        val filteredTmp = ArrayList<MediaItem>()
        val sourceList: List<MediaItem> = if (kategoriOn) MultiSourceStore.getAllMerged(this) else combinedMedia
        for (m in sourceList) {
            var cocokFilter = false
            if (currentFilter == 0) cocokFilter = true
            else if (currentFilter == 1 && m.type == MediaItem.TYPE_IMAGE) cocokFilter = true
            else if (currentFilter == 2 && m.type == MediaItem.TYPE_VIDEO) cocokFilter = true
            var cocokSearch = true
            if (searchQuery.isNotEmpty()) {
                val judul = m.title ?: ""
                cocokSearch = judul.lowercase(Locale.getDefault()).contains(searchQuery.lowercase(Locale.getDefault()))
            }
            if (cocokFilter && cocokSearch) filteredTmp.add(m)
        }
        if (kategoriOn) {
            emptyState?.visibility = View.GONE
            grid.visibility = View.GONE
            scrollKategori?.visibility = View.VISIBLE
            renderKategori(filteredTmp)
            return
        } else {
            scrollKategori?.visibility = View.GONE
        }
        val filtered = ArrayList<MediaItem>()
        for (m in combinedMedia) {
            var cocokFilter = false
            if (currentFilter == 0) cocokFilter = true
            else if (currentFilter == 1 && m.type == MediaItem.TYPE_IMAGE) cocokFilter = true
            else if (currentFilter == 2 && m.type == MediaItem.TYPE_VIDEO) cocokFilter = true
            if (!cocokFilter) continue
            if (searchQuery.isNotEmpty()) {
                val judul = m.title?.lowercase(Locale.getDefault()) ?: ""
                if (!judul.contains(searchQuery.lowercase(Locale.getDefault()))) continue
            }
            filtered.add(m)
        }
        applySort(filtered)
        if (filtered.isEmpty()) {
            emptyState?.visibility = View.VISIBLE
            grid.visibility = View.GONE
        } else {
            emptyState?.visibility = View.GONE
            grid.visibility = View.VISIBLE
            grid.adapter = ThumbnailAdapter(this, filtered, object : ThumbnailAdapter.OnThumbnailClickListener {
                override fun onThumbnailClick(item: MediaItem) {
                    val clickIdx = filtered.indexOfFirst { it.uri == item.uri }
                    if (clickIdx < 0) return
                    if (item.type == 4) {
                        val intent = Intent(this@MainActivity, AudioPlayerActivity::class.java)
                        intent.putExtra("audio_uri", item.uri.toString())
                        intent.putExtra("audio_title", item.title ?: "Audio")
                        startActivity(intent)
                    } else if (item.type == MediaItem.TYPE_VIDEO || item.type == MediaItem.TYPE_IMAGE) {
                        MixedPlaylistHolder.set(filtered, clickIdx)
                        if (item.type == MediaItem.TYPE_VIDEO) {
                            val vp = ArrayList<String>()
                            filtered.forEach { if (it.type == MediaItem.TYPE_VIDEO) vp.add(it.uri.toString()) }
                            PlaylistHolder.set(vp)
                            val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
                            intent.putExtra("index", clickIdx)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@MainActivity, PreviewImageActivity::class.java)
                            intent.putExtra("index", clickIdx)
                            startActivity(intent)
                        }
                    }
                }
            })
        }
        try { buildCategoryChips() } catch (ignored: Exception) {}
    }
}
