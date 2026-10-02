package com.memecio.app.plugin;

import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorLink;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Helper untuk bikin callback Function1 (Kotlin) dari Java.
 * C-2b-3.
 *
 * Plugin CloudStream panggil:
 *   loadLinks(data, isCasting, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit)
 */
public class LoadLinksCallback {

    public interface OnSubtitle {
        void onSubtitle(SubtitleFile s);
    }

    public interface OnLink {
        void onLink(ExtractorLink l);
    }

    /** Bikin Function1<SubtitleFile, Unit> dari Java callback */
    public static Function1<SubtitleFile, Unit> subtitle(final OnSubtitle cb) {
        return new Function1<SubtitleFile, Unit>() {
            @Override
            public Unit invoke(SubtitleFile s) {
                try { if (cb != null) cb.onSubtitle(s); } catch (Throwable ignored) {}
                return Unit.INSTANCE;
            }
        };
    }

    /** Bikin Function1<ExtractorLink, Unit> dari Java callback */
    public static Function1<ExtractorLink, Unit> link(final OnLink cb) {
        return new Function1<ExtractorLink, Unit>() {
            @Override
            public Unit invoke(ExtractorLink l) {
                try { if (cb != null) cb.onLink(l); } catch (Throwable ignored) {}
                return Unit.INSTANCE;
            }
        };
    }
}
