package com.memecio.app

class ChangelogStore {

    class BuildEntry(var version: String, var timestamp: String) {
        val changes: MutableList<String> = mutableListOf()

        fun add(change: String): BuildEntry {
            changes.add(change)
            return this
        }
    }

    class FeatureStatus(val name: String, val status: String, val dateAdded: String)

    companion object {
        @JvmStatic
        fun getBuilds(): List<BuildEntry> {
            val list = mutableListOf<BuildEntry>()

            // === TERBARU DI ATAS ===

            BuildEntry("V.1.16.93", "19 Sep 2026, 07:00:00").apply {
                add("Tahap 2: Helper HTTP + Console untuk JS plugin")
                add("JsHttp: http.get(url) / http.post(url, body)")
                add("JsHttpResponse: code, text, document(), ok()")
                add("JsConsole: console.log/error/warn/info")
                add("JsBridge: inject helper ke scope Rhino")
                add("Test via kode rahasia 8889 (fetch + parse HTML)")
                add("Bump versi: 1.16.92 -> 1.16.93")
            }.let { list.add(it) }

            BuildEntry("V.1.16.92", "19 Sep 2026, 06:30:00").apply {
                add("Tahap 1: Runtime JavaScript (Rhino) ditambahkan")
                add("JsRuntime.java: eval JS, return Object/String")
                add("Rhino 1.7.14 dependency (~1 MB)")
                add("Test via kode rahasia 8888 di kalkulator")
                add("Bump versi: 1.16.91 -> 1.16.92")
            }.let { list.add(it) }

            BuildEntry("V.1.16.91", "19 Sep 2026, 06:00:00").apply {
                add("Fase A: hapus sistem plugin CloudStream + framework lama")
                add("Hapus 31 file com/lagradost/ (stub CloudStream)")
                add("Hapus 22 file com/memecio/app/plugin/ (framework lama)")
                add("Hapus PluginBrowserActivity + PluginManagerActivity")
                add("Hapus tombol Kelola Plugin dari content_sumber")
                add("Hapus Kotlin dependency (tidak butuh lagi)")
                add("Aplikasi utama tetap normal, siap untuk plugin JS baru")
                add("Bump versi: 1.16.90 -> 1.16.91")
            }.let { list.add(it) }

            BuildEntry("V.1.16.90", "19 Sep 2026, 00:00:00").apply {
                add("Fix: MainActivityKt.getApp() -> nicehttp.Requests")
                add("Stub baru: com/lagradost/nicehttp/Requests + Response")
                add("CloudstreamAppKt pakai Requests (bukan App.java)")
                add("Tombol Salin Debug cepat di layar error (tap 1x copy)")
                add("Progress: MainAPI sudah ke-resolve (Anoboy instantiated)")
                add("Bump versi: 1.16.89 -> 1.16.90")
            }.let { list.add(it) }

            BuildEntry("V.1.16.89", "18 Sep 2026, 23:30:00").apply {
                add("Fix 6 stub sekaligus dari inspeksi dex Anoboy")
                add("CloudstreamPlugin.java baru (subclass Plugin)")
                add("SearchResponseKt.java baru (extension toSearchResponse)")
                add("MainAPIKt: newHomePageResponse$default + newEpisode + newAnimeSearchResponse$default + newAnimeLoadResponse$default")
                add("Inspeksi dex - tidak iterasi 1-1 lagi")
                add("Bump versi: 1.16.88 -> 1.16.89")
            }.let { list.add(it) }

            BuildEntry("V.1.16.88", "18 Sep 2026, 23:00:00").apply {
                add("Fix: MainAPIKt.mainPageOf() stub ditambahkan")
                add("Penyebab: NoSuchMethodError mainPageOf saat instantiate Anoboy()")
                add("Fix: PluginManifest deteksi CloudStream via pluginClassName (bukan cuma ekstensi .cs3)")
                add("Bump versi: 1.16.87 -> 1.16.88")
            }.let { list.add(it) }

            BuildEntry("V.1.16.87", "18 Sep 2026, 22:00:00").apply {
                add("Fix: TvType.Ova -> OVA (plugin Anoboy akses OVA)")
                add("Penyebab: NoSuchFieldError - field name salah huruf")
                add("Fix: tryInstantiateDirectApi set hasil ke csPlugin.setApi()")
                add("Debug dump plugin berhasil - ketemu akar masalah")
                add("Bump versi: 1.16.86 -> 1.16.87")
            }.let { list.add(it) }

            BuildEntry("V.1.16.86", "18 Sep 2026, 21:00:00").apply {
                add("Auto-dump plugin debug saat search gagal")
                add("Tidak perlu tap tombol Debug - otomatis muncul di error")
                add("User tinggal long-press error -> copy -> paste")
                add("Bump versi: 1.16.85 -> 1.16.86")
            }.let { list.add(it) }

            BuildEntry("V.1.16.85", "18 Sep 2026, 20:00:00").apply {
                add("Debug: tombol Debug di Plugin Browser")
                add("Dump semua method + field AnoboyPlugin via reflection")
                add("Dump class Anoboy + coba instantiate langsung")
                add("Tujuan: identifikasi pola plugin Anoboy (setApi vs field vs lain)")
                add("Bump versi: 1.16.84 -> 1.16.85")
            }.let { list.add(it) }

            BuildEntry("V.1.16.84", "18 Sep 2026, 19:00:00").apply {
                add("Fix MainAPI null (attempt 2): Plugin.api harus PUBLIC")
                add("Penyebab: plugin pakai api = Anoboy() langsung, field kita private")
                add("Plugin.java: field api/mainApi/mainPlugin jadi public + 3 getter/setter")
                add("CSProvider: resolveMainApi() coba method + field + instantiate langsung")
                add("CSProvider: preserve error load() (tidak overwrite di search)")
                add("Bump versi: 1.16.83 -> 1.16.84")
            }.let { list.add(it) }

            BuildEntry("V.1.16.83", "18 Sep 2026, 18:00:00").apply {
                add("Fix: MainAPI null saat search - plugin belum di-load")
                add("Penyebab: getMainPlugin() dipanggil di constructor sebelum load()")
                add("Fix: CSProvider.onInit() re-fetch mainApi setelah csPlugin.load()")
                add("Anoboy & plugin CloudStream lain pakai setApi() di dalam load()")
                add("Bump versi: 1.16.82 -> 1.16.83")
            }.let { list.add(it) }

            BuildEntry("V.1.16.82", "18 Sep 2026, 17:00:00").apply {
                add("Plugin System C-2b-4: loadDetail() - sinopsis + episode list")
                add("PluginMediaItem: field duration + tags + episodeCount")
                add("MemecioProvider: method loadDetail() (default return item)")
                add("CSProvider.loadDetail: panggil plugin load() via coroutine")
                add("PluginBrowser: dialog detail async - tampil durasi + episode")
                add("Belum teruji - C-2b-2/3 belum build")
                add("Bump versi: 1.16.81 -> 1.16.82")
            }.let { list.add(it) }

            BuildEntry("V.1.16.81", "18 Sep 2026, 16:00:00").apply {
                add("Plugin System C-2b-3: implementasi loadLinks()")
                add("LoadLinksCallback baru: bungkus Function1 (Kotlin) jadi Java callback")
                add("CSProvider.loadLinks: reflection + Function1 subtitle + link callback")
                add("PluginBrowser: tampil error loadLinks (bisa di-copy)")
                add("Belum teruji - C-2b-2 belum di-build")
                add("Bump versi: 1.16.80 -> 1.16.81")
            }.let { list.add(it) }

            BuildEntry("V.1.16.80", "18 Sep 2026, 15:00:00").apply {
                add("Plugin System C-2b-2: panggil search() plugin via Kotlin coroutine")
                add("CoroutineHelper baru: reflection + Continuation + wait")
                add("CloudStreamProviderAdapter: search() + getMainPage() real")
                add("PluginBrowserActivity: tampil error dari plugin (bisa di-copy)")
                add("SearchQuality enum stub ditambahkan")
                add("loadLinks masih placeholder (C-2b-3)")
                add("Bump versi: 1.16.79 -> 1.16.80")
            }.let { list.add(it) }

            BuildEntry("V.1.16.79", "18 Sep 2026, 14:00:00").apply {
                add("Fix author/description plugin CloudStream kosong (unknown)")
                add("PluginDownloader override author + description dari RepositoryPlugin")
                add("manifest.json CloudStream biasanya tidak punya field author")
                add("Bump versi: 1.16.78 -> 1.16.79")
            }.let { list.add(it) }

            BuildEntry("V.1.16.78", "18 Sep 2026, 13:00:00").apply {
                add("Fix C-2b-1: hapus block .cs3 di PluginDownloader")
                add("PluginDownloader lama masih reject .cs3 sebelum C-2b-1 sempat load")
                add("Sekarang .cs3 diizinkan download + load via CloudStreamPluginAdapter")
                add("Bump versi: 1.16.77 -> 1.16.78")
            }.let { list.add(it) }

            BuildEntry("V.1.16.77", "18 Sep 2026, 12:00:00").apply {
                add("Plugin System C-2b-1: Loader .cs3 dasar + adapter CloudStream")
                add("CloudStreamPluginAdapter: bungkus plugin CloudStream jadi MemecioPlugin")
                add("CloudStreamProviderAdapter: bungkus MainAPI jadi MemecioProvider")
                add("PluginLoader: deteksi .cs3, instantiate Plugin, register via adapter")
                add("PluginManifest: baca pluginClassName (format CloudStream)")
                add("PluginDescriptor: field isCloudStream + pluginClassName")
                add("Belum bisa search/load - placeholder (C-2b-2)")
                add("Bump versi: 1.16.76 -> 1.16.77")
            }.let { list.add(it) }

            BuildEntry("V.1.16.76", "18 Sep 2026, 11:00:00").apply {
                add("Plugin System C-2a: 14 stub CloudStream tambahan")
                add("Stub baru: DubStatus, ShowStatus, Episode, AnimeLoadResponse, AnimeSearchResponse")
                add("Stub baru: MainPageData, MainPageRequest, ErrorLoadingException")
                add("Stub baru: MainAPIKt, MainActivityKt, plugins/Plugin")
                add("Stub baru: utils/ExtractorLink, utils/ExtractorLinkType, utils/ExtractorApiKt")
                add("Belum bisa load plugin .cs3 - loader khusus di C-2b")
                add("Bump versi: 1.16.75 -> 1.16.76")
            }.let { list.add(it) }

            BuildEntry("V.1.16.75", "18 Sep 2026, 10:00:00").apply {
                add("Plugin System C-1: Fondasi Kotlin + Stub CloudStream")
                add("12 stub class di package com.lagradost.cloudstream3 (TvType, LoadType, MainAPI, dll)")
                add("App.java: HTTP wrapper (get/post) kompatibel plugin CloudStream")
                add("CloudstreamAppKt: static field `app` untuk plugin akses HTTP")
                add("Kotlin runtime: kotlin-stdlib 1.8.10 + coroutines 1.6.4")
                add("Belum bisa load plugin .cs3 - loader khusus di C-2")
                add("Bump versi: 1.16.74 -> 1.16.75")
            }.let { list.add(it) }

            BuildEntry("V.1.16.74", "18 Sep 2026, 09:00:00").apply {
                add("Fix A: Support URL scheme apapun (cloudstreamrepo:// -> https://)")
                add("Fix B: Parse format repo CloudStream (pluginLists + manifestVersion)")
                add("Repo CloudStream bisa di-muat, daftar plugin muncul")
                add("Plugin .cs3 dideteksi, kasih pesan ramah 'belum didukung'")
                add("Badge CloudStream di daftar plugin (biar jelas)")
                add("Bump versi: 1.16.73 -> 1.16.74")
            }.let { list.add(it) }

            BuildEntry("V.1.16.73", "18 Sep 2026, 08:00:00").apply {
                add("Plugin System Tahap 4: Plugin Browser (search + play)")
                add("2 file Java baru: PluginBrowserActivity + PluginItemAdapter")
                add("2 layout baru: activity_plugin_browser (portrait + landscape) + item_plugin_card")
                add("Spinner pilih plugin + toggle 'cari di semua plugin'")
                add("Grid hasil + dialog detail + tombol Play")
                add("Tombol 'Buka Plugin Browser' di halaman Kelola Plugin")
                add("Play video pakai VideoPlayerActivity existing (tidak bikin jalur baru)")
                add("Bump versi: 1.16.72 -> 1.16.73")
            }.let { list.add(it) }

            BuildEntry("V.1.16.72", "17 Sep 2026, 10:00:00").apply {
                add("Plugin System Tahap 3: Repository GitHub")
                add("5 file baru: RepositoryPlugin, PluginRepository, RepositoryManager, RepositoryStorage, PluginDownloader")
                add("UI 2 tab di PluginManagerActivity: Terinstall + Repository")
                add("Fetch repo.json dari URL + parse daftar plugin")
                add("Install plugin 1 klik (download .jar + load otomatis)")
                add("Simpan daftar repo URL di SharedPreferences")
                add("Bump versi: 1.16.71 -> 1.16.72")
            }.let { list.add(it) }

            BuildEntry("V.1.16.71", "17 Sep 2026, 09:00:00").apply {
                add("Plugin System Tahap 2: Loader + UI Kelola Plugin")
                add("5 file loader baru di package plugin (Descriptor, Manifest, Loader, Storage, Result)")
                add("PluginManagerActivity: halaman kelola plugin (portrait + landscape)")
                add("Tombol 'Kelola Plugin' di menu Sumber (portrait + landscape)")
                add("Icon baru: ic_plugin.xml (puzzle piece)")
                add("Bisa tambah plugin .jar via file picker Android")
                add("Plugin disimpan di /sdcard/Termux/MemecioPlugins/")
                add("Bump versi: 1.16.70 -> 1.16.71")
            }.let { list.add(it) }

            BuildEntry("V.1.16.70", "17 Sep 2026, 00:30:00").apply {
                add("Plugin System Tahap 1: fondasi interface (backend only, belum UI)")
                add("9 file baru di package com.memecio.app.plugin + plugin.model")
                add("MemecioPlugin (interface), MemecioProvider (abstract), PluginManager, PluginLogger")
                add("Model: PluginMediaItem, PluginSearchResult, PluginLink")
                add("SamplePlugin: plugin dummy untuk testing fondasi")
                add("Tidak ada perubahan UI/layout. Tidak ada fitur lama yang diubah")
                add("Bump versi: 1.16.69 -> 1.16.70")
            }.let { list.add(it) }

            BuildEntry("V.1.16.69", "16 Sep 2026, 22:30:00").apply {
                add("REVERT V.1.16.67: HP landscape TIDAK dipaksa jadi TV lagi")
                add("Hapus isLandscape() dari DisplayModeStore")
                add("onConfigurationChanged jadi no-op")
                add("applyTvFocusRingsIfTv jadi no-op (Android TV sama dengan landscape)")
                add("Hapus configChanges MainActivity dari manifest")
                add("Tampilan landscape kembali rapi (seperti sebelum V.1.16.67)")
                add("Bump versi: 1.16.68 -> 1.16.69")
            }.let { list.add(it) }

            BuildEntry("V.1.16.68", "16 Sep 2026, 22:00:00").apply {
                add("FIX Smart Playlist Online: tampilkan SEMUA sumber (Drive+Streaming+Server) bersamaan")
                add("Akar: activeSource masih 'offline' saat Smart ON -> kategoriOn=false -> Smart tidak jalan")
                add("Fix: saat Smart ON -> activeSource dipaksa 'external'")
                add("Fix: switch Smart di menu Sumber juga set activeSource='external'")
                add("Fix: foto tidak lagi dibuang di renderKategori (tampil bersama video)")
                add("Bump versi: 1.16.67 -> 1.16.68")
            }.let { list.add(it) }

            BuildEntry("V.1.16.67", "16 Sep 2026, 21:00:00").apply {
                add("Landscape otomatis jadi Android TV (mode AUTO)")
                add("DisplayModeStore: cek landscape -> return MODE_TV")
                add("MainActivity: configChanges + onConfigurationChanged")
                add("Mode MANUAL Android tetap dihormati (tidak paksa TV)")
                add("Bump versi: 1.16.66 -> 1.16.67")
            }.let { list.add(it) }

            BuildEntry("V.1.16.66", "16 Sep 2026, 20:34:17").apply {
                add("Update dokumentasi aturan versioning (Section 1 ProjectExportHelper)")
                add("Fix typo build_coun -> build_count di ProjectExportHelper")
                add("Sesuaikan versionName dengan tanggal realtime (1.14.65 -> 1.16.66)")
                add("Tambah catatan bug versioning di BugNotesStore")
                add("Bump versi: 1.14.65 -> 1.16.66")
                add("Tambah kode 808 di SecretCodeRegistry (Daftar Download)")
                add("Buat RoadmapStore.java — Section 9 auto-pull (tidak hardcoded lagi)")
                add("Fix Section 9 dobel: hapus 'Android TV support' (sudah selesai)")
                add("Catat 2 bug baru di BugNotesStore (kode 808 + Section 9)")
            }.let { list.add(it) }

            BuildEntry("V.1.14.65", "16 Sep 2026, 15:45:00").apply {
                add("Debug Multi: dialog custom 2 tombol (Salin + Exit)")
                add("Tombol Salin: copy debug content ke clipboard")
                add("Tombol Exit: tutup dialog")
                add("Rapikan posisi tombol Debug Multi di Profil (row 3 portrait + landscape)")
                add("DevTools tetap ada di landscape (hidden, aktif via kalkulator 101)")
                add("Bump versi: 1.14.64 -> 1.14.65")
            }.let { list.add(it) }

            BuildEntry("V.1.14.64", "16 Sep 2026, 15:30:00").apply {
                add("Fix: sourceTitle null di semua item — auto-set dari sourceLabel")
                add("Fix: data dobel (migrated_* & src_ lama) — auto-deduplicate")
                add("Debug dialog: tombol Reset Semua untuk bersihkan sumber lama")
                add("Multi-source Smart Playlist sekarang pisah per sumber dengan benar")
                add("Bump versi: 1.14.63 -> 1.14.64")
            }.let { list.add(it) }

            BuildEntry("V.1.14.63", "16 Sep 2026, 15:00:00").apply {
                add("Bersihkan landscape: hapus btnZoomUI + btnDataSaver (sudah tidak dipakai)")
                add("Tombol Debug Multi-Source di Profil (portrait + landscape)")
                add("Debug dialog: lihat daftar source + item per sumber")
                add("Bump versi: 1.14.62 -> 1.14.63")
            }.let { list.add(it) }

            BuildEntry("V.1.14.62", "16 Sep 2026, 14:30:00").apply {
                add("Fix MultiSourceStore: sourceId unique (label + timestamp)")
                add("Penyebab: label 'Drive Folder' & 'server' hardcoded -> sourceId tabrakan")
                add("Sekarang tiap sumber (bahkan label sama) tersimpan terpisah")
                add("Bump versi: 1.14.61 -> 1.14.62")
            }.let { list.add(it) }

            BuildEntry("V.1.14.61", "16 Sep 2026, 11:00:00").apply {
                add("Fix MultiSourceStore: item pertama hilang dari setiap sumber")
                add("Penyebab: SEP_ITEM tidak ditulis sebelum item pertama")
                add("Smart Playlist sekarang tampil semua sumber dengan benar")
                add("Bump versi: 1.14.60 -> 1.14.61")
            }.let { list.add(it) }

            BuildEntry("V.1.14.60", "16 Sep 2026, 10:00:00").apply {
                add("Fix: Smart Playlist tidak jalan di landscape/Android TV")
                add("Layout-land/content_sumber.xml: switchKategori -> switchSmart + tvSmartMode")
                add("Layout-land/content_beranda.xml: tambah overlayExpand + gridExpand + btnShrink")
                add("Semua fitur (Smart Playlist, Media Scan) support portrait + landscape")
                add("Bump versi: 1.14.59 -> 1.14.60")
            }.let { list.add(it) }

            BuildEntry("V.1.14.59", "16 Sep 2026, 09:00:00").apply {
                add("MultiSourceStore baru: simpan semua sumber terpisah (tidak timpa)")
                add("Auto-save ke MultiSourceStore dari ExternalMediaStore.gantiSemua")
                add("Smart Playlist ON -> ambil dari MultiSourceStore.getAllMerged")
                add("Semua sumber (Drive/Streaming/Server) tampil bersamaan")
                add("Bump versi: 1.14.58 -> 1.14.59")
            }.let { list.add(it) }

            BuildEntry("V.1.14.58", "16 Sep 2026, 08:00:00").apply {
                add("Gabung Media Scan ke dalam Sumber Offline")
                add("Hapus switch Media Scan terpisah dari menu Sumber")
                add("Sumber Offline ON -> popup Media Scan (switch + pilih mode)")
                add("Bump versi: 1.14.57 -> 1.14.58")
            }.let { list.add(it) }

            BuildEntry("V.1.14.57", "16 Sep 2026, 07:30:00").apply {
                add("Smart Playlist: 1 tombol (ganti dari 2 opsi manual)")
                add("ON -> otomatis gabung semua mode (Riwayat + Populer + Terbaru + Kategori + Fallback)")
                add("Bump versi: 1.14.56 -> 1.14.57")
            }.let { list.add(it) }

            BuildEntry("V.1.14.56", "16 Sep 2026, 09:00:00").apply {
                add("Fix: mode Otomatis Smart Playlist kosong kalau tidak ada keyword match")
                add("Tambah fallback sourceTitle -> Lainnya di jalur Otomatis")
                add("Bump versi: 1.14.55 -> 1.14.56")
            }.let { list.add(it) }

            BuildEntry("V.1.14.55", "16 Sep 2026, 08:00:00").apply {
                add("Fix: Drive/Streaming kosong saat kedua switch (Offline + Smart) OFF")
                add("Empty state cuma kalau activeSource = offline")
                add("Bump versi: 1.14.54 -> 1.14.55")
            }.let { list.add(it) }

            BuildEntry("V.1.14.54", "16 Sep 2026, 07:00:00").apply {
                add("Fix mutex: switchSmart ON -> uncheck switchMediaScan juga")
                add("Fix: switchMediaScan ON -> set offline_enabled = true")
                add("Bump versi: 1.14.53 -> 1.14.54")
            }.let { list.add(it) }

            BuildEntry("V.1.14.53", "16 Sep 2026, 06:00:00").apply {
                add("Fix: sumber Drive kosong walau cache ada 31 file")
                add("Penyebab: smartMode sisa Media Scan (folder/date/dll) trigger Smart Playlist di online")
                add("Fix: Smart Playlist filter cek mode valid (otomatis/kategori saja)")
                add("Fix init switchSmart: cuma ON kalau mode valid online (bukan sisa Media Scan)")
                add("Bump versi: 1.14.52 -> 1.14.53")
            }.let { list.add(it) }

            BuildEntry("V.1.14.52", "16 Sep 2026, 04:00:00").apply {
                add("Kategori Fallback: sourceTitle -> Lainnya")
                add("MediaItem: tambah field sourceTitle")
                add("M3uParser: parse group-title dari #EXTINF")
                add("Drive/Server/Streaming: isi sourceTitle dari sumber")
                add("Bump versi: 1.14.51 -> 1.14.52")
            }.let { list.add(it) }

            BuildEntry("V.1.14.51", "16 Sep 2026, 03:00:00").apply {
                add("Smart Playlist (online): cuma 2 opsi — Otomatis + Kategori")
                add("Media Scan (offline): popup pilih mode — Folder/Date/Size/Duration")
                add("Mutex: Smart Playlist ON <-> Media Scan OFF (dan sebaliknya)")
                add("Bump versi: 1.14.50 -> 1.14.51")
            }.let { list.add(it) }

            BuildEntry("V.1.14.50", "16 Sep 2026, 02:00:00").apply {
                add("Fix Drive: timeout 5s -> 20s")
                add("3-layer fallback: JSON -> DOM parse -> embeddedfolderview")
                add("Layer 2 baru: DOM parse HTML halaman utama")
                add("Tambah shimmer library (persiapan loading indicator)")
                add("Bump versi: 1.14.49 -> 1.14.50")
            }.let { list.add(it) }

            BuildEntry("V.1.14.49", "16 Sep 2026, 00:00:00").apply {
                add("Fix: sumber online (Drive/Streaming/Server) tidak tampil setelah Smart Playlist")
                add("Smart Playlist filter hanya aktif di sumber ONLINE")
                add("Media Scan by Folder filter hanya aktif di sumber OFFLINE")
                add("Bump versi: 1.14.48 -> 1.14.49")
            }.let { list.add(it) }

            BuildEntry("V.1.14.48", "15 Sep 2026, 23:00:00").apply {
                add("Sub-Patch A1: HDR auto-detect (ColorInfo ST2084/HLG)")
                add("Tab baru Tampilan di setting player + toggle HDR")
                add("Info status HDR (Video: HDR/SDR)")
                add("Tone mapping ditunda — Media3 1.4.1 belum support native")
                add("Bump versi: 1.14.47 -> 1.14.48")
            }.let { list.add(it) }

            BuildEntry("V.1.14.47", "15 Sep 2026, 22:30:00").apply {
                add("Sub-Patch A0: Upgrade Media3 1.2.1 -> 1.4.1")
                add("Tambah media3-effect 1.4.1 (untuk HDR & Shader)")
                add("Persiapan fitur HDR, Brightness/Contrast, Sharpen, Anime4K")
                add("Bump versi: 1.14.46 -> 1.14.47")
            }.let { list.add(it) }

            BuildEntry("V.1.14.46", "15 Sep 2026, 22:00:00").apply {
                add("Fitur baru: Media Scan by Folder (khusus offline)")
                add("Switch di menu Sumber + popup konfirmasi")
                add("List folder di Beranda, tap folder -> grid isi folder")
                add("Nama folder = nama terakhir (Camera, Screenshots, dll)")
                add("Bump versi: 1.14.45 -> 1.14.46")
            }.let { list.add(it) }

            BuildEntry("V.1.14.45", "15 Sep 2026, 21:30:00").apply {
                add("Fix dialog Smart Playlist: back button bisa keluar + tombol Tutup")
                add("Fix dialog: tidak auto-switch ke Beranda setelah pilih mode")
                add("Dialog pakai Switch individual di tiap opsi (single-mode)")
                add("Bump versi: 1.14.44 -> 1.14.45")
            }.let { list.add(it) }

            BuildEntry("V.1.14.44", "15 Sep 2026, 21:00:00").apply {
                add("Sub-dialog Size: Kecil / Sedang / Besar")
                add("Bump versi: 1.14.43 -> 1.14.44")
            }.let { list.add(it) }

            BuildEntry("V.1.14.43", "15 Sep 2026, 20:30:00").apply {
                add("Rapikan susunan tombol Profil — pindah Auto Exit ke Row 2")
                add("Bump versi: 1.14.42 -> 1.14.43")
            }.let { list.add(it) }

            BuildEntry("V.1.14.42", "15 Sep 2026, 20:00:00").apply {
                add("Gabung Otomatis + Kategori (5 opsi dialog, hilangkan duplikat)")
                add("Sub-dialog Date: Kelompok / 7 hari / 30 hari / 6 bulan / 1 tahun")
                add("Sub-dialog Duration: Pendek / Sedang / Panjang / <1m / >1h")
                add("Hapus tombol Data Saver + Zoom UI (tidak berguna)")
                add("Bump versi: 1.14.41 -> 1.14.42")
            }.let { list.add(it) }

            BuildEntry("V.1.14.41", "15 Sep 2026, 19:30:00").apply {
                add("Fix: kedua switch OFF -> Beranda kosong (sebelumnya masih tampil grid)")
                add("Fix: freeze/forceclose pada mode Date/Folder/Size/Duration")
                add("Limit 30 item per baris di addKategoriRow (cegah overflow thread)")
                add("Bump versi: 1.14.40 -> 1.14.41")
            }.let { list.add(it) }

            BuildEntry("V.1.14.40", "15 Sep 2026, 19:00:00").apply {
                add("Toggle FLAG_SECURE via Profil (Privasi Screenshot: ON/OFF)")
                add("Kode rahasia: 0000 + DEL 3x + = (toggle cepat + Toast)")
                add("Default ON, auto-reset saat app di-kill (in-memory)")
                add("Terdaftar di daftar kode rahasia (kode 000)")
                add("Bump versi: 1.14.39 -> 1.14.40")
            }.let { list.add(it) }

            BuildEntry("V.1.14.39", "15 Sep 2026, 18:30:00").apply {
                add("Restore fitur kategori lama (Riwayat + Populer + Terbaru + TV + Film + Anime)")
                add("Smart Playlist switch on/off — ganti tombol")
                add("Dialog: Otomatis, Kategori, Folder, Date, Size, Duration")
                add("Mutex: Offline ON <-> Smart Playlist OFF (dan sebaliknya)")
                add("Keduanya OFF -> Beranda kosong. Default: Offline ON")
                add("Bump versi: 1.14.38 -> 1.14.39")
            }.let { list.add(it) }

            BuildEntry("V.1.14.38", "15 Sep 2026, 18:00:00").apply {
                add("Smart Playlist: ganti toggle Mat Kategori jadi 1 tombol pintar")
                add("Dialog 6 mode: Reset, Kategori, Folder, Date, Size, Duration")
                add("MediaItem: tambah field size + duration")
                add("Query MediaStore sekaligus (SIZE + DURATION) — tidak ANR")
                add("SmartPlaylistHelper.java: helper grouping folder/date/size/duration")
                add("Mode Kategori pakai CategoryHelper existing (TV, Film, Anime, dll)")
                add("Bump versi: 1.14.37 -> 1.14.38")
            }.let { list.add(it) }

            BuildEntry("V.1.14.37", "15 Sep 2026, 17:30:00").apply {
                add("Fix ANR: extractFolderPathFromUri query per item bikin hang")
                add("Ganti ke parseFolderFromUri — tanpa query, biar tidak block main thread")
                add("Bump versi: 1.14.36 -> 1.14.37")
            }.let { list.add(it) }

            BuildEntry("V.1.14.36", "15 Sep 2026, 10:00:00").apply {
                add("Auto-Scan Patch 1: fix bug duplikat Audio query (scan 2x)")
                add("MediaItem: tambah field folderPath")
                add("MediaScanCache baru: cache scan 5 menit")
                add("Auto-refresh onResume: scan ulang kalau cache expired")
                add("Bump versi: 1.14.35 -> 1.14.36")
            }.let { list.add(it) }

            BuildEntry("V.1.14.35", "15 Sep 2026, 09:30:00").apply {
                add("Buffer tuning: LoadControl custom untuk stabilkan playback")
                add("Single video: buffer 15-120s, target 50MB, max resolusi 1080p")
                add("Multiview: buffer 10-30s, target 20MB per player, max resolusi 720p")
                add("Prioritize time over size — buffer waktu lebih penting dari ukuran")
                add("Bump versi: 1.14.34 -> 1.14.35")
            }.let { list.add(it) }

            BuildEntry("V.1.14.34", "15 Sep 2026, 09:00:00").apply {
                add("Fitur baru: Banking-style privacy — Cover overlay saat app ke background")
                add("CoverActivity: full-screen cover portrait/landscape, tap → PIN")
                add("FLAG_SECURE di semua activity (block screenshot + Recents preview)")
                add("Launcher icon baru: icon_terbaru.png di-resize 512x512")
                add("Bump versi: 1.14.33 -> 1.14.34")
            }.let { list.add(it) }

            BuildEntry("V.1.14.33", "15 Sep 2026, 06:30:00").apply {
                add("Multiview: drag & drop swap slot (long-press slot, geser ke slot tujuan)")
                add("Multiview: tombol Play/Pause per slot (auto-hide 3 detik)")
                add("Multiview: tombol Mute/Unmute per slot (audio bisa bersamaan)")
                add("Multiview: tombol Fullscreen per slot (buka player penuh)")
                add("Multiview: tombol Replay otomatis muncul saat video habis")
                add("Tap slot = auto-focus audio (mute lain), unmute manual untuk multi-audio")
                add("Bump versi: 1.14.32 -> 1.14.33")
            }.let { list.add(it) }

            BuildEntry("V.1.14.32", "15 Sep 2026, 06:00:00").apply {
                add("Fix Multiview: slot lama tetap terisi setelah tambah slot baru")
                add("MultiviewPickHolder: simpan slotUrls + slotTypes persistent antar instance")
                add("Fix Multiview: foto sekarang tampil (pakai ImageView, bukan ExoPlayer)")
                add("Auto-detect tipe: image/* pakai ImageView, video/* pakai ExoPlayer")
                add("Bump versi: 1.14.31 -> 1.14.32")
            }.let { list.add(it) }

            BuildEntry("V.1.14.31", "15 Sep 2026, 05:30:00").apply {
                add("Fix Multiview onResume: toast 'Pilih media untuk Slot N' saat balik dari Multiview")
                add("Auto-switch ke Beranda saat mode pick aktif")
                add("Bump versi: 1.14.30 -> 1.14.31")
            }.let { list.add(it) }

            BuildEntry("V.1.14.30", "14 Sep 2026, 22:00:00").apply {
                add("Fix Multiview: tap Slot -> buka Beranda -> pilih media dari semua sumber")
                add("MultiviewPickHolder baru: passing data slot antara Multiview & MainActivity")
                add("Intercept onThumbnailClick: kalau mode pick, media langsung masuk slot")
                add("Audio focus otomatis pindah ke slot yang baru diisi")
                add("Bump versi: 1.14.29 -> 1.14.30")
            }.let { list.add(it) }

            BuildEntry("V.1.14.29", "14 Sep 2026, 21:30:00").apply {
                add("Fitur baru: Multiview — 4 video/foto sekaligus (grid 2x2)")
                add("Audio focus per slot: tap slot untuk jadikan bersuara")
                add("Tombol pick per slot (dari galeri)")
                add("Tombol close per slot, layout toggle, close multiview")
                add("Tombol A-Z di Beranda diganti jadi tombol Multiview")
                add("Tutup hutang dokumentasi: BugNotesStore + ProjectExportHelper")
                add("Bump versi: 1.14.28 -> 1.14.29")
            }.let { list.add(it) }

            BuildEntry("V.1.14.28", "14 Sep 2026, 21:00:00").apply {
                add("Fitur baru: Video Zoom & Pan (pinch 2 jari, max 4x, double-tap reset)")
                add("Fitur baru: Android TV Support (banner tv_banner.png, LEANBACK launcher)")
                add("Zoom reset otomatis saat ganti video")
            }.let { list.add(it) }

            return list
        }
    }
}
