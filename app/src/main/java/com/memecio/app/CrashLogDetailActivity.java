package com.memecio.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CrashLogDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_log_detail);

        String title = getIntent().getStringExtra("crash_title");
        final String body = getIntent().getStringExtra("crash_body");

        TextView btnBack = findViewById(R.id.btnBackCrashDetail);
        TextView tvTitle = findViewById(R.id.tvCrashDetailTitle);
        TextView tvBody = findViewById(R.id.tvCrashDetailBody);
        TextView btnCopy = findViewById(R.id.btnCopyCrash);

        tvTitle.setText(title != null ? title : "Detail Crash");
        tvBody.setText(body != null ? body : "");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("crash_log", body != null ? body : "");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(CrashLogDetailActivity.this, "Log disalin", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

