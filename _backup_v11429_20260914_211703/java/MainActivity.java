package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.provider.Settings;
import android.view.KeyEvent;
import android.net.NetworkRequest;
import android.net.NetworkCapabilities;
import android.net.Network;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.util.Map;
import java.util.LinkedHashMap;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQ_STORAGE_PERMISSION = 100;

    private FrameLayout container;
    private List<MediaItem> localMedia = new ArrayList<>();
    private List<MediaItem> combinedMedia = new ArrayList<>();
    private int currentFilter = 0;
    private String activeSource = "offline";

    private FrameLayout btnBeranda, btnSumber, btnProfil;
    private android.widget.ImageView tvIconBeranda;
    private TextView tvLabelBeranda;
    private android.widget.ImageView tvIconSumber;
    private TextView tvLabelSumber;
    private android.widget.ImageView tvIconProfil;
    private TextView tvLabelProfil;

    private ListView sidebarList;
    private ArrayAdapter<MediaItem> sidebarAdapter;
    private List<MediaItem> sidebarSavedItems = new ArrayList<>();
    private LinearLayout sidebarContainer;
    private boolean sidebarOpen = false;
    private ImageButton btnToggleSidebar;
    private LinearLayout kategoriList;
    private ScrollView scrollKategori;
    private FrameLayout overlayExpand;
    private android.widget.GridLayout gridExpand;
    private TextView tvExpandTitle;
    private ImageButton btnShrink;
    private GridView gridBeranda;
    private EditText etSearch;
    private LinearLayout historyChipsContainer;
    private android.view.View btnSort;
    private String searchQuery = "";
    private String activeCategory = "";
    private LinearLayout categoryChipsContainer;
    private View categoryChipScroll;
    private android.os.Handler searchDebounceHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchDebounceRunnable;

    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_SORT_MODE = "sort_mode";

    private TextView tvKoneksiIndicator;
    private int currentTabIndex = 0;
    private java.util.Map<Integer, android.view.View> cachedTabViews = new java.util.HashMap<>();
    private java.util.Set<Integer> alreadySetup = new java.util.HashSet<>();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    private void debugLogMain(String msg) {
        try {
            java.io.File dir = new java.io.File("/sdcard/Download");
            if (!dir.exists()) dir = getExternalFilesDir(null);
            if (dir == null) return;
            java.io.File f = new java.io.File(dir, "memecio_preview_debug.txt");
            java.io.FileWriter fw = new java.io.FileWriter(f, true);
            fw.write(new java.util.Date() + " | [Main] " + msg + "\n");
            fw.close();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doOnCreate(savedInstanceState);
    }

    private void doOnCreate(Bundle savedInstanceState) {
        try {
            doOnCreateInner(savedInstanceState);
        } catch (Throwable t) {
            logCrashToFile(t);
            android.util.Log.e("MEMECIO_CRASH", "doOnCreate failed", t);
            throw new RuntimeException(t);
        }
    }

    private void doOnCreateInner(Bundle savedInstanceState) {
        SoundHelper.init(this);
        setContentView(R.layout.activity_main);
        tvKoneksiIndicator = findViewById(R.id.tvKoneksiIndicator);
        setupKoneksiIndicator();

        container = findViewById(R.id.container);

        btnBeranda = findViewById(R.id.btnBeranda);
        btnSumber = findViewById(R.id.btnSumber);
        btnProfil = findViewById(R.id.btnProfil);

        tvIconBeranda = findViewById(R.id.tvIconBeranda);
        tvLabelBeranda = findViewById(R.id.tvLabelBeranda);
        tvIconSumber = findViewById(R.id.tvIconSumber);
        tvLabelSumber = findViewById(R.id.tvLabelSumber);
        tvIconProfil = findViewById(R.id.tvIconProfil);
        tvLabelProfil = findViewById(R.id.tvLabelProfil);

        btnBeranda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab(0);
                resolveActiveSourceAndLoad();
            }
        });

        btnSumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab(1);
            }
        });

        btnProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab(2);
            }
        });

        switchToTab(0);

        handleIntentExtras(getIntent());
        startClock();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            SoundHelper.click();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int code = event.getKeyCode();
            if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) {
                SoundHelper.nav();
            } else if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER
                || code == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                SoundHelper.click();
            }

            // Navigasi tab via remote (khusus Mode TV)
            if (DisplayModeStore.isTvMode(this)) {
                View focus = getCurrentFocus();
                boolean focusDiTab = (focus == btnBeranda || focus == btnSumber || focus == btnProfil);
                if (focusDiTab) {
                    if (code == KeyEvent.KEYCODE_DPAD_LEFT && currentTabIndex > 0) {
                        int newIdx = currentTabIndex - 1;
                        switchToTab(newIdx);
                        focusTabButton(newIdx);
                        return true;
                    }
                    if (code == KeyEvent.KEYCODE_DPAD_RIGHT && currentTabIndex < 2) {
                        int newIdx = currentTabIndex + 1;
                        switchToTab(newIdx);
                        focusTabButton(newIdx);
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void setupKoneksiIndicator() {
        if (tvKoneksiIndicator == null) return;
        try {
            connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            tvKoneksiIndicator.setTextColor(0xFFFFC107); // kuning = menghubungkan

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> tvKoneksiIndicator.setTextColor(0xFF4CAF50)); // hijau
                }
                @Override
                public void onLost(Network network) {
                    runOnUiThread(() -> tvKoneksiIndicator.setTextColor(0xFFF44336)); // merah
                }
                @Override
                public void onUnavailable() {
                    runOnUiThread(() -> tvKoneksiIndicator.setTextColor(0xFF000000)); // hitam
                }
            };
            NetworkRequest req = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(req, networkCallback);
            cekStatusAwal();
        } catch (Exception ignored) {}
    }

    private void cekStatusAwal() {
        try {
            int airplane = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
            if (airplane == 1) {
                tvKoneksiIndicator.setTextColor(0xFF000000);
                return;
            }
            Network activeNet = connectivityManager.getActiveNetwork();
            if (activeNet == null) {
                tvKoneksiIndicator.setTextColor(0xFF000000);
                return;
            }
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNet);
            if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                tvKoneksiIndicator.setTextColor(0xFF4CAF50);
            } else {
                tvKoneksiIndicator.setTextColor(0xFFF44336);
            }
        } catch (Exception ignored) {}
    }

    private void handleIntentExtras(Intent intent) {
        int customIndex = intent.getIntExtra("open_custom_playlist", -1);
        if (customIndex >= 0) {
            List<CustomPlaylistStore.Playlist> pls = CustomPlaylistStore.getAll(this);
            if (customIndex < pls.size()) {
                CustomPlaylistStore.Playlist pl = pls.get(customIndex);
                combinedMedia.clear();
                combinedMedia.addAll(pl.items);
                activeSource = "custom";
                switchToTab(0);
                return;
            }
        }

        String driveFolderUrl = intent.getStringExtra("open_drive_folder_url");
        if (driveFolderUrl != null) {
            openDriveFolder(driveFolderUrl);
            return;
        }

        String playlistUrl = intent.getStringExtra("open_playlist_url");
        String playlistTitle = intent.getStringExtra("open_playlist_title");
        if (playlistUrl != null) {
            MediaItem item = new MediaItem(Uri.parse(playlistUrl), MediaItem.TYPE_VIDEO);
            item.title = playlistTitle;
            loadPlaylist(item);
        } else if (intent.getBooleanExtra("focus_source", false)) {
            activeSource = ExternalMediaStore.getSourceLabel(this);
            renderFromExternalOnly();
        } else {
            SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
            boolean offlineOn = prefs.getBoolean("offline_enabled", true);
            String lastSource = prefs.getString("last_source", "offline");
            if (offlineOn || lastSource.equals("offline")) {
                activeSource = "offline";
                checkPermissionAndLoad();
            } else {
                activeSource = "external";
                renderFromExternalOnly();
            }
        }
    }

    private void openDriveFolder(final String url) {
        final String folderId = extractFolderId(url);
        if (folderId == null) {
            Toast.makeText(this, "ID Folder tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MediaItem> cached = DriveFolderCache.load(this, folderId);
        if (cached != null && !cached.isEmpty()) {
            ExternalMediaStore.gantiSemua(this, cached, "Drive Folder");
            activeSource = ExternalMediaStore.getSourceLabel(this);
            renderFromExternalOnly();
            Toast.makeText(this, "Dari cache (" + cached.size() + " file)", Toast.LENGTH_SHORT).show();
            refreshDriveFolderInBackground(folderId, true);
            return;
        }

        Toast.makeText(this, "Memuat folder Drive...", Toast.LENGTH_SHORT).show();
        refreshDriveFolderInBackground(folderId, false);
    }

    private void refreshDriveFolderInBackground(final String folderId, final boolean silent) {
        final String folderPageUrl = "https://drive.google.com/drive/folders/" + folderId;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(folderPageUrl)
                            .timeout(5000)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Accept-Encoding", "gzip, deflate")
                            .header("Accept-Language", "en-US,en;q=0.9")
                            .maxBodySize(0)
                            .get();

                    String html = doc.html();
                    final List<MediaItem> isiFolder = new ArrayList<>();

                    // Cari pola _DRIVE_ivd = '...';
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("_DRIVE_ivd\\s*=\\s*'([^']+)'");
                    java.util.regex.Matcher m = p.matcher(html);
                    if (m.find()) {
                        String raw = m.group(1);
                        // Unescape karakter escape sequence
                        String json = raw.replace("\\x", "\\u00")
                                          .replace("\\/", "/");
                        try {
                            org.json.JSONArray root = new org.json.JSONArray(json);
                            org.json.JSONArray files = null;
                            // Struktur data bisa nested, cari array yang berisi file
                            for (int i = 0; i < root.length(); i++) {
                                Object o = root.get(i);
                                if (o instanceof org.json.JSONArray) {
                                    org.json.JSONArray arr = (org.json.JSONArray) o;
                                    if (arr.length() > 0 && arr.opt(0) instanceof org.json.JSONArray) {
                                        org.json.JSONArray candidate = (org.json.JSONArray) arr.get(0);
                                        if (candidate.length() >= 3) {
                                            files = arr;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (files != null) {
                                for (int i = 0; i < files.length(); i++) {
                                    org.json.JSONArray entry = files.getJSONArray(i);
                                    if (entry.length() < 3) continue;
                                    String fileId = entry.getString(0);
                                    String title = entry.getString(2);
                                    String mime = entry.length() > 3 ? entry.optString(3, "") : "";
                                    if (fileId == null || fileId.isEmpty()) continue;
                                    if (title == null) title = "File";

                                    int type = MediaItem.TYPE_VIDEO;
                                    if (mime.startsWith("image/")) type = MediaItem.TYPE_IMAGE;
                                    else if (mime.startsWith("audio/")) type = MediaItem.TYPE_AUDIO;
                                    else if (mime.startsWith("video/")) type = MediaItem.TYPE_VIDEO;
                                    else {
                                        String lower = title.toLowerCase();
                                        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                                                || lower.endsWith(".webp") || lower.endsWith(".gif")) {
                                            type = MediaItem.TYPE_IMAGE;
                                        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a")) {
                                            type = MediaItem.TYPE_AUDIO;
                                        }
                                    }

                                    String directUrl = "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&confirm=t";
                                    MediaItem mi = new MediaItem(Uri.parse(directUrl), type);
                                    mi.isLocal = false;
                                    mi.title = title;
                                    mi.thumbUrl = "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w400";
                                    isiFolder.add(mi);
                                }
                            }
                        } catch (Exception ignoredJSON) {}
                    }

                    // Fallback: jika JSON gagal, coba embeddedfolderview
                    if (isiFolder.isEmpty()) {
                        try {
                            org.jsoup.nodes.Document doc2 = org.jsoup.Jsoup.connect(
                                    "https://drive.google.com/embeddedfolderview?id=" + folderId + "#list")
                                    .timeout(5000)
                                    .userAgent("Mozilla/5.0")
                                    .get();
                            org.jsoup.select.Elements links = doc2.select("a[href]");
                            java.util.Set<String> seen = new java.util.HashSet<>();
                            for (org.jsoup.nodes.Element link : links) {
                                String href = link.attr("href");
                                String title = link.text();
                                String fileId = null;
                                if (href.contains("/file/d/")) {
                                    fileId = href.split("/file/d/")[1].split("/")[0];
                                } else if (href.contains("/open?id=")) {
                                    fileId = href.split("/open\\?id=")[1].split("&")[0];
                                } else if (href.contains("id=")) {
                                    fileId = href.split("id=")[1].split("&")[0];
                                }
                                if (fileId == null || seen.contains(fileId)) continue;
                                seen.add(fileId);
                                int type = MediaItem.TYPE_VIDEO;
                                String lower = title.toLowerCase();
                                if (lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png")
                                        || lower.contains(".webp") || lower.contains(".gif")) {
                                    type = MediaItem.TYPE_IMAGE;
                                } else if (lower.contains(".mp3") || lower.contains(".wav") || lower.contains(".m4a")) {
                                    type = MediaItem.TYPE_AUDIO;
                                }
                                String directUrl = "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&confirm=t";
                                MediaItem mi = new MediaItem(Uri.parse(directUrl), type);
                                mi.isLocal = false;
                                mi.title = title;
                                mi.thumbUrl = "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w400";
                                isiFolder.add(mi);
                            }
                        } catch (Exception ignoredFB) {}
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isiFolder.isEmpty()) {
                                if (!silent) {
                                    SoundHelper.error();
                                    Toast.makeText(MainActivity.this, "Folder kosong atau tidak dapat diakses", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                if (!silent) SoundHelper.success();
                                DriveFolderCache.save(MainActivity.this, folderId, isiFolder);
                                ExternalMediaStore.gantiSemua(MainActivity.this, isiFolder, "Drive Folder");
                                activeSource = ExternalMediaStore.getSourceLabel(MainActivity.this);
                                renderFromExternalOnly();
                                if (silent) {
                                    Toast.makeText(MainActivity.this, "Diperbarui: " + isiFolder.size() + " file", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SoundHelper.error();
                            Toast.makeText(MainActivity.this, "Gagal memuat folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private String extractFolderId(String url) {
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("/drive/folders/([a-zA-Z0-9_-]+)");
            java.util.regex.Matcher m = p.matcher(url);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return null;
    }

    private void startClock() {
        final TextView tvHari = findViewById(R.id.tvHariTanggal);
        final TextView tvJam = findViewById(R.id.tvJamRealtime);
        if (tvHari == null || tvJam == null) return;

        clockRunnable = new Runnable() {
            @Override
            public void run() {
                java.util.Date now = new java.util.Date();
                SimpleDateFormat fmtHari = new SimpleDateFormat("EEEE, dd MMM yyyy", new Locale("id", "ID"));
                SimpleDateFormat fmtJam = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                tvHari.setText(fmtHari.format(now));
                tvJam.setText(fmtJam.format(now));
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockRunnable != null) clockHandler.removeCallbacks(clockRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyDisplayModeSafe();
        try { applyImmersiveIfLandscape(); } catch (Exception ignored) {}
        try { AutoExitManager.getInstance().onActivityResumed(this); } catch (Exception ignored) {}
        refreshSidebar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        switchToTab(0);
        handleIntentExtras(intent);
    }

    private void selectTab(int index) {
        currentTabIndex = index;
        int unselected = getResources().getColor(R.color.nav_unselected);

        tvLabelBeranda.setTextColor(0xFF727272);
        tvLabelSumber.setTextColor(0xFF727272);
        tvLabelProfil.setTextColor(0xFF727272);

        float dens = getResources().getDisplayMetrics().density;
        float yNaik = -20f * dens;
        float yNormal = 0f;

        android.view.View bulletBeranda = findViewById(R.id.bulletBeranda);
        if (bulletBeranda != null) {
            bulletBeranda.setBackgroundResource(index == 0 ? R.drawable.bg_nav_bullet_blue : 0);
            bulletBeranda.setTranslationY(index == 0 ? yNaik : yNormal);
        }
        android.view.View bulletSumber = findViewById(R.id.bulletSumber);
        if (bulletSumber != null) {
            bulletSumber.setBackgroundResource(index == 1 ? R.drawable.bg_nav_bullet_blue : 0);
            bulletSumber.setTranslationY(index == 1 ? yNaik : yNormal);
        }
        android.view.View bulletProfil = findViewById(R.id.bulletProfil);
        if (bulletProfil != null) {
            bulletProfil.setBackgroundResource(index == 2 ? R.drawable.bg_nav_bullet_blue : 0);
            bulletProfil.setTranslationY(index == 2 ? yNaik : yNormal);
        }

        int iconNonaktif = 0xFF727272;
        int iconAktif = 0xFFFFFFFF;
        if (tvIconBeranda != null) tvIconBeranda.setColorFilter(index == 0 ? iconAktif : iconNonaktif);
        if (tvIconSumber != null) tvIconSumber.setColorFilter(index == 1 ? iconAktif : iconNonaktif);
        if (tvIconProfil != null) tvIconProfil.setColorFilter(index == 2 ? iconAktif : iconNonaktif);
    }

    private void switchToTab(int index) {
        if (index < 0 || index > 2) return;
        selectTab(index);
        if (index == 0) {
            showContent(R.layout.content_beranda);
            if (!alreadySetup.contains(0)) {
                setupFilterTabs();
                setupSidebar();
                alreadySetup.add(0);
            }
            // Defer renderBeranda biar tombol responsif dulu
            container.post(new Runnable() {
                @Override public void run() { try { renderBeranda(); } catch (Exception ignored) {} }
            });
        } else if (index == 1) {
            showContent(R.layout.content_sumber);
            if (!alreadySetup.contains(1)) {
                setupSumberButtons();
                alreadySetup.add(1);
            }
        } else if (index == 2) {
            showContent(R.layout.content_profil);
            if (!alreadySetup.contains(2)) {
                setupProfilButtons();
                alreadySetup.add(2);
            }
        }
    }

    private void focusTabButton(int index) {
        View target = (index == 0) ? btnBeranda : (index == 1 ? btnSumber : btnProfil);
        if (target != null) target.requestFocus();
    }

    private void showContent(int layoutRes) {
        android.view.View cached = cachedTabViews.get(layoutRes);
        if (cached == null) {
            cached = getLayoutInflater().inflate(layoutRes, container, false);
            cachedTabViews.put(layoutRes, cached);
        }
        if (cached.getParent() != null) {
            ((android.view.ViewGroup) cached.getParent()).removeView(cached);
        }
        container.removeAllViews();
        container.addView(cached);
    }

    private void setupSumberButtons() {
        android.widget.Switch switchOffline = container.findViewById(R.id.switchOffline);
        android.widget.Switch switchKategori = container.findViewById(R.id.switchKategori);

        if (switchKategori != null) {
            SharedPreferences prefsKat = getSharedPreferences("memecio_settings", MODE_PRIVATE);
            boolean katOn = prefsKat.getBoolean("kategori_enabled", false);
            switchKategori.setChecked(katOn);
            switchKategori.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getSharedPreferences("memecio_settings", MODE_PRIVATE)
                        .edit().putBoolean("kategori_enabled", isChecked).apply();
                    renderBeranda();
                }
            });
        }
        View btnDrive = container.findViewById(R.id.btnSumberDrive);
        View btnStreaming = container.findViewById(R.id.btnSumberStreaming);
        View btnPlaylistManual = container.findViewById(R.id.btnPlaylistManual);
        View btnServer = container.findViewById(R.id.btnSumberServer);
        View btnRiwayat = container.findViewById(R.id.btnRiwayat);
        View btnKembalikan = container.findViewById(R.id.btnKembalikan);

        if (switchOffline != null) {
            // Baca state terakhir
            SharedPreferences prefsSw = getSharedPreferences("memecio_settings", MODE_PRIVATE);
            boolean offlineOn = prefsSw.getBoolean("offline_enabled", true);
            switchOffline.setChecked(offlineOn);

            switchOffline.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
                    prefs.edit().putBoolean("offline_enabled", isChecked).apply();
                    if (isChecked) {
                        activeSource = "offline";
                        prefs.edit().putString("last_source", "offline").apply();
                    } else {
                        activeSource = "external";
                        String last = prefs.getString("last_source", "drive");
                        if (last.equals("offline")) last = "drive";
                        prefs.edit().putString("last_source", last).apply();
                    }
                    switchToTab(0);
                    resolveActiveSourceAndLoad();
                }
            });
        }
        if (btnDrive != null) {
            btnDrive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
                        .putString("last_source", "drive").putBoolean("offline_enabled", false).apply();
                    startActivity(new Intent(MainActivity.this, DriveSourceActivity.class));
                }
            });
        }
        if (btnStreaming != null) {
            btnStreaming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
                        .putString("last_source", "streaming").putBoolean("offline_enabled", false).apply();
                    startActivity(new Intent(MainActivity.this, StreamingSourceActivity.class));
                }
            });
        }
        if (btnPlaylistManual != null) {
            btnPlaylistManual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, CustomPlaylistActivity.class));
                }
            });
        }
        if (btnServer != null) {
            btnServer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("memecio_settings", MODE_PRIVATE).edit()
                        .putString("last_source", "server").putBoolean("offline_enabled", false).apply();
                    Intent intent = new Intent(MainActivity.this, PinDialogActivity.class);
                    intent.putExtra(PinDialogActivity.EXTRA_TARGET, PinDialogActivity.TARGET_SERVER);
                    startActivity(intent);
                }
            });
        }
        if (btnKembalikan != null) {
            int jumlah = HiddenMediaStore.getCount();
            setGroupText(btnKembalikan, "Kembalikan Media (" + jumlah + ")");
            btnKembalikan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(MainActivity.this, HiddenMediaListActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Gagal buka: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (btnRiwayat != null) {
            btnRiwayat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, PinDialogActivity.class);
                    intent.putExtra(PinDialogActivity.EXTRA_TARGET, PinDialogActivity.TARGET_RIWAYAT);
                    startActivity(intent);
                }
            });
        }
    }

    private void resolveActiveSourceAndLoad() {
        if ("offline".equals(activeSource)) {
            checkPermissionAndLoad();
        } else if ("custom".equals(activeSource)) {
            renderBeranda();
        } else {
            renderFromExternalOnly();
        }
    }

    private void renderFromExternalOnly() {
        combinedMedia.clear();
        combinedMedia.addAll(ExternalMediaStore.getAll(this));
        renderBeranda();
        refreshSidebar();
    }

    private void checkPermissionAndLoad() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= 33) {
            perms = new String[]{"android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO"};
        } else {
            perms = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
        }

        boolean allGranted = true;
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            requestPermissions(perms, REQ_STORAGE_PERMISSION);
        } else {
            loadOfflineMedia();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE_PERMISSION) {
            boolean granted = grantResults.length > 0;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) granted = false;
            }
            if (granted) {
                loadOfflineMedia();
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show();
                renderBeranda();
            }
        }
    }

    private void loadOfflineMedia() {
        localMedia.clear();

        Cursor imgCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED},
                null, null,
                MediaStore.Images.Media.DATE_ADDED + " DESC");
        int countImg = 0;
        if (imgCursor != null) {
            while (imgCursor.moveToNext()) {
                long id = imgCursor.getLong(0);
                long date = imgCursor.getLong(1);
                Uri uri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                MediaItem item = new MediaItem(uri, MediaItem.TYPE_IMAGE);
                item.isLocal = true;
                item.dateAdded = date;
                localMedia.add(item);
                countImg++;
            }
            imgCursor.close();
        }
        debugLogMain("Scan IMAGE: " + countImg);

        Cursor vidCursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_ADDED},
                null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC");
        int countVid = 0;
        if (vidCursor != null) {
            while (vidCursor.moveToNext()) {
                long id = vidCursor.getLong(0);
                long date = vidCursor.getLong(1);
                Uri uri = Uri.withAppendedPath(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                MediaItem item = new MediaItem(uri, MediaItem.TYPE_VIDEO);
                item.isLocal = true;
                item.dateAdded = date;
                localMedia.add(item);
                countVid++;
            }
            vidCursor.close();
        }
        debugLogMain("Scan VIDEO: " + countVid);

        // Query Audio (MP3, dll)
        try {
            Cursor audCursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE},
                    null, null,
                    MediaStore.Audio.Media.DATE_ADDED + " DESC");
            if (audCursor != null) {
                while (audCursor.moveToNext()) {
                    long id = audCursor.getLong(0);
                    String title = audCursor.getString(1);
                    Uri uri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    MediaItem item = new MediaItem(uri, 4); // TYPE_AUDIO = 4
                    item.isLocal = true;
                    item.title = title;
                    localMedia.add(item);
                }
                audCursor.close();
            }
        } catch (Exception ignored) {}

        // Query Audio (MP3, dll)
        try {
            Cursor audCursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE},
                    null, null,
                    MediaStore.Audio.Media.DATE_ADDED + " DESC");
            if (audCursor != null) {
                while (audCursor.moveToNext()) {
                    long id = audCursor.getLong(0);
                    String title = audCursor.getString(1);
                    Uri uri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    MediaItem item = new MediaItem(uri, 4); // TYPE_AUDIO = 4
                    item.isLocal = true;
                    item.title = title;
                    localMedia.add(item);
                }
                audCursor.close();
            }
        } catch (Exception ignored) {}

        // Tambahkan media tersembunyi
        try {
            List<MediaItem> hidden = HiddenMediaStore.scanHidden(this);
            localMedia.addAll(hidden);
        } catch (Exception ignored) {}

        combinedMedia.clear();
        combinedMedia.addAll(localMedia);
        int nImg = 0, nVid = 0, nAud = 0;
        for (MediaItem m : combinedMedia) {
            if (m.type == MediaItem.TYPE_IMAGE) nImg++;
            else if (m.type == MediaItem.TYPE_VIDEO) nVid++;
            else if (m.type == 4) nAud++;
        }
        debugLogMain("Setelah combinedMedia: img=" + nImg + " vid=" + nVid + " aud=" + nAud + " total=" + combinedMedia.size());
        renderBeranda();
        refreshSidebar();
    }

    private void kembalikanSemuaMedia() {
        List<MediaItem> hidden = HiddenMediaStore.scanHidden(this);
        if (hidden.isEmpty()) {
            Toast.makeText(this, "Tidak ada media tersembunyi", Toast.LENGTH_SHORT).show();
            return;
        }
        int berhasil = 0;
        for (MediaItem item : hidden) {
            if (HideHelper.kembalikan(this, item)) berhasil++;
        }
        // Hapus file .nomedia agar galeri bisa baca lagi
        try {
            File folder = HiddenMediaStore.getFolder();
            File nomedia = new File(folder, ".nomedia");
            if (nomedia.exists()) nomedia.delete();
        } catch (Exception ignored) {}

        Toast.makeText(this, berhasil + " media dikembalikan ke galeri", Toast.LENGTH_LONG).show();
        checkPermissionAndLoad();
    }

    private void setupFilterTabs() {
        final android.view.View btnSemua = container.findViewById(R.id.btnFilterSemua);
        final android.view.View btnFoto = container.findViewById(R.id.btnFilterFoto);
        final android.view.View btnVideo = container.findViewById(R.id.btnFilterVideo);

        if (btnSemua == null) return;

        // Init chip kategori
        categoryChipsContainer = container.findViewById(R.id.categoryChipsContainer);
        categoryChipScroll = container.findViewById(R.id.categoryChipScroll);

        btnSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 0;
                updateFilterTabsUI();
                renderBeranda();
            }
        });
        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 1;
                updateFilterTabsUI();
                renderBeranda();
            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 2;
                updateFilterTabsUI();
                renderBeranda();
            }
        });

        updateFilterTabsUI();
        setupSortButton();
        setupSearch();

        kategoriList = container.findViewById(R.id.kategoriList);
        scrollKategori = container.findViewById(R.id.scrollKategori);
        overlayExpand = container.findViewById(R.id.overlayExpand);
        gridExpand = container.findViewById(R.id.gridExpand);
        tvExpandTitle = container.findViewById(R.id.tvExpandTitle);
        btnShrink = container.findViewById(R.id.btnShrink);
        if (btnShrink != null) {
            btnShrink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoundHelper.click();
                    shrinkKategori();
                }
            });
        }
        gridBeranda = container.findViewById(R.id.gridBeranda);
    }

    private void setupSortButton() {
        btnSort = container.findViewById(R.id.btnSort);
        if (btnSort == null) return;
        SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        final String currentMode = prefs.getString(KEY_SORT_MODE, "tanggal_baru");
        setSortLabelSafe(currentMode);
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curr = getSharedPreferences("memecio_settings", MODE_PRIVATE).getString(KEY_SORT_MODE, "tanggal_baru");
                String next;
                if (curr.equals("nama_asc")) next = "nama_desc";
                else if (curr.equals("nama_desc")) next = "tanggal_desc";
                else next = "nama_asc";
                getSharedPreferences("memecio_settings", MODE_PRIVATE).edit().putString(KEY_SORT_MODE, next).apply();
                setSortLabelSafe(next);
                renderBeranda();
            }
        });
    }

    private void setSortLabelSafe(String mode) {
        if (btnSort == null) return;
        String label = getSortLabel(mode);
        try {
            if (btnSort instanceof TextView) {
                ((TextView) btnSort).setText(label);
            } else if (btnSort instanceof android.widget.ImageButton) {
                // ImageButton tidak punya text — ganti icon sesuai mode
                android.widget.ImageButton ib = (android.widget.ImageButton) btnSort;
                ib.setColorFilter(0xFF1C1C1E);
                // Bisa set contentDescription untuk aksesibilitas
                ib.setContentDescription("Sort: " + label);
            }
        } catch (Exception ignored) {}
    }

    private String getSortLabel(String mode) {
        if ("tanggal_baru".equals(mode)) return "Terbaru";
        if (mode.equals("nama_desc")) return "Z-A";
        if (mode.equals("tanggal_desc")) return "New";
        return "A-Z";
    }

    private void setupSearch() {
        etSearch = container.findViewById(R.id.etSearch);
        historyChipsContainer = container.findViewById(R.id.historyChipsContainer);
        if (etSearch == null) return;
        loadSearchHistory();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                // Debounce 300ms biar tidak lag saat ketik cepat
                if (searchDebounceRunnable != null) searchDebounceHandler.removeCallbacks(searchDebounceRunnable);
                searchDebounceRunnable = new Runnable() {
                    @Override public void run() { renderBeranda(); }
                };
                searchDebounceHandler.postDelayed(searchDebounceRunnable, 300);
            }
        });
        ImageButton btnSearch = container.findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchQuery = etSearch.getText().toString().trim();
                    if (!searchQuery.isEmpty()) saveSearchHistory(searchQuery);
                    renderBeranda();
                }
            });
        }
    }

    private void saveSearchHistory(String query) {
        SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        String history = prefs.getString(KEY_SEARCH_HISTORY, "");
        if (!history.contains(query + "|")) {
            history = query + "|" + history;
            String[] items = history.split("\\|");
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (String item : items) {
                if (!item.isEmpty() && count < 10) {
                    if (sb.length() > 0) sb.append("|");
                    sb.append(item);
                    count++;
                }
            }
            prefs.edit().putString(KEY_SEARCH_HISTORY, sb.toString()).apply();
            loadSearchHistory();
        }
    }

    private void loadSearchHistory() {
        if (historyChipsContainer == null) return;
        historyChipsContainer.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        String history = prefs.getString(KEY_SEARCH_HISTORY, "");
        if (history.isEmpty()) return;
        String[] items = history.split("\\|");
        for (String item : items) {
            if (item.isEmpty()) continue;
            final String keyword = item;
            TextView chip = new TextView(this);
            chip.setText(item);
            chip.setTextSize(12f);
            chip.setPadding(24, 12, 24, 12);
            chip.setTextColor(0xFF1C1C1E);
            chip.setBackgroundResource(R.drawable.bg_glass_button);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 8, 0);
            historyChipsContainer.addView(chip, params);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etSearch.setText(keyword);
                    etSearch.setSelection(keyword.length());
                }
            });
        }
    }

    private void applySort(List<MediaItem> list) {
        SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        String mode = prefs.getString(KEY_SORT_MODE, "tanggal_baru");
        Comparator<MediaItem> comp;
        if (mode.equals("tanggal_baru")) {
            comp = new Comparator<MediaItem>() {
                @Override
                public int compare(MediaItem a, MediaItem b) {
                    return Long.compare(b.dateAdded, a.dateAdded);
                }
            };
        } else if (mode.equals("nama_desc")) {
            comp = new Comparator<MediaItem>() {
                @Override
                public int compare(MediaItem a, MediaItem b) {
                    String na = a.title == null ? "" : a.title;
                    String nb = b.title == null ? "" : b.title;
                    return nb.compareToIgnoreCase(na);
                }
            };
        } else if (mode.equals("tanggal_desc")) {
            comp = new Comparator<MediaItem>() {
                @Override
                public int compare(MediaItem a, MediaItem b) {
                    String ua = a.uri == null ? "" : a.uri.toString();
                    String ub = b.uri == null ? "" : b.uri.toString();
                    return ub.compareTo(ua);
                }
            };
        } else {
            comp = new Comparator<MediaItem>() {
                @Override
                public int compare(MediaItem a, MediaItem b) {
                    String na = a.title == null ? "" : a.title;
                    String nb = b.title == null ? "" : b.title;
                    return na.compareToIgnoreCase(nb);
                }
            };
        }
        Collections.sort(list, comp);
    }

    private void updateFilterTabsUI() {
        android.view.View btnSemua = container.findViewById(R.id.btnFilterSemua);
        android.view.View btnFoto = container.findViewById(R.id.btnFilterFoto);
        android.view.View btnVideo = container.findViewById(R.id.btnFilterVideo);
        if (btnSemua == null) return;

        applyFilterStyle(btnSemua, currentFilter == 0);
        applyFilterStyle(btnFoto, currentFilter == 1);
        applyFilterStyle(btnVideo, currentFilter == 2);
    }

    private void applyFilterStyle(android.view.View btn, boolean isActive) {
        if (btn == null) return;
        try {
            int bgRes = isActive ? R.drawable.bg_glass_button_selected : R.drawable.bg_glass_button;
            btn.setBackgroundResource(bgRes);
        } catch (Exception ignored) {}

        // Kalau TextView: set text color
        if (btn instanceof TextView) {
            ((TextView) btn).setTextColor(isActive ? 0xFFFFFFFF : getResources().getColor(R.color.text_dark));
        }
        // Kalau ImageButton: set image tint
        else if (btn instanceof android.widget.ImageButton) {
            try {
                int tint = isActive ? 0xFFFFFFFF : 0xFF1C1C1E;
                ((android.widget.ImageButton) btn).setColorFilter(tint);
            } catch (Exception ignored) {}
        }
    }

    private void setupSidebar() {
        sidebarContainer = container.findViewById(R.id.sidebarContainer);
        sidebarList = container.findViewById(R.id.sidebarSavedLinks);
        btnToggleSidebar = container.findViewById(R.id.btnToggleSidebar);
        View rightContentContainer = container.findViewById(R.id.rightContentContainer);

        if (sidebarList == null || sidebarContainer == null) return;

        sidebarOpen = false;
        sidebarContainer.setVisibility(View.GONE);

        sidebarAdapter = new ArrayAdapter<MediaItem>(this, 0, sidebarSavedItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_playlist_row, parent, false);
                }
                final MediaItem entry = getItem(position);
                final TextView tv = view.findViewById(R.id.tvPlaylistTitle);
                String judul = (entry.title == null || entry.title.trim().isEmpty())
                        ? (entry.uri != null ? entry.uri.toString() : "(tanpa judul)")
                        : entry.title;
                tv.setText(judul);
                tv.setSelected(true);

                final boolean[] longFired = new boolean[1];
                final Handler longHandler = new Handler(Looper.getMainLooper());
                final Runnable longRunnable = new Runnable() {
                    @Override
                    public void run() {
                        longFired[0] = true;
                        confirmDeletePlaylist(entry);
                    }
                };

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadPlaylist(entry);
                        toggleSidebar(false);
                    }
                });

            TextView btnRename = view.findViewById(R.id.btnRenamePlaylist);
            if (btnRename != null) {
                btnRename.setFocusable(true);
                btnRename.setFocusableInTouchMode(true);
                btnRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tampilkanDialogRename(entry);
                    }
                });
            }

                TextView btnHapus = view.findViewById(R.id.btnHapusPlaylist);
                if (btnHapus != null) {
                    btnHapus.setFocusable(true);
                    btnHapus.setFocusableInTouchMode(true);
                    btnHapus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            konfirmasiHapusPlaylist(entry);
                        }
                    });
                    btnHapus.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, android.view.KeyEvent event) {
                            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN
                                    && keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                                toggleSidebar(false);
                                return true;
                            }
                            return false;
                        }
                    });
                }

                return view;
            }
        };
        sidebarList.setAdapter(sidebarAdapter);

        if (btnToggleSidebar != null) {
            btnToggleSidebar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSidebar(!sidebarOpen);
                }
            });
        }

        if (rightContentContainer != null) {
            rightContentContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && sidebarOpen) {
                        toggleSidebar(false);
                    }
                    return false;
                }
            });
        }

        // Tap di seluruh container untuk menutup sidebar
        if (container != null) {
            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && sidebarOpen) {
                        toggleSidebar(false);
                    }
                    return false;
                }
            });
        }

        refreshSidebar();
    }

    private void toggleSidebar(final boolean show) {
        if (sidebarContainer == null) return;
        if (sidebarOpen == show) return;

        sidebarOpen = show;
        float targetX = show ? 0f : -sidebarContainer.getWidth();

        sidebarContainer.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(sidebarContainer, "translationX",
                sidebarContainer.getTranslationX(), targetX);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!sidebarOpen) {
                    sidebarContainer.setVisibility(View.GONE);
                }
            }
        });
        animator.start();
    }

    private void confirmDeletePlaylist(final MediaItem entry) {
        String judul = (entry.title == null || entry.title.trim().isEmpty()) ? "playlist ini" : entry.title;
        final String[] opsi = {"Ganti Nama", "Hapus"};
        new AlertDialog.Builder(this)
                .setTitle("\"" + judul + "\"")
                .setItems(opsi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            tampilkanDialogRename(entry);
                        } else {
                            konfirmasiHapusPlaylist(entry);
                        }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void tampilkanDialogRename(final MediaItem entry) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_judul);

        final EditText etJudul = dialog.findViewById(R.id.etJudulPopup);
        View btnSimpan = dialog.findViewById(R.id.btnTampilkanPopup);
        View btnBatal = dialog.findViewById(R.id.btnBatalPopup);

        String judulLama = entry.title != null ? entry.title : "";
        if (etJudul != null) {
            etJudul.setText(judulLama);
            etJudul.setSelection(judulLama.length());
        }

        if (btnSimpan != null) {
            if (btnSimpan instanceof TextView) ((TextView) btnSimpan).setText("Simpan");
            btnSimpan.setOnClickListener(v -> {
                String namaBaru = etJudul != null ? etJudul.getText().toString().trim() : "";
                if (namaBaru.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                SavedLinksStore.updateTitle(MainActivity.this, entry.uri.toString(), namaBaru);
                refreshSidebar();
                Toast.makeText(MainActivity.this, "Nama diubah", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (btnBatal != null) {
            btnBatal.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void konfirmasiHapusPlaylist(final MediaItem entry) {
        String judul = (entry.title == null || entry.title.trim().isEmpty()) ? "playlist ini" : entry.title;
        new AlertDialog.Builder(this)
                .setTitle("Hapus Playlist")
                .setMessage("Apakah anda ingin menghapus \"" + judul + "\"?")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SavedLinksStore.hapus(MainActivity.this, entry.uri.toString());
                        refreshSidebar();
                        Toast.makeText(MainActivity.this, "Playlist dihapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void loadPlaylist(final MediaItem playlistItem) {
        final String url = playlistItem.uri.toString();

        // Deteksi Drive Folder
        if (url.contains("/drive/folders/")) {
            openDriveFolder(url);
            return;
        }

        Toast.makeText(this, "Memuat playlist...", Toast.LENGTH_SHORT).show();

        M3uParser.parseAsync(this, url, new M3uParser.Callback() {
            @Override
            public void onSuccess(List<M3uParser.MediaEntry> entries) {
                List<MediaItem> mediaItems = new ArrayList<>();
                if (entries.isEmpty()) {
                    MediaItem mi = new MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO);
                    mi.isLocal = false;
                    mediaItems.add(mi);
                } else {
                    for (M3uParser.MediaEntry e : entries) {
                        MediaItem mi = new MediaItem(Uri.parse(e.url), MediaItem.TYPE_VIDEO);
                        mi.isLocal = false;
                        mi.title = e.title;
                        mi.thumbUrl = e.thumbUrl;
                        mi.isM3u = true;
                        mediaItems.add(mi);
                    }
                }
                ExternalMediaStore.gantiSemua(MainActivity.this, mediaItems, playlistItem.title);
                activeSource = ExternalMediaStore.getSourceLabel(MainActivity.this);
                SoundHelper.success();
                renderFromExternalOnly();
            }

            @Override
            public void onError(String message) {
                List<MediaItem> mediaItems = new ArrayList<>();
                MediaItem mi = new MediaItem(Uri.parse(url), MediaItem.TYPE_VIDEO);
                mi.isLocal = false;
                mediaItems.add(mi);
                ExternalMediaStore.gantiSemua(MainActivity.this, mediaItems, playlistItem.title);
                activeSource = ExternalMediaStore.getSourceLabel(MainActivity.this);
                SoundHelper.error();
                Toast.makeText(MainActivity.this, "Gagal parsing: " + message, Toast.LENGTH_LONG).show();
                renderFromExternalOnly();
            }
        });
    }

    private void refreshSidebar() {
        if (sidebarList == null || sidebarAdapter == null) return;

        sidebarSavedItems.clear();
        sidebarSavedItems.addAll(SavedLinksStore.getAll(this));

        sidebarAdapter.notifyDataSetChanged();
    }

    private void setGroupText(View v, String text) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View c = vg.getChildAt(i);
                if (c instanceof TextView) {
                    ((TextView) c).setText(text);
                    return;
                }
            }
        }
    }

    private void logCrashToFile(Throwable t) {
        try {
            java.io.File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            java.io.File f = new java.io.File(dir, "memecio_crash.txt");
            java.io.FileWriter fw = new java.io.FileWriter(f, true);
            fw.write("\n=== " + new java.util.Date() + " ===\n");
            java.io.StringWriter sw = new java.io.StringWriter();
            t.printStackTrace(new java.io.PrintWriter(sw));
            fw.write(sw.toString());
            fw.close();
            android.util.Log.e("MEMECIO_CRASH", "Logged to: " + f.getAbsolutePath());
        } catch (Exception ignored) {}
    }

    private void applyTvFocusRingsIfTv() {
        boolean isTv = DisplayModeStore.isTvMode(this);
        View root = findViewById(android.R.id.content);
        traverseFocusRings(root, isTv);

        // Auto-focus untuk verifikasi visual (Mode TV)
        if (isTv && btnBeranda != null) {
            btnBeranda.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnBeranda.requestFocus();
                }
            }, 300);
        }
    }

    private void traverseFocusRings(View v, boolean apply) {
        if (v == null) return;
        if (v.isFocusable()) {
            if (apply) {
                if (v.getTag(R.id.tag_original_bg) == null) {
                    v.setTag(R.id.tag_original_bg, v.getBackground());
                }
                v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            view.setBackgroundResource(R.drawable.bg_tv_focus_ring);
                            view.setElevation(12f);
                        } else {
                            Object orig = view.getTag(R.id.tag_original_bg);
                            if (orig instanceof android.graphics.drawable.Drawable) {
                                view.setBackground((android.graphics.drawable.Drawable) orig);
                            } else {
                                view.setBackground(null);
                            }
                            view.setElevation(0f);
                        }
                    }
                });
            } else {
                v.setOnFocusChangeListener(null);
                Object orig = v.getTag(R.id.tag_original_bg);
                if (orig instanceof android.graphics.drawable.Drawable) {
                    v.setBackground((android.graphics.drawable.Drawable) orig);
                }
                v.setElevation(0f);
                v.setTag(R.id.tag_original_bg, null);
            }
        }
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                traverseFocusRings(vg.getChildAt(i), apply);
            }
        }
    }

    private void applyDisplayModeSafe() {
        try {
            applyDisplayMode();
        } catch (Throwable t) {
            logCrashToFile(t);
            android.util.Log.e("MEMECIO_CRASH", "applyDisplayMode failed", t);
        }
    }

    private void applyImmersiveIfLandscape() {
        try {
            int orient = getResources().getConfiguration().orientation;
            if (orient == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.layoutInDisplayCutoutMode =
                        android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    getWindow().setAttributes(lp);
                }
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    android.view.WindowInsetsController c = getWindow().getInsetsController();
                    if (c != null) {
                        c.hide(android.view.WindowInsets.Type.statusBars());
                        c.setSystemBarsBehavior(
                            android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    }
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    );
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    android.view.WindowInsetsController c = getWindow().getInsetsController();
                    if (c != null) c.show(android.view.WindowInsets.Type.statusBars());
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        } catch (Exception ignored) {}
    }

    private void applyDisplayMode() {
        String effectiveMode = DisplayModeStore.getEffectiveMode(this);
        int currentOrientation = getResources().getConfiguration().orientation;
        int requested = getRequestedOrientation();
        if (effectiveMode.equals("tv")) {
            if (currentOrientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    || requested != android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            if (requested != android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
        applyTvFocusRingsIfTv();
    }

    private void updateModeButton(View btnMode) {
        String mode = DisplayModeStore.getMode(this);
        String label = DisplayModeStore.getModeLabel(mode);
        String suffix = "";
        if (DisplayModeStore.MODE_AUTO.equals(mode)) {
            String effective = DisplayModeStore.getEffectiveMode(this);
            suffix = " (" + (DisplayModeStore.MODE_TV.equals(effective) ? "TV" : "HP") + ")";
        }
        setGroupText(btnMode, "Mode Tampilan: " + label + suffix);
    }

    private void setupProfilButtons() {
        final View btnMode = container.findViewById(R.id.btnModeTampilan);
        final View btnDataSaver = container.findViewById(R.id.btnDataSaver);
        View btnZoomUI = container.findViewById(R.id.btnZoomUI);
        View btnPencarianOnline = container.findViewById(R.id.btnPencarianOnline);
        View btnStatistik = container.findViewById(R.id.btnStatistik);
        TextView tvVersiBuild = container.findViewById(R.id.tvVersiBuild);
        if (tvVersiBuild != null) {
            try {
                android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                long buildTime = pInfo.lastUpdateTime;
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", new java.util.Locale("id", "ID"));
                tvVersiBuild.setText("Build: " + fmt.format(new java.util.Date(buildTime)));
            } catch (Exception e) {
                tvVersiBuild.setText("Build: -");
            }
        }
        TextView tvVersiApp = container.findViewById(R.id.tvVersiApp);
        if (tvVersiApp != null) {
            try {
                android.content.pm.PackageInfo pInfo2 = getPackageManager().getPackageInfo(getPackageName(), 0);
                tvVersiApp.setText("Memec.io v" + pInfo2.versionName);
            } catch (Exception e) {
                tvVersiApp.setText("Memec.io");
            }
        }
        View btnBackupLengkap = container.findViewById(R.id.btnBackupLengkap);
        View btnLogCrash = container.findViewById(R.id.btnLogCrash);
        View btnDevTools = container.findViewById(R.id.btnDevTools);
        View btnAutoExit = container.findViewById(R.id.btnAutoExit);

        if (btnMode != null) {
            updateModeButton(btnMode);

            btnMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String current = DisplayModeStore.getMode(MainActivity.this);
                    String next = DisplayModeStore.nextMode(current);
                    DisplayModeStore.setMode(MainActivity.this, next);
                    updateModeButton(btnMode);
                    applyDisplayMode();
                }
            });
        }

        if (btnDataSaver != null) {
            final SharedPreferences prefs = getSharedPreferences("memecio_settings", MODE_PRIVATE);
            boolean isOn = prefs.getBoolean("data_saver", false);
            setGroupText(btnDataSaver, isOn ? "Mode Hemat Data: Aktif" : "Mode Hemat Data: Mati");
            btnDataSaver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean current = prefs.getBoolean("data_saver", false);
                    boolean next = !current;
                    prefs.edit().putBoolean("data_saver", next).apply();
                    setGroupText(btnDataSaver, next ? "Mode Hemat Data: Aktif" : "Mode Hemat Data: Mati");
                    Toast.makeText(MainActivity.this, next ? "Hemat data aktif" : "Hemat data mati", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnZoomUI != null) {
            final SharedPreferences prefsZoom = getSharedPreferences("memecio_settings", MODE_PRIVATE);
            float currentScale = prefsZoom.getFloat("ui_scale", 1.0f);
            setGroupText(btnZoomUI, "Ukuran UI: " + (int)(currentScale * 100) + "%");
            btnZoomUI.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    float cur = prefsZoom.getFloat("ui_scale", 1.0f);
                    float next;
                    if (cur < 1.2f) next = 1.3f;
                    else if (cur < 1.4f) next = 1.5f;
                    else next = 1.0f;
                    prefsZoom.edit().putFloat("ui_scale", next).apply();
                    setGroupText(btnZoomUI, "Ukuran UI: " + (int)(next * 100) + "%");
                    applyUiScale(next);
                    Toast.makeText(MainActivity.this, "Ukuran UI: " + (int)(next * 100) + "%", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View btnGridColumns = container.findViewById(R.id.btnGridColumns);
        if (btnGridColumns != null) {
            setGroupText(btnGridColumns, "Kolom Grid: " + GridColumnsStore.label(MainActivity.this));
            btnGridColumns.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] labels = {"Auto", "1 Kolom", "2 Kolom", "3 Kolom", "4 Kolom", "5 Kolom", "6 Kolom", "7 Kolom", "8 Kolom"};
                    new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Jumlah Kolom Grid")
                        .setItems(labels, new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface d, int which) {
                                int val = (which == 0) ? GridColumnsStore.AUTO : which;
                                GridColumnsStore.set(MainActivity.this, val);
                                setGroupText(btnGridColumns, "Kolom Grid: " + GridColumnsStore.label(MainActivity.this));
                                if (gridBeranda != null) {
                                    int cols = GridColumnsStore.get(MainActivity.this);
                                    if (cols > 0) gridBeranda.setNumColumns(cols);
                                }
                                Toast.makeText(MainActivity.this, "Kolom Grid: " + GridColumnsStore.label(MainActivity.this), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                }
            });
        }

        if (btnPencarianOnline != null) {
            btnPencarianOnline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Ketuk tab Beranda untuk mencari konten", Toast.LENGTH_LONG).show();
                    switchToTab(0);
                    renderFromExternalOnly();
                }
            });
        }

        if (btnStatistik != null) {
            btnStatistik.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, StatistikActivity.class));
                }
            });
        }

        if (btnBackupLengkap != null) {
            btnBackupLengkap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBackupChoiceDialog();
                }
            });
        }

        if (btnLogCrash != null) {
            btnLogCrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, CrashLogActivity.class));
                }
            });
        }

        // Auto Exit Timer
        if (btnAutoExit != null) {
            btnAutoExit.setOnClickListener(v -> showAutoExitDialog());
        }

        // Dev Tools — hanya tampil kalau Developer Mode aktif
        if (btnDevTools != null) {
            if (DeveloperModeStore.isEnabled(this)) {
                btnDevTools.setVisibility(View.VISIBLE);
                btnDevTools.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDevToolsDialog();
                    }
                });
            } else {
                btnDevTools.setVisibility(View.GONE);
            }
        }
    }

    private void showBackupChoiceDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_backup_choice);

        android.view.Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.btnChoiceBackupAman).setOnClickListener(v -> {
            dialog.dismiss();
            checkPermissionAndBackupAman();
        });

        dialog.findViewById(R.id.btnChoiceEkspor).setOnClickListener(v -> {
            dialog.dismiss();
            BackupRestoreHelper.exportPlaylists(MainActivity.this);
        });

        dialog.findViewById(R.id.btnChoiceImpor).setOnClickListener(v -> {
            dialog.dismiss();
            new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Impor JSON")
                .setMessage("Impor data dari file backup terbaru?\nData lama tetap ada, hanya ditambahkan.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Impor", (d, which) -> BackupRestoreHelper.importPlaylists(MainActivity.this))
                .show();
        });

        dialog.findViewById(R.id.btnBatalBackupChoice).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkPermissionAndBackupAman() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Izinkan akses semua file, lalu tekan Backup lagi", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
                return;
            }
        }
        ProjectExportHelper.export(MainActivity.this);
    }

    private void showDevToolsDialog() {
        java.util.List<SecretCodeRegistry.Code> all = SecretCodeRegistry.getAll();
        String[] items = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            SecretCodeRegistry.Code c = all.get(i);
            items[i] = c.code + " - " + c.title + (c.hidden ? " [hidden]" : "");
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("Developer Tools")
            .setItems(items, (d, which) -> {
                SecretCodeRegistry.Code c = all.get(which);
                handleDevCode(c.code);
            })
            .setNegativeButton("Tutup", null)
            .show();
    }

    private void handleDevCode(String code) {
        try {
            if ("000".equals(code)) startActivity(new Intent(this, SecretCodesActivity.class));
            else if ("111".equals(code)) startActivity(new Intent(this, CrashHistoryActivity.class));
            else if ("222".equals(code)) startActivity(new Intent(this, ChangelogActivity.class));
            else if ("333".equals(code)) startActivity(new Intent(this, SystemInfoActivity.class));
            else if ("555".equals(code)) startActivity(new Intent(this, NetworkInfoActivity.class));
            else if ("666".equals(code)) startActivity(new Intent(this, PermissionInfoActivity.class));
            else if ("777".equals(code)) startActivity(new Intent(this, StorageAnalyzerActivity.class));
            else if ("888".equals(code)) startActivity(new Intent(this, StatistikActivity.class));
            else if ("999".equals(code)) ProjectExportHelper.export(this);
            else if ("123".equals(code)) CacheResetter.confirmAndReset(this);
            else if ("456".equals(code)) RepairDatabaseHelper.confirmAndRepair(this);
            else if ("789".equals(code)) FactoryResetHelper.confirmAndReset(this);
            else if ("101".equals(code)) {
                DeveloperModeStore.toggle(this);
                recreate();
            }
            else if ("103".equals(code)) TestGestureHelper.showGuide(this);
            else if ("104".equals(code)) TestModesHelper.showChoice(this);
            else if ("102".equals(code)) throw new RuntimeException("Force crash via Dev Tools");
        } catch (Exception e) {
            Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAutoExitDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_auto_exit);

        android.view.Window w = dialog.getWindow();
        if (w != null) w.setBackgroundDrawableResource(android.R.color.transparent);

        View b5 = dialog.findViewById(R.id.btnExit5);
        View b15 = dialog.findViewById(R.id.btnExit15);
        View b30 = dialog.findViewById(R.id.btnExit30);
        View b60 = dialog.findViewById(R.id.btnExit60);
        View b120 = dialog.findViewById(R.id.btnExit120);
        View bCancel = dialog.findViewById(R.id.btnExitCancel);
        View bBatal = dialog.findViewById(R.id.btnExitBatal);

        if (b5 != null) b5.setOnClickListener(v -> { AutoExitManager.getInstance().start(MainActivity.this, 5); dialog.dismiss(); });
        if (b15 != null) b15.setOnClickListener(v -> { AutoExitManager.getInstance().start(MainActivity.this, 15); dialog.dismiss(); });
        if (b30 != null) b30.setOnClickListener(v -> { AutoExitManager.getInstance().start(MainActivity.this, 30); dialog.dismiss(); });
        if (b60 != null) b60.setOnClickListener(v -> { AutoExitManager.getInstance().start(MainActivity.this, 60); dialog.dismiss(); });
        if (b120 != null) b120.setOnClickListener(v -> { AutoExitManager.getInstance().start(MainActivity.this, 120); dialog.dismiss(); });
        if (bCancel != null) bCancel.setOnClickListener(v -> {
            AutoExitManager.getInstance().cancel(MainActivity.this);
            Toast.makeText(MainActivity.this, "Auto Exit dimatikan", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        if (bBatal != null) bBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void buildCategoryChips() {
        if (categoryChipsContainer == null || categoryChipScroll == null) return;
        categoryChipsContainer.removeAllViews();

        // Hitung jumlah per kategori dari combinedMedia
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        for (MediaItem m : combinedMedia) {
            String judul = m.title != null ? m.title : m.uri.toString();
            String cat = CategoryHelper.deteksiKategoriUtama(judul);
            if (cat == null || cat.isEmpty()) continue;
            if ("Lainnya".equals(cat) || "Radio".equals(cat)) continue;
            Integer c = counts.get(cat);
            counts.put(cat, c == null ? 1 : c + 1);
        }

        if (counts.isEmpty()) {
            categoryChipScroll.setVisibility(View.GONE);
            return;
        }

        categoryChipScroll.setVisibility(View.VISIBLE);

        // Sort: terbanyak dulu
        java.util.List<java.util.Map.Entry<String, Integer>> sorted = new java.util.ArrayList<>(counts.entrySet());
        java.util.Collections.sort(sorted, (a, b) -> b.getValue() - a.getValue());

        float density = getResources().getDisplayMetrics().density;

        // Chip "Semua Kategori" (kalau activeCategory tidak kosong)
        if (!activeCategory.isEmpty()) {
            TextView chipAll = new TextView(this);
            chipAll.setText("\u2715 Semua");
            chipAll.setTextSize(11f);
            chipAll.setPadding((int)(12*density), (int)(6*density), (int)(12*density), (int)(6*density));
            chipAll.setTextColor(0xFFFFFFFF);
            chipAll.setBackgroundResource(R.drawable.bg_category_chip_active);
            LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp0.setMargins(0, 0, (int)(6*density), 0);
            chipAll.setLayoutParams(lp0);
            chipAll.setOnClickListener(v -> {
                activeCategory = "";
                buildCategoryChips();
                renderBeranda();
            });
            categoryChipsContainer.addView(chipAll);
        }

        for (java.util.Map.Entry<String, Integer> e : sorted) {
            String cat = e.getKey();
            int count = e.getValue();

            TextView chip = new TextView(this);
            chip.setText(cat + " (" + count + ")");
            chip.setTextSize(11f);
            chip.setPadding((int)(12*density), (int)(6*density), (int)(12*density), (int)(6*density));

            boolean isActive = cat.equals(activeCategory);
            if (isActive) {
                chip.setTextColor(0xFFFFFFFF);
                chip.setBackgroundResource(R.drawable.bg_category_chip_active);
            } else {
                chip.setTextColor(getResources().getColor(R.color.text_dark));
                chip.setBackgroundResource(R.drawable.bg_glass_button);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, (int)(6*density), 0);
            chip.setLayoutParams(lp);

            final String catFinal = cat;
            chip.setOnClickListener(v -> {
                if (catFinal.equals(activeCategory)) {
                    activeCategory = "";  // Toggle off
                } else {
                    activeCategory = catFinal;
                }
                buildCategoryChips();
                renderBeranda();
            });

            categoryChipsContainer.addView(chip);
        }
    }

    private void applyUiScale(float scale) {
        try {
            android.content.res.Configuration config = getResources().getConfiguration();
            config.fontScale = scale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            recreate();
        } catch (Exception ignored) {}
    }


    private void renderKategori(List<MediaItem> items) {
        if (kategoriList == null) return;
        kategoriList.removeAllViews();

        // Kelompokkan
        Map<String, List<MediaItem>> groups = new LinkedHashMap<>();
        List<MediaItem> videoOnly = new ArrayList<>();
        for (MediaItem m : items) {
            if (m.type == MediaItem.TYPE_VIDEO) videoOnly.add(m);
        }
        if (videoOnly.isEmpty()) {
            TextView tvKosong = new TextView(this);
            tvKosong.setText("Belum ada video untuk kategori");
            tvKosong.setTextColor(0xFF1C1C1E);
            tvKosong.setTextSize(13f);
            tvKosong.setPadding(24, 24, 24, 24);
            kategoriList.addView(tvKosong);
            return;
        }

        // 1. Riwayat (paling atas)
        List<MediaItem> riwayatList = new ArrayList<>();
        List<String> riwayatUrls = RiwayatStore.getAll(this);
        for (String url : riwayatUrls) {
            for (MediaItem m : videoOnly) {
                if (m.uri.toString().equals(url)) {
                    riwayatList.add(m);
                    break;
                }
            }
        }
        if (!riwayatList.isEmpty()) groups.put("Riwayat", riwayatList);

        // 2. Populer (berdasarkan counter)
        List<MediaItem> populerList = new ArrayList<>(videoOnly);
        java.util.Collections.sort(populerList, new java.util.Comparator<MediaItem>() {
            @Override public int compare(MediaItem a, MediaItem b) {
                return ViewCountStore.getCount(MainActivity.this, b.uri.toString())
                     - ViewCountStore.getCount(MainActivity.this, a.uri.toString());
            }
        });
        List<MediaItem> populerTop = new ArrayList<>();
        for (int i = 0; i < populerList.size() && i < 20; i++) {
            if (ViewCountStore.getCount(this, populerList.get(i).uri.toString()) > 0) {
                populerTop.add(populerList.get(i));
            }
        }
        if (!populerTop.isEmpty()) groups.put("Populer", populerTop);

        // 3. Terbaru (dari urutan riwayat, terbalik)
        if (!riwayatList.isEmpty()) groups.put("Terbaru", riwayatList);

        // 4. Kategori keyword dari judul
        Map<String, List<MediaItem>> kategoriMap = new LinkedHashMap<>();
        for (MediaItem m : videoOnly) {
            List<String> cats = CategoryHelper.deteksiKategori(m.title != null ? m.title : m.uri.toString());
            for (String cat : cats) {
                if (!kategoriMap.containsKey(cat)) kategoriMap.put(cat, new ArrayList<MediaItem>());
                kategoriMap.get(cat).add(m);
            }
        }

        // Gabungkan: Riwayat, Populer, Terbaru, lalu kategori keyword
        for (Map.Entry<String, List<MediaItem>> e : kategoriMap.entrySet()) {
            groups.put(e.getKey(), e.getValue());
        }

        // Render setiap grup
        for (Map.Entry<String, List<MediaItem>> entry : groups.entrySet()) {
            addKategoriRow(entry.getKey(), entry.getValue());
        }
    }

    private void expandKategori(String judul, final List<MediaItem> items) {
        if (overlayExpand == null || gridExpand == null) return;
        if (tvExpandTitle != null) tvExpandTitle.setText(judul);
        gridExpand.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int itemWidthPx = (int)(110 * density);
        int itemHeightPx = (int)(80 * density);

        for (int i = 0; i < items.size(); i++) {
            final MediaItem item = items.get(i);
            View card = getLayoutInflater().inflate(R.layout.item_thumbnail_kat, gridExpand, false);

            android.widget.GridLayout.LayoutParams glp = new android.widget.GridLayout.LayoutParams();
            glp.width = 0;
            glp.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            glp.setMargins(6, 6, 6, 6);
            card.setLayoutParams(glp);

            FrameLayout frameKat = card.findViewById(R.id.frameKat);
            ViewGroup.LayoutParams fp = frameKat.getLayoutParams();
            fp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            fp.height = itemHeightPx;
            frameKat.setLayoutParams(fp);

            ImageView img = card.findViewById(R.id.imgThumbKat);
            TextView tvTitle = card.findViewById(R.id.tvTitleKat);
            TextView tvPlay = card.findViewById(R.id.tvPlayBadgeKat);
            View overlay = card.findViewById(R.id.overlayKat);
            if (overlay != null) overlay.setVisibility(View.GONE);

            ViewGroup.LayoutParams tp = tvTitle.getLayoutParams();
            tp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            tvTitle.setLayoutParams(tp);

            tvTitle.setText(item.title != null && !item.title.isEmpty() ? item.title : "Video");
            tvPlay.setVisibility(View.VISIBLE);

            img.setTag(item.uri.toString());
            loadThumbToImageView(item, img);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoundHelper.click();
                    ArrayList<String> videoPlaylist = new ArrayList<>();
                    int clickedIndex = 0;
                    int counter = 0;
                    for (MediaItem m : items) {
                        if (m.type == MediaItem.TYPE_VIDEO) {
                            videoPlaylist.add(m.uri.toString());
                            if (m.uri.equals(item.uri)) clickedIndex = counter;
                            counter++;
                        }
                    }
                    PlaylistHolder.set(videoPlaylist);
                    Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("index", clickedIndex);
                    startActivity(intent);
                }
            });

            gridExpand.addView(card);
        }

        overlayExpand.setVisibility(View.VISIBLE);
        overlayExpand.setAlpha(0f);
        overlayExpand.setScaleX(0.85f);
        overlayExpand.setScaleY(0.85f);
        overlayExpand.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(350)
                .start();
    }

    private void shrinkKategori() {
        if (overlayExpand == null) return;
        overlayExpand.animate()
                .alpha(0f).scaleX(0.85f).scaleY(0.85f)
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        overlayExpand.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        if (overlayExpand != null && overlayExpand.getVisibility() == View.VISIBLE) {
            shrinkKategori();
        } else {
            super.onBackPressed();
        }
    }

    private void addKategoriRow(final String judul, List<MediaItem> items) {
        if (kategoriList == null) return;

        // Header dengan tombol Perluas
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        headerRow.setPadding(16, 18, 16, 6);

        TextView header = new TextView(this);
        header.setText(judul);
        header.setTextColor(0xFF1C1C1E);
        header.setTextSize(14f);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        header.setLayoutParams(hp);
        headerRow.addView(header);

        ImageButton btnExpand = new ImageButton(this);
        btnExpand.setImageResource(android.R.drawable.ic_menu_more);
        btnExpand.setBackgroundResource(R.drawable.bg_glass_button);
        btnExpand.setPadding(8, 8, 8, 8);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(34, 34);
        btnExpand.setLayoutParams(bp);
        btnExpand.setFocusable(true);
        btnExpand.setFocusableInTouchMode(true);
        final List<MediaItem> itemsFinal = items;
        btnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundHelper.click();
                expandKategori(judul, itemsFinal);
            }
        });
        btnExpand.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (overlayExpand != null && overlayExpand.getVisibility() == View.VISIBLE && btnShrink != null) {
                    btnShrink.requestFocus();
                    SoundHelper.nav();
                    return true;
                }
                return false;
            }
        });
        headerRow.addView(btnExpand);

        kategoriList.addView(headerRow);

        // Ukuran item
        int orientation = getResources().getConfiguration().orientation;
        int itemWidthDp = (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) ? 120 : 110;
        float density = getResources().getDisplayMetrics().density;
        int itemWidthPx = (int)(itemWidthDp * density);
        int itemHeightPx = (int)(80 * density);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setFocusable(true);
        hsv.setFocusableInTouchMode(true);
        hsv.setPadding(itemWidthPx / 2, 0, itemWidthPx / 2, 0);
        hsv.setClipToPadding(false);
        LinearLayout.LayoutParams hsvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hsv.setLayoutParams(hsvParams);

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(0, 4, 0, 4);

        for (int i = 0; i < items.size(); i++) {
            final MediaItem item = items.get(i);
            final int pos = i;
            final int total = items.size();

            View card = getLayoutInflater().inflate(R.layout.item_thumbnail_kat, rowLayout, false);

            FrameLayout frameKat = card.findViewById(R.id.frameKat);
            ImageView img = card.findViewById(R.id.imgThumbKat);
            View overlay = card.findViewById(R.id.overlayKat);
            TextView tvTitle = card.findViewById(R.id.tvTitleKat);
            TextView tvPlay = card.findViewById(R.id.tvPlayBadgeKat);

            ViewGroup.LayoutParams fp = frameKat.getLayoutParams();
            fp.width = itemWidthPx;
            fp.height = itemHeightPx;
            frameKat.setLayoutParams(fp);

            ViewGroup.LayoutParams tp = tvTitle.getLayoutParams();
            tp.width = itemWidthPx;
            tvTitle.setLayoutParams(tp);

            tvTitle.setText(item.title != null && !item.title.isEmpty() ? item.title : "Video");
            tvPlay.setVisibility(View.VISIBLE);

            if (pos == 0 && total > 1) {
                overlay.setVisibility(View.VISIBLE);
                overlay.setBackgroundResource(R.drawable.bg_gradient_left);
            } else if (pos == total - 1 && total > 1) {
                overlay.setVisibility(View.VISIBLE);
                overlay.setBackgroundResource(R.drawable.bg_gradient_right);
            } else {
                overlay.setVisibility(View.GONE);
            }

            img.setTag(item.uri.toString());
            loadThumbToImageView(item, img);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoundHelper.click();
                    ArrayList<String> videoPlaylist = new ArrayList<>();
                    int clickedIndex = 0;
                    int counter = 0;
                    for (MediaItem m : items) {
                        if (m.type == MediaItem.TYPE_VIDEO) {
                            videoPlaylist.add(m.uri.toString());
                            if (m.uri.equals(item.uri)) clickedIndex = counter;
                            counter++;
                        }
                    }
                    PlaylistHolder.set(videoPlaylist);
                    Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("index", clickedIndex);
                    startActivity(intent);
                }
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    itemWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(3, 0, 3, 0);
            rowLayout.addView(card, lp);
        }

        hsv.addView(rowLayout);
        kategoriList.addView(hsv);
    }

    private void loadThumbToImageView(final MediaItem item, final ImageView img) {
        new Thread(new Runnable() {
            @Override public void run() {
                android.graphics.Bitmap bmp = null;
                try {
                    if (item.isLocal) {
                        long id = Long.parseLong(item.uri.getLastPathSegment());
                        bmp = android.provider.MediaStore.Video.Thumbnails.getThumbnail(
                                getContentResolver(), id,
                                android.provider.MediaStore.Video.Thumbnails.MINI_KIND, null);
                    } else if (item.thumbUrl != null && !item.thumbUrl.isEmpty()) {
                        java.net.URL url = new java.net.URL(item.thumbUrl);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(4000);
                        conn.setReadTimeout(4000);
                        java.io.InputStream is = conn.getInputStream();
                        bmp = android.graphics.BitmapFactory.decodeStream(is);
                        is.close();
                    }
                } catch (Exception ignored) {}
                final android.graphics.Bitmap fb = bmp;
                if (fb != null) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (item.uri.toString().equals(img.getTag())) {
                                img.setImageBitmap(fb);
                            }
                        }
                    });
                }
            }
        }).start();
    }




    private void renderBeranda() {
        View emptyState = container.findViewById(R.id.emptyState);
        GridView grid = container.findViewById(R.id.gridBeranda);
        if (grid == null) return;

        int userCols = GridColumnsStore.get(this);
        if (userCols > 0) grid.setNumColumns(userCols);

        // Cek mode kategori
        SharedPreferences prefsKat = getSharedPreferences("memecio_settings", MODE_PRIVATE);
        boolean kategoriOn = prefsKat.getBoolean("kategori_enabled", false);

        // Bangun daftar filtered dulu
        final List<MediaItem> filteredTmp = new ArrayList<>();
        for (MediaItem m : combinedMedia) {
            boolean cocokFilter = false;
            if (currentFilter == 0) cocokFilter = true;
            else if (currentFilter == 1 && m.type == MediaItem.TYPE_IMAGE) cocokFilter = true;
            else if (currentFilter == 2 && m.type == MediaItem.TYPE_VIDEO) cocokFilter = true;
            else if (currentFilter == 0 && m.type == 4) cocokFilter = true;

            boolean cocokSearch = true;
            if (!searchQuery.isEmpty()) {
                String judul = m.title != null ? m.title : "";
                cocokSearch = judul.toLowerCase().contains(searchQuery.toLowerCase());
            }

            if (cocokFilter && cocokSearch) filteredTmp.add(m);
        }

        if (kategoriOn) {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            grid.setVisibility(View.GONE);
            if (scrollKategori != null) scrollKategori.setVisibility(View.VISIBLE);
            renderKategori(filteredTmp);
            return;
        } else {
            if (scrollKategori != null) scrollKategori.setVisibility(View.GONE);
        }

        final List<MediaItem> filtered = new ArrayList<>();
        final String queryLower = searchQuery.isEmpty() ? "" : searchQuery.toLowerCase();

        List<MediaItem> startsWith = new ArrayList<>();
        List<MediaItem> contains = new ArrayList<>();

        for (MediaItem m : combinedMedia) {
            boolean cocokFilter = false;
            if (currentFilter == 0) cocokFilter = true;
            else if (currentFilter == 1 && m.type == MediaItem.TYPE_IMAGE) cocokFilter = true;
            else if (currentFilter == 2 && m.type == MediaItem.TYPE_VIDEO) cocokFilter = true;

            if (!cocokFilter) continue;

            // Filter kategori konten
            if (!activeCategory.isEmpty()) {
                String judulCat = m.title != null ? m.title : m.uri.toString();
                String catItem = CategoryHelper.deteksiKategoriUtama(judulCat);
                if (!activeCategory.equals(catItem)) continue;
            }

            if (queryLower.isEmpty()) {
                filtered.add(m);
            } else {
                String judul = m.title != null ? m.title.toLowerCase() : "";
                if (judul.startsWith(queryLower)) startsWith.add(m);
                else if (judul.contains(queryLower)) contains.add(m);
            }
        }

        if (!queryLower.isEmpty()) {
            filtered.addAll(startsWith);
            filtered.addAll(contains);
        }

        applySort(filtered);

        if (filtered.isEmpty()) {
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
                if (!queryLower.isEmpty()) {
                    try {
                        TextView tvE = (TextView) emptyState;
                        tvE.setText("Tidak ada hasil untuk \"" + searchQuery + "\"");
                    } catch (Exception ignored) {}
                }
            }
            grid.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            grid.setVisibility(View.VISIBLE);
            ThumbnailAdapter adapter = new ThumbnailAdapter(this, filtered, new ThumbnailAdapter.OnThumbnailClickListener() {
                @Override
                public void onThumbnailClick(MediaItem item) {
                    debugLogMain("Thumbnail diklik: type=" + item.type + " uri=" + item.uri + " isLocal=" + item.isLocal);
                    if (item.type == 4) {
                        // TYPE_AUDIO
                        Intent audioIntent = new Intent(MainActivity.this, AudioPlayerActivity.class);
                        audioIntent.putExtra("audio_uri", item.uri.toString());
                        audioIntent.putExtra("audio_title", item.title != null ? item.title : "Audio");
                        startActivity(audioIntent);
                    } else if (item.type == MediaItem.TYPE_VIDEO || item.type == MediaItem.TYPE_IMAGE) {
                        // Cari index di filtered
                        int clickIdx = 0;
                        for (int i = 0; i < filtered.size(); i++) {
                            if (filtered.get(i).uri.equals(item.uri)) { clickIdx = i; break; }
                        }
                        MixedPlaylistHolder.set(filtered, clickIdx);

                        if (item.type == MediaItem.TYPE_VIDEO) {
                            // Bangun PlaylistHolder berisi HANYA video (untuk kompatibilitas VideoPlayerActivity)
                            ArrayList<String> videoPlaylist = new ArrayList<>();
                            for (MediaItem m : filtered) {
                                if (m.type == MediaItem.TYPE_VIDEO) videoPlaylist.add(m.uri.toString());
                            }
                            PlaylistHolder.set(videoPlaylist);
                            Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                            intent.putExtra("index", clickIdx);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, PreviewImageActivity.class);
                            intent.putExtra("index", clickIdx);
                            startActivity(intent);
                        }
                    }
                }
            });
            adapter.setOnHideListener(new ThumbnailAdapter.OnHideListener() {
                @Override
                public void onHide(final MediaItem item) {
                    new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Sembunyikan Media")
                        .setMessage("Sembunyikan file ini dari galeri HP?\nFile tetap ada di folder Termux/MediaTersembunyi.")
                        .setNegativeButton("Batal", null)
                        .setPositiveButton("Sembunyikan", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean ok = HideHelper.sembunyikan(MainActivity.this, item);
                                if (ok) {
                                    SoundHelper.success();
                                    Toast.makeText(MainActivity.this, "Media disembunyikan", Toast.LENGTH_SHORT).show();
                                    combinedMedia.remove(item);
                                    renderBeranda();
                                } else {
                                    SoundHelper.error();
                                }
                            }
                        })
                        .show();
                }
            });
            grid.setAdapter(adapter);
        }
    
        try { buildCategoryChips(); } catch (Exception ignored) {}
    }
}
