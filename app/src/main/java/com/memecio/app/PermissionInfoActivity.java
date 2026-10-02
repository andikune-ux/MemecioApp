package com.memecio.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PermissionInfoActivity extends Activity {

    static class Item {
        String name;
        String desc;
        boolean granted;
        Item(String n, String d, boolean g) { name=n; desc=d; granted=g; }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_permission_info);
        findViewById(R.id.btnBackPerm).setOnClickListener(v -> finish());

        List<Item> items = new ArrayList<>();
        add(items, "READ_EXTERNAL_STORAGE", "Baca file dari penyimpanan (Android 12-)", Manifest.permission.READ_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 33) {
            add(items, "READ_MEDIA_IMAGES", "Baca foto dari galeri", "android.permission.READ_MEDIA_IMAGES");
            add(items, "READ_MEDIA_VIDEO", "Baca video dari galeri", "android.permission.READ_MEDIA_VIDEO");
            add(items, "READ_MEDIA_AUDIO", "Baca audio dari galeri", "android.permission.READ_MEDIA_AUDIO");
        }
        add(items, "INTERNET", "Akses internet", Manifest.permission.INTERNET);
        if (Build.VERSION.SDK_INT >= 30) {
            add(items, "MANAGE_EXTERNAL_STORAGE", "Akses semua file (khusus)", "android.permission.MANAGE_EXTERNAL_STORAGE");
        }

        ListView list = findViewById(R.id.listPerm);
        final List<Item> finalItems = items;

        list.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return finalItems.size(); }
            @Override public Object getItem(int p) { return finalItems.get(p); }
            @Override public long getItemId(int p) { return p; }
            @Override
            public View getView(int pos, View cv, ViewGroup parent) {
                View v = cv;
                if (v == null) v = LayoutInflater.from(PermissionInfoActivity.this)
                    .inflate(R.layout.item_permission, parent, false);
                Item it = finalItems.get(pos);
                ((TextView) v.findViewById(R.id.tvPermName)).setText(it.name);
                ((TextView) v.findViewById(R.id.tvPermDesc)).setText(it.desc);
                TextView st = v.findViewById(R.id.tvPermStatus);
                st.setText(it.granted ? "Granted" : "Denied");
                st.setBackgroundColor(it.granted ? 0xFF4CAF50 : 0xFFE53935);
                return v;
            }
        });

        // Tap untuk buka App Info settings
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
    }

    private void add(List<Item> list, String name, String desc, String permission) {
        boolean g = false;
        try { g = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED; } catch (Exception ignored) {}
        list.add(new Item(name, desc, g));
    }
}
