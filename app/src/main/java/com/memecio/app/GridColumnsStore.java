package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GridColumnsStore — simpan preferensi jumlah kolom GridView (1-8).
 * Nilai -1 artinya "Auto" (pakai nilai default dari XML: portrait 5, landscape 7).
 */
public class GridColumnsStore {
    private static final String PREF = "memecio_settings";
    private static final String KEY = "grid_columns";
    public static final int AUTO = -1;

    public static int get(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return p.getInt(KEY, AUTO);
    }

    public static void set(Context ctx, int value) {
        SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (value < 1 || value > 8) {
            p.edit().remove(KEY).apply();
        } else {
            p.edit().putInt(KEY, value).apply();
        }
    }

    public static String label(Context ctx) {
        int v = get(ctx);
        return v < 1 ? "Auto" : (v + " Kolom");
    }
}
