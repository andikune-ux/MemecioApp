package com.memecio.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ChangelogActivity extends Activity {

    private LinearLayout content;
    private TextView tabVersion, tabFeatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        findViewById(R.id.btnBackChangelog).setOnClickListener(v -> finish());

        content = findViewById(R.id.changelogContent);
        tabVersion = findViewById(R.id.tabChangelogVersion);
        tabFeatures = findViewById(R.id.tabChangelogFeatures);

        tabVersion.setOnClickListener(v -> showVersionTab());
        tabFeatures.setOnClickListener(v -> showFeaturesTab());

        showVersionTab();
    }

    private void showVersionTab() {
        tabVersion.setTextColor(0xFF3D5AFE);
        tabVersion.setBackgroundResource(R.drawable.bg_glass_button_selected);
        tabFeatures.setTextColor(0xFF888888);
        tabFeatures.setBackground(null);

        content.removeAllViews();

        // ============ VERSI AKTIF (dari BuildConfig) ============
        LinearLayout aktifCard = new LinearLayout(this);
        aktifCard.setOrientation(LinearLayout.VERTICAL);
        aktifCard.setBackgroundResource(R.drawable.bg_glass_card);
        aktifCard.setPadding(16, 16, 16, 16);

        TextView tvTitleAktif = new TextView(this);
        tvTitleAktif.setText("Versi Aktif");
        tvTitleAktif.setTextSize(11f);
        tvTitleAktif.setTextColor(0xFF888888);
        aktifCard.addView(tvTitleAktif);

        TextView tvVersiAktif = new TextView(this);
        String vName = "?";
        try { vName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception ignored) {}
        tvVersiAktif.setText("V." + vName);
        tvVersiAktif.setTextSize(20f);
        tvVersiAktif.setTextColor(0xFF3D5AFE);
        tvVersiAktif.setTypeface(null, android.graphics.Typeface.BOLD);
        tvVersiAktif.setPadding(0, 4, 0, 0);
        aktifCard.addView(tvVersiAktif);

        LinearLayout.LayoutParams lpAktif = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lpAktif.setMargins(0, 0, 0, 16);
        aktifCard.setLayoutParams(lpAktif);
        content.addView(aktifCard);

        // ============ DAFTAR BUILD ============
        TextView tvHeader = new TextView(this);
        tvHeader.setText("Riwayat Build");
        tvHeader.setTextSize(13f);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeader.setTextColor(0xFF1C1C1E);
        tvHeader.setPadding(8, 4, 8, 8);
        content.addView(tvHeader);

        List<ChangelogStore.BuildEntry> builds = ChangelogStore.getBuilds();
        for (ChangelogStore.BuildEntry b : builds) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_changelog_version, content, false);
            ((TextView) card.findViewById(R.id.tvVersionNumber)).setText(b.version);
            ((TextView) card.findViewById(R.id.tvVersionDate)).setText(b.timestamp);

            LinearLayout container = card.findViewById(R.id.versionFeaturesContainer);
            for (String c : b.changes) {
                TextView tv = new TextView(this);
                tv.setText("• " + c);
                tv.setTextSize(12f);
                tv.setTextColor(0xFF555555);
                tv.setPadding(0, 4, 0, 4);
                container.addView(tv);
            }
            content.addView(card);
        }

        // ============ RINGKASAN STATUS FITUR ============
        LinearLayout ringkas = new LinearLayout(this);
        ringkas.setOrientation(LinearLayout.VERTICAL);
        ringkas.setBackgroundResource(R.drawable.bg_glass_card);
        ringkas.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams lpR = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lpR.setMargins(0, 16, 0, 0);
        ringkas.setLayoutParams(lpR);

        TextView tvRingkasTitle = new TextView(this);
        tvRingkasTitle.setText("Ringkasan Status Fitur");
        tvRingkasTitle.setTextSize(13f);
        tvRingkasTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvRingkasTitle.setTextColor(0xFF1C1C1E);
        ringkas.addView(tvRingkasTitle);

        int[] summary = ChangelogStore.getFeatureSummary();
        TextView tvRingkasDetail = new TextView(this);
        tvRingkasDetail.setText("Selesai: " + summary[0] + "  |  Sebagian: " + summary[1] + "  |  Belum: " + summary[2]);
        tvRingkasDetail.setTextSize(12f);
        tvRingkasDetail.setTextColor(0xFF555555);
        tvRingkasDetail.setPadding(0, 8, 0, 8);
        ringkas.addView(tvRingkasDetail);

        TextView btnLihatDetail = new TextView(this);
        btnLihatDetail.setText("Lihat Detail >");
        btnLihatDetail.setTextSize(13f);
        btnLihatDetail.setTextColor(0xFF3D5AFE);
        btnLihatDetail.setTypeface(null, android.graphics.Typeface.BOLD);
        btnLihatDetail.setPadding(0, 4, 0, 4);
        btnLihatDetail.setOnClickListener(v -> showFeaturesTab());
        ringkas.addView(btnLihatDetail);

        content.addView(ringkas);
    }

    private void showFeaturesTab() {
        tabFeatures.setTextColor(0xFF3D5AFE);
        tabFeatures.setBackgroundResource(R.drawable.bg_glass_button_selected);
        tabVersion.setTextColor(0xFF888888);
        tabVersion.setBackground(null);

        content.removeAllViews();

        TextView tvHeader = new TextView(this);
        tvHeader.setText("Status Fitur (per hari ini)");
        tvHeader.setTextSize(13f);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeader.setTextColor(0xFF1C1C1E);
        tvHeader.setPadding(8, 4, 8, 8);
        content.addView(tvHeader);

        List<ChangelogStore.FeatureStatus> features = ChangelogStore.getFeatureStatus();

        // Kelompokkan per status
        String[] order = {"Selesai", "Sebagian", "Belum"};
        for (String kategori : order) {
            int count = 0;
            for (ChangelogStore.FeatureStatus f : features) {
                if (f.status.equals(kategori)) count++;
            }
            if (count == 0) continue;

            TextView tvKat = new TextView(this);
            tvKat.setText(kategori + " (" + count + ")");
            tvKat.setTextSize(12f);
            tvKat.setTypeface(null, android.graphics.Typeface.BOLD);
            tvKat.setTextColor(0xFF1C1C1E);
            tvKat.setPadding(8, 12, 8, 4);
            content.addView(tvKat);

            for (ChangelogStore.FeatureStatus f : features) {
                if (!f.status.equals(kategori)) continue;
                View card = LayoutInflater.from(this).inflate(R.layout.item_changelog_feature, content, false);
                ((TextView) card.findViewById(R.id.tvFeatureName)).setText(f.name);
                TextView tvSt = card.findViewById(R.id.tvFeatureStatus);
                tvSt.setText(f.status);
                if ("Selesai".equals(f.status)) tvSt.setBackgroundColor(0xFF4CAF50);
                else if ("Sebagian".equals(f.status)) tvSt.setBackgroundColor(0xFFFF9800);
                else tvSt.setBackgroundColor(0xFF9E9E9E);
                ((TextView) card.findViewById(R.id.tvFeatureDate)).setText("Update: " + f.dateAdded);
                content.addView(card);
            }
        }
    }
}
