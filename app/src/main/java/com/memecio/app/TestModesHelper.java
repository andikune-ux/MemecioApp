package com.memecio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class TestModesHelper {
    public static void showChoice(final Context context) {
        String current = DisplayModeStore.getMode(context);
        String effective = DisplayModeStore.getEffectiveMode(context);
        String info = "Mode Sekarang: " + current +
                      "\nEfektif: " + effective + "\n\nPilih mode:";

        final String[] options = {
            "Auto (deteksi perangkat)",
            "Android (paksa)",
            "Android TV (paksa)"
        };

        new AlertDialog.Builder(context)
            .setTitle("Test Modes")
            .setMessage(info)
            .setItems(options, (d, which) -> {
                String mode;
                if (which == 0) mode = DisplayModeStore.MODE_AUTO;
                else if (which == 1) mode = DisplayModeStore.MODE_ANDROID;
                else mode = DisplayModeStore.MODE_TV;

                DisplayModeStore.setMode(context, mode);
                Toast.makeText(context, "Mode di-set: " + mode + "\nRestart aplikasi untuk efek penuh.", Toast.LENGTH_LONG).show();
            })
            .show();
    }
}
