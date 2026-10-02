package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundHelper {

    private static ToneGenerator toneGenerator;
    private static boolean enabled = true;

    public static void init(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("memecio_settings", Context.MODE_PRIVATE);
        enabled = prefs.getBoolean("sfx_enabled", true);
        if (enabled && toneGenerator == null) {
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
            } catch (Exception ignored) {}
        }
    }

    public static void click() {
        play(ToneGenerator.TONE_PROP_BEEP, 40);
    }

    public static void success() {
        play(ToneGenerator.TONE_PROP_ACK, 150);
    }

    public static void error() {
        play(ToneGenerator.TONE_PROP_NACK, 250);
    }

    public static void nav() {
        play(ToneGenerator.TONE_PROP_BEEP2, 30);
    }

    public static void playAudio() {
        play(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

    private static void play(int tone, int durationMs) {
        if (!enabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(tone, durationMs);
        } catch (Exception ignored) {}
    }
}
