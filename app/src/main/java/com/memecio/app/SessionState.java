package com.memecio.app;

public class SessionState {
    public static String lastActivityClass = null;
    public static int lastVideoIndex = 0;

    private static long suppressLockUntil = 0;
    private static final long SUPPRESS_DURATION_MS = 30_000; // 30 detik

    // Flag 1-shot untuk transisi internal
    private static boolean internalTransition = false;

    public static void suppressLock() {
        suppressLockUntil = System.currentTimeMillis() + SUPPRESS_DURATION_MS;
    }

    public static void clearSuppress() {
        suppressLockUntil = 0;
    }

    public static boolean isLockSuppressed() {
        return System.currentTimeMillis() < suppressLockUntil;
    }

    public static void markInternalTransition() {
        internalTransition = true;
    }

    public static boolean consumeInternalTransition() {
        boolean old = internalTransition;
        internalTransition = false;
        return old;
    }

    public static void clear() {
        lastActivityClass = null;
        lastVideoIndex = 0;
    }
}
