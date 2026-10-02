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
