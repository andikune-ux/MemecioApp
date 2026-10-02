package com.memecio.app.js

import android.util.Log
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

object JsRuntime {

    private var sContext: Context? = null
    private var sScope: Scriptable? = null

    @Synchronized
    private fun ensureInit() {
        if (sContext != null && sScope != null) return
        try {
            sContext = Context.enter()
            sContext!!.optimizationLevel = -1
            sScope = sContext!!.initStandardObjects()
            JsBridge.inject(sContext!!, sScope!!)
            val marker = sScope!!.get("__jsBridgeReady", sScope)
            if (marker == Scriptable.NOT_FOUND) {
                Log.e(TAG, "MARKER MISSING - inject gagal!")
            } else {
                Log.d(TAG, "Runtime init OK")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "init error: " + t.message, t)
            sContext = null
            sScope = null
        }
    }

    @Synchronized
    fun reset() {
        sContext = null
        sScope = null
    }

    fun eval(code: String): Any? {
        ensureInit()
        if (sContext == null || sScope == null) {
            throw RuntimeException("Runtime tidak ter-init")
        }
        return sContext!!.evaluateString(sScope, code, "script", 1, null)
    }

    fun evalToString(code: String): String {
        return try {
            val r = eval(code)
            r?.toString() ?: "null"
        } catch (t: Throwable) {
            "ERROR: " + t.javaClass.simpleName + " - " + t.message
        }
    }

    fun getScope(): Scriptable? {
        ensureInit()
        return sScope
    }

    fun getContext(): Context? {
        ensureInit()
        return sContext
    }

    private const val TAG = "JsRuntime"
}
