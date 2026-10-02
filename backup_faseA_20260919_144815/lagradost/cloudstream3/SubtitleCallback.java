package com.lagradost.cloudstream3;

/**
 * Stub CloudStream - SubtitleCallback (C-1).
 * Functional interface untuk plugin panggil subtitle.
 */
public interface SubtitleCallback {
    void invoke(SubtitleFile subtitle);
}
