package com.memecio.app

import android.content.Context
import android.os.Environment
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object DownloadCache {

    private var simpleCache: SimpleCache? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null
    private var downloadManager: DownloadManager? = null

    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        if (simpleCache != null) return
        try {
            val cacheDir = File(Environment.getExternalStorageDirectory(), "Termux/Download/.hls")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            databaseProvider = StandaloneDatabaseProvider(context)
            // Max 500 MB cache
            val evictor = LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024L)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider!!)
            val executor: Executor = Executors.newFixedThreadPool(4)
            downloadManager = DownloadManager(
                context,
                databaseProvider!!,
                simpleCache!!,
                DefaultHttpDataSource.Factory(),
                executor
            )
            downloadManager?.setMaxParallelDownloads(2)
            downloadManager?.resumeDownloads()
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun getSimpleCache(): SimpleCache? = simpleCache

    @JvmStatic
    fun getDownloadManager(): DownloadManager? = downloadManager

    @JvmStatic
    fun getDatabaseProvider(): StandaloneDatabaseProvider? = databaseProvider
}
