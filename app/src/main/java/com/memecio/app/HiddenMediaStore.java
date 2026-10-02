package com.memecio.app;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HiddenMediaStore {

    public static File getFolder() {
        File dir = new File(Environment.getExternalStorageDirectory(), "Termux/MediaTersembunyi");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void ensureNoMedia(Context context) {
        try {
            File folder = getFolder();
            File nomedia = new File(folder, ".nomedia");
            if (!nomedia.exists()) {
                nomedia.createNewFile();
            }
        } catch (Exception ignored) {}
    }

    public static List<MediaItem> scanHidden(Context context) {
        List<MediaItem> result = new ArrayList<>();
        try {
            File folder = getFolder();
            File[] files = folder.listFiles();
            if (files == null) return result;
            for (File f : files) {
                if (f.isDirectory()) continue;
                String name = f.getName().toLowerCase();
                if (name.startsWith(".")) continue;

                int type = -1;
                if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                        || name.endsWith(".webp") || name.endsWith(".gif")) {
                    type = MediaItem.TYPE_IMAGE;
                } else if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi")
                        || name.endsWith(".mov") || name.endsWith(".webm")) {
                    type = MediaItem.TYPE_VIDEO;
                } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
                    type = MediaItem.TYPE_AUDIO;
                }

                if (type == -1) continue;

                MediaItem item = new MediaItem(Uri.fromFile(f), type);
                item.isLocal = true;
                item.title = f.getName();
                result.add(item);
            }
        } catch (Exception ignored) {}
        return result;
    }

    public static int getCount() {
        File folder = getFolder();
        File[] files = folder.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile() && !f.getName().startsWith(".")) count++;
        }
        return count;
    }
}
