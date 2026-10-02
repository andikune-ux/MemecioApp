package com.memecio.app.js

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class JsHttp {

    fun get(url: String): JsHttpResponse = request("GET", url, null, null)

    fun get(url: String, headers: Map<String, String>?): JsHttpResponse =
        request("GET", url, headers, null)

    fun post(url: String, body: String?): JsHttpResponse =
        request("POST", url, null, body)

    fun post(url: String, headers: Map<String, String>?, body: String?): JsHttpResponse =
        request("POST", url, headers, body)

    private fun request(
        method: String,
        url: String,
        headers: Map<String, String>?,
        body: String?
    ): JsHttpResponse {
        val r = JsHttpResponse()
        r.url = url
        var conn: HttpURLConnection? = null
        try {
            val u = URL(url)
            conn = u.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.requestMethod = method
            conn.instanceFollowRedirects = true
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/110.0.0.0"
            )
            headers?.forEach { (key, value) ->
                conn.setRequestProperty(key, value)
            }
            if ("POST" == method && body != null) {
                conn.doOutput = true
                val os: OutputStream = conn.outputStream
                os.write(body.toByteArray(Charsets.UTF_8))
                os.close()
            }
            r.code = conn.responseCode
            val `is` = if (r.code >= 400) conn.errorStream else conn.inputStream
            if (`is` == null) {
                r.text = ""
                return r
            }
            val br = BufferedReader(InputStreamReader(`is`, Charsets.UTF_8))
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
            br.close()
            r.text = sb.toString()
        } catch (t: Throwable) {
            r.code = -1
            r.text = ""
            r.error = t.javaClass.simpleName + ": " + t.message
        } finally {
            try {
                conn?.disconnect()
            } catch (ignored: Throwable) {
            }
        }
        return r
    }

    companion object {
        private const val TIMEOUT_MS = 20000
    }
}
