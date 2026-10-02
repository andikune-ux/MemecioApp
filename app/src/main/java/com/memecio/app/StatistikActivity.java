package com.memecio.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class StatistikActivity extends Activity {

    private LinearLayout barContainer;
    private ArrayList<String> labels = new ArrayList<>();
    private ArrayList<Integer> values = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistik);

        TextView btnBack = findViewById(R.id.btnBackStatistik);
        barContainer = findViewById(R.id.barContainer);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        SharedPreferences prefs = getSharedPreferences("memecio_statistik", MODE_PRIVATE);
        int totalMedia = prefs.getInt("total_media", 0);
        int totalVideo = prefs.getInt("total_video", 0);
        int totalFoto = prefs.getInt("total_foto", 0);
        int totalAudio = prefs.getInt("total_audio", 0);

        TextView tvTotalMedia = findViewById(R.id.tvTotalMedia);
        tvTotalMedia.setText("Total Media: " + totalMedia
                + "\nVideo: " + totalVideo
                + "\nFoto: " + totalFoto
                + "\nAudio: " + totalAudio);

        labels.add("Video");
        labels.add("Foto");
        labels.add("Audio");
        values.add(totalVideo);
        values.add(totalFoto);
        values.add(totalAudio);

        renderBarChart();
    }

    private void renderBarChart() {
        int maxVal = 1;
        for (int v : values) if (v > maxVal) maxVal = v;

        for (int i = 0; i < labels.size(); i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 16, 0, 16);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView label = new TextView(this);
            label.setText(labels.get(i));
            label.setTextColor(Color.BLACK);
            label.setTextSize(14f);
            row.addView(label, new LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT));

            View bar = new View(this);
            bar.setBackgroundColor(Color.parseColor("#5B6EF5"));
            int width = (int) (300f * (values.get(i) / (float) maxVal));
            if (width < 10) width = 10;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, 40);
            params.setMargins(16, 0, 0, 0);
            row.addView(bar, params);

            TextView valueText = new TextView(this);
            valueText.setText("  " + values.get(i));
            valueText.setTextColor(Color.BLACK);
            valueText.setTextSize(14f);
            row.addView(valueText);

            barContainer.addView(row);
        }
    }
}
