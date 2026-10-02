package com.memecio.app

/**
 * PrivacyStore — kontrol FLAG_SECURE on/off.
 * Static field (in-memory) — otomatis reset ke ON saat app di-kill.
 */
object PrivacyStore {

    private var enabled: Boolean = true // default ON

    @JvmStatic
    fun isEnabled(): Boolean = enabled

    @JvmStatic
    fun setEnabled(e: Boolean) {
        enabled = e
    }

    @JvmStatic
    fun toggle(): Boolean {
        enabled = !enabled
        return enabled
    }

    @JvmStatic
    fun resetToDefault() {
        enabled = true
    }
}
