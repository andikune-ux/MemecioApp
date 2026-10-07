package com.memecio.app

import android.app.Application
import com.memecio.app.data.MediaRepository

class MemecioApplication : Application() {
    lateinit var repository: MediaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = MediaRepository(this)
    }

    companion object {
        lateinit var instance: MemecioApplication
            private set
    }
}
