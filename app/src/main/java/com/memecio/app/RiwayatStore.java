package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class RiwayatStore {

    private static final String PREF_NAME = "memecio_riwayat";
    private static final String KEY_LIST = "riwayat_list";
    private static final String SEPARATOR = "\u0001";

    public static void tambah(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_LIST, "");

        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(url);
        if (!existing.isEmpty()) {
            set.addAll(Arrays.asList(existing.split(SEPARATOR)));
        }

        saveSet(prefs, set);
    }

    public static void hapus(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_LIST, "");
        if (existing.isEmpty()) return;

        List<String> list = new ArrayList<>(Arrays.asList(existing.split(SEPARATOR)));
        list.remove(url);

        LinkedHashSet<String> set = new LinkedHashSet<>(list);
        saveSet(prefs, set);
    }

    public static void saveAll(Context context, List<String> items) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        LinkedHashSet<String> set = new LinkedHashSet<>(items);
        saveSet(prefs, set);
    }

    public static void kosongkan(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, "").apply();
    }

    public static List<String> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_LIST, "");
        List<String> result = new ArrayList<>();
        if (!existing.isEmpty()) {
            result.addAll(Arrays.asList(existing.split(SEPARATOR)));
        }
        return result;
    }

    private static void saveSet(SharedPreferences prefs, LinkedHashSet<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String item : set) {
            if (sb.length() > 0) sb.append(SEPARATOR);
            sb.append(item);
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply();
    }
}
