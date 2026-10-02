package com.memecio.app;

/**
 * PrivacyStore — kontrol FLAG_SECURE on/off.
 * Static field (in-memory) — otomatis reset ke ON saat app di-kill.
 */
public class PrivacyStore {
    private static boolean enabled = true; // default ON

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean e) { enabled = e; }
    public static boolean toggle() { enabled = !enabled; return enabled; }
    public static void resetToDefault() { enabled = true; }
}
