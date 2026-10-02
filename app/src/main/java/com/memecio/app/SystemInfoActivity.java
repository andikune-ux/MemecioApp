package com.memecio.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_system_info);
        findViewById(R.id.btnBackSystem).setOnClickListener(v -> finish());

        final String info = buildInfo();
        TextView tv = findViewById(R.id.tvSystemContent);
        tv.setText(info);

        findViewById(R.id.btnSalinSystem).setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("system_info", info));
                Toast.makeText(this, "Disalin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildInfo() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("=== APLIKASI ===\n");
            sb.append("Package   : ").append(getPackageName()).append("\n");
            sb.append("Versi     : ").append(getPackageManager().getPackageInfo(getPackageName(), 0).versionName).append("\n");
            sb.append("Build     : ").append(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");

            sb.append("=== PERANGKAT ===\n");
            sb.append("Model     : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
            sb.append("Android   : ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
            sb.append("Brand     : ").append(Build.BRAND).append("\n");
            sb.append("Device    : ").append(Build.DEVICE).append("\n");
            sb.append("Product   : ").append(Build.PRODUCT).append("\n");
            sb.append("Board     : ").append(Build.BOARD).append("\n");
            sb.append("CPU ABI   : ").append(Build.SUPPORTED_ABIS[0]).append("\n\n");

            sb.append("=== LAYAR ===\n");
            DisplayMetrics dm = getResources().getDisplayMetrics();
            sb.append("Resolusi  : ").append(dm.widthPixels).append(" x ").append(dm.heightPixels).append(" px\n");
            sb.append("Density   : ").append(dm.densityDpi).append(" dpi\n\n");

            sb.append("=== MEMORI ===\n");
            Runtime rt = Runtime.getRuntime();
            long maxMB = rt.maxMemory() / 1048576;
            long totalMB = rt.totalMemory() / 1048576;
            long freeMB = rt.freeMemory() / 1048576;
            sb.append("Max heap  : ").append(maxMB).append(" MB\n");
            sb.append("Total     : ").append(totalMB).append(" MB\n");
            sb.append("Free      : ").append(freeMB).append(" MB\n\n");

            sb.append("=== STORAGE ===\n");
            File dir = getFilesDir();
            sb.append("Internal  : ").append(dir.getAbsolutePath()).append("\n");
            if (dir.getParentFile() != null) {
                sb.append("Free app  : ").append(dir.getParentFile().getFreeSpace() / 1048576).append(" MB\n");
                sb.append("Total app : ").append(dir.getParentFile().getTotalSpace() / 1048576).append(" MB\n");
            }

            sb.append("\n=== PREFERENSI ===\n");
            sb.append("Mode      : ").append(DisplayModeStore.getEffectiveMode(this)).append("\n");
            sb.append("Sort      : ").append(getSharedPreferences("memecio_settings", MODE_PRIVATE).getString("sort_mode", "tanggal_baru")).append("\n");
            sb.append("Data saver: ").append(getSharedPreferences("memecio_settings", MODE_PRIVATE).getBoolean("data_saver", false) ? "Aktif" : "Mati").append("\n");
        } catch (Exception e) {
            sb.append("\nError: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
