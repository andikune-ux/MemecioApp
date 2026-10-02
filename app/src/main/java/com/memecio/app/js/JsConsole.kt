package com.memecio.app.js

import android.util.Log

class JsConsole {
    fun log(msg: String?) {
        Log.d(TAG, msg ?: "null")
    }

    fun error(msg: String?) {
        Log.e(TAG, msg ?: "null")
    }

    fun warn(msg: String?) {
        Log.w(TAG, msg ?: "null")
    }

    fun info(msg: String?) {
        Log.i(TAG, msg ?: "null")
    }

    companion object {
        private const val TAG = "JsPlugin"
    }
}
