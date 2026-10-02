package com.memecio.app;

import android.content.Context;
import android.os.Environment;

import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.offline.DownloadManager;

import java.io.File;

public class DownloadCache {

    private static SimpleCache simpleCache;
    private static StandaloneDatabaseProvider databaseProvider;
    private static DownloadManager downloadManager;

    public static synchronized void init(Context context) {
        if (simpleCache != null) return;
        try {
            File cacheDir = new File(Environment.getExternalStorageDirectory(), "Termux/Download/.hls");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            databaseProvider = new StandaloneDatabaseProvider(context);

            // Max 500 MB cache
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024L);
            simpleCache = new SimpleCache(cacheDir, evictor, databaseProvider);

            java.util.concurrent.Executor executor = java.util.concurrent.Executors.newFixedThreadPool(4);
            downloadManager = new DownloadManager(
                context,
                databaseProvider,
                simpleCache,
                new androidx.media3.datasource.DefaultHttpDataSource.Factory(),
                executor
            );
            downloadManager.setMaxParallelDownloads(2);
            downloadManager.resumeDownloads();
        } catch (Exception ignored) {}
    }

    public static SimpleCache getSimpleCache() {
        return simpleCache;
    }

    public static DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public static StandaloneDatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }
}
