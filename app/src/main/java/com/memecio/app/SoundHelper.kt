package com.memecio.app

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

object SoundHelper {

    private var toneGenerator: ToneGenerator? = null
    private var enabled = true

    @JvmStatic
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("memecio_settings", Context.MODE_PRIVATE)
        enabled = prefs.getBoolean("sfx_enabled", true)
        if (enabled && toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (_: Exception) {}
        }
    }

    @JvmStatic fun click() = play(ToneGenerator.TONE_PROP_BEEP, 40)
    @JvmStatic fun success() = play(ToneGenerator.TONE_PROP_ACK, 150)
    @JvmStatic fun error() = play(ToneGenerator.TONE_PROP_NACK, 250)
    @JvmStatic fun nav() = play(ToneGenerator.TONE_PROP_BEEP2, 30)
    @JvmStatic fun playAudio() = play(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)

    private fun play(tone: Int, durationMs: Int) {
        if (!enabled || toneGenerator == null) return
        try {
            toneGenerator?.startTone(tone, durationMs)
        } catch (_: Exception) {}
    }
}
