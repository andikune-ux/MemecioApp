package com.memecio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class FactoryResetHelper {
    public static void confirmAndReset(final Context context) {
        // Step 1: Peringatan awal
        new AlertDialog.Builder(context)
            .setTitle("⚠️ Factory Reset")
            .setMessage("Ini akan MENGHAPUS SEMUA DATA aplikasi:\n\n" +
                "• Playlist & riwayat link\n" +
                "• Media eksternal aktif\n" +
                "• Pengaturan aplikasi\n" +
                "• Semua file tersembunyi\n" +
                "• Log crash\n\n" +
                "Tindakan ini TIDAK BISA DIBATALKAN.\n\n" +
                "Disarankan Export JSON dulu sebelum lanjut.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Lanjut", (d, w) -> step2(context))
            .show();
    }

    private static void step2(final Context context) {
        final EditText input = new EditText(context);
        input.setHint("Ketik HAPUS di sini");
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);
        layout.addView(input);

        new AlertDialog.Builder(context)
            .setTitle("Konfirmasi Terakhir")
            .setMessage("Ketik 'HAPUS' (huruf kapital) untuk melanjutkan:")
            .setView(layout)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Reset", (d, w) -> {
                String val = input.getText().toString().trim();
                if (!val.equals("HAPUS")) {
                    Toast.makeText(context, "Kata konfirmasi salah. Dibatalkan.", Toast.LENGTH_LONG).show();
                    return;
                }
                doReset(context);
            })
            .show();
    }

    private static void doReset(Context context) {
        try {
            // 1. Hapus semua SharedPreferences
            File sharedPrefsDir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            if (sharedPrefsDir.exists()) {
                File[] files = sharedPrefsDir.listFiles();
                if (files != null) {
                    for (File f : files) f.delete();
                }
            }

            // 2. Hapus semua file di filesDir
            File filesDir = context.getFilesDir();
            if (filesDir.exists()) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) deleteRecursive(f);
                        else f.delete();
                    }
                }
            }

            // 3. Hapus cache
            File cacheDir = context.getCacheDir();
            if (cacheDir.exists()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) deleteRecursive(f);
                        else f.delete();
                    }
                }
            }

            Toast.makeText(context, "Semua data dihapus. Aplikasi akan ditutup.", Toast.LENGTH_LONG).show();
            // Force close
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            Toast.makeText(context, "Gagal reset: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void deleteRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteRecursive(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
