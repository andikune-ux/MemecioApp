package com.memecio.app

object SecretCodeRegistry {
    data class Code(
        val code: String,
        val title: String,
        val description: String,
        val hidden: Boolean = false
    )

    fun getAll(): List<Code> = listOf(
        Code("140399", "Buka Aplikasi Utama", "Membuka antarmuka utama Memec.io (Beranda, Sumber, Profil)", false),
        Code("000", "Daftar Kode Rahasia", "Menampilkan seluruh kode rahasia dan shortcut sistem", false),
        Code("111", "Riwayat Crash", "Melihat riwayat log crash & error sistem", false),
        Code("222", "Changelog & Status Fitur", "Daftar catatan versi dan status penyelesaian fitur", false),
        Code("333", "System Info", "Informasi spesifikasi perangkat, prosesor, RAM & OS Android", false),
        Code("555", "Network Info", "Informasi koneksi Wi-Fi, jaringan seluler, IP & status koneksi", false),
        Code("666", "Permission Info", "Daftar status izin Android (Storage, Media, Internet)", false),
        Code("777", "Storage Analyzer", "Analisis penggunaan ruang penyimpanan internal dan eksternal", false),
        Code("808", "Daftar Download", "Melihat dan mengelola file hasil download offline", false),
        Code("888", "Statistik", "Statistik jumlah media lokal, durasi, dan sumber terdaftar", false),
        Code("999", "Ekspor Panduan Proyek", "Ekspor panduan proyek lengkap untuk AI & Developer", false),
        Code("101", "Toggle Developer Mode", "Mengaktifkan / menonaktifkan mode developer", false),
        Code("102", "Force Crash", "Simulasi crash runtime untuk menguji sistem logging", true),
        Code("103", "Panduan Gestur", "Menampilkan panduan gestur video player", false),
        Code("104", "Pilihan Mode Tampilan", "Memilih mode antarmuka (Auto, Mobile, Tablet, TV)", false),
        Code("123", "Reset Cache", "Membersihkan file cache & temporary aplikasi", false),
        Code("444", "Test Media Player", "Uji coba pemutaran video / streaming sampel", false),
        Code("456", "Repair Database", "Perbaiki dan refresh pemindaian media lokal", false),
        Code("789", "Factory Reset", "Mengembalikan seluruh pengaturan aplikasi ke bawaan", false),
        Code("200", "Info Aplikasi (Settings)", "Buka menu pengaturan aplikasi di pengaturan Android", false),
        Code("201", "Optimasi Baterai", "Buka pengaturan optimasi baterai sistem", false),
        Code("202", "Pengaturan Notifikasi", "Buka pengaturan notifikasi sistem Android", false)
    )
}
