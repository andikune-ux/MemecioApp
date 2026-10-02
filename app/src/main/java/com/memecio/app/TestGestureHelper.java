package com.memecio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

public class TestGestureHelper {
    public static void showGuide(final Context context) {
        String panduan = "Panduan Gestur Media Player\n\n" +
            "Aktif HANYA di Mode LANDSCAPE (HP dimiringkan):\n\n" +
            "1. TAHAN sisi KANAN layar\n" +
            "   → Percepat playback 3x (indikator '3x' muncul)\n" +
            "   → Lepas untuk kembali normal\n\n" +
            "2. GESER ATAS/BAWAH sisi KIRI layar\n" +
            "   → Atur BRIGHTNESS (kecerahan layar)\n" +
            "   → Geser ke atas = terang, ke bawah = redup\n\n" +
            "3. GESER ATAS/BAWAH sisi KANAN layar\n" +
            "   → Atur VOLUME\n" +
            "   → Geser ke atas = naik, ke bawah = turun\n\n" +
            "4. SWIPE KIRI/KANAN di tengah\n" +
            "   → Pindah video berikutnya/sebelumnya\n\n" +
            "5. TAP sekali di tengah\n" +
            "   → Munculkan/sembunyikan kontrol\n\n" +
            "6. GESER PROGRESS BAR\n" +
            "   → Preview mini muncul (video lokal)\n\n" +
            "Tekan OK untuk buka video test.";

        new AlertDialog.Builder(context)
            .setTitle("Test Gesture")
            .setMessage(panduan)
            .setNegativeButton("Tutup", null)
            .setPositiveButton("Buka Video", (d, w) -> {
                String url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                java.util.ArrayList<String> list = new java.util.ArrayList<>();
                list.add(url);
                PlaylistHolder.set(list);
                Intent i = new Intent(context, VideoPlayerActivity.class);
                i.putExtra("index", 0);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            })
            .show();
    }
}
