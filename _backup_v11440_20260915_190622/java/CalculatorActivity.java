package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CalculatorActivity extends Activity {

    private TextView tvDisplay;
    private TextView tvDisplayScientific;
    private String rawInput = "";
    private static final String SECRET_CODE = "140399";
    private boolean isScientific = false;

    private String lastAppliedMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyModeAndLayout();
        CrashCheckHelper.checkAndShow(this);
        setupButtons();
    }

    private void applyModeAndLayout() {
        String mode = DisplayModeStore.getEffectiveMode(this);
        lastAppliedMode = mode;

        if (mode.equals("tv")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_calculator_scientific);
            tvDisplayScientific = findViewById(R.id.tvDisplayScientific);
            isScientific = true;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            int orientation = getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_calculator_scientific);
                tvDisplayScientific = findViewById(R.id.tvDisplayScientific);
                isScientific = true;
            } else {
                setContentView(R.layout.activity_calculator);
                tvDisplay = findViewById(R.id.tvDisplay);
                isScientific = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentMode = DisplayModeStore.getEffectiveMode(this);
        if (lastAppliedMode != null && !lastAppliedMode.equals(currentMode)) {
            recreate();
        }
    }

    private void setupButtons() {
        if (isScientific) {
            bindScienceButtons();
        } else {
            bindPortraitButtons();
        }
    }

    private void bindPortraitButtons() {
        int[] digitIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn00
        };
        for (int id : digitIds) {
            TextView btn = findViewById(id);
            btn.setOnClickListener(v -> {
                rawInput += ((TextView) v).getText().toString();
                updateDisplay();
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> { rawInput += "."; updateDisplay(); });
        findViewById(R.id.btnPlus).setOnClickListener(v -> { rawInput += "+"; updateDisplay(); });
        findViewById(R.id.btnMinus).setOnClickListener(v -> { rawInput += "-"; updateDisplay(); });
        findViewById(R.id.btnMultiply).setOnClickListener(v -> { rawInput += "*"; updateDisplay(); });
        findViewById(R.id.btnDivide).setOnClickListener(v -> { rawInput += "/"; updateDisplay(); });

        findViewById(R.id.btnClear).setOnClickListener(v -> { rawInput = ""; updateDisplay(); });
        findViewById(R.id.btnDel).setOnClickListener(v -> {
            if (!rawInput.isEmpty()) rawInput = rawInput.substring(0, rawInput.length() - 1);
            updateDisplay();
        });
        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            try {
                double val = Double.parseDouble(rawInput);
                rawInput = formatResult(val / 100.0);
            } catch (Exception ignored) { }
            updateDisplay();
        });
        findViewById(R.id.btnEquals).setOnClickListener(v -> onEquals());
    }

    private void bindScienceButtons() {
        int[] digitIds = {
                R.id.btn0Sci, R.id.btn1Sci, R.id.btn2Sci, R.id.btn3Sci, R.id.btn4Sci,
                R.id.btn5Sci, R.id.btn6Sci, R.id.btn7Sci, R.id.btn8Sci, R.id.btn9Sci, R.id.btn00Sci
        };
        for (int id : digitIds) {
            TextView btn = findViewById(id);
            btn.setOnClickListener(v -> {
                rawInput += ((TextView) v).getText().toString();
                updateDisplayScientific();
            });
        }

        findViewById(R.id.btnDotSci).setOnClickListener(v -> { rawInput += "."; updateDisplayScientific(); });
        findViewById(R.id.btnPlusSci).setOnClickListener(v -> { rawInput += "+"; updateDisplayScientific(); });
        findViewById(R.id.btnMinusSci).setOnClickListener(v -> { rawInput += "-"; updateDisplayScientific(); });
        findViewById(R.id.btnMultiplySci).setOnClickListener(v -> { rawInput += "*"; updateDisplayScientific(); });
        findViewById(R.id.btnDivideSci).setOnClickListener(v -> { rawInput += "/"; updateDisplayScientific(); });
        findViewById(R.id.btnPower).setOnClickListener(v -> { rawInput += "^"; updateDisplayScientific(); });

        findViewById(R.id.btnClearSci).setOnClickListener(v -> { rawInput = ""; updateDisplayScientific(); });
        findViewById(R.id.btnDelSci).setOnClickListener(v -> {
            if (!rawInput.isEmpty()) rawInput = rawInput.substring(0, rawInput.length() - 1);
            updateDisplayScientific();
        });
        findViewById(R.id.btnPercentSci).setOnClickListener(v -> {
            try {
                double val = Double.parseDouble(rawInput);
                rawInput = formatResult(val / 100.0);
            } catch (Exception ignored) { }
            updateDisplayScientific();
        });
        findViewById(R.id.btnEqualsSci).setOnClickListener(v -> onEquals());

        findViewById(R.id.btnLeftParen).setOnClickListener(v -> { rawInput += "("; updateDisplayScientific(); });
        findViewById(R.id.btnRightParen).setOnClickListener(v -> { rawInput += ")"; updateDisplayScientific(); });
        findViewById(R.id.btnPi).setOnClickListener(v -> {
            rawInput += Math.PI;
            updateDisplayScientific();
        });
        findViewById(R.id.btnSin).setOnClickListener(v -> {
            try { rawInput = formatResult(Math.sin(Double.parseDouble(rawInput))); } catch (Exception ignored) {}
            updateDisplayScientific();
        });
        findViewById(R.id.btnCos).setOnClickListener(v -> {
            try { rawInput = formatResult(Math.cos(Double.parseDouble(rawInput))); } catch (Exception ignored) {}
            updateDisplayScientific();
        });
        findViewById(R.id.btnTan).setOnClickListener(v -> {
            try { rawInput = formatResult(Math.tan(Double.parseDouble(rawInput))); } catch (Exception ignored) {}
            updateDisplayScientific();
        });
        findViewById(R.id.btnLog).setOnClickListener(v -> {
            try { rawInput = formatResult(Math.log10(Double.parseDouble(rawInput))); } catch (Exception ignored) {}
            updateDisplayScientific();
        });
        findViewById(R.id.btnLn).setOnClickListener(v -> {
            try { rawInput = formatResult(Math.log(Double.parseDouble(rawInput))); } catch (Exception ignored) {}
            updateDisplayScientific();
        });
        findViewById(R.id.btnSqrt).setOnClickListener(v -> {
            try { rawInput = formatResult(Math.sqrt(Double.parseDouble(rawInput))); } catch (Exception ignored) {}
            updateDisplayScientific();
        });
    }

    private void onEquals() {
        String input = rawInput.trim();
        if (input.equals(SECRET_CODE)) {
            openMainApp();
            return;
        }
        // Kode rahasia
        if (input.equals("000")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, SecretCodesActivity.class)); return; }
        if (input.equals("111")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, CrashHistoryActivity.class)); return; }
        if (input.equals("222")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, ChangelogActivity.class)); return; }
        if (input.equals("808")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, DownloadListActivity.class)); return; }
        if (input.equals("888")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, StatistikActivity.class)); return; }
        if (input.equals("102")) {
            rawInput = "";
            throw new RuntimeException("Force Crash — User triggered via secret code 102");
        }
        if (input.equals("103")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            TestGestureHelper.showGuide(this); return; }
        if (input.equals("104")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            TestModesHelper.showChoice(this); return; }
        if (input.equals("101")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            DeveloperModeStore.toggle(this); return; }
        if (input.equals("123")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            CacheResetter.confirmAndReset(this); return; }
        if (input.equals("456")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            RepairDatabaseHelper.confirmAndRepair(this); return; }
        if (input.equals("444")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            TestMediaHelper.showChoice(this); return; }
        if (input.equals("789")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            FactoryResetHelper.confirmAndReset(this); return; }
        if (input.equals("777")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, StorageAnalyzerActivity.class)); return; }
        if (input.equals("999")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            ProjectExportHelper.export(this); return; }
        if (input.equals("333")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, SystemInfoActivity.class)); return; }
        if (input.equals("555")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, NetworkInfoActivity.class)); return; }
        if (input.equals("666")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            startActivity(new Intent(this, PermissionInfoActivity.class)); return; }
        if (input.equals("200")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent); return; }
        if (input.equals("201")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "Tidak didukung di HP ini", android.widget.Toast.LENGTH_SHORT).show();
            }
            return; }
        if (input.equals("202")) { rawInput = ""; updateDisplay(); updateDisplayScientific();
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "Tidak didukung di HP ini", android.widget.Toast.LENGTH_SHORT).show();
            }
            return; }
        try {
            double result = evaluate(rawInput);
            String resultStr = formatResult(result);
            rawInput = resultStr;
            updateDisplay();
            updateDisplayScientific();
        } catch (Exception e) {
            rawInput = "";
            if (tvDisplay != null) tvDisplay.setText("Error");
            if (tvDisplayScientific != null) tvDisplayScientific.setText("Error");
        }
    }

    private void openMainApp() {
        rawInput = "";
        updateDisplay();
        updateDisplayScientific();

        // Tandai bahwa auth sudah sukses (biar tidak loop)
        try { SessionManager.markAuthPassed(); } catch (Exception ignored) {}

        // Cek apakah ada activity terakhir yang perlu di-restore
        try {
            String lastClass = SessionState.lastActivityClass;
            if (lastClass != null && lastClass.contains("VideoPlayerActivity")) {
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtra("index", SessionState.lastVideoIndex);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                SessionState.clear();
                startActivity(intent);
                return;
            }
        } catch (Exception ignored) {}

        SessionState.clear();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void updateDisplay() {
        if (tvDisplay != null) {
            tvDisplay.setText(rawInput.isEmpty() ? "0" : rawInput);
        }
    }

    private void updateDisplayScientific() {
        if (tvDisplayScientific != null) {
            tvDisplayScientific.setText(rawInput.isEmpty() ? "0" : rawInput);
        }
    }

    private String formatResult(double val) {
        if (val == Math.floor(val) && !Double.isInfinite(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }

    private double evaluate(String expr) {
        return new ExprParser(expr).parse();
    }

    private static class ExprParser {
        private final String s;
        private int pos = 0;

        ExprParser(String s) { this.s = s; }

        double parse() {
            double result = parseTerm();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '+') { pos++; result += parseTerm(); }
                else if (c == '-') { pos++; result -= parseTerm(); }
                else break;
            }
            return result;
        }

        double parseTerm() {
            double result = parsePower();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '*') { pos++; result *= parsePower(); }
                else if (c == '/') { pos++; result /= parsePower(); }
                else break;
            }
            return result;
        }

        double parsePower() {
            double result = parseFactor();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '^') { pos++; result = Math.pow(result, parseFactor()); }
                else break;
            }
            return result;
        }

        double parseFactor() {
            int start = pos;
            if (pos < s.length() && s.charAt(pos) == '-') { pos++; return -parseFactor(); }
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) { pos++; }
            if (start == pos) throw new RuntimeException("Invalid expression");
            return Double.parseDouble(s.substring(start, pos));
        }
    }
}
