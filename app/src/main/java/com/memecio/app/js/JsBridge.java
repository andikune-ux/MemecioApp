package com.memecio.app.js;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Bridge: daftarkan Java functions ke scope Rhino.
 * Pakai defineFunctionProperties (paling stabil di Android).
 */
public class JsBridge {

    private static final String TAG = "JsBridge";

    public static void inject(Context cx, Scriptable scope) {
        // Step 1: daftarkan Java static functions jadi global JS
        try {
            int count = 0;
            for (java.lang.reflect.Method m : JsNative.class.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers())
                    && m.getDeclaringClass() == JsNative.class) {
                    org.mozilla.javascript.FunctionObject fobj =
                        new org.mozilla.javascript.FunctionObject(m.getName(), m, scope);
                    scope.put(m.getName(), scope, fobj);
                    count++;
                }
            }
            Log.d(TAG, "Registered " + count + " JS functions");
        } catch (Throwable t) {
            Log.e(TAG, "defineFunctionProperties FAIL: " + t.getMessage(), t);
        }

        // Step 2: JS bootstrap - bikin object http, console, html
        String bootstrap = ""
            + "var http = {"
            + "  get: function(url) {"
            + "    var j = JSON.parse(httpGet(String(url)));"
            + "    return {"
            + "      code: j.code,"
            + "      text: j.text,"
            + "      url: j.url,"
            + "      error: j.error,"
            + "      ok: function() { return this.code >= 200 && this.code < 300; },"
            + "      document: function() {"
            + "        var self = this;"
            + "        return {"
            + "          title: function() { return htmlTitle(self.text); },"
            + "          select: function(sel) {"
            + "            var arr = JSON.parse(htmlSelect(self.text, String(sel)));"
            + "            arr.first = function() { return this.length > 0 ? this[0] : null; };"
            + "            arr.get = function(i) { return this[i]; };"
            + "            return arr;"
            + "          }"
            + "        };"
            + "      }"
            + "    };"
            + "  },"
            + "  post: function(url, body) {"
            + "    var j = JSON.parse(httpPost(String(url), String(body || '')));"
            + "    return { code: j.code, text: j.text, url: j.url, error: j.error,"
            + "             ok: function() { return this.code >= 200 && this.code < 300; } };"
            + "  }"
            + "};"
            + "var console = {"
            + "  log: function(m) { log(String(m)); },"
            + "  error: function(m) { log('ERROR: ' + String(m)); },"
            + "  warn: function(m) { log('WARN: ' + String(m)); },"
            + "  info: function(m) { log('INFO: ' + String(m)); }"
            + "};";

        try {
            cx.evaluateString(scope, bootstrap, "bootstrap", 1, null);
            Log.d(TAG, "bootstrap OK");
            // Verifikasi
            Object http = scope.get("http", scope);
            Log.d(TAG, "http type: " + (http == Scriptable.NOT_FOUND ? "NOT_FOUND" : "FOUND"));
            Object con = scope.get("console", scope);
            Log.d(TAG, "console type: " + (con == Scriptable.NOT_FOUND ? "NOT_FOUND" : "FOUND"));
        } catch (Throwable t) {
            Log.e(TAG, "bootstrap FAIL: " + t.getMessage(), t);
        }
    }
}
