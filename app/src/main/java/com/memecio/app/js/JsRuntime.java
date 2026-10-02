package com.memecio.app.js;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Runtime JavaScript untuk Memec.io.
 *
 * Pakai Context SINGLETON — init sekali, inject sekali, eval berkali-kali.
 * Kalau bikin Context baru tiap eval, wrapped Java object jadi invalid.
 */
public class JsRuntime {

    private static final String TAG = "JsRuntime";

    private static Context sContext = null;
    private static Scriptable sScope = null;

    private static synchronized void ensureInit() {
        if (sContext != null && sScope != null) return;

        try {
            sContext = Context.enter();
            sContext.setOptimizationLevel(-1);
            sScope = sContext.initStandardObjects();

            // Inject helper ke scope (sekali)
            JsBridge.inject(sContext, sScope);

            // Verifikasi marker
            Object marker = sScope.get("__jsBridgeReady", sScope);
            if (marker == Scriptable.NOT_FOUND) {
                Log.e(TAG, "MARKER MISSING - inject gagal!");
            } else {
                Log.d(TAG, "Runtime init OK");
            }
        } catch (Throwable t) {
            Log.e(TAG, "init error: " + t.getMessage(), t);
            sContext = null;
            sScope = null;
        }
    }

    /** Reset runtime (kalau perlu re-init) */
    public static synchronized void reset() {
        sContext = null;
        sScope = null;
    }

    /** Evaluasi JS, return Object */
    public static Object eval(String code) {
        ensureInit();
        if (sContext == null || sScope == null) {
            throw new RuntimeException("Runtime tidak ter-init");
        }
        return sContext.evaluateString(sScope, code, "script", 1, null);
    }

    /** Evaluasi JS, return String */
    public static String evalToString(String code) {
        try {
            Object r = eval(code);
            return (r == null) ? "null" : r.toString();
        } catch (Throwable t) {
            return "ERROR: " + t.getClass().getSimpleName() + " - " + t.getMessage();
        }
    }

    /** Ambil scope (untuk keperluan advanced) */
    public static Scriptable getScope() {
        ensureInit();
        return sScope;
    }

    /** Ambil context (untuk keperluan advanced) */
    public static Context getContext() {
        ensureInit();
        return sContext;
    }
}
