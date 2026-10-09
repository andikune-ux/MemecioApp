package com.memecio.app

import android.content.Context
import android.media.AudioManager

object SoundHelper {
    private var audioManager: AudioManager? = null

    @JvmStatic
    fun init(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    @JvmStatic
    fun click() {
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        } catch (ignored: Exception) {}
    }

    @JvmStatic
    fun nav() {
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        } catch (ignored: Exception) {}
    }
}
