package com.memecio.app

/**
 * ============================================================
 * BUG NOTES STORE — CATATAN BUG & KENDALA PERMANEN
 * ============================================================
 *
 * PENTING UNTUK AI / DEVELOPER LAIN:
 * File ini adalah "otak" catatan bug & kendala aplikasi Memec.io.
 * Setiap kali AI assistant menemukan bug baru, memperbaiki bug,
 * atau menemukan kendala teknis, WAJIB update file ini:
 *   1. Bug baru ditemukan -> tambah entry di getBugsOpen()
 *   2. Bug sudah diperbaiki -> pindah dari getBugsOpen() ke getBugsFixed() + tanggal
 *   3. Kendala teknis baru -> tambah di getLimitations()
 *
 * Format tanggal: "dd MMM yyyy" (contoh: "12 Sep 2026")
 *
 * Setiap kali update file ini, WAJIB juga:
 *   1. Bump versionName di build.gradle
 *   2. Tambah BuildEntry di ChangelogStore.kt
 *   3. Update getFeatureStatus() kalau ada fitur baru
 *
 * Tujuan: agar AI assistant lain bisa langsung nyambung
 * tanpa banyak tanya.
 * ============================================================
 */
class BugNotesStore {

    data class BugNote(
        val title: String,
        val description: String,
        val status: String,
        val dateFound: String,
        val dateFixed: String,
        val solution: String
    )

    data class Limitation(
        val title: String,
        val description: String
    )

    companion object {

        @JvmStatic
        fun getBugsFixed(): List<BugNote> {
            val list = mutableListOf<BugNote>()

            list.add(BugNote(
                "Tahap 2: Helper HTTP + Console untuk plugin JavaScript",
                "Rhino (Tahap 1) sudah bisa eval JS, tapi JS belum bisa akses HTTP/HTML. Plugin butuh http.get + parse HTML",
                "Fixed", "19 Sep 2026", "19 Sep 2026",
                "JsHttp + JsHttpResponse (pakai Jsoup untuk document()) + JsConsole + JsBridge. Inject ke Rhino scope. Test via kode rahasia 8889"
            ))

            list.add(BugNote(
                "Tahap 1: Runtime JavaScript (Rhino) untuk plugin system baru",
                "Stub CloudStream (Kotlin) tidak bisa jalan tanpa library asli. Mulai sistem baru: plugin JavaScript pakai Rhino runtime - jauh lebih ringan, tidak perlu Kotlin",
                "Fixed", "19 Sep 2026", "19 Sep 2026",
                "Tambah JsRuntime.java (eval JS), dependency org.mozilla:rhino:1.7.14, test via kode rahasia 8888"
            ))

            list.add(BugNote(
                "Fase A: hapus sistem plugin CloudStream + framework lama",
                "Stub CloudStream (lagradost) + framework plugin lama (plugin/) tidak bisa jalan tanpa library asli. Dihapus untuk bikin sistem plugin JS baru yang lebih ringan dan bisa di-maintain",
                "Fixed", "19 Sep 2026", "19 Sep 2026",
                "Hapus 61 file (lagradost + plugin + activity + layout). Update MainActivity, content_sumber, Manifest. Backup di backup_faseA_*"
            ))

            list.add(BugNote(
                "MainActivityKt.getApp() butuh nicehttp.Requests",
                "Plugin panggil getApp() di MainActivityKt untuk akses HTTP client global. Return type com.lagradost.nicehttp.Requests - library CloudStream yang belum di-stub",
                "Fixed", "19 Sep 2026", "19 Sep 2026",
                "Stub nicehttp.Requests (get/post) + nicehttp.Response (text/document). MainActivityKt.getApp() return singleton Requests. CloudstreamAppKt juga pakai Requests"
            ))

            list.add(BugNote(
                "6 stub CloudStream kurang - fix sekaligus dari inspeksi dex",
                "Inspeksi strings classes.dex Anoboy tunjukkan 6 class/method yang belum di-stub: CloudstreamPlugin, toSearchResponse, newHomePageResponse\$default, newEpisode, newAnimeSearchResponse\$default, newAnimeLoadResponse\$default",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "Bikin CloudstreamPlugin + SearchResponseKt. Rewrite MainAPIKt dengan 7 helper lengkap (termasuk versi \$default)"
            ))

            list.add(BugNote(
                "MainAPIKt.mainPageOf() tidak ada di stub",
                "Setelah fix TvType.OVA, instantiate Anoboy() lanjut sampai panggil MainAPIKt.mainPageOf(vararg Pair). Method belum ada di stub - NoSuchMethodError",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "MainAPIKt.mainPageOf(Pair[]) -> List. Setiap Pair jadi HomePageList(name, list). Deteksi CloudStream plugin dari field pluginClassName (bukan cuma .cs3)"
            ))

            list.add(BugNote(
                "TvType.Ova salah huruf - seharusnya OVA",
                "Plugin Anoboy akses TvType.OVA (kapital). Stub saya tulis Ova (huruf kecil). Akibatnya constructor Anoboy() error NoSuchFieldError - api tidak pernah di-set - MainAPI null",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "TvType.java: Ova -> OVA. tryInstantiateDirectApi: set hasil ke csPlugin.setApi() supaya getMainPlugin() return bukan-null"
            ))

            list.add(BugNote(
                "MainAPI null attempt 2: Plugin.api harus public",
                "Fix sebelumnya (pindah getMainPlugin ke onInit) belum berhasil. Ternyata plugin pakai pola `api = Anoboy()` (field access langsung). Field api kita private, jadi plugin gagal set",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "Plugin.java: 3 field public (api, mainApi, mainPlugin) + getter/setter. CSProvider: resolveMainApi() coba method + field + instantiate direct. Error load() di-preserve"
            ))

            list.add(BugNote(
                "MainAPI null saat search plugin CloudStream",
                "CloudStreamProviderAdapter constructor panggil getMainPlugin() sebelum csPlugin.load(). Plugin CloudStream pola umum pakai setApi(Anoboy()) di dalam load(). Jadi constructor dapat api=null",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "Pindah getMainPlugin() ke onInit() - setelah csPlugin.load(context). Re-fetch kalau mainApi masih null"
            ))

            list.add(BugNote(
                "C-2b-4: loadDetail() untuk plugin CloudStream",
                "Tap film -> dialog detail perlu sinopsis + episode list. Plugin CloudStream punya method load() yang return LoadResponse, tapi tidak dipanggil",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "CSProvider.loadDetail() panggil plugin load() via CoroutineHelper. Convert LoadResponse -> PluginMediaItem (plot, duration, tags, episodeCount). PluginBrowser dialog async + tampil info lengkap"
            ))

            list.add(BugNote(
                "C-2b-3: implementasi loadLinks() untuk plugin CloudStream",
                "Plugin CloudStream loadLinks() pakai callback Function1 (Kotlin) untuk subtitle + extractor link. Java tidak bisa implement Function1 langsung tanpa wrapper",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "LoadLinksCallback bungkus Function1. CSProvider.loadLinks() pakai reflection + CountDownLatch tunggu callback. Error ditampilkan di UI"
            ))

            list.add(BugNote(
                "Author plugin CloudStream tampil 'unknown'",
                "manifest.json CloudStream tidak punya field author/description (beda dengan Memec.io). Info author ada di repo.json (field authors), tapi tidak diwariskan ke PluginDescriptor",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "PluginDownloader.download() override result.descriptor.author + description dari RepositoryPlugin setelah load sukses"
            ))

            list.add(BugNote(
                "PluginDownloader block .cs3 sebelum loader C-2b-1 sempat jalan",
                "Saat C-2b-1, plugin loader sudah support .cs3, tapi PluginDownloader masih punya block lama (.cs3 belum didukung) yang dieksekusi dulu. Akibatnya install selalu gagal sebelum loader dipanggil",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "Hapus block deteksi .cs3 di PluginDownloader.download(). Sekarang .cs3 diizinkan download + load via CloudStreamPluginAdapter"
            ))

            list.add(BugNote(
                "Plugin System C-2b-1: Loader .cs3 + adapter CloudStream",
                "Loader lama hanya support plugin .jar yang implements MemecioPlugin. Plugin CloudStream (.cs3) extends Plugin dari library CloudStream, butuh adapter khusus",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "CloudStreamPluginAdapter + CloudStreamProviderAdapter. PluginLoader deteksi ekstensi .cs3 + instanceof Plugin CloudStream. Manifest baca pluginClassName. Panggilan method asli (search/load) di C-2b-2"
            ))

            list.add(BugNote(
                "Plugin System C-2a: 14 stub CloudStream tambahan",
                "Inspeksi Anoboy.cs3 tunjukkan class CloudStream yang belum di-stub. Package ExtractorLink harusnya di utils/, bukan root",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "Tambah 14 file stub di com.lagradost.cloudstream3 + subpackage (plugins, utils). ExtractorLink di utils extends root untuk kompatibilitas"
            ))

            list.add(BugNote(
                "Plugin System C-1: Fondasi Kotlin + CloudStream stub",
                "Untuk support plugin CloudStream (.cs3), butuh Kotlin runtime + stub class CloudStream (MainAPI, LoadResponse, dll). Ini fondasi — belum bisa load plugin nyata",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "12 stub class di com.lagradost.cloudstream3 + App.java HTTP wrapper + CloudstreamAppKt + kotlin-stdlib 1.8.10 + coroutines 1.6.4"
            ))

            list.add(BugNote(
                "URL cloudstreamrepo:// ditolak aplikasi",
                "User input URL repo CloudStream dengan skema cloudstreamrepo://, PluginManagerActivity tolak karena cuma terima http/https. Repo CloudStream format beda juga (pluginLists)",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "RepositoryManager.normalizeUrl() convert cloudstreamrepo:// -> https://. Parse format CloudStream (pluginLists + manifestVersion). Plugin .cs3 dideteksi + badge CloudStream"
            ))

            list.add(BugNote(
                "Plugin System Tahap 4 ditambahkan (Plugin Browser)",
                "Tahap 3 selesai repository. User minta bisa browsing konten plugin dari UI, seperti MovieBox",
                "Fixed", "18 Sep 2026", "18 Sep 2026",
                "PluginBrowserActivity dengan spinner plugin + toggle semua + search bar + grid hasil + dialog detail + play video. Tombol akses di Kelola Plugin"
            ))

            list.add(BugNote(
                "Plugin System Tahap 2 ditambahkan (loader + UI)",
                "Tahap 1 hanya fondasi interface. User minta bisa tambah plugin dari dalam app tanpa manual taruh di folder",
                "Fixed", "17 Sep 2026", "17 Sep 2026",
                "PluginLoader (DexClassLoader) + PluginManifest (parse manifest.json dari jar) + PluginStorage (SharedPreferences) + PluginManagerActivity (UI) + tombol di menu Sumber + icon puzzle"
            ))

            list.add(BugNote(
                "V.1.16.67 paksa landscape jadi TV - rusak tampilan",
                "Patch V.1.16.67 paksa HP landscape jadi mode TV, aktifkan focus ring + auto-focus. Tampilan landscape yang rapi jadi berantakan (border tebal di tombol, auto-highlight). User minta Android TV dibuat SAMA dengan landscape, bukan sebaliknya",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Revert V.1.16.67 di V.1.16.69: hapus isLandscape dari DisplayModeStore, onConfigurationChanged jadi no-op, applyTvFocusRingsIfTv jadi no-op, hapus configChanges MainActivity"
            ))

            list.add(BugNote(
                "Smart Playlist Online tidak tampilkan semua sumber bersamaan",
                "Saat Smart Playlist ON dari tab Sumber, activeSource masih 'offline' (default). Akibatnya kategoriOn=false -> renderKategori tidak jalan -> tidak semua sumber (Drive+Streaming+Server) muncul bersamaan",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Set activeSource='external' saat Smart ON (di dialog TERAPKAN + di switchSmart listener). Foto juga tidak dibuang di renderKategori"
            ))

            list.add(BugNote(
                "MainActivity tidak punya configChanges di manifest",
                "Saat rotate layar, MainActivity di-recreate (state hilang, layout flicker). VideoPlayerActivity sudah punya, MainActivity belum",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Tambah configChanges di MainActivity manifest + override onConfigurationChanged"
            ))

            list.add(BugNote(
                "Kode 808 tidak terdaftar di SecretCodeRegistry",
                "Section 3 & 7b ProjectExportHelper sebut kode 808 (DownloadListActivity) tapi tidak ada di SecretCodeRegistry.getAll() — jadi tidak muncul di panduan Section 7",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Tambah entry kode 808 'Daftar Download' di SecretCodeRegistry.getAll() sebelum komentar Kode tersembunyi"
            ))

            list.add(BugNote(
                "Section 9 ProjectExportHelper hardcoded & dobel dengan Section 6",
                "Section 9 manual tulis '- Android TV support' padahal di Section 6 (auto-pull ChangelogStore) sudah tertulis 'Android TV Support - Selesai'. Kontradiksi di panduan export",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Buat RoadmapStore.java untuk auto-pull Section 9. Hapus 'Android TV support' dari roadmap karena sudah selesai"
            ))

            list.add(BugNote(
                "VersionName tidak mengikuti tanggal realtime",
                "Format V... — komponen tanggal tidak auto-update saat ganti tanggal. Selama 16 Sep 2026, versionName masih 1.14.65 (tanggal 14) padahal build tanggal 16",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Sesuaikan versionName manual ke 1.16.66. Ke depannya, saat build baru, cek tanggal realtime + build_count hari itu + patch_inti (reset saat ganti bulan)"
            ))

            list.add(BugNote(
                "Multi-source: sourceTitle null di semua item + data dobel",
                "Auto-save ExternalMediaStore.gantiSemua tidak set sourceTitle di item. Akibatnya renderSmartGroups fallback ke 'Lainnya' — Drive & Streaming tercampur. Data lama juga ada dalam 2 format (migrated_* & src_) dan baru (src_ _)",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Fix 1: set sourceTitle dari sourceLabel di auto-save. Fix 2: tombol Reset Semua di Debug dialog. Fix 3: auto-deduplicate saat onCreate (hapus migrated_* & src_ lama, simpan src_ _)"
            ))

            list.add(BugNote(
                "Landscape masih ada btnZoomUI + btnDataSaver (sudah dihapus di Java)",
                "V.1.14.42 hapus tombol Zoom UI + Data Saver di portrait, tapi landscape belum dibersihkan. Tombol muncul tapi tidak berfungsi (findViewById null)",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Hapus btnZoomUI + btnDataSaver dari layout-land/content_profil.xml. Tambah tombol Debug Multi-Source di portrait + landscape"
            ))

            list.add(BugNote(
                "Multi-source cuma tampil 1 (sourceId tabrakan)",
                "sourceId = src_ + hash(label). Label Drive 'Drive Folder' & Server 'server' hardcoded -> semua Drive/Server punya sourceId sama -> saling timpa",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Ganti sourceId jadi src_ + System.currentTimeMillis() + _ + abs(hash(label)). Tiap penambahan sumber baru dapat sourceId unik walau label sama"
            ))

            list.add(BugNote(
                "Smart Playlist cuma tampil 1 sumber — item pertama hilang",
                "MultiSourceStore.save tidak menulis SEP_ITEM sebelum item pertama. Akibatnya topParts[0] berisi label+type+item1. parseData loop dari i=1 -> item1 hilang",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "save: selalu tulis SEP_ITEM sebelum tiap item. Format baru: label\\u0003type\\u0002item1\\u0002item2. parseData: topParts[0] = header saja, items dari [1..]"
            ))

            list.add(BugNote(
                "Smart Playlist & Media Scan tidak jalan di landscape/Android TV",
                "layout-land/content_sumber.xml masih pakai switchKategori (nama lama). Tidak ada switchSmart -> handler Java tidak temukan switch. Layout-land/content_beranda.xml tidak punya overlayExpand/gridExpand/btnShrink -> tap expand kategori crash/null",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Update layout-land/content_sumber.xml: ganti switchKategori -> switchSmart + tambah tvSmartMode. Update layout-land/content_beranda.xml: tambah overlayExpand + gridExpand + btnShrink + tvExpandTitle. Aturan baru: setiap fitur WAJIB support portrait + landscape"
            ))

            list.add(BugNote(
                "Smart Playlist cuma tampil 1 sumber (yang terakhir)",
                "ExternalMediaStore cuma punya 1 slot. Setiap ganti sumber -> gantiSemua() menimpa. Smart Playlist jalan di atas 1 sumber",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Buat MultiSourceStore.java (multi-slot pakai SharedPreferences). ExternalMediaStore.gantiSemua auto-save ke MultiSourceStore. renderBeranda: kategoriOn ambil dari MultiSourceStore.getAllMerged. Migrasi data lama otomatis di onCreate"
            ))

            list.add(BugNote(
                "Media Scan switch terpisah dari Sumber Offline",
                "Media Scan punya switch sendiri di menu Sumber, padahal Media Scan cuma berguna kalau Offline ON",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Hapus switch Media Scan dari XML. Gabung ke popup yang muncul saat switchOffline ON. Popup isi switch + pilih mode (Folder/Date/Size/Duration)"
            ))

            list.add(BugNote(
                "Smart Playlist terlihat seperti playlist manual (2 opsi)",
                "Nama 'Smart Playlist' tapi user harus pilih manual antara Otomatis / Kategori. Kontradiksi — smart harusnya otomatis pilih semua",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Ganti dialog jadi 1 switch ON/OFF. ON -> smartMode = 'otomatis' (gabung semua mode). OFF -> reset"
            ))

            list.add(BugNote(
                "Mode Otomatis Smart Playlist kosong saat tidak ada keyword match",
                "Jalur renderKategori untuk mode Otomatis tidak punya fallback. File Drive/Streaming/Server tanpa keyword Film/Anime/TV -> kategoriMap kosong -> groups kosong -> tidak ada chip",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Tambah fallback sourceTitle -> Lainnya di jalur kategoriMap (mode Otomatis). Sinkron dengan fallback yang sudah ada di renderSmartGroups (mode Kategori)"
            ))

            list.add(BugNote(
                "Drive/Streaming kosong saat kedua switch OFF",
                "Empty state check (!offlineOn && !smartOn) tidak cek activeSource. Kalau user matikan Offline + Smart, lalu buka Drive, combinedMedia ke-clear",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Tambah cek isOfflineSource di empty state. Kalau activeSource bukan offline, tetap render grid dari ExternalMediaStore"
            ))

            list.add(BugNote(
                "Drive kosong walau cache ada 31 file",
                "smartMode sisa nilai Media Scan (folder/date/size/duration) trigger Smart Playlist di sumber online. Filter folderPath null untuk URL Drive -> semua tersaring -> kosong",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "kategoriOn cek mode valid: cuma otomatis/kategori yang trigger di online. Mode folder/date/size/duration khusus offline"
            ))

            list.add(BugNote(
                "Video tanpa kategori keyword 'nyasar' di Smart Playlist",
                "Video dari Drive/Streaming/Server dengan nama file acak tidak punya kategori (Film/Anime/TV) -> hilang dari tampilan Smart Playlist mode Kategori",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Tambah field sourceTitle di MediaItem. M3uParser parse group-title dari #EXTINF. Drive set sourceTitle = 'Drive', Server = 'Server'. renderSmartGroups fallback: kalau CategoryHelper tidak match -> pakai sourceTitle -> kalau kosong -> 'Lainnya'"
            ))

            list.add(BugNote(
                "Smart Playlist dan Media Scan bercampur di satu menu",
                "Smart Playlist punya 5 opsi (Folder/Date/Size/Duration) padahal untuk sumber online yang tidak punya folder/tanggal. Media Scan by Folder juga di menu Sumber bukan di dalam Sumber Offline",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Smart Playlist (online): 2 opsi (Otomatis + Kategori). Media Scan (offline): popup pilih mode (Folder/Date/Size/Duration). Mutex antar keduanya"
            ))

            list.add(BugNote(
                "Drive folder sering timeout, video/foto tidak tampil",
                "Jsoup timeout 5s terlalu pendek untuk folder besar / koneksi lambat. Google Drive scraping rapuh kalau HTML berubah",
                "Fixed", "16 Sep 2026", "16 Sep 2026",
                "Timeout 5s -> 20s. 3-layer fallback: (1) _DRIVE_ivd JSON, (2) DOM parse HTML halaman utama, (3) embeddedfolderview. Tambah library shimmer untuk loading indicator patch berikutnya"
            ))

            list.add(BugNote(
                "Sumber online (Drive/Streaming/Server) kosong setelah Smart Playlist",
                "Filter smartMode di renderBeranda aktif tanpa cek activeSource. Saat user pindah ke sumber online, smartMode masih berisi nilai (folder/date/size/duration) → filter folderPath di URL online = null → semua tersaring",
                "Fixed", "15 Sep 2026", "16 Sep 2026",
                "renderBeranda: tambah cek activeSource di kategoriOn. Filter smartMode hanya aktif kalau activeSource = offline. Untuk sumber online, tampil grid biasa"
            ))

            list.add(BugNote(
                "HDR belum terdeteksi di video player",
                "Video HDR tidak dikasih badge/indikator. User tidak tahu apakah video HDR atau SDR",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Deteksi HDR via Format.colorInfo.colorTransfer (ST2084/HLG). Tambah tab Tampilan di setting player dengan toggle HDR + status. Tone mapping real ditunda karena Media3 1.4.1 belum support native API"
            ))

            list.add(BugNote(
                "Media3 versi 1.2.1 terlalu lama — tidak support media3-effect",
                "Media3 1.2.1 tidak punya library media3-effect untuk HDR, Brightness/Contrast, Sharpen, Anime4K. Fitur-fitur itu butuh minimal Media3 1.3.0",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Upgrade semua modul Media3 dari 1.2.1 ke 1.4.1 (exoplayer, ui, common, exoplayer-hls, session, database) + tambah media3-effect 1.4.1"
            ))

            list.add(BugNote(
                "Media Scan by Folder belum ada",
                "User minta tampilan offline Beranda bisa grouping per folder (Camera, Screenshots, dll) dengan nama folder terakhir",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Query DATA column di MediaStore (batch, sekaligus dengan SIZE+DURATION — tidak ANR). extractFolderName ambil nama folder terakhir dari path. Switch di Sumber + popup konfirmasi. renderFolderList + addFolderRow untuk tampilan"
            ))

            list.add(BugNote(
                "Smart Playlist tidak jelas + tidak berguna",
                "Dialog terlalu banyak opsi duplikat (Otomatis + Kategori). Date/Duration tidak ada pengaturan range. Tombol Data Saver & Zoom UI tidak berguna",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Gabung Otomatis + Kategori. Sub-dialog Date (kelompok/7d/30d/6m/1y) dan Duration (pendek/sedang/panjang/<1m/>1h). Hapus Data Saver + Zoom UI"
            ))

            list.add(BugNote(
                "Semua switch OFF tapi Beranda masih tampil + freeze di mode Date/Folder/Size/Duration",
                "1) Kedua switch OFF, Beranda masih tampil grid karena combinedMedia tidak di-clear. 2) addKategoriRow membuat 1 thread baru PER ITEM -> kalau kategori punya ribuan video, freeze + forceclose",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "1) renderBeranda cek offline_enabled + smartMode, kalau keduanya OFF -> clear media + tampil empty state. 2) addKategoriRow limit maxItems = 30 per baris"
            ))

            list.add(BugNote(
                "User tidak bisa screenshot karena FLAG_SECURE permanen",
                "Patch banking cover (V.1.14.34) mengaktifkan FLAG_SECURE tanpa toggle. User tidak bisa screenshot untuk debugging",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "PrivacyStore (in-memory) untuk on/off. Toggle via Profil + kode rahasia 0000+DEL 3x+=. SessionManager cek PrivacyStore sebelum apply FLAG_SECURE. Default ON, reset saat app di-kill"
            ))

            list.add(BugNote(
                "Fitur kategori lama hilang setelah Smart Playlist",
                "Waktu ganti toggle Mat Kategori jadi tombol Smart Playlist, fitur lama (chip kategori + Riwayat + Populer + Terbaru) hilang dari XML. User capek design",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Restore sebagai mode Otomatis di popup. Ganti tombol jadi Switch on/off. Mutex dengan Offline. Default Offline ON. Gaya baris dipertahankan"
            ))

            list.add(BugNote(
                "Smart Playlist: perlu mode grouping (folder/date/size/duration)",
                "Toggle Mat Kategori di menu Sumber tidak fleksibel. User perlu 1 tombol pintar untuk grouping multi-mode: Kategori, Folder, Date, Size, Duration",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Ganti toggle Mat Kategori jadi tombol Smart Playlist. Dialog 6 mode. MediaItem tambah size + duration. Query MediaStore sekaligus (bukan per item — cegah ANR). Buat SmartPlaylistHelper.java. Mode Kategori pakai CategoryHelper existing"
            ))

            list.add(BugNote(
                "ANR: aplikasi hang saat buka (layar gelap lalu force close)",
                "Setelah patch Auto-Scan, loadOfflineMedia() panggil extractFolderPathFromUri() PER ITEM. Method itu query MediaStore lagi. Untuk 6000+ foto = 6000 query di main thread = ANR",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Ganti extractFolderPathFromUri (query per item) jadi parseFolderFromUri (tanpa query, langsung dari uri.getPath()). Query tambahan dihapus, main thread tidak diblokir"
            ))

            list.add(BugNote(
                "Media scan lambat + bug query Audio duplikat",
                "loadOfflineMedia() query Audio dijalankan 2x (duplikat). Tidak ada cache scan — setiap buka app scan ulang. MediaItem tidak punya folderPath",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Hapus blok query Audio kedua. Buat MediaScanCache (cache 5 menit). Auto-refresh di onResume kalau cache expired. Tambah field folderPath di MediaItem + isi dari kolom DATA MediaStore"
            ))

            list.add(BugNote(
                "Buffering lama saat playback video (single & multiview)",
                "ExoPlayer pakai LoadControl default dengan buffer kecil. Multiview dengan 4 player streaming bareng tanpa limit resolusi — bandwidth habis",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Custom LoadControl: single (15s-120s, 50MB, 1080p), multiview (10s-30s, 20MB per player, 720p). Pakai setPrioritizeTimeOverSizeThresholds(true). Batasi resolusi via TrackSelectionParameters.setMaxVideoSize()"
            ))

            list.add(BugNote(
                "Privacy tidak banking-style — konten terlihat di Recents",
                "Saat app ke background, konten kelihatan di Recents preview. Tidak ada overlay cover foto. Tidak ada block screenshot",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Buat CoverActivity full-screen dengan foto cover portrait/landscape. SessionManager redirect ke Cover saat app ke background (bukan langsung Calculator). Tap cover -> PIN. FLAG_SECURE di semua activity non-whitelist untuk block screenshot + Recents. Launcher icon diganti 512x512 dari icon_terbaru.png"
            ))

            list.add(BugNote(
                "Multiview minim kontrol per slot + audio tidak bisa multi",
                "Multiview tidak punya tombol play/pause, mute, fullscreen, dan replay per slot. Tidak bisa drag & drop swap. Audio hanya 1 slot (yang aktif), tidak bisa dengar bersamaan",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "Tambah tombol playPause (center, auto-hide 3s), muteBtn (top-start), fullscreen (top-end), replay (muncul saat STATE_ENDED). Long-press slot + startDragAndDrop untuk swap. Audio focus: tap slot = auto-mute semua + unmute slot itu. Tap mute/unmute manual = multi-audio (bisa dengar bersamaan)"
            ))

            list.add(BugNote(
                "Multiview: slot lama blank saat tambah slot baru, foto tidak tampil",
                "Tiap kali pick slot, MultiviewActivity di-finish dan di-start ulang -> slot lama hilang karena ExoPlayer di-release. Foto juga tidak tampil karena ExoPlayer cuma render video",
                "Fixed", "15 Sep 2026", "15 Sep 2026",
                "MultiviewPickHolder simpan slotUrls[4] + slotTypes[4] persistent. MultiviewActivity load semua slot dari holder saat onCreate. Foto pakai ImageView (image1-4 di XML), video pakai ExoPlayer, auto-detect dari tipe MediaItem + extension URL"
            ))

            list.add(BugNote(
                "Multiview pick slot buka file picker Android, bukan playlist Beranda",
                "Desain awal: tap Slot 1-4 buka file picker Android (ACTION_OPEN_DOCUMENT). User maunya pilih dari playlist Beranda (semua sumber: offline/streaming/drive/server/manual), bukan cari file manual",
                "Fixed", "14 Sep 2026", "14 Sep 2026",
                "Buat MultiviewPickHolder static. Tap Slot di Multiview -> finish + set pickSlot -> MainActivity onResume kasih toast & switch ke Beranda -> intercept onThumbnailClick -> deliver URL ke holder -> launch Multiview lagi -> onResume load ke slot"
            ))

            list.add(BugNote(
                "Crash FrameLayout tidak bisa di-cast ke LinearLayout",
                "Setelah nav redesign (V.1.14.9), btnBeranda/btnSumber/btnProfil diubah dari LinearLayout ke FrameLayout di XML, tapi deklarasi Java masih LinearLayout. Menyebabkan ClassCastException saat MainActivity dibuka",
                "Fixed", "13 Sep 2026", "13 Sep 2026",
                "Ganti deklarasi private LinearLayout btnBeranda, btnSumber, btnProfil menjadi private FrameLayout. Pastikan import android.widget.FrameLayout ada"
            ))

            list.add(BugNote(
                "Nav bullet terpotong & icon tab nonaktif hilang",
                "Bullet biru di nav terpotong karena clipChildren=true di parent. Setelah fix clip, muncul bug baru: setVisibility(bullet) saat tab nonaktif membuat icon di dalamnya juga hilang",
                "Fixed", "13 Sep 2026", "14 Sep 2026",
                "Tambah clipChildren=false + clipToPadding=false di root FrameLayout & child LinearLayout. Ganti setVisibility dengan setBackgroundResource (jadi hanya background bullet yang hilang, icon tetap ada)"
            ))

            return list
        }
    }
}
