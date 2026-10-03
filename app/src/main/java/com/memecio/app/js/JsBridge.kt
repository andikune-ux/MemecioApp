package com.memecio.app.js

import android.util.Log
import org.mozilla.javascript.Context
import org.mozilla.javascript.FunctionObject
import org.mozilla.javascript.Scriptable
import java.lang.reflect.Modifier

/**
 * Bridge: daftarkan Java/Kotlin functions ke scope Rhino.
 */
object JsBridge {

    private const val TAG = "JsBridge"

    @JvmStatic
    fun inject(cx: Context, scope: Scriptable) {
        // Step 1: daftarkan static functions jadi global JS
        try {
            var count = 0
            for (m in JsNative::class.java.methods) {
                if (Modifier.isStatic(m.modifiers) && m.declaringClass == JsNative::class.java) {
                    val fobj = FunctionObject(m.name, m, scope)
                    scope.put(m.name, scope, fobj)
                    count++
                }
            }
            Log.d(TAG, "Registered $count JS functions")
        } catch (t: Throwable) {
            Log.e(TAG, "defineFunctionProperties FAIL: ${t.message}", t)
        }

        // Step 2: JS bootstrap - bikin object http, console, html
        val bootstrap = """
            var http = {
              get: function(url) {
                var j = JSON.parse(httpGet(String(url)));
                return {
                  code: j.code,
                  text: j.text,
                  url: j.url,
                  error: j.error,
                  ok: function() { return this.code >= 200 && this.code < 300; },
                  document: function() {
                    var self = this;
                    return {
                      title: function() { return htmlTitle(self.text); },
                      select: function(sel) {
                        var arr = JSON.parse(htmlSelect(self.text, String(sel)));
                        arr.first = function() { return this.length > 0 ? this[0] : null; };
                        arr.get = function(i) { return this[i]; };
                        return arr;
                      }
                    };
                  }
                };
              },
              post: function(url, body) {
                var j = JSON.parse(httpPost(String(url), String(body || '')));
                return { code: j.code, text: j.text, url: j.url, error: j.error,
                         ok: function() { return this.code >= 200 && this.code < 300; } };
              }
            };
            var console = {
              log: function(m) { log(String(m)); },
              error: function(m) { log('ERROR: ' + String(m)); },
              warn: function(m) { log('WARN: ' + String(m)); },
              info: function(m) { log('INFO: ' + String(m)); }
            };
        """.trimIndent()

        try {
            cx.evaluateString(scope, bootstrap, "bootstrap", 1, null)
            Log.d(TAG, "bootstrap OK")
            val http = scope.get("http", scope)
            Log.d(TAG, "http type: ${if (http === Scriptable.NOT_FOUND) "NOT_FOUND" else "FOUND"}")
            val con = scope.get("console", scope)
            Log.d(TAG, "console type: ${if (con === Scriptable.NOT_FOUND) "NOT_FOUND" else "FOUND"}")
        } catch (t: Throwable) {
            Log.e(TAG, "bootstrap FAIL: ${t.message}", t)
        }
    }
}
