package com.memecio.app.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Java functions yang di-expose ke JavaScript.
 * JS panggil: httpGet(url), htmlSelect(html, sel), dll.
 */
public class JsNative extends ScriptableObject {

    @Override
    public String getClassName() { return "JsNative"; }

    /** httpGet(url) -> JSON string {"code":200,"text":"...","url":"..."} */
    public static Object httpGet(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        try {
            String url = (args.length > 0 && args[0] != null) ? args[0].toString() : "";
            JsHttpResponse r = new JsHttp().get(url);
            return "{" + jsonPair("code", r.code) + "," +
                        jsonStr("text", r.text) + "," +
                        jsonStr("url", url) + "," +
                        jsonStr("error", r.error) + "}";
        } catch (Throwable t) {
            return "{\"code\":-1,\"text\":\"\",\"url\":\"\",\"error\":\"" + escape(t.getMessage()) + "\"}";
        }
    }

    /** httpPost(url, body) -> JSON string */
    public static Object httpPost(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        try {
            String url = (args.length > 0 && args[0] != null) ? args[0].toString() : "";
            String body = (args.length > 1 && args[1] != null) ? args[1].toString() : "";
            JsHttpResponse r = new JsHttp().post(url, body);
            return "{" + jsonPair("code", r.code) + "," +
                        jsonStr("text", r.text) + "," +
                        jsonStr("url", url) + "," +
                        jsonStr("error", r.error) + "}";
        } catch (Throwable t) {
            return "{\"code\":-1,\"text\":\"\",\"url\":\"\",\"error\":\"" + escape(t.getMessage()) + "\"}";
        }
    }

    /** htmlSelect(html, selector) -> JSON array: [{"text":"...","html":"...","href":"..."}, ...] */
    public static Object htmlSelect(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        try {
            String html = (args.length > 0 && args[0] != null) ? args[0].toString() : "";
            String sel = (args.length > 1 && args[1] != null) ? args[1].toString() : "";
            Document doc = Jsoup.parse(html);
            Elements els = doc.select(sel);
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Element el : els) {
                if (!first) sb.append(",");
                first = false;
                sb.append("{");
                sb.append(jsonStr("text", el.text())).append(",");
                sb.append(jsonStr("html", el.html())).append(",");
                sb.append(jsonStr("href", el.attr("href"))).append(",");
                sb.append(jsonStr("src", el.attr("src"))).append(",");
                sb.append(jsonStr("title", el.attr("title")));
                sb.append("}");
            }
            sb.append("]");
            return sb.toString();
        } catch (Throwable t) {
            return "[]";
        }
    }

    /** htmlTitle(html) -> string */
    public static Object htmlTitle(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        try {
            String html = (args.length > 0 && args[0] != null) ? args[0].toString() : "";
            return Jsoup.parse(html).title();
        } catch (Throwable t) {
            return "";
        }
    }

    /** log(msg) */
    public static Object log(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length > 0 && args[0] != null) {
            android.util.Log.d("JsPlugin", args[0].toString());
        }
        return null;
    }

    // ===== Helper JSON =====
    private static String jsonPair(String k, int v) {
        return "\"" + k + "\":" + v;
    }

    private static String jsonStr(String k, String v) {
        return "\"" + k + "\":\"" + escape(v) + "\"";
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
