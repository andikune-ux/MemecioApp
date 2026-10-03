package com.memecio.app

import android.app.Activity
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View

object BlurHelper {
    private const val BLUR_RADIUS = 30f

    @JvmStatic
    fun apply(activity: Activity?) {
        if (activity == null) return
        if (Build.VERSION.SDK_INT < 31) return
        try {
            val root = activity.window.decorView ?: return
            root.setRenderEffect(
                RenderEffect.createBlurEffect(
                    BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.MIRROR
                )
            )
            root.invalidate()
            root.postInvalidate()
        } catch (ignored: Exception) {
        }
    }

    @JvmStatic
    fun clear(activity: Activity?) {
        if (activity == null) return
        if (Build.VERSION.SDK_INT < 31) return
        try {
            val root = activity.window.decorView
            if (root != null) {
                root.setRenderEffect(null)
                root.invalidate()
                root.postInvalidate()
            }
        } catch (ignored: Exception) {
        }
    }
}
