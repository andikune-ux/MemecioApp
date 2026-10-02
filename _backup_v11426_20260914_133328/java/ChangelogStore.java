package com.memecio.app;

import java.util.ArrayList;
import java.util.List;

public class ChangelogStore {

    public static class BuildEntry {
        public String version;
        public String timestamp;
        public List<String> changes = new ArrayList<>();

        public BuildEntry(String version, String timestamp) {
            this.version = version;
            this.timestamp = timestamp;
        }

        public BuildEntry add(String change) {
            changes.add(change);
            return this;
        }
    }

    public static class FeatureStatus {
        public String name;
        public String status;
        public String dateAdded;

        public FeatureStatus(String name, String status, String dateAdded) {
            this.name = name;
            this.status = status;
            this.dateAdded = dateAdded;
        }
    }

    public static List<BuildEntry> getBuilds() {
        List<BuildEntry> list = new ArrayList<>();

        // === TERBARU DI ATAS ===
        BuildEntry b25 = new BuildEntry("V.1.14.25", "14 Sep 2026, 13:30:00");
        b25.add("Restrukturisasi nav jadi overlay: grid extend ke belakang tombol");
        b25.add("Portrait: nav pindah ke root FrameLayout (layout_gravity=bottom)");
        b25.add("Landscape: root LinearLayout -> FrameLayout, nav_col overlay start");
        b25.add("Grid kini bisa terlihat di dalam tombol nav yang translucent");
        b25.add("Bump versi: 1.14.24 -> 1.14.25");
        list.add(b25);
        
        BuildEntry b24 = new BuildEntry("V.1.14.24", "14 Sep 2026, 13:00:00");
        b24.add("Nav bullet: hapus elevation 6dp di portrait (rata tanpa bayangan)");
        b24.add("Portrait & landscape konsisten (tanpa bayangan)");
        b24.add("Bump versi: 1.14.23 -> 1.14.24");
        list.add(b24);
        
        BuildEntry b23 = new BuildEntry("V.1.14.23", "14 Sep 2026, 12:30:00");
        b23.add("Nav bawah: hapus elevation 8dp (panel abu/shadow di sekitar nav hilang)");
        b23.add("Portrait & landscape konsisten");
        b23.add("Bullet tetap elevation 6dp (tetap mengambang)");
        b23.add("Bump versi: 1.14.22 -> 1.14.23");
        list.add(b23);
        
        BuildEntry b22 = new BuildEntry("V.1.14.22", "14 Sep 2026, 09:00:00");
        b22.add("Ganti badge 'io' dengan icon custom (ic_logo_memecio.png)");
        b22.add("Header portrait: TextView io -> ImageView");
        b22.add("Menu Profil portrait: TextView io -> ImageView");
        b22.add("Menu Profil landscape: TextView io -> ImageView");
        b22.add("Bump versi: 1.14.21 -> 1.14.22");
        list.add(b22);
        
        BuildEntry b21 = new BuildEntry("V.1.14.21", "14 Sep 2026, 07:30:00");
        b21.add("Nav: hapus panel abu bg_nav_bar_dark dari container");
        b21.add("Hanya tombol putih nav yang terlihat, tanpa background");
        b21.add("Portrait & landscape konsisten");
        b21.add("Bump versi: 1.14.20 -> 1.14.21");
        list.add(b21);
        
        BuildEntry b20 = new BuildEntry("V.1.14.20", "14 Sep 2026, 02:00:00");
        b20.add("Fix: video gagal play dari poster (balik ke poster)");
        b20.add("Manifest: tambah configChanges di VideoPlayerActivity");
        b20.add("Guard onVideoSizeChanged: skip set orientasi kalau sudah sama");
        b20.add("Guard onCreate: tidak show poster ulang saat recreate");
        b20.add("Bump versi: 1.14.19 -> 1.14.20");
        list.add(b20);
        
        BuildEntry b19 = new BuildEntry("V.1.14.19", "14 Sep 2026, 01:30:00");
        b19.add("Fitur: pengaturan jumlah kolom grid (1-8) via menu Profil");
        b19.add("GridColumnsStore baru: simpan preferensi di memecio_settings");
        b19.add("Tombol Kolom Grid di Profil (portrait + landscape)");
        b19.add("Apply otomatis saat buka Beranda");
        b19.add("Bump versi: 1.14.18 -> 1.14.19");
        list.add(b19);
        
        BuildEntry b18 = new BuildEntry("V.1.14.18", "14 Sep 2026, 01:00:00");
        b18.add("Filter tombol: bg 100% transparan (grid di belakang tembus)");
        b18.add("Tombol Play/Hide: 32dp -> 22dp, text 9sp -> 7sp");
        b18.add("Berlaku di item_thumbnail, item_thumbnail_kat, item_thumbnail_horizontal");
        b18.add("Bump versi: 1.14.17 -> 1.14.18");
        list.add(b18);
        
        BuildEntry b17 = new BuildEntry("V.1.14.17", "14 Sep 2026, 00:45:00");
        b17.add("Filter tombol: background transparan + outline hitam (tanpa panel abu)");
        b17.add("Grid portrait: 3 -> 5 kolom, padding dihapus");
        b17.add("Grid landscape: 5 -> 7 kolom");
        b17.add("Thumbnail: border bulat 14dp (bg_thumbnail_rounded + clipToOutline)");
        b17.add("Bump versi: 1.14.16 -> 1.14.17");
        list.add(b17);
        
        BuildEntry b16 = new BuildEntry("V.1.14.16", "14 Sep 2026, 00:30:00");
        b16.add("Landscape: filter column tanpa panel abu, hanya tombol");
        b16.add("Landscape: filter column paddingTop 60dp (tidak tabrakan search bar)");
        b16.add("Grid fully extend ke belakang filter column");
        b16.add("Bump versi: 1.14.15 -> 1.14.16");
        list.add(b16);
        
        BuildEntry b15 = new BuildEntry("V.1.14.15", "14 Sep 2026, 00:15:00");
        b15.add("Landscape: hapus marginStart 44dp — grid benar-benar extend ke belakang filter col");
        b15.add("Landscape: filter col dapat elevation 8dp (mengambang di atas grid)");
        b15.add("Fix: overlay landscape sekarang benar-benar overlay");
        b15.add("Bump versi: 1.14.14 -> 1.14.15");
        list.add(b15);
        
        BuildEntry b14 = new BuildEntry("V.1.14.14", "14 Sep 2026, 00:00:00");
        b14.add("Filter row (Semua/Foto/Video/New) jadi overlay di atas grid");
        b14.add("Landscape: filter column kiri jadi overlay, grid punya marginStart 44dp");
        b14.add("Portrait: filter row pindah ke dalam overlay (satu layer dengan search bar)");
        b14.add("Bump versi: 1.14.13 -> 1.14.14");
        list.add(b14);
        
        BuildEntry b13 = new BuildEntry("V.1.14.13", "13 Sep 2026, 23:45:00");
        b13.add("Search bar: overlay mengambang (FrameLayout) di atas grid");
        b13.add("Search bar: putih transparan 5% + outline hitam");
        b13.add("Icon search di-tint hitam");
        b13.add("Portrait & landscape konsisten");
        b13.add("Bump versi: 1.14.12 -> 1.14.13");
        list.add(b13);
        
        BuildEntry b12 = new BuildEntry("V.1.14.12", "13 Sep 2026, 23:30:00");
        b12.add("Fix nav bullet: icon di tengah tab saat normal, naik saat ditekan");
        b12.add("Fix bullet terpotong: clipChildren=false di root & child layout");
        b12.add("Bullet translationY animasi: 0 normal, -20dp saat aktif");
        b12.add("Bump versi: 1.14.11 -> 1.14.12");
        list.add(b12);
        
        BuildEntry b11 = new BuildEntry("V.1.14.11", "13 Sep 2026, 23:00:00");
        b11.add("Theme: background utama jadi abu #D9D9D9, card/button/header jadi abu #C0C0C0");
        b11.add("Warna tulisan konsisten #727272 (nonaktif), putih (aktif di bullet)");
        b11.add("Nav overlay mengambang: margin 12dp dari tepi, elevation 8dp");
        b11.add("Icon tab otomatis: putih saat aktif, #727272 saat nonaktif");
        b11.add("Bug fix: nav bullet tidak terpotong di tepi");
        b11.add("Bump versi: 1.14.10 -> 1.14.11");
        list.add(b11);
        
        BuildEntry b10 = new BuildEntry("V.1.14.10", "13 Sep 2026, 22:35:00");
        b10.add("Fix crash: FrameLayout cannot be cast to LinearLayout di MainActivity");
        b10.add("Ganti deklarasi btnBeranda/btnSumber/btnProfil dari LinearLayout ke FrameLayout");
        b10.add("Bump versi: 1.14.9 -> 1.14.10");
        list.add(b10);
        
        BuildEntry b9 = new BuildEntry("V.1.14.9", "13 Sep 2026, 22:30:00");
        b9.add("Nav redesign v2: container abu gelap + tab putih glass card");
        b9.add("Bullet aktif biru + efek glass, mengambang di atas tab");
        b9.add("Tulisan abu + glow hitam tipis (kontras dengan tab putih)");
        b9.add("Fix bug: icon tab nonaktif tidak ikut hilang");
        b9.add("Portrait & landscape konsisten");
        b9.add("Bump versi: 1.14.8 -> 1.14.9");
        list.add(b9);
        
        BuildEntry b8 = new BuildEntry("V.1.14.8", "13 Sep 2026, 23:00:00");
        b8.add("Nav redesign: bulatan biru muda glass di belakang icon tab aktif");
        b8.add("Bulatan muncul/hilang mengikuti tab yang dipilih");
        b8.add("Nav background abu-abu glass, icon & label putih");
        b8.add("Portrait & landscape diperbarui seragam");
        b8.add("Bump versi: 1.14.7 -> 1.14.8");
        list.add(b8);
        
        BuildEntry b7 = new BuildEntry("V.1.14.7", "13 Sep 2026, 22:00:00");
        b7.add("Fix ProjectExportHelper: Section 9 sinkron dengan ChangelogStore");
        b7.add("Fix ProjectExportHelper: Section 6 pakai checkbox [x]/[~]/[ ]");
        b7.add("Section 9 hanya tampilkan fitur yang benar-benar belum dikerjakan");
        b7.add("Bump versi: 1.14.6 -> 1.14.7");
        list.add(b7);
        
        BuildEntry b6 = new BuildEntry("V.1.14.6", "13 Sep 2026, 21:00:00");
        b6.add("Nav bottom portrait: pill seragam untuk Beranda/Sumber/Profil");
        b6.add("Nav bottom landscape: vertikal kiri, pill panjang ke bawah");
        b6.add("Pill aktif + nonaktif pakai efek glass card (gradien putih)");
        b6.add("Versi di menu Profil otomatis dari PackageManager");
        b6.add("BugNotesStore: rapikan FeatureStatus (9 fitur dipindah ke Selesai)");
        b6.add("Bump versi: 1.14.5 -> 1.14.6");
        list.add(b6);
        
        BuildEntry b5 = new BuildEntry("V.1.14.5", "13 Sep 2026, 13:00:00");
        b5.add("Batch C: Download MP4/Drive via DownloadManager + folder Termux/Download");
        b5.add("Batch C: HLS download via Media3 DownloadService (HlsDownloadService)");
        b5.add("Batch C: Halaman Daftar Download (kode 808) — lihat, buka, hapus file");
        b5.add("Fix: file M3U dari penyimpanan internal bisa dimasukkan playlist");
        b5.add("Fix: SessionManager internal transition — tidak redirect ke kalkulator saat pilih file");
        b5.add("Fix: ExternalMediaStore pakai file-based fallback untuk playlist besar");
        b5.add("Update: minSdk tetap 21 (library youtubedl di-rollback)");
        b5.add("Skip: YouTube download (library expired & tidak stabil)");
        list.add(b5);

        BuildEntry b4 = new BuildEntry("V.1.13.4", "13 Sep 2026, 00:50:00");
        b4.add("Batch A: Video Detail Overlay (poster + judul + tombol Play/Download)");
        b4.add("Batch Privacy: SessionManager global (counter-based, anti-bug)");
        b4.add("Batch Privacy: BlurHelper (RenderEffect Android 12+, blur opsional)");
        b4.add("Batch Privacy: Redirect ke Kalkulator saat keluar / layar mati");
        b4.add("Batch Privacy: Grace period 1 detik untuk transisi internal");
        b4.add("Batch Privacy: Pengecualian PiP, rotasi, CalculatorActivity");
        b4.add("Fix: playlist Drive & streaming load dengan benar dari sidebar");
        b4.add("Fix: M3uCacheStore + DriveFolderCache (percepat scan 30 menit)");
        b4.add("Audio Player: MediaSession + service bind + kontrol notifikasi");
        b4.add("Kategori: patch inti naik ke 1 karena perubahan besar");
        list.add(b4);

        BuildEntry b3 = new BuildEntry("V.1.12.3", "12 Sep 2026, 21:00:00");
        b3.add("Batch 5b: Storage Analyzer (777), Reset Cache (123), Test Media (444)");
        b3.add("Batch 5c: Repair Database (456), Factory Reset (789), Developer Mode (101)");
        b3.add("Batch 5d: Test Gesture (103), Test Modes (104)");
        b3.add("BugNotesStore: tambah 4 catatan bug yang sudah diperbaiki");
        b3.add("Total kode rahasia aktif: 21 (000-999)");
        list.add(b3);

        BuildEntry b2 = new BuildEntry("V.1.12.2", "12 Sep 2026, 20:15:00");
        b2.add("Backup: tombol baru di Profil gabungkan Backup Aman + Ekspor JSON + Impor JSON");
        b2.add("Backup: popup pilihan 3 tombol (dialog konfirmasi)");
        b2.add("Ekspor JSON: tambah playlist URL, riwayat link, media eksternal");
        b2.add("Impor JSON: tambah data ke existing (skip duplikat)");
        b2.add("File JSON disimpan sebagai data_memecio_[tanggal].json");
        b2.add("Menu Profil: hapus tombol Ekspor/Impor JSON dari grid, jadi 7 tombol");
        b2.add("Bump versi 1.12.0 -> 1.12.2");
        list.add(b2);

        BuildEntry b1 = new BuildEntry("V.1.12.1", "12 Sep 2026, 18:30:00");
        b1.add("Batch 5a: System Info (333), Network Info (555), Permission Info (666)");
        b1.add("Fix error getAddresses() di NetworkInfoActivity");
        b1.add("Manifest: daftarkan 3 Activity baru");
        list.add(b1);

        BuildEntry b0 = new BuildEntry("V.1.12.0", "12 Sep 2026, 17:55:00");
        b0.add("Sistem versioning baru: V.patch_inti.tanggal.build_count");
        b0.add("Changelog: refactor per-build dengan timestamp");
        b0.add("Batch 4: Halaman Kode Rahasia (000), Crash History (111), Changelog (222)");
        b0.add("Kode rahasia: Force Crash (102, hidden), Settings shortcuts (200, 201, 202)");
        b0.add("ChangelogStore: struktur data baru (BuildEntry + FeatureStatus)");
        list.add(b0);

        BuildEntry awal = new BuildEntry("V.1.0.0", "10 Sep 2026, 20:00:00");
        awal.add("Rilis awal Memec.io");
        awal.add("Video Player, PreviewImage, Audio Player");
        awal.add("Streaming / Drive / Server Source");
        awal.add("Statistik, Riwayat, Crash Log");
        awal.add("PIN Dialog, Kalkulator akses");
        list.add(awal);

        return list;
    }

    public static List<FeatureStatus> getFeatureStatus() {
        List<FeatureStatus> list = new ArrayList<>();

        // === SELESAI ===
        list.add(new FeatureStatus("Video Player: auto-orientasi Portrait/Landscape", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Video Player: tombol Fullscreen + tombol Keluar", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Video Player: reset orientasi saat keluar", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Progress bar tipis + thumb merah saat digeser", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Preview mini saat geser progress (video lokal)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Gestur Media Player: tahan kanan=3x, kiri=brightness, kanan=volume", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Mode TV 3-state (Auto/Android TV/Android)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("D-pad navigasi tab + focus ring", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Kunci layar: auto-hide + ikon", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Sort by tanggal: foto & video tampil bersama", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Swipe antar tipe: foto dan video dalam satu playlist", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Fix crash foto di PreviewImageActivity", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Menu GIR: PiP, Cast placeholder, Tambah ke Playlist, Speed, Auto Skip", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Crash logging global (semua crash tercatat)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Kode rahasia kalkulator (000, 111, 222, 333, 555, 666, 888, 102, 200, 201, 202)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("System Info (333)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Network Info (555)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Permission Info (666)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Crash History (111) dengan kategori FORCE CLOSE / CRASH", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Changelog & Status Fitur (222)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Backup gabungan (Backup Aman + Ekspor/Impor JSON)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Ekspor JSON: playlist manual + URL + riwayat + media eksternal", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Export Panduan Proyek (kode 999)", "Selesai", "12 Sep 2026"));

        list.add(new FeatureStatus("Storage Analyzer (777)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Reset Cache (123)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Repair Database (456)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Factory Reset (789)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Toggle Developer Mode (101)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Test Gesture (103)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Test Modes (104)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Test Media Player (444)", "Selesai", "12 Sep 2026"));
        list.add(new FeatureStatus("Download offline", "Selesai", "13 Sep 2026"));
        list.add(new FeatureStatus("Nav bottom seragam (portrait + landscape)", "Selesai", "13 Sep 2026"));
        list.add(new FeatureStatus("Versi otomatis di menu Profil", "Selesai", "13 Sep 2026"));
        // === SEBAGIAN ===
        list.add(new FeatureStatus("D-pad dan focus ring TV - belum dites di TV asli", "Sebagian", "12 Sep 2026"));
        list.add(new FeatureStatus("Cast - butuh Google Cast SDK", "Sebagian", "12 Sep 2026"));
        list.add(new FeatureStatus("Preview mini untuk streaming - butuh library tambahan", "Sebagian", "12 Sep 2026"));

        // === BELUM ===
        list.add(new FeatureStatus("Widget channel favorit", "Belum", "-"));
        list.add(new FeatureStatus("Multi-audio track & subtitle", "Belum", "-"));
        list.add(new FeatureStatus("Mode Gelap/Terang", "Belum", "-"));
        list.add(new FeatureStatus("Sleep timer audio", "Belum", "-"));
        list.add(new FeatureStatus("Update aplikasi otomatis", "Belum", "-"));

        return list;
    }

    public static int[] getFeatureSummary() {
        int selesai = 0, sebagian = 0, belum = 0;
        for (FeatureStatus f : getFeatureStatus()) {
            if ("Selesai".equals(f.status)) selesai++;
            else if ("Sebagian".equals(f.status)) sebagian++;
            else belum++;
        }
        return new int[]{selesai, sebagian, belum};
    }
}
