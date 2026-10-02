package com.memecio.app.js

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class JsHttpResponse {
    @JvmField
    var code: Int = 0

    @JvmField
    var text: String = ""

    @JvmField
    var url: String = ""

    @JvmField
    var error: String? = null

    fun document(): Document {
        return try {
            Jsoup.parse(text, url)
        } catch (t: Throwable) {
            Jsoup.parse("")
        }
    }

    fun ok(): Boolean = code in 200..299
}
