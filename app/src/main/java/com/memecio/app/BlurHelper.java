package com.memecio.app;

import android.app.Activity;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

public class BlurHelper {
    private static final float BLUR_RADIUS = 30f;

    public static void apply(Activity activity) {
        if (activity == null) return;
        if (Build.VERSION.SDK_INT < 31) return;
        try {
            final View root = activity.getWindow().getDecorView();
            if (root == null) return;
            root.setRenderEffect(RenderEffect.createBlurEffect(
                BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.MIRROR));
            root.invalidate();
            root.postInvalidate();
        } catch (Exception ignored) {}
    }

    public static void clear(Activity activity) {
        if (activity == null) return;
        if (Build.VERSION.SDK_INT < 31) return;
        try {
            View root = activity.getWindow().getDecorView();
            if (root != null) {
                root.setRenderEffect(null);
                root.invalidate();
                root.postInvalidate();
            }
        } catch (Exception ignored) {}
    }
}
