package com.memecio.app

object SessionState {

    @JvmField
    var lastActivityClass: String? = null

    @JvmField
    var lastVideoIndex: Int = 0

    private var suppressLockUntil: Long = 0
    private const val SUPPRESS_DURATION_MS = 30_000L // 30 detik

    // Flag 1-shot untuk transisi internal
    private var internalTransition: Boolean = false

    @JvmStatic
    fun suppressLock() {
        suppressLockUntil = System.currentTimeMillis() + SUPPRESS_DURATION_MS
    }

    @JvmStatic
    fun clearSuppress() {
        suppressLockUntil = 0
    }

    @JvmStatic
    fun isLockSuppressed(): Boolean {
        return System.currentTimeMillis() < suppressLockUntil
    }

    @JvmStatic
    fun markInternalTransition() {
        internalTransition = true
    }

    @JvmStatic
    fun consumeInternalTransition(): Boolean {
        val old = internalTransition
        internalTransition = false
        return old
    }

    @JvmStatic
    fun clear() {
        lastActivityClass = null
        lastVideoIndex = 0
    }
}
