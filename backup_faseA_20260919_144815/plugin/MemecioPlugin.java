package com.memecio.app.plugin;

/**
 * ============================================================
 * MEMECIO PLUGIN — INTERFACE DASAR
 * ============================================================
 *
 * Setiap plugin yang ingin terdaftar di Memec.io harus implement interface ini.
 * Plugin berisi 1 provider utama (bisa lebih di masa depan).
 *
 * Contoh pemakaian:
 *   PluginManager.register(new SamplePlugin());
 */
public interface MemecioPlugin {

    /** Nama plugin — harus unik, contoh: "Anichin" */
    String getName();

    /** Versi plugin — contoh: "1.0.0" */
    String getVersion();

    /** Nama developer/pembuat plugin */
    String getAuthor();

    /** Deskripsi singkat plugin */
    String getDescription();

    /** Provider utama — entry point ambil konten */
    MemecioProvider getProvider();

    /** Tipe konten yang didukung — {"movie", "tv", "anime"} */
    String[] getSupportedTypes();
}
