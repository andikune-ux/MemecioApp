package com.memecio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

public class TestMediaHelper {
    public static void showChoice(final Context context) {
        final String[] options = {
            "Video lokal (dari galeri HP)",
            "Video sample online (MP4)"
        };
        new AlertDialog.Builder(context)
            .setTitle("Test Media Player")
            .setItems(options, (d, which) -> {
                if (which == 0) {
                    // Buka player biasa, user pilih sendiri
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } else {
                    // Video sample online
                    String url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                    java.util.ArrayList<String> list = new java.util.ArrayList<>();
                    list.add(url);
                    PlaylistHolder.set(list);
                    Intent i = new Intent(context, VideoPlayerActivity.class);
                    i.putExtra("index", 0);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            })
            .show();
    }
}
