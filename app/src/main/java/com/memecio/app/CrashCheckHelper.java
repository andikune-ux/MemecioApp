package com.memecio.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class CrashCheckHelper {

    private static final String PREF_NAME = "memecio_settings";
    private static final String KEY_LAST_SHOWN_LENGTH = "crash_last_shown_length";

    public static void checkAndShow(final Activity activity) {
        File file = new File(activity.getFilesDir(), "memecio_crash.txt");
        if (!file.exists() || file.length() == 0) return;

        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastShownLength = prefs.getLong(KEY_LAST_SHOWN_LENGTH, 0);
        long currentLength = file.length();

        if (currentLength <= lastShownLength) return;

        final String fullContent = readFile(file);
        if (fullContent == null || fullContent.trim().isEmpty()) return;

        showPopup(activity, fullContent);

        prefs.edit().putLong(KEY_LAST_SHOWN_LENGTH, currentLength).apply();
    }

    private static String readFile(File file) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static void showPopup(final Activity activity, final String content) {
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_crash_popup);
        dialog.setCancelable(true);

        TextView tvBody = dialog.findViewById(R.id.tvCrashPopupBody);
        TextView btnSalin = dialog.findViewById(R.id.btnSalinCrashPopup);
        TextView btnClose = dialog.findViewById(R.id.btnCloseCrashPopup);

        tvBody.setText(content);

        btnSalin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("crash_log", content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "Log disalin", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}

