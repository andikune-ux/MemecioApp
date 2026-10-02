package com.memecio.app;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * BUG NOTES STORE — CATATAN BUG & KENDALA PERMANEN
 * ============================================================
 *
 * PENTING UNTUK AI / DEVELOPER LAIN:
 *
 * File ini adalah "otak" catatan bug & kendala aplikasi Memec.io.
 * Setiap kali AI assistant menemukan bug baru, memperbaiki bug,
 * atau menemukan kendala teknis, WAJIB update file ini:
 *
 * 1. Bug baru ditemukan   -> tambah entry di getBugsOpen()
 * 2. Bug sudah diperbaiki -> pindah dari getBugsOpen() ke getBugsFixed() + tanggal
 * 3. Kendala teknis baru  -> tambah di getLimitations()
 *
 * Format tanggal: "dd MMM yyyy" (contoh: "12 Sep 2026")
 *
 * Setiap kali update file ini, WAJIB juga:
 * 1. Bump versionName di build.gradle
 * 2. Tambah BuildEntry di ChangelogStore.java
 * 3. Update getFeatureStatus() kalau ada fitur baru
 *
 * Tujuan: agar AI assistant lain bisa langsung nyambung tanpa banyak tanya.
 * ============================================================
 */
public class BugNotesStore {

    public static class BugNote {
        public String title;
        public String description;
        public String status;
        public String dateFound;
        public String dateFixed;
        public String solution;

        public BugNote(String title, String description, String status,
                       String dateFound, String dateFixed, String solution) {
            this.title = title;
            this.description = description;
            this.status = status;
            this.dateFound = dateFound;
            this.dateFixed = dateFixed;
            this.solution = solution;
        }
    }

    public static class Limitation {
        public String title;
        public String description;

        public Limitation(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public static List<BugNote> getBugsFixed() {
        List<BugNote> list = new ArrayList<>();

        list.add(new BugNote(
            "Smart Playlist terlihat seperti playlist manual (2 opsi)",
            "Nama 'Smart Playlist' tapi user harus pilih manual antara Otomatis / Kategori. Kontradiksi — smart harusnya otomatis pilih semua",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "Ganti dialog jadi 1 switch ON/OFF. ON -> smartMode = 'otomatis' (gabung semua mode). OFF -> reset"
        ));

        list.add(new BugNote(
            "Mode Otomatis Smart Playlist kosong saat tidak ada keyword match",
            "Jalur renderKategori untuk mode Otomatis tidak punya fallback. File Drive/Streaming/Server tanpa keyword Film/Anime/TV -> kategoriMap kosong -> groups kosong -> tidak ada chip",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "Tambah fallback sourceTitle -> Lainnya di jalur kategoriMap (mode Otomatis). Sinkron dengan fallback yang sudah ada di renderSmartGroups (mode Kategori)"
        ));

        list.add(new BugNote(
            "Drive/Streaming kosong saat kedua switch OFF",
            "Empty state check (!offlineOn && !smartOn) tidak cek activeSource. Kalau user matikan Offline + Smart, lalu buka Drive, combinedMedia ke-clear",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "Tambah cek isOfflineSource di empty state. Kalau activeSource bukan offline, tetap render grid dari ExternalMediaStore"
        ));

        list.add(new BugNote(
            "Drive kosong walau cache ada 31 file",
            "smartMode sisa nilai Media Scan (folder/date/size/duration) trigger Smart Playlist di sumber online. Filter folderPath null untuk URL Drive -> semua tersaring -> kosong",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "kategoriOn cek mode valid: cuma otomatis/kategori yang trigger di online. Mode folder/date/size/duration khusus offline"
        ));

        list.add(new BugNote(
            "Video tanpa kategori keyword 'nyasar' di Smart Playlist",
            "Video dari Drive/Streaming/Server dengan nama file acak tidak punya kategori (Film/Anime/TV) -> hilang dari tampilan Smart Playlist mode Kategori",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "Tambah field sourceTitle di MediaItem. M3uParser parse group-title dari #EXTINF. Drive set sourceTitle = 'Drive', Server = 'Server'. renderSmartGroups fallback: kalau CategoryHelper tidak match -> pakai sourceTitle -> kalau kosong -> 'Lainnya'"
        ));

        list.add(new BugNote(
            "Smart Playlist dan Media Scan bercampur di satu menu",
            "Smart Playlist punya 5 opsi (Folder/Date/Size/Duration) padahal untuk sumber online yang tidak punya folder/tanggal. Media Scan by Folder juga di menu Sumber bukan di dalam Sumber Offline",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "Smart Playlist (online): 2 opsi (Otomatis + Kategori). Media Scan (offline): popup pilih mode (Folder/Date/Size/Duration). Mutex antar keduanya"
        ));

        list.add(new BugNote(
            "Drive folder sering timeout, video/foto tidak tampil",
            "Jsoup timeout 5s terlalu pendek untuk folder besar / koneksi lambat. Google Drive scraping rapuh kalau HTML berubah",
            "Fixed",
            "16 Sep 2026",
            "16 Sep 2026",
            "Timeout 5s -> 20s. 3-layer fallback: (1) _DRIVE_ivd JSON, (2) DOM parse HTML halaman utama, (3) embeddedfolderview. Tambah library shimmer untuk loading indicator patch berikutnya"
        ));

        list.add(new BugNote(
            "Sumber online (Drive/Streaming/Server) kosong setelah Smart Playlist",
            "Filter smartMode di renderBeranda aktif tanpa cek activeSource. Saat user pindah ke sumber online, smartMode masih berisi nilai (folder/date/size/duration) → filter folderPath di URL online = null → semua tersaring",
            "Fixed",
            "15 Sep 2026",
            "16 Sep 2026",
            "renderBeranda: tambah cek activeSource di kategoriOn. Filter smartMode hanya aktif kalau activeSource = offline. Untuk sumber online, tampil grid biasa"
        ));

        list.add(new BugNote(
            "HDR belum terdeteksi di video player",
            "Video HDR tidak dikasih badge/indikator. User tidak tahu apakah video HDR atau SDR",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Deteksi HDR via Format.colorInfo.colorTransfer (ST2084/HLG). Tambah tab Tampilan di setting player dengan toggle HDR + status. Tone mapping real ditunda karena Media3 1.4.1 belum support native API"
        ));

        list.add(new BugNote(
            "Media3 versi 1.2.1 terlalu lama — tidak support media3-effect",
            "Media3 1.2.1 tidak punya library media3-effect untuk HDR, Brightness/Contrast, Sharpen, Anime4K. Fitur-fitur itu butuh minimal Media3 1.3.0",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Upgrade semua modul Media3 dari 1.2.1 ke 1.4.1 (exoplayer, ui, common, exoplayer-hls, session, database) + tambah media3-effect 1.4.1"
        ));

        list.add(new BugNote(
            "Media Scan by Folder belum ada",
            "User minta tampilan offline Beranda bisa grouping per folder (Camera, Screenshots, dll) dengan nama folder terakhir",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Query DATA column di MediaStore (batch, sekaligus dengan SIZE+DURATION — tidak ANR). extractFolderName ambil nama folder terakhir dari path. Switch di Sumber + popup konfirmasi. renderFolderList + addFolderRow untuk tampilan"
        ));

        list.add(new BugNote(
            "Smart Playlist tidak jelas + tidak berguna",
            "Dialog terlalu banyak opsi duplikat (Otomatis + Kategori). Date/Duration tidak ada pengaturan range. Tombol Data Saver & Zoom UI tidak berguna",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Gabung Otomatis + Kategori. Sub-dialog Date (kelompok/7d/30d/6m/1y) dan Duration (pendek/sedang/panjang/<1m/>1h). Hapus Data Saver + Zoom UI"
        ));

        list.add(new BugNote(
            "Semua switch OFF tapi Beranda masih tampil + freeze di Date/Folder/Size/Duration",
            "1) Ketika Offline + Smart Playlist OFF, Beranda masih tampil grid karena combinedMedia tidak di-clear. 2) addKategoriRow membuat 1 thread baru PER ITEM -> kalau kategori punya ribuan video, freeze + forceclose",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "1) renderBeranda cek offline_enabled + smartMode, kalau keduanya OFF -> clear media + tampil empty state. 2) addKategoriRow limit maxItems = 30 per baris"
        ));

        list.add(new BugNote(
            "User tidak bisa screenshot karena FLAG_SECURE permanen",
            "Patch banking cover (V.1.14.34) mengaktifkan FLAG_SECURE tanpa toggle. User tidak bisa screenshot untuk debugging",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "PrivacyStore (in-memory) untuk on/off. Toggle via Profil + kode rahasia 0000+DEL 3x+=. SessionManager cek PrivacyStore sebelum apply FLAG_SECURE. Default ON, reset saat app di-kill"
        ));

        list.add(new BugNote(
            "Fitur kategori lama hilang setelah Smart Playlist",
            "Waktu ganti toggle Mat Kategori jadi tombol Smart Playlist, fitur lama (chip kategori + Riwayat + Populer + Terbaru) hilang dari XML. User capek design",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Restore sebagai mode Otomatis di popup. Ganti tombol jadi Switch on/off. Mutex dengan Offline. Default Offline ON. Gaya baris dipertahankan"
        ));

        list.add(new BugNote(
            "Smart Playlist: perlu mode grouping (folder/date/size/duration)",
            "Toggle Mat Kategori di menu Sumber tidak fleksibel. User perlu 1 tombol pintar untuk grouping multi-mode: Kategori, Folder, Date, Size, Duration",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Ganti toggle Mat Kategori jadi tombol Smart Playlist. Dialog 6 mode. MediaItem tambah size + duration. Query MediaStore sekaligus (bukan per item — cegah ANR). Buat SmartPlaylistHelper.java. Mode Kategori pakai CategoryHelper existing"
        ));

        list.add(new BugNote(
            "ANR: aplikasi hang saat buka (layar gelap lalu force close)",
            "Setelah patch Auto-Scan, loadOfflineMedia() panggil extractFolderPathFromUri() PER ITEM. Method itu query MediaStore lagi. Untuk 6000+ foto = 6000 query di main thread = ANR",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Ganti extractFolderPathFromUri (query per item) jadi parseFolderFromUri (tanpa query, langsung dari uri.getPath()). Query tambahan dihapus, main thread tidak diblokir"
        ));

        list.add(new BugNote(
            "Media scan lambat + bug query Audio duplikat",
            "loadOfflineMedia() query Audio dijalankan 2x (duplikat). Tidak ada cache scan — setiap buka app scan ulang. MediaItem tidak punya folderPath",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Hapus blok query Audio kedua. Buat MediaScanCache (cache 5 menit). Auto-refresh di onResume kalau cache expired. Tambah field folderPath di MediaItem + isi dari kolom DATA MediaStore"
        ));

        list.add(new BugNote(
            "Buffering lama saat playback video (single & multiview)",
            "ExoPlayer pakai LoadControl default dengan buffer kecil. Multiview dengan 4 player streaming bareng tanpa limit resolusi — bandwidth habis",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Custom LoadControl: single (15s-120s, 50MB, 1080p), multiview (10s-30s, 20MB per player, 720p). Pakai setPrioritizeTimeOverSizeThresholds(true). Batasi resolusi via TrackSelectionParameters.setMaxVideoSize()"
        ));

        list.add(new BugNote(
            "Privacy tidak banking-style — konten terlihat di Recents",
            "Saat app ke background, konten kelihatan di Recents preview. Tidak ada overlay cover foto. Tidak ada block screenshot",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Buat CoverActivity full-screen dengan foto cover portrait/landscape. SessionManager redirect ke Cover saat app ke background (bukan langsung Calculator). Tap cover -> PIN. FLAG_SECURE di semua activity non-whitelist untuk block screenshot + Recents. Launcher icon diganti 512x512 dari icon_terbaru.png"
        ));

        list.add(new BugNote(
            "Multiview minim kontrol per slot + audio tidak bisa multi",
            "Multiview tidak punya tombol play/pause, mute, fullscreen, dan replay per slot. Tidak bisa drag & drop swap. Audio hanya 1 slot (yang aktif), tidak bisa dengar bersamaan",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "Tambah tombol playPause (center, auto-hide 3s), muteBtn (top-start), fullscreen (top-end), replay (muncul saat STATE_ENDED). Long-press slot + startDragAndDrop untuk swap. Audio focus: tap slot = auto-mute semua + unmute slot itu. Tap mute/unmute manual = multi-audio (bisa dengar bersamaan)"
        ));

        list.add(new BugNote(
            "Multiview: slot lama blank saat tambah slot baru, foto tidak tampil",
            "Tiap kali pick slot, MultiviewActivity di-finish dan di-start ulang -> slot lama hilang karena ExoPlayer di-release. Foto juga tidak tampil karena ExoPlayer cuma render video",
            "Fixed",
            "15 Sep 2026",
            "15 Sep 2026",
            "MultiviewPickHolder simpan slotUrls[4] + slotTypes[4] persistent. MultiviewActivity load semua slot dari holder saat onCreate. Foto pakai ImageView (image1-4 di XML), video pakai ExoPlayer, auto-detect dari tipe MediaItem + extension URL"
        ));

        list.add(new BugNote(
            "Multiview pick slot buka file picker Android, bukan playlist Beranda",
            "Desain awal: tap Slot 1-4 buka file picker Android (ACTION_OPEN_DOCUMENT). User maunya pilih dari playlist Beranda (semua sumber: offline/streaming/drive/server/manual), bukan cari file manual",
            "Fixed",
            "14 Sep 2026",
            "14 Sep 2026",
            "Buat MultiviewPickHolder static. Tap Slot di Multiview -> finish + set pickSlot -> MainActivity onResume kasih toast & switch ke Beranda -> intercept onThumbnailClick -> deliver URL ke holder -> launch Multiview lagi -> onResume load ke slot"
        ));

        list.add(new BugNote(
            "Crash FrameLayout tidak bisa di-cast ke LinearLayout",
            "Setelah nav redesign (V.1.14.9), btnBeranda/btnSumber/btnProfil diubah dari LinearLayout ke FrameLayout di XML, tapi deklarasi Java masih LinearLayout. Menyebabkan ClassCastException saat MainActivity dibuka",
            "Fixed",
            "13 Sep 2026",
            "13 Sep 2026",
            "Ganti deklarasi private LinearLayout btnBeranda, btnSumber, btnProfil menjadi private FrameLayout. Pastikan import android.widget.FrameLayout ada"
        ));

        list.add(new BugNote(
            "Nav bullet terpotong & icon tab nonaktif hilang",
            "Bullet biru di nav terpotong karena clipChildren=true di parent. Setelah fix clip, muncul bug baru: setVisibility(bullet) saat tab nonaktif membuat icon di dalamnya juga hilang",
            "Fixed",
            "13 Sep 2026",
            "14 Sep 2026",
            "Tambah clipChildren=false + clipToPadding=false di root FrameLayout & child LinearLayout. Ganti setVisibility dengan setBackgroundResource (jadi hanya background bullet yang hilang, icon tetap ada)"
        ));

        list.add(new BugNote(
            "Video gagal play dari poster (balik ke poster)",
            "Saat di tampilan poster lalu tekan Play, video sering gagal play dan balik ke poster. Penyebab: VideoPlayerActivity tidak punya configChanges di manifest, sehingga onVideoSizeChanged memaksa set orientasi lalu activity recreate",
            "Fixed",
            "14 Sep 2026",
            "14 Sep 2026",
            "Tambah android:configChanges=orientation|screenSize|keyboardHidden|screenLayout|smallestScreenSize di manifest. Guard onVideoSizeChanged: cek getRequestedOrientation() != target sebelum set. Guard onCreate: hanya show detail overlay saat savedInstanceState == null"
        ));

        list.add(new BugNote(
            "Nav tidak overlay — grid tidak extend ke belakang tombol",
            "Grid di Beranda tidak bisa extend ke belakang nav bawah (portrait) atau nav kiri (landscape), karena nav punya slot sendiri di LinearLayout. Tidak bisa overlay",
            "Fixed",
            "14 Sep 2026",
            "14 Sep 2026",
            "Portrait: pindah nav keluar dari LinearLayout ke root FrameLayout (layout_gravity=bottom). Landscape: ubah root LinearLayout ke FrameLayout, nav_col jadi overlay (layout_gravity=start). Grid extend sampai ke belakang nav, padding GridView 88dp portrait / 56dp landscape + clipToPadding=false"
        ));

        list.add(new BugNote(
            "Landscape — nav col dan filter col numpuk",
            "Setelah restrukturisasi nav overlay, nav col (48dp, gravity=start) di activity_main.xml dan filter col (40dp) di content_beranda.xml dua-duanya di kiri. Sehingga tombol numpuk",
            "Fixed",
            "14 Sep 2026",
            "14 Sep 2026",
            "Tambah layout_marginStart=54dp di filter col content_beranda landscape, biar berada di sebelah kanan nav col (tidak overlap)"
        ));

        list.add(new BugNote(
            "Video gagal play dari poster (balik ke poster)",
            "Saat di tampilan poster lalu tekan Play, video sering gagal play dan balik ke poster. Penyebab: VideoPlayerActivity tidak punya configChanges di manifest. onVideoSizeChanged memaksa set orientasi -> activity recreate -> ExoPlayer di-release -> player re-init gagal",
            "Fixed",
            "14 Sep 2026",
            "14 Sep 2026",
            "Tambah android:configChanges=orientation|screenSize|keyboardHidden|screenLayout|smallestScreenSize di manifest VideoPlayerActivity. Guard onVideoSizeChanged: cek getRequestedOrientation() != target sebelum set. Guard onCreate: hanya show detail overlay saat savedInstanceState == null"
        ));

        list.add(new BugNote(
            "Nav bottom tidak seragam (portrait + landscape)",
            "Portrait: hanya tombol aktif yang punya pill biru, non-aktif polos, ada celah 6dp. Landscape: nav vertikal cuma 3 ikon di atas, sisa kolom kosong ke bawah",
            "Fixed",
            "13 Sep 2026",
            "13 Sep 2026",
            "Buat bg_nav_pill_active.xml & bg_nav_pill_inactive.xml dengan efek glass (gradien putih). Portrait: hapus margin 6dp, container padding 12dp -> 4dp. Landscape: tombol height 40dp -> 0dp+weight=1 biar bagi rata tinggi. MainActivity: semua tombol dapat pill (aktif biru, nonaktif abu glass)"
        ));

        list.add(new BugNote(
            "Crash foto di PreviewImageActivity",
            "Foto crash saat dibuka karena setImageURI tanpa try-catch dan tanpa sampling bitmap",
            "Fixed",
            "11 Sep 2026",
            "12 Sep 2026",
            "Refactor loadCurrent(): pakai BitmapFactory dengan inSampleSize, recycle bitmap, guard null"
        ));

        list.add(new BugNote(
            "Crash MainActivity saat Mode TV aktif",
            "NullPointerException di setupSidebar — sidebarContainer null saat activity di-recreate",
            "Fixed",
            "11 Sep 2026",
            "11 Sep 2026",
            "Guard null: if (sidebarList == null || sidebarContainer == null) return; Pindahkan applyDisplayMode ke onResume"
        ));

        list.add(new BugNote(
            "Crash VideoPlayerActivity saat buka video",
            "InflateException: gagal resolve atribut ?attr/selectableItemBackgroundBorderless di btnFullscreen",
            "Fixed",
            "11 Sep 2026",
            "11 Sep 2026",
            "Ganti ?attr/selectableItemBackgroundBorderless ke @android:color/transparent"
        ));

        list.add(new BugNote(
            "Video tidak muncul di filter Semua",
            "Video tertimbun di bawah 6000+ foto karena urutan scan default",
            "Fixed",
            "12 Sep 2026",
            "12 Sep 2026",
            "Tambah dateAdded ke MediaItem, default sort jadi tanggal_baru (terbaru dulu)"
        ));

        list.add(new BugNote(
            "Swipe dari foto ke video tidak berfungsi",
            "PreviewImageActivity dan VideoPlayerActivity punya playlist terpisah, tidak bisa cross-tipe",
            "Fixed",
            "12 Sep 2026",
            "12 Sep 2026",
            "Buat MixedPlaylistHolder, di swipe cek tipe item berikutnya dan pindah activity"
        ));

        list.add(new BugNote(
            "SetupSidebar crash saat mode TV",
            "NPE di setupSidebar — sidebarContainer null saat activity di-recreate",
            "Fixed",
            "11 Sep 2026",
            "11 Sep 2026",
            "Guard null + applyDisplayMode di onResume"
        ));

        list.add(new BugNote(
            "Kalkulator force close setelah ubah mode",
            "Halaman kalkulator tidak sinkron setelah mode diubah di MainActivity",
            "Fixed",
            "11 Sep 2026",
            "11 Sep 2026",
            "Tambah onResume di CalculatorActivity, recreate kalau mode berubah"
        ));

        list.add(new BugNote(
            "Helper method tidak ketemu (showGestureIndicator)",
            "Script patch gagal karena kondisi cek string sudah include nama di listener",
            "Fixed",
            "12 Sep 2026",
            "12 Sep 2026",
            "Cek definisi method lengkap dengan 'private void showX' bukan hanya nama method"
        ));

        list.add(new BugNote(
            "File M3U dari penyimpanan internal tidak bisa dimasukkan playlist",
            "ACTION_GET_CONTENT tidak persist permission, session manager juga trigger redirect ke kalkulator saat file picker dibuka",
            "Fixed",
            "13 Sep 2026",
            "13 Sep 2026",
            "Pakai ACTION_OPEN_DOCUMENT + copy ke cache lokal + suppression window SessionState + markInternalTransition untuk transisi gotoBeranda"
        ));

        list.add(new BugNote(
            "Playlist M3U besar (>200 KB) tidak tersimpan",
            "SharedPreferences limit ~1 MB, file M3U dengan 1800+ channel tidak muat",
            "Fixed",
            "13 Sep 2026",
            "13 Sep 2026",
            "ExternalMediaStore pakai file-based fallback untuk data besar (external_media_big.txt)"
        ));

        list.add(new BugNote(
            "YouTube download via youtubedl-android gagal",
            "yt-dlp bundel (2025.11.12) expired >90 hari, YouTube API berubah. UpdateChannel.STABLE/MASTER tidak resolve",
            "Skipped",
            "13 Sep 2026",
            "-",
            "Fitur YouTube download di-skip. Alternatif: user share URL ke Seal/NewPipe"
        ));

        list.add(new BugNote(
            "Blur recents tidak jalan di ROM Infinix XOS",
            "RenderEffect di onActivityPaused tidak mem-blur snapshot recents di ROM Infinix. Limitasi sistem.",
            "Workaround",
            "13 Sep 2026",
            "-",
            "Andalkan redirect ke kalkulator sebagai proteksi utama. Blur opsional di Android 12+ dengan ROM vanilla."
        ));

        list.add(new BugNote(
            "SavedLinksStore.saveAll & RiwayatStore.saveAll tidak ada",
            "BackupRestoreHelper panggil saveAll yang tidak didefinisikan",
            "Fixed",
            "12 Sep 2026",
            "12 Sep 2026",
            "Pakai .tambah() satu-satu + skip duplikat"
        ));

        return list;
    }

    public static List<BugNote> getBugsOpen() {
        List<BugNote> list = new ArrayList<>();

        list.add(new BugNote(
            "Preview mini tidak muncul untuk streaming",
            "MediaMetadataRetriever hanya bisa baca video lokal, bukan HLS/MP4 streaming",
            "Workaround",
            "12 Sep 2026",
            "-",
            "Gunakan ExoPlayer ThumbnailProvider (butuh library tambahan) atau skip fitur"
        ));

        return list;
    }

    public static List<Limitation> getLimitations() {
        List<Limitation> list = new ArrayList<>();

        list.add(new Limitation(
            "Cast (Chromecast) belum berfungsi",
            "Butuh Google Cast SDK (3MB+), registrasi Cast ID dengan akun Google."
        ));

        list.add(new Limitation(
            "D-pad TV belum dites di TV asli",
            "Kode sudah ada tapi belum diverifikasi tanpa Android TV/STB fisik."
        ));

        list.add(new Limitation(
            "Focus ring TV belum dites di TV asli",
            "Butuh perangkat TV fisik untuk verifikasi visual."
        ));

        list.add(new Limitation(
            "Data aplikasi tidak bertahan setelah uninstall",
            "Batasan sistem Android — file apapun di /data/data/com.memecio.app/ akan hilang saat uninstall."
        ));

        list.add(new Limitation(
            "Bug notes hanya bisa diperbarui via rebuild APK",
            "Karena catatan di-hardcode di BugNotesStore.java. AI assistant update source code, bukan runtime."
        ));

        return list;
    }
}
