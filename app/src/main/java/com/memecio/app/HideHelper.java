package com.memecio.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class HideHelper {

    public static boolean sembunyikan(Context context, MediaItem item) {
        try {
            HiddenMediaStore.ensureNoMedia(context);
            Uri uri = item.uri;

            String displayName = getDisplayName(context, uri);
            if (displayName == null || displayName.isEmpty()) {
                displayName = "media_" + System.currentTimeMillis();
            }

            File folder = HiddenMediaStore.getFolder();
            File destFile = new File(folder, displayName);

            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return false;

            FileOutputStream out = new FileOutputStream(destFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();

            // Hapus file asli dari MediaStore
            try {
                context.getContentResolver().delete(uri, null, null);
            } catch (Exception e) {
                // Kalau gagal hapus (izin), minimal sudah ada copy
            }

            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Gagal menyembunyikan: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static boolean kembalikan(Context context, MediaItem item) {
        try {
            Uri uri = item.uri;
            if (!uri.getScheme().equals("file")) return false;
            File srcFile = new File(uri.getPath());
            if (!srcFile.exists()) return false;

            String name = srcFile.getName();

            File destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (item.type == MediaItem.TYPE_VIDEO) {
                destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            } else if (item.type == MediaItem.TYPE_AUDIO) {
                destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            }
            if (!destDir.exists()) destDir.mkdirs();

            File destFile = new File(destDir, name);
            int counter = 1;
            while (destFile.exists()) {
                String base = name;
                String ext = "";
                int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    base = name.substring(0, dot);
                    ext = name.substring(dot);
                }
                destFile = new File(destDir, base + "_" + counter + ext);
                counter++;
            }

            FileInputStreamHelper.copy(srcFile, destFile);
            srcFile.delete();

            // Trigger Media Scanner agar galeri tahu
            try {
                android.media.MediaScannerConnection.scanFile(context,
                        new String[]{destFile.getAbsolutePath()},
                        null, null);
            } catch (Exception ignored) {}

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getDisplayName(Context context, Uri uri) {
        try {
            if (uri.getScheme().equals("file")) {
                return new File(uri.getPath()).getName();
            }
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0 && cursor.moveToFirst()) {
                    String name = cursor.getString(idx);
                    cursor.close();
                    return name;
                }
                cursor.close();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static class FileInputStreamHelper {
        static void copy(File src, File dest) throws Exception {
            java.io.FileInputStream in = new java.io.FileInputStream(src);
            java.io.FileOutputStream out = new java.io.FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.close();
            in.close();
        }
    }
}
