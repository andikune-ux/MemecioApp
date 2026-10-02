package com.memecio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class RepairDatabaseHelper {
    public static void confirmAndRepair(Context context) {
        new AlertDialog.Builder(context)
            .setTitle("Repair Database")
            .setMessage("Scan dan perbaiki SharedPreferences yang rusak?\n\nProses ini hanya memperbaiki, tidak menghapus data valid.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Repair", (d, w) -> doRepair(context))
            .show();
    }

    private static void doRepair(Context context) {
        StringBuilder log = new StringBuilder();
        int fixed = 0;

        // Cek beberapa SharedPreferences yang penting
        fixed += cekPref(context, "memecio_settings", log);
        fixed += cekPref(context, "memecio_external_media", log);
        fixed += cekPref(context, "memecio_custom_playlists", log);
        fixed += cekPref(context, "memecio_watch_later", log);
        fixed += cekPref(context, "memecio_saved_links", log);
        fixed += cekPref(context, "memecio_riwayat", log);
        fixed += cekPref(context, "memecio_versioning", log);

        String msg = fixed == 0
            ? "Database bersih, tidak ada kerusakan."
            : fixed + " masalah ditemukan dan diperbaiki.";
        new AlertDialog.Builder(context)
            .setTitle("Hasil Repair")
            .setMessage(msg + "\n\nLog:\n" + log.toString())
            .setPositiveButton("OK", null)
            .show();
    }

    private static int cekPref(Context context, String name, StringBuilder log) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            prefs.getAll();
            log.append("OK: ").append(name).append("\n");
            return 0;
        } catch (Exception e) {
            try {
                context.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .edit().clear().apply();
                log.append("FIX: ").append(name).append(" (rusak, direset)\n");
                return 1;
            } catch (Exception ignored) {
                log.append("FAIL: ").append(name).append("\n");
                return 0;
            }
        }
    }
}
