package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

public class CoverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Blokir screenshot & Recents preview
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        } catch (Exception ignored) {}

        // Fullscreen immersive
        try {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } catch (Exception ignored) {}

        setContentView(R.layout.activity_cover);

        // Pilih foto sesuai orientasi
        ImageView cover = findViewById(R.id.coverImage);
        try {
            boolean isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
            cover.setImageResource(isLandscape
                ? R.drawable.cover_landscape
                : R.drawable.cover_portrait);
        } catch (Exception ignored) {}

        // Tap di mana saja → redirect ke Calculator
        findViewById(R.id.coverRoot).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(CoverActivity.this, CalculatorActivity.class);
                intent.putExtra("reauth", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (Exception ignored) {}
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Block back button
    }
}
