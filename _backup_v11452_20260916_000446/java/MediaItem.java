package com.memecio.app;

import android.net.Uri;

public class MediaItem {
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_FOLDER = 3;
    public static final int TYPE_AUDIO = 4;

    public Uri uri;
    public int type;
    public boolean isLocal = true;
    public String title;
    public String thumbUrl;
    public boolean isM3u = false;
    public long dateAdded = 0L;
    public String folderPath = null;
    public long size = 0L;      // ukuran dalam byte
    public long duration = 0L;  // durasi dalam ms (video only)

    public MediaItem(Uri uri, int type) {
        this.uri = uri;
        this.type = type;
    }
}
