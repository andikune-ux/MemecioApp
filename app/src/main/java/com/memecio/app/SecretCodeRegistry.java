package com.memecio.app;

import java.util.ArrayList;
import java.util.List;

public class SecretCodeRegistry {

    public static class Code {
        public String code;
        public String title;
        public String description;
        public boolean hidden; // kalau true, tidak tampil di daftar 000

        public Code(String code, String title, String description, boolean hidden) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.hidden = hidden;
        }
    }

    public static List<Code> getAll() {
        List<Code> list = new ArrayList<>();
        // Kode utama (tampil di daftar)
        list.add(new Code("000", "Daftar Kode Rahasia", "Menampilkan daftar semua kode rahasia", false));
        list.add(new Code("0000", "Toggle FLAG SECURE", "Ketik 0000 lalu DEL 3x lalu = untuk on/off privasi screenshot", false));
        list.add(new Code("111", "Riwayat Crash Log", "Riwayat crash aplikasi (force close / balik ke kalkulator)", false));
        list.add(new Code("222", "Changelog & Status Fitur", "Daftar perubahan versi + status fitur", false));
        list.add(new Code("333", "System Info", "Info perangkat: Android, RAM, storage, layar", false));
        list.add(new Code("444", "Test Media Player", "Buka player dengan video test", false));
        list.add(new Code("555", "Network Info", "Status koneksi, IP, kecepatan", false));
        list.add(new Code("666", "Daftar Permission", "Status izin aplikasi + shortcut Settings", false));
        list.add(new Code("777", "Storage Analyzer", "Ukuran cache, log, media tersimpan", false));
        list.add(new Code("888", "Statistik Lengkap", "Statistik penggunaan aplikasi", false));
        list.add(new Code("999", "Export Data", "Export semua data ke JSON", false));
        list.add(new Code("123", "Reset Cache", "Hapus cache thumbnail & preview", false));
        list.add(new Code("456", "Repair Database", "Scan & perbaiki SharedPreferences rusak", false));
        list.add(new Code("789", "Factory Reset", "Hapus semua data aplikasi", false));
        list.add(new Code("101", "Toggle Developer Mode", "Aktifkan tombol debug di Profil", false));
        list.add(new Code("103", "Test Gesture", "Simulasi gestur di media player", false));
        list.add(new Code("104", "Test Modes", "Ganti cepat mode HP / TV / Auto", false));
        list.add(new Code("200", "Settings Android", "Buka halaman App Info Memec.io", false));
        list.add(new Code("201", "Battery Optimization", "Hindari app di-kill saat background", false));
        list.add(new Code("202", "Notification Settings", "Atur notifikasi playback", false));

        list.add(new Code("808", "Daftar Download", "Lihat & hapus file download di /sdcard/Termux/Download/", false));

        // Kode tersembunyi (tidak tampil di daftar)
                list.add(new Code("8888", "Test JS Runtime", "Cek Rhino JavaScript engine", false));

        list.add(new Code("8888", "Test JS Runtime", "Cek Rhino JavaScript engine", false));
        list.add(new Code("8889", "Test HTTP + HTML", "Test http.get + parse HTML via JS", false));

        list.add(new Code("102", "Force Crash", "Sengaja crash-kan aplikasi untuk test", true));

        return list;
    }

    public static List<Code> getVisible() {
        List<Code> result = new ArrayList<>();
        for (Code c : getAll()) {
            if (!c.hidden) result.add(c);
        }
        return result;
    }
}
