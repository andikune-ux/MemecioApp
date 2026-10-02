package com.memecio.app.js

import org.jsoup.Jsoup
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

class JsNative : ScriptableObject() {

    override fun getClassName(): String = "JsNative"

    companion object {
        @JvmStatic
        fun httpGet(cx: Context, thisObj: Scriptable, args: Array<Any>, funObj: Function): Any {
            return try {
                val url = if (args.isNotEmpty() && args[0] != null) args[0].toString() else ""
                val r = JsHttp().get(url)
                "{" + jsonPair("code", r.code) + "," + jsonStr("text", r.text) + "," + jsonStr("url", url) + "," + jsonStr("error", r.error) + "}"
            } catch (t: Throwable) {
                "{\"code\":-1,\"text\":\"\",\"url\":\"\",\"error\":\"" + escape(t.message) + "\"}"
            }
        }

        @JvmStatic
        fun httpPost(cx: Context, thisObj: Scriptable, args: Array<Any>, funObj: Function): Any {
            return try {
                val url = if (args.isNotEmpty() && args[0] != null) args[0].toString() else ""
                val body = if (args.size > 1 && args[1] != null) args[1].toString() else ""
                val r = JsHttp().post(url, body)
                "{" + jsonPair("code", r.code) + "," + jsonStr("text", r.text) + "," + jsonStr("url", url) + "," + jsonStr("error", r.error) + "}"
            } catch (t: Throwable) {
                "{\"code\":-1,\"text\":\"\",\"url\":\"\",\"error\":\"" + escape(t.message) + "\"}"
            }
        }

        @JvmStatic
        fun htmlSelect(cx: Context, thisObj: Scriptable, args: Array<Any>, funObj: Function): Any {
            return try {
                val html = if (args.isNotEmpty() && args[0] != null) args[0].toString() else ""
                val sel = if (args.size > 1 && args[1] != null) args[1].toString() else ""
                val doc = Jsoup.parse(html)
                val els = doc.select(sel)
                val sb = StringBuilder("[")
                var first = true
                for (el in els) {
                    if (!first) sb.append(",")
                    first = false
                    sb.append("{")
                    sb.append(jsonStr("text", el.text())).append(",")
                    sb.append(jsonStr("html", el.html())).append(",")
                    sb.append(jsonStr("href", el.attr("href"))).append(",")
                    sb.append(jsonStr("src", el.attr("src"))).append(",")
                    sb.append(jsonStr("title", el.attr("title")))
                    sb.append("}")
                }
                sb.append("]")
                sb.toString()
            } catch (t: Throwable) {
                "[]"
            }
        }

        @JvmStatic
        fun htmlTitle(cx: Context, thisObj: Scriptable, args: Array<Any>, funObj: Function): Any {
            return try {
                val html = if (args.isNotEmpty() && args[0] != null) args[0].toString() else ""
                Jsoup.parse(html).title()
            } catch (t: Throwable) {
                ""
            }
        }

        private fun jsonPair(key: String, value: Int): String = "\"$key\":$value"

        private fun jsonStr(key: String, value: String?): String =
            "\"$key\":\"" + escape(value) + "\""

        private fun escape(s: String?): String {
            if (s == null) return ""
            return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        }
    }
}
